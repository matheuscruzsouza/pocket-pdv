package com.github.matheuscruzsouza.pocketpdv.domain.model;

public class CaixaTurno {
    private long id;
    private long funcionarioId;
    private String dataAbertura;
    private String dataFechamento;
    private long valorAberturaCentavos;
    private Long valorFechamentoDeclaradoCentavos;
    private String status;

    public CaixaTurno() {}

    public CaixaTurno(long id, long funcionarioId, String dataAbertura, String dataFechamento, long valorAberturaCentavos, Long valorFechamentoDeclaradoCentavos, String status) {
        this.id = id;
        this.funcionarioId = funcionarioId;
        this.dataAbertura = dataAbertura;
        this.dataFechamento = dataFechamento;
        this.valorAberturaCentavos = valorAberturaCentavos;
        this.valorFechamentoDeclaradoCentavos = valorFechamentoDeclaradoCentavos;
        this.status = status;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(long funcionarioId) { this.funcionarioId = funcionarioId; }
    
    public String getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(String dataAbertura) { this.dataAbertura = dataAbertura; }
    
    public String getDataFechamento() { return dataFechamento; }
    public void setDataFechamento(String dataFechamento) { this.dataFechamento = dataFechamento; }
    
    public long getValorAberturaCentavos() { return valorAberturaCentavos; }
    public void setValorAberturaCentavos(long valorAberturaCentavos) { this.valorAberturaCentavos = valorAberturaCentavos; }
    
    public Long getValorFechamentoDeclaradoCentavos() { return valorFechamentoDeclaradoCentavos; }
    public void setValorFechamentoDeclaradoCentavos(Long valorFechamentoDeclaradoCentavos) { this.valorFechamentoDeclaradoCentavos = valorFechamentoDeclaradoCentavos; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getValorTrocoInicialCentavos() {
        return this.valorAberturaCentavos;
    }

    public long getValorFechamentoCentavos() {
        return this.valorFechamentoDeclaradoCentavos != null ? this.valorFechamentoDeclaradoCentavos : 0L;
    }

    public long getValorTotalSistemaCentavos() {
        return this.valorAberturaCentavos; // TODO: Implementar cálculo real com movimentações
    }
}
