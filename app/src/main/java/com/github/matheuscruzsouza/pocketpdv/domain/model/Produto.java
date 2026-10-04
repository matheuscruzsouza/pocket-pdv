package com.github.matheuscruzsouza.pocketpdv.domain.model;

import com.github.matheuscruzsouza.nanospring.validation.Min;
import com.github.matheuscruzsouza.nanospring.validation.NotBlank;
import com.github.matheuscruzsouza.nanospring.validation.Size;

public class Produto {

    private long id;
    private String codigoBarras;

    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres")
    private String nome;

    @Min(value = 0, message = "O preço não pode ser negativo")
    private long precoCentavos;

    @Min(value = 0, message = "O estoque não pode ser negativo")
    private int estoque;

    public Produto() {
    }

    public Produto(long id, String codigoBarras, String nome, long precoCentavos, int estoque) {
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

    public long getPrecoCentavos() { return precoCentavos; }
    public void setPrecoCentavos(long precoCentavos) { this.precoCentavos = precoCentavos; }

    public int getEstoque() { return estoque; }
    public void setEstoque(int estoque) { this.estoque = estoque; }

    public String getPrecoFormatado() {
        return com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.formatarDinheiro(precoCentavos);
    }
}
