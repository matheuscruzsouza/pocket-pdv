package com.github.matheuscruzsouza.pocketpdv.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.github.matheuscruzsouza.nanospring.server.Environment;
import com.github.matheuscruzsouza.nanospring.server.Server;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Carrinho;
import com.github.matheuscruzsouza.pocketpdv.persistence.DatabaseHelper;
import com.github.matheuscruzsouza.pocketpdv.ui.MainActivity;

import java.io.IOException;

public class PocketPdvService extends Service {

    private static final String TAG = "PocketPdvService";
    private static final String CHANNEL_ID = "pocketpdv_service_channel";
    private static final int NOTIFICATION_ID = 1001;
    public static final int PORT = 8080;

    private static PocketPdvService instance;
    private Server server;
    private DatabaseHelper dbHelper;

    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

    public static PocketPdvService getInstance() {
        return instance;
    }

    private static volatile ServerState state = ServerState.STOPPED;
    private static volatile String lastError;

    public static ServerState getState() {
        return state;
    }

    public static String getLastError() {
        return lastError;
    }

    public static Context getAppContext() {
        return instance != null ? instance.getApplicationContext() : null;
    }

    public Server getServer() {
        return server;
    }

    public DatabaseHelper getDbHelper() {
        return dbHelper;
    }

    private SessionService sessionService;

    public SessionService getSessionService() {
        if (sessionService == null) {
            sessionService = new SessionService();
        }
        return sessionService;
    }

    public Carrinho getCarrinho(String sessionId) {
        return getSessionService().obterCarrinho(sessionId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        state = ServerState.STARTING;
        lastError = null;
        startForegroundNotification();
        acquireLocks();

        try {
            // Inicializa ambiente do nano-spring
            Environment.init(this);

            // Banco de dados
            dbHelper = DatabaseHelper.getInstance(this);

            // Servidor nano-spring escaneando com.github.matheuscruzsouza.pocketpdv
            server = new Server(this, PORT, "com.github.matheuscruzsouza.pocketpdv");

            // Registra singletons para injecao via @Autowired
            sessionService = new SessionService();
            server.registerSingleton(Context.class, this);
            server.registerSingleton(DatabaseHelper.class, dbHelper);
            server.registerSingleton(SQLiteDatabase.class, dbHelper.getWritableDatabase());
            server.registerSingleton(SessionService.class, sessionService);

            state = ServerState.RUNNING;
            Log.i(TAG, "NanoSpring Server rodando na porta " + PORT);

        } catch (Exception e) {
            state = ServerState.FAILED;
            lastError = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            Log.e(TAG, "Erro na inicialização do servidor: " + e.getMessage(), e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (state == ServerState.FAILED) {
            stopSelf();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseLocks();
        if (server != null) {
            try {
                server.stop();
                Log.i(TAG, "NanoSpring Server parado.");
            } catch (Exception e) {
                Log.e(TAG, "Erro ao parar servidor: " + e.getMessage(), e);
            }
        }
        try {
            EstoqueSseHub.getInstance().encerrar();
        } catch (Exception ignored) {}
        if (dbHelper != null) {
            dbHelper.close();
        }
        if (state != ServerState.FAILED) {
            state = ServerState.STOPPED;
        }
        instance = null;
    }

    private void acquireLocks() {
        try {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (powerManager != null && (wakeLock == null || !wakeLock.isHeld())) {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PocketPDV::ServerWakeLock");
                wakeLock.setReferenceCounted(false);
                wakeLock.acquire();
                Log.i(TAG, "WakeLock (CPU) adquirido para manter servidor ativo com tela apagada.");
            }
        } catch (Exception e) {
            Log.w(TAG, "Falha ao adquirir WakeLock: " + e.getMessage());
        }

        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null && (wifiLock == null || !wifiLock.isHeld())) {
                int wifiMode = WifiManager.WIFI_MODE_FULL_HIGH_PERF;
                wifiLock = wifiManager.createWifiLock(wifiMode, "PocketPDV::ServerWifiLock");
                wifiLock.setReferenceCounted(false);
                wifiLock.acquire();
                Log.i(TAG, "WifiLock (Modo High-Perf) adquirido para manter rádio Wi-Fi conectado.");
            }
        } catch (Exception e) {
            Log.w(TAG, "Falha ao adquirir WifiLock: " + e.getMessage());
        }
    }

    private void releaseLocks() {
        try {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                wakeLock = null;
                Log.i(TAG, "WakeLock liberado.");
            }
        } catch (Exception e) {
            Log.w(TAG, "Erro ao liberar WakeLock: " + e.getMessage());
        }

        try {
            if (wifiLock != null && wifiLock.isHeld()) {
                wifiLock.release();
                wifiLock = null;
                Log.i(TAG, "WifiLock liberado.");
            }
        } catch (Exception e) {
            Log.w(TAG, "Erro ao liberar WifiLock: " + e.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "PocketPDV Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Serviço do servidor HTTP local PocketPDV");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE
                        : 0
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("PocketPDV Ativo")
                .setContentText("Servidor HTTP rodando na porta " + PORT)
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }
}
