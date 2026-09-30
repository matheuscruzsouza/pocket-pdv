package com.github.matheuscruzsouza.pocketpdv.domain.model;

public class RelatorioDiarioDTO {

    private final String data;
    private final int quantidadeVendas;
    private final int totalCentavos;
    private final int ticketMedioCentavos;

    public RelatorioDiarioDTO(int quantidadeVendas, int totalCentavos) {
        this("", quantidadeVendas, totalCentavos);
    }

    public RelatorioDiarioDTO(String data, int quantidadeVendas, int totalCentavos) {
        this.data = data;
        this.quantidadeVendas = quantidadeVendas;
        this.totalCentavos = totalCentavos;
        this.ticketMedioCentavos = quantidadeVendas > 0 ? (totalCentavos / quantidadeVendas) : 0;
    }

    public String getData() { return data; }
    public int getQuantidadeVendas() { return quantidadeVendas; }
    public int getTotalCentavos() { return totalCentavos; }
    public int getTicketMedioCentavos() { return ticketMedioCentavos; }

    public String getTotalFormatado() {
        return String.format("R$ %.2f", totalCentavos / 100.0);
    }

    public String getTicketMedioFormatado() {
        return String.format("R$ %.2f", ticketMedioCentavos / 100.0);
    }
}
