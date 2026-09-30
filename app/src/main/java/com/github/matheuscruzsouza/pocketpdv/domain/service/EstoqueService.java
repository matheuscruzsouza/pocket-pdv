package com.github.matheuscruzsouza.pocketpdv.domain.service;

import com.github.matheuscruzsouza.pocketpdv.domain.model.AlertaEstoqueDTO;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Produto;

import java.util.List;

public interface EstoqueService {
    Produto buscarPorCodigoBarras(String codigoBarras);
    Produto buscarPorId(long id);
    List<Produto> listarCatalogo();
    List<AlertaEstoqueDTO> verificarAlertas(int limiteMinimo);
    boolean ajustarEstoque(long produtoId, int novoEstoque);
    long salvarProduto(Produto produto);
}
