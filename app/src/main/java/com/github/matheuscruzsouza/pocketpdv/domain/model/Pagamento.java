package com.github.matheuscruzsouza.pocketpdv.domain.model;

import java.util.Locale;

public class Pagamento {

    private long id;
    private long vendaId;
    private String tipo;
    private int valorCentavos;
    private int valorRecebidoCentavos;
    private int trocoCentavos;

    public Pagamento() {
    }

    public Pagamento(long id, long vendaId, String tipo, int valorCentavos, int valorRecebidoCentavos, int trocoCentavos) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("O tipo de pagamento não pode ser vazio.");
        }
        if (valorCentavos < 0) {
            throw new IllegalArgumentException("O valor do pagamento não pode ser negativo.");
        }
        if (valorRecebidoCentavos < 0) {
            throw new IllegalArgumentException("O valor recebido não pode ser negativo.");
        }
        if (trocoCentavos < 0) {
            throw new IllegalArgumentException("O troco não pode ser negativo.");
        }

        this.id = id;
        this.vendaId = vendaId;
        this.tipo = tipo.trim().toUpperCase();
        this.valorCentavos = valorCentavos;
        this.valorRecebidoCentavos = valorRecebidoCentavos;
        this.trocoCentavos = trocoCentavos;
    }

    public Pagamento(String tipo, int valorCentavos, int valorRecebidoCentavos, int trocoCentavos) {
        this(0, 0, tipo, valorCentavos, valorRecebidoCentavos, trocoCentavos);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVendaId() {
        return vendaId;
    }

    public void setVendaId(long vendaId) {
        this.vendaId = vendaId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("O tipo de pagamento não pode ser vazio.");
        }
        this.tipo = tipo.trim().toUpperCase();
    }

    public int getValorCentavos() {
        return valorCentavos;
    }

    public void setValorCentavos(int valorCentavos) {
        if (valorCentavos < 0) {
            throw new IllegalArgumentException("O valor do pagamento não pode ser negativo.");
        }
        this.valorCentavos = valorCentavos;
    }

    public int getValorRecebidoCentavos() {
        return valorRecebidoCentavos;
    }

    public void setValorRecebidoCentavos(int valorRecebidoCentavos) {
        if (valorRecebidoCentavos < 0) {
            throw new IllegalArgumentException("O valor recebido não pode ser negativo.");
        }
        this.valorRecebidoCentavos = valorRecebidoCentavos;
    }

    public int getTrocoCentavos() {
        return trocoCentavos;
    }

    public void setTrocoCentavos(int trocoCentavos) {
        if (trocoCentavos < 0) {
            throw new IllegalArgumentException("O troco não pode ser negativo.");
        }
        this.trocoCentavos = trocoCentavos;
    }

    public String getValorFormatado() {
        return String.format(Locale.GERMANY, "R$ %.2f", valorCentavos / 100.0);
    }

    public String getValorRecebidoFormatado() {
        return String.format(Locale.GERMANY, "R$ %.2f", valorRecebidoCentavos / 100.0);
    }

    public String getTrocoFormatado() {
        return String.format(Locale.GERMANY, "R$ %.2f", trocoCentavos / 100.0);
    }
}
