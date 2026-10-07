package com.github.matheuscruzsouza.pocketpdv.domain.model;

public class MovimentacaoCaixa {
    private long id;
    private long caixaTurnoId;
    private String tipo; // VENDA, SANGRIA, SUPRIMENTO
    private long valorCentavos;
    private String descricao;
    private String dataHora;

    public MovimentacaoCaixa() {}

    public MovimentacaoCaixa(long id, long caixaTurnoId, String tipo, long valorCentavos, String descricao, String dataHora) {
        this.id = id;
        this.caixaTurnoId = caixaTurnoId;
        this.tipo = tipo;
        this.valorCentavos = valorCentavos;
        this.descricao = descricao;
        this.dataHora = dataHora;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getCaixaTurnoId() { return caixaTurnoId; }
    public void setCaixaTurnoId(long caixaTurnoId) { this.caixaTurnoId = caixaTurnoId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public long getValorCentavos() { return valorCentavos; }
    public void setValorCentavos(long valorCentavos) { this.valorCentavos = valorCentavos; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getDataHora() { return dataHora; }
    public void setDataHora(String dataHora) { this.dataHora = dataHora; }
}
