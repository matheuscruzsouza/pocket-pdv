package com.github.matheuscruzsouza.pocketpdv.domain.model;

import java.util.Locale;

public class RelatorioVendaItemDTO {

    private final long id;
    private final String dataHora;
    private final long funcionarioId;
    private final String funcionarioNome;
    private final int quantidadeItens;
    private final long totalCentavos;
    private final String status;
    private final String formaPagamento;

    public RelatorioVendaItemDTO(long id, String dataHora, long funcionarioId, String funcionarioNome,
                                int quantidadeItens, long totalCentavos, String status) {
        this(id, dataHora, funcionarioId, funcionarioNome, quantidadeItens, totalCentavos, status, "NÃO INFORMADO");
    }

    public RelatorioVendaItemDTO(long id, String dataHora, long funcionarioId, String funcionarioNome,
                                int quantidadeItens, long totalCentavos, String status, String formaPagamento) {
        this.id = id;
        this.dataHora = dataHora;
        this.funcionarioId = funcionarioId;
        this.funcionarioNome = funcionarioNome != null ? funcionarioNome : "Não informado";
        this.quantidadeItens = quantidadeItens;
        this.totalCentavos = totalCentavos;
        this.status = status != null ? status : "CONCLUIDA";
        this.formaPagamento = (formaPagamento != null && !formaPagamento.trim().isEmpty()) ? formaPagamento : "NÃO INFORMADO";
    }

    public long getId() { return id; }
    public String getDataHora() { return dataHora; }
    public long getFuncionarioId() { return funcionarioId; }
    public String getFuncionarioNome() { return funcionarioNome; }
    public int getQuantidadeItens() { return quantidadeItens; }
    public long getTotalCentavos() { return totalCentavos; }
    public String getStatus() { return status; }
    public String getFormaPagamento() { return formaPagamento; }

    public String getTotalFormatado() {
        return com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.formatarDinheiro(totalCentavos);
    }

    public String getDataHoraLegivel() {
        if (dataHora == null) return "-";
        // Se for formato ISO ex: 2026-09-29T18:50:07Z
        if (dataHora.contains("T")) {
            String[] partes = dataHora.replace("Z", "").split("T");
            String data = partes[0];
            String hora = partes.length > 1 ? partes[1] : "";
            String[] dataParts = data.split("-");
            if (dataParts.length == 3) {
                data = dataParts[2] + "/" + dataParts[1] + "/" + dataParts[0];
            }
            return data + (hora.isEmpty() ? "" : " às " + hora);
        }
        return dataHora;
    }
}
