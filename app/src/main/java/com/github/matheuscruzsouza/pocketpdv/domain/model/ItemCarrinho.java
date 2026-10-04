package com.github.matheuscruzsouza.pocketpdv.domain.model;

public class ItemCarrinho {

    private final long id;
    private final Produto produto;
    private int quantidade;

    public ItemCarrinho(long id, Produto produto, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero: " + quantidade);
        }
        this.id = id;
        this.produto = produto;
        this.quantidade = quantidade;
    }

    public long getId() { return id; }
    public Produto getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero: " + quantidade);
        }
        this.quantidade = quantidade;
    }

    public long getPrecoUnitCentavos() {
        return produto.getPrecoCentavos();
    }

    public long getSubtotalCentavos() {
        return produto.getPrecoCentavos() * quantidade;
    }

    public String getSubtotalFormatado() {
        return com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.formatarDinheiro(getSubtotalCentavos());
    }

    public String getPrecoUnitarioFormatado() {
        return com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.formatarDinheiro(produto.getPrecoCentavos());
    }
}
