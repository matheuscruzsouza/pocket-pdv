package com.github.matheuscruzsouza.pocketpdv.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;
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
    private Carrinho carrinho;

    public static PocketPdvService getInstance() {
        return instance;
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

    public Carrinho getCarrinho() {
        return carrinho;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        startForegroundNotification();

        try {
            // Inicializa ambiente do nano-spring
            Environment.init(this);

            // Banco de dados e carrinho
            dbHelper = new DatabaseHelper(this);
            carrinho = new Carrinho();

            // Servidor nano-spring escaneando com.github.matheuscruzsouza.pocketpdv
            server = new Server(this, PORT, "com.github.matheuscruzsouza.pocketpdv");

            // Registra singletons para injecao via @Autowired
            server.registerSingleton(Context.class, this);
            server.registerSingleton(DatabaseHelper.class, dbHelper);
            server.registerSingleton(SQLiteDatabase.class, dbHelper.getWritableDatabase());
            server.registerSingleton(Carrinho.class, carrinho);

            Log.i(TAG, "NanoSpring Server rodando na porta " + PORT);

        } catch (Exception e) {
            Log.e(TAG, "Erro na inicialização do servidor: " + e.getMessage(), e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null) {
            try {
                server.stop();
                Log.i(TAG, "NanoSpring Server parado.");
            } catch (Exception e) {
                Log.e(TAG, "Erro ao parar servidor: " + e.getMessage(), e);
            }
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
        instance = null;
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
