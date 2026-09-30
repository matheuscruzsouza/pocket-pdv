package com.github.matheuscruzsouza.pocketpdv.domain.model;

import java.util.Locale;

public class RelatorioVendasPorFuncionarioDTO {

    private final long funcionarioId;
    private final String funcionarioNome;
    private final int totalVendas;
    private final int totalItensVendidos;
    private final int faturamentoCentavos;

    public RelatorioVendasPorFuncionarioDTO(long funcionarioId, String funcionarioNome,
                                          int totalVendas, int totalItensVendidos,
                                          int faturamentoCentavos) {
        this.funcionarioId = funcionarioId;
        this.funcionarioNome = funcionarioNome != null ? funcionarioNome : "Não informado";
        this.totalVendas = totalVendas;
        this.totalItensVendidos = totalItensVendidos;
        this.faturamentoCentavos = faturamentoCentavos;
    }

    public long getFuncionarioId() { return funcionarioId; }
    public String getFuncionarioNome() { return funcionarioNome; }
    public int getTotalVendas() { return totalVendas; }
    public int getTotalItensVendidos() { return totalItensVendidos; }
    public int getFaturamentoCentavos() { return faturamentoCentavos; }

    public String getFaturamentoFormatado() {
        return String.format(Locale.GERMANY, "R$ %.2f", faturamentoCentavos / 100.0);
    }
}
