package com.github.matheuscruzsouza.pocketpdv.domain.model;

public class ItemVendaComando {

    private final long produtoId;
    private final int quantidade;
    private final int precoUnitCentavos;

    public ItemVendaComando(long produtoId, int quantidade) {
        this(produtoId, quantidade, 0);
    }

    public ItemVendaComando(long produtoId, int quantidade, int precoUnitCentavos) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.precoUnitCentavos = precoUnitCentavos;
    }

    public long getProdutoId() {
        return produtoId;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public int getPrecoUnitCentavos() {
        return precoUnitCentavos;
    }
}
