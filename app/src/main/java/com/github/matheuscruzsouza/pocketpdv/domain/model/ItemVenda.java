package com.github.matheuscruzsouza.pocketpdv.domain.model;

public class ItemVenda {

    private long id;
    private long vendaId;
    private long produtoId;
    private int quantidade;
    private long precoUnitCentavos;

    public ItemVenda() {
    }

    public ItemVenda(long id, long vendaId, long produtoId, int quantidade, long precoUnitCentavos) {
        this.id = id;
        this.vendaId = vendaId;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.precoUnitCentavos = precoUnitCentavos;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getVendaId() { return vendaId; }
    public void setVendaId(long vendaId) { this.vendaId = vendaId; }

    public long getProdutoId() { return produtoId; }
    public void setProdutoId(long produtoId) { this.produtoId = produtoId; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public long getPrecoUnitCentavos() { return precoUnitCentavos; }
    public void setPrecoUnitCentavos(long precoUnitCentavos) { this.precoUnitCentavos = precoUnitCentavos; }

    public long getSubtotalCentavos() {
        return (long) quantidade * precoUnitCentavos;
    }
}
