package com.github.matheuscruzsouza.pocketpdv.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Venda {

    private long id;
    private String dataHora;
    private int totalCentavos;
    private String status; // 'CONCLUIDA' ou 'CANCEL'
    private long funcionarioId = 1;
    private final List<ItemVenda> itens = new ArrayList<>();

    public Venda() {
    }

    public Venda(long id, String dataHora, int totalCentavos, String status) {
        this(id, dataHora, totalCentavos, status, 1);
    }

    public Venda(long id, String dataHora, int totalCentavos, String status, long funcionarioId) {
        this.id = id;
        this.dataHora = dataHora;
        this.totalCentavos = totalCentavos;
        this.status = status;
        this.funcionarioId = funcionarioId > 0 ? funcionarioId : 1;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDataHora() { return dataHora; }
    public void setDataHora(String dataHora) { this.dataHora = dataHora; }

    public int getTotalCentavos() { return totalCentavos; }
    public void setTotalCentavos(int totalCentavos) { this.totalCentavos = totalCentavos; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(long funcionarioId) { this.funcionarioId = funcionarioId; }

    public List<ItemVenda> getItens() { return itens; }

    public String getTotalFormatado() {
        return String.format("R$ %.2f", totalCentavos / 100.0);
    }
}
