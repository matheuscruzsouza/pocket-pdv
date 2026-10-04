package com.github.matheuscruzsouza.pocketpdv.domain.model;

import java.util.Locale;

public class Pagamento {

    private long id;
    private long vendaId;
    private String tipo;
    private long valorCentavos;
    private long valorRecebidoCentavos;
    private long trocoCentavos;

    public Pagamento() {
    }

    public Pagamento(long id, long vendaId, String tipo, long valorCentavos, long valorRecebidoCentavos, long trocoCentavos) {
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

    public Pagamento(String tipo, long valorCentavos, long valorRecebidoCentavos, long trocoCentavos) {
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

    public long getValorCentavos() {
        return valorCentavos;
    }

    public void setValorCentavos(long valorCentavos) {
        if (valorCentavos < 0) {
            throw new IllegalArgumentException("O valor do pagamento não pode ser negativo.");
        }
        this.valorCentavos = valorCentavos;
    }

    public long getValorRecebidoCentavos() {
        return valorRecebidoCentavos;
    }

    public void setValorRecebidoCentavos(long valorRecebidoCentavos) {
        if (valorRecebidoCentavos < 0) {
            throw new IllegalArgumentException("O valor recebido não pode ser negativo.");
        }
        this.valorRecebidoCentavos = valorRecebidoCentavos;
    }

    public long getTrocoCentavos() {
        return trocoCentavos;
    }

    public void setTrocoCentavos(long trocoCentavos) {
        if (trocoCentavos < 0) {
            throw new IllegalArgumentException("O troco não pode ser negativo.");
        }
        this.trocoCentavos = trocoCentavos;
    }

    public String getValorFormatado() {
        return com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.formatarDinheiro(valorCentavos);
    }

    public String getValorRecebidoFormatado() {
        return com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.formatarDinheiro(valorRecebidoCentavos);
    }

    public String getTrocoFormatado() {
        return com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.formatarDinheiro(trocoCentavos);
    }
}
