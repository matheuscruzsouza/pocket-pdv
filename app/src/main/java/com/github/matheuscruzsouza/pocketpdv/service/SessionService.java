package com.github.matheuscruzsouza.pocketpdv.service;

import com.github.matheuscruzsouza.nanospring.annotation.Service;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Carrinho;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Funcionario;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {

    public static final String COOKIE_NAME = "POCKETPDV_SESSION";
    private static final long DURACAO_PADRAO_MILLIS = 24 * 60 * 60 * 1000L; // 24 horas

    private final Map<String, Session> sessoes = new ConcurrentHashMap<>();
    private final Map<String, Carrinho> carrinhosPorSessao = new ConcurrentHashMap<>();

    public Session criarSessao(Funcionario funcionario) {
        if (funcionario == null) {
            return null;
        }
        String sessionId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        long expiresAt = now + DURACAO_PADRAO_MILLIS;

        Session session = new Session(
                sessionId,
                funcionario.getId(),
                funcionario.getUsuario(),
                funcionario.getNome(),
                funcionario.getCargo(),
                now,
                expiresAt
        );

        sessoes.put(sessionId, session);
        return session;
    }

    public Session obterSessao(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return null;
        }
        String chave = sessionId.trim();
        Session session = sessoes.get(chave);
        if (session == null) {
            carrinhosPorSessao.remove(chave);
            return null;
        }
        if (session.isExpired()) {
            sessoes.remove(chave);
            carrinhosPorSessao.remove(chave);
            return null;
        }
        return session;
    }

    public Carrinho obterCarrinho(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return new Carrinho();
        }
        String chave = sessionId.trim();
        return carrinhosPorSessao.computeIfAbsent(chave, k -> new Carrinho());
    }

    public void encerrarSessao(String sessionId) {
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            String chave = sessionId.trim();
            sessoes.remove(chave);
            carrinhosPorSessao.remove(chave);
        }
    }

    public void limparExpiradas() {
        long now = System.currentTimeMillis();
        sessoes.entrySet().removeIf(entry -> {
            boolean expirada = entry.getValue() == null || entry.getValue().getExpiresAt() < now;
            if (expirada && entry.getKey() != null) {
                carrinhosPorSessao.remove(entry.getKey());
            }
            return expirada;
        });
        carrinhosPorSessao.keySet().removeIf(key -> !sessoes.containsKey(key));
    }

    public int getQuantidadeSessoesAtivas() {
        limparExpiradas();
        return sessoes.size();
    }
}
