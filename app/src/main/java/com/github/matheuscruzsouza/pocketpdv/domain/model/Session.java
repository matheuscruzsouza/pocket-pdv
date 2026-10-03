package com.github.matheuscruzsouza.pocketpdv.domain.model;

import java.io.Serializable;

public class Session implements Serializable {

    private final String id;
    private final long userId;
    private final String usuario;
    private final String nome;
    private final String cargo;
    private final long createdAt;
    private long expiresAt;

    public Session(String id, long userId, String usuario, String nome, String cargo, long createdAt, long expiresAt) {
        this.id = id;
        this.userId = userId;
        this.usuario = usuario;
        this.nome = nome;
        this.cargo = cargo;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getNome() {
        return nome;
    }

    public String getCargo() {
        return cargo;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}
