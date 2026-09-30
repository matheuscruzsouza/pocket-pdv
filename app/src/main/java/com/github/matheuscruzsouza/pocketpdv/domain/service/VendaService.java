package com.github.matheuscruzsouza.pocketpdv.domain.service;

import com.github.matheuscruzsouza.pocketpdv.domain.model.Carrinho;
import com.github.matheuscruzsouza.pocketpdv.domain.model.ItemVendaComando;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Venda;

import java.util.List;

public interface VendaService {
    Venda finalizarVenda(List<ItemVendaComando> itens);
    Venda finalizarVenda(Carrinho carrinho);
    Venda buscarVenda(long id);
    List<Venda> listarVendasRecentes(int limit);
}
