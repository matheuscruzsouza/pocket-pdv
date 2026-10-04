package com.github.matheuscruzsouza.pocketpdv.service;

import android.util.Log;

import com.github.matheuscruzsouza.nanospring.sse.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EstoqueSseHub {

    private static final String TAG = "EstoqueSseHub";
    public static final int MAX_EMITTERS = 32;
    private static final long HEARTBEAT_INTERVAL_SECONDS = 15;

    private static EstoqueSseHub instance;

    public interface VendaConcluidaListener {
        void onVendaConcluida(long vendaId, long totalCentavos, String operador, int totalItens);
    }

    public interface EstoqueAtualizadoListener {
        void onEstoqueAtualizado(long produtoId, int novoEstoque);
    }

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final List<VendaConcluidaListener> vendaListeners = new CopyOnWriteArrayList<>();
    private final List<EstoqueAtualizadoListener> estoqueListeners = new CopyOnWriteArrayList<>();
    private ScheduledExecutorService heartbeatScheduler;

    private EstoqueSseHub() {
    }

    public static synchronized EstoqueSseHub getInstance() {
        if (instance == null) {
            instance = new EstoqueSseHub();
        }
        return instance;
    }

    public synchronized SseEmitter registrar() {
        garantirHeartbeatAtivo();

        // Evita acúmulo descontrolado de conexões caso o limite seja atingido
        while (emitters.size() >= MAX_EMITTERS && !emitters.isEmpty()) {
            SseEmitter antigo = emitters.remove(0);
            try {
                antigo.complete();
            } catch (Exception ignored) {}
            Log.w(TAG, "Limite de emitters atingido (" + MAX_EMITTERS + "). Conexão mais antiga descartada.");
        }

        SseEmitter emitter = new SseEmitter();
        emitters.add(emitter);
        Log.d(TAG, "Novo cliente SSE conectado. Total ativo: " + emitters.size());
        return emitter;
    }

    public void remover(SseEmitter emitter) {
        if (emitter == null) return;
        boolean removed = emitters.remove(emitter);
        if (removed) {
            try {
                emitter.complete();
            } catch (Exception ignored) {}
            Log.d(TAG, "Cliente SSE desconectado manualmente. Total ativo: " + emitters.size());
        }
    }

    private synchronized void garantirHeartbeatAtivo() {
        if (heartbeatScheduler == null || heartbeatScheduler.isShutdown()) {
            heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "sse-heartbeat");
                t.setDaemon(true);
                return t;
            });
            heartbeatScheduler.scheduleWithFixedDelay(this::enviarHeartbeat,
                    HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
            Log.d(TAG, "Heartbeat periódico SSE iniciado (a cada " + HEARTBEAT_INTERVAL_SECONDS + "s).");
        }
    }

    private void enviarHeartbeat() {
        if (emitters.isEmpty()) return;

        for (SseEmitter emitter : emitters) {
            try {
                // Ping SSE via comentário ": ping\n\n". Não interfere no payload de eventos e valida a conexão TCP
                emitter.sendComment("ping");
            } catch (IOException | RuntimeException e) {
                // Conexão morta ou cliente desconectado
                emitters.remove(emitter);
                try {
                    emitter.complete();
                } catch (Exception ignored) {}
                Log.d(TAG, "Conexão SSE inativa/quebrada detectada pelo heartbeat e removida. Restantes: " + emitters.size());
            }
        }
    }

    public synchronized void encerrar() {
        if (heartbeatScheduler != null) {
            heartbeatScheduler.shutdownNow();
            heartbeatScheduler = null;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.complete();
            } catch (Exception ignored) {}
        }
        emitters.clear();
        vendaListeners.clear();
        estoqueListeners.clear();
        Log.d(TAG, "EstoqueSseHub encerrado e todos os emitters fechados.");
    }

    public void registrarVendaListener(VendaConcluidaListener listener) {
        if (listener != null && !vendaListeners.contains(listener)) {
            vendaListeners.add(listener);
        }
    }

    public void removerVendaListener(VendaConcluidaListener listener) {
        vendaListeners.remove(listener);
    }

    public void registrarEstoqueListener(EstoqueAtualizadoListener listener) {
        if (listener != null && !estoqueListeners.contains(listener)) {
            estoqueListeners.add(listener);
        }
    }

    public void removerEstoqueListener(EstoqueAtualizadoListener listener) {
        estoqueListeners.remove(listener);
    }

    public void notificarEstoque(long produtoId, int novoEstoque) {
        for (EstoqueAtualizadoListener l : estoqueListeners) {
            try {
                l.onEstoqueAtualizado(produtoId, novoEstoque);
            } catch (Exception ignored) {}
        }
        if (emitters.isEmpty()) return;
        String json = "{\"produtoId\":" + produtoId + ",\"novoEstoque\":" + novoEstoque + "}";
        broadcast("estoque-atualizado", json);
    }

    public void notificarCarrinho(String operador, com.github.matheuscruzsouza.pocketpdv.domain.model.Carrinho carrinho) {
        if (emitters.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"operador\":\"").append(operador != null ? operador : "caixa").append("\",");
        if (carrinho == null || carrinho.isVazio()) {
            sb.append("\"totalCentavos\":0,\"totalItens\":0,\"totalFormatado\":\"R$ 0,00\",\"itens\":[]");
        } else {
            sb.append("\"totalCentavos\":").append(carrinho.getTotalCentavos()).append(",");
            int totalItens = 0;
            for (com.github.matheuscruzsouza.pocketpdv.domain.model.ItemCarrinho ic : carrinho.getItens()) {
                totalItens += ic.getQuantidade();
            }
            sb.append("\"totalItens\":").append(totalItens).append(",");
            sb.append("\"totalFormatado\":\"").append(String.format("R$ %.2f", carrinho.getTotalCentavos() / 100.0).replace(".", ",")).append("\",");
            sb.append("\"itens\":[");
            List<com.github.matheuscruzsouza.pocketpdv.domain.model.ItemCarrinho> list = carrinho.getItens();
            for (int i = 0; i < list.size(); i++) {
                com.github.matheuscruzsouza.pocketpdv.domain.model.ItemCarrinho item = list.get(i);
                if (i > 0) sb.append(",");
                sb.append("{")
                  .append("\"produtoId\":").append(item.getProduto().getId()).append(",")
                  .append("\"nome\":\"").append(item.getProduto().getNome().replace("\"", "\\\"")).append("\",")
                  .append("\"quantidade\":").append(item.getQuantidade()).append(",")
                  .append("\"precoFormatado\":\"").append(String.format("R$ %.2f", item.getProduto().getPrecoCentavos() / 100.0).replace(".", ",")).append("\",")
                  .append("\"subtotalFormatado\":\"").append(String.format("R$ %.2f", item.getSubtotalCentavos() / 100.0).replace(".", ",")).append("\"")
                  .append("}");
            }
            sb.append("]");
        }
        sb.append("}");

        broadcast("carrinho-atualizado", sb.toString());
    }

    public void notificarVendaConcluida(long vendaId, long totalCentavos, String operador, int totalItens) {
        // Notifica observadores na interface nativa Android
        for (VendaConcluidaListener l : vendaListeners) {
            try {
                l.onVendaConcluida(vendaId, totalCentavos, operador, totalItens);
            } catch (Exception ignored) {}
        }

        if (emitters.isEmpty()) return;

        // Dispara evento SSE para a tela secundária do cliente (Customer Display)
        String json = "{\"vendaId\":" + vendaId +
                ",\"totalCentavos\":" + totalCentavos +
                ",\"operador\":\"" + (operador != null ? operador : "caixa") + "\"" +
                ",\"totalItens\":" + totalItens +
                ",\"totalFormatado\":\"" + com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.formatarDinheiro(totalCentavos).replace(".", ",") + "\"}";

        broadcast("venda-concluida", json);
    }

    private void broadcast(String evento, String json) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(evento, json);
            } catch (IOException | RuntimeException e) {
                emitters.remove(emitter);
                try {
                    emitter.complete();
                } catch (Exception ignored) {}
            }
        }
    }
}
