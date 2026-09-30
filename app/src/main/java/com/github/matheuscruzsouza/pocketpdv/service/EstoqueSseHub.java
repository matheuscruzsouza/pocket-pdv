package com.github.matheuscruzsouza.pocketpdv.service;

import android.util.Log;

import com.github.matheuscruzsouza.nanospring.sse.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EstoqueSseHub {

    private static final String TAG = "EstoqueSseHub";
    private static EstoqueSseHub instance;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private EstoqueSseHub() {
    }

    public static synchronized EstoqueSseHub getInstance() {
        if (instance == null) {
            instance = new EstoqueSseHub();
        }
        return instance;
    }

    public SseEmitter registrar() {
        SseEmitter emitter = new SseEmitter();
        emitters.add(emitter);
        Log.d(TAG, "Novo cliente SSE conectado. Total: " + emitters.size());
        return emitter;
    }

    public void notificarEstoque(long produtoId, int novoEstoque) {
        if (emitters.isEmpty()) return;

        // Payload JSON simples e enxuto
        String json = "{\"produtoId\":" + produtoId + ",\"novoEstoque\":" + novoEstoque + "}";

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send("estoque-atualizado", json);
            } catch (IOException | RuntimeException e) {
                Log.d(TAG, "Cliente SSE desconectado ou erro ao enviar: " + e.getMessage());
                emitters.remove(emitter);
                try {
                    emitter.complete();
                } catch (Exception ignored) {}
            }
        }
    }
}
