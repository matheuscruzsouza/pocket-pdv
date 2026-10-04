package com.github.matheuscruzsouza.pocketpdv.domain.service;

import com.github.matheuscruzsouza.pocketpdv.domain.model.Carrinho;
import com.github.matheuscruzsouza.pocketpdv.domain.model.ItemVendaComando;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Pagamento;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Venda;

import java.util.List;

public interface VendaService {
    Venda finalizarVenda(List<ItemVendaComando> itens);
    Venda finalizarVenda(List<ItemVendaComando> itens, long funcionarioId);
    Venda finalizarVenda(List<ItemVendaComando> itens, long funcionarioId, List<Pagamento> pagamentos);
    Venda finalizarVenda(Carrinho carrinho);
    Venda finalizarVenda(Carrinho carrinho, long funcionarioId);
    Venda finalizarVenda(Carrinho carrinho, long funcionarioId, List<Pagamento> pagamentos);
    Venda finalizarVenda(Carrinho carrinho, long funcionarioId, String formaPagamento, int valorRecebidoCentavos, int trocoCentavos);
    Venda buscarVenda(long id);
    List<Venda> listarVendasRecentes(int limit);
    boolean estornarVenda(long vendaId);
}

