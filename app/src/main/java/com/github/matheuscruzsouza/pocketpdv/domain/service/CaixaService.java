package com.github.matheuscruzsouza.pocketpdv.domain.service;

import com.github.matheuscruzsouza.pocketpdv.domain.model.CaixaTurno;
import com.github.matheuscruzsouza.pocketpdv.domain.model.MovimentacaoCaixa;

import java.util.List;

public interface CaixaService {
    CaixaTurno abrirCaixa(long funcionarioId, long trocoInicialCentavos);
    MovimentacaoCaixa registrarMovimentacao(long caixaTurnoId, String tipo, long valorCentavos, String descricao);
    CaixaTurno fecharCaixa(long caixaTurnoId, long valorDeclaradoCentavos);
    CaixaTurno obterCaixaAberto(long funcionarioId);
    List<CaixaTurno> listarCaixasRecentes(int limit);
    List<MovimentacaoCaixa> listarMovimentacoes(long caixaTurnoId);
}
