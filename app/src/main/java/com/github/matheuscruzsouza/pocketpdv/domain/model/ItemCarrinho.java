package com.github.matheuscruzsouza.pocketpdv.domain.model;

public class ItemCarrinho {

    private final long id;
    private final Produto produto;
    private int quantidade;

    public ItemCarrinho(long id, Produto produto, int quantidade) {
        this.id = id;
        this.produto = produto;
        this.quantidade = quantidade;
    }

    public long getId() { return id; }
    public Produto getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public int getPrecoUnitCentavos() {
        return produto.getPrecoCentavos();
    }

    public int getSubtotalCentavos() {
        return produto.getPrecoCentavos() * quantidade;
    }

    public String getSubtotalFormatado() {
        return String.format("R$ %.2f", getSubtotalCentavos() / 100.0);
    }

    public String getPrecoUnitarioFormatado() {
        return String.format("R$ %.2f", produto.getPrecoCentavos() / 100.0);
    }
}
