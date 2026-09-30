package com.github.matheuscruzsouza.pocketpdv.domain.model;

public class Produto {

    private long id;
    private String codigoBarras;
    private String nome;
    private int precoCentavos;
    private int estoque;

    public Produto() {
    }

    public Produto(long id, String codigoBarras, String nome, int precoCentavos, int estoque) {
        this.id = id;
        this.codigoBarras = codigoBarras;
        this.nome = nome;
        this.precoCentavos = precoCentavos;
        this.estoque = estoque;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getPrecoCentavos() { return precoCentavos; }
    public void setPrecoCentavos(int precoCentavos) { this.precoCentavos = precoCentavos; }

    public int getEstoque() { return estoque; }
    public void setEstoque(int estoque) { this.estoque = estoque; }

    public String getPrecoFormatado() {
        return String.format("R$ %.2f", precoCentavos / 100.0);
    }
}
