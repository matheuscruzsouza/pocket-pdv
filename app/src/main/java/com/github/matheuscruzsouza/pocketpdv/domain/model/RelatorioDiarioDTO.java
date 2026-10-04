package com.github.matheuscruzsouza.pocketpdv.domain.model;

public class RelatorioDiarioDTO {

    private final String data;
    private final int quantidadeVendas;
    private final long totalCentavos;
    private final long ticketMedioCentavos;

    public RelatorioDiarioDTO(int quantidadeVendas, long totalCentavos) {
        this("", quantidadeVendas, totalCentavos);
    }

    public RelatorioDiarioDTO(String data, int quantidadeVendas, long totalCentavos) {
        this.data = data;
        this.quantidadeVendas = quantidadeVendas;
        this.totalCentavos = totalCentavos;
        this.ticketMedioCentavos = quantidadeVendas > 0 ? (totalCentavos / quantidadeVendas) : 0L;
    }

    public String getData() { return data; }
    public int getQuantidadeVendas() { return quantidadeVendas; }
    public long getTotalCentavos() { return totalCentavos; }
    public long getTicketMedioCentavos() { return ticketMedioCentavos; }

    public String getTotalFormatado() {
        return com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.formatarDinheiro(totalCentavos);
    }

    public String getTicketMedioFormatado() {
        return com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.formatarDinheiro(ticketMedioCentavos);
    }
}
