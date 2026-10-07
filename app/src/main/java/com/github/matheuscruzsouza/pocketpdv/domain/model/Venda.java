package com.github.matheuscruzsouza.pocketpdv.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Venda {

    private long id;
    private String dataHora;
    private long totalCentavos;
    private String status; // 'CONCLUIDA' ou 'CANCEL'
    private long funcionarioId = 1;
    private Long caixaTurnoId = null;
    private final List<ItemVenda> itens = new ArrayList<>();

    public Venda() {
    }

    public Venda(long id, String dataHora, long totalCentavos, String status) {
        this(id, dataHora, totalCentavos, status, 1);
    }

    public Venda(long id, String dataHora, long totalCentavos, String status, long funcionarioId) {
        this.id = id;
        this.dataHora = dataHora;
        this.totalCentavos = totalCentavos;
        this.status = status;
        this.funcionarioId = funcionarioId > 0 ? funcionarioId : 1;
    }

    public Venda(long id, String dataHora, long totalCentavos, String status, long funcionarioId, Long caixaTurnoId) {
        this.id = id;
        this.dataHora = dataHora;
        this.totalCentavos = totalCentavos;
        this.status = status;
        this.funcionarioId = funcionarioId > 0 ? funcionarioId : 1;
        this.caixaTurnoId = caixaTurnoId;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDataHora() { return dataHora; }
    public void setDataHora(String dataHora) { this.dataHora = dataHora; }

    public long getTotalCentavos() { return totalCentavos; }
    public void setTotalCentavos(long totalCentavos) { this.totalCentavos = totalCentavos; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(long funcionarioId) { this.funcionarioId = funcionarioId; }

    public Long getCaixaTurnoId() { return caixaTurnoId; }
    public void setCaixaTurnoId(Long caixaTurnoId) { this.caixaTurnoId = caixaTurnoId; }

    public List<ItemVenda> getItens() { return itens; }

    public String getTotalFormatado() {
        return com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.formatarDinheiro(totalCentavos);
    }
}
