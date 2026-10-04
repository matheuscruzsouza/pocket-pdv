package com.github.matheuscruzsouza.pocketpdv.domain.model;

import com.github.matheuscruzsouza.nanospring.validation.Min;

public class ItemVendaComando {

    @Min(value = 1, message = "O ID do produto deve ser maior que zero")
    private final long produtoId;

    @Min(value = 1, message = "A quantidade deve ser de no mínimo 1 unidade")
    private final int quantidade;

    @Min(value = 0, message = "O preço não pode ser negativo")
    private final long precoUnitCentavos;

    public ItemVendaComando(long produtoId, int quantidade) {
        this(produtoId, quantidade, 0L);
    }

    public ItemVendaComando(long produtoId, int quantidade, long precoUnitCentavos) {
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

    public long getPrecoUnitCentavos() {
        return precoUnitCentavos;
    }
}
