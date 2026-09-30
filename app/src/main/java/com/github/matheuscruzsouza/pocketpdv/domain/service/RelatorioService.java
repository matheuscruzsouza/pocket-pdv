package com.github.matheuscruzsouza.pocketpdv.domain.service;

import com.github.matheuscruzsouza.pocketpdv.domain.model.AlertaEstoqueDTO;
import com.github.matheuscruzsouza.pocketpdv.domain.model.RelatorioDiarioDTO;

import java.util.Date;
import java.util.List;

public interface RelatorioService {
    RelatorioDiarioDTO obterResumoDoDia(Date data);
    List<AlertaEstoqueDTO> verificarAlertasCriticos(int limite);
}
