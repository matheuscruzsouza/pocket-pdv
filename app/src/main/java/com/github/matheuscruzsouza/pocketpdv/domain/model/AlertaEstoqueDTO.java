package com.github.matheuscruzsouza.pocketpdv.domain.model;

public class AlertaEstoqueDTO {

    private final long produtoId;
    private final String codigoBarras;
    private final String nome;
    private final int estoqueAtual;
    private final int corteMinimo;

    public AlertaEstoqueDTO(long produtoId, String nome, int estoqueAtual, int corteMinimo) {
        this(produtoId, "", nome, estoqueAtual, corteMinimo);
    }

    public AlertaEstoqueDTO(long produtoId, String codigoBarras, String nome, int estoqueAtual, int corteMinimo) {
        this.produtoId = produtoId;
        this.codigoBarras = codigoBarras;
        this.nome = nome;
        this.estoqueAtual = estoqueAtual;
        this.corteMinimo = corteMinimo;
    }

    public long getProdutoId() { return produtoId; }
    public String getCodigoBarras() { return codigoBarras; }
    public String getNome() { return nome; }
    public int getEstoqueAtual() { return estoqueAtual; }
    public int getCorteMinimo() { return corteMinimo; }
    public int getLimiteMinimo() { return corteMinimo; }
}
