package com.github.matheuscruzsouza.pocketpdv.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Carrinho {

    private final AtomicLong idSequence = new AtomicLong(100);
    private final List<ItemCarrinho> itens = new ArrayList<>();

    public synchronized ItemCarrinho adicionarItem(Produto produto, int quantidade) {
        for (ItemCarrinho item : itens) {
            if (item.getProduto().getId() == produto.getId()) {
                item.setQuantidade(item.getQuantidade() + quantidade);
                return item;
            }
        }
        ItemCarrinho novoItem = new ItemCarrinho(idSequence.incrementAndGet(), produto, quantidade);
        itens.add(novoItem);
        return novoItem;
    }

    public synchronized ItemCarrinho removerItem(long itemId) {
        for (int i = 0; i < itens.size(); i++) {
            ItemCarrinho item = itens.get(i);
            if (item.getId() == itemId) {
                return itens.remove(i);
            }
        }
        return null;
    }

    public synchronized void adicionar(Produto produto, int quantidade) {
        adicionarItem(produto, quantidade);
    }

    public synchronized void remover(long id) {
        for (int i = 0; i < itens.size(); i++) {
            ItemCarrinho item = itens.get(i);
            if (item.getProduto().getId() == id || item.getId() == id) {
                itens.remove(i);
                return;
            }
        }
    }

    public synchronized void diminuir(long id, int quantidade) {
        for (int i = 0; i < itens.size(); i++) {
            ItemCarrinho item = itens.get(i);
            if (item.getProduto().getId() == id || item.getId() == id) {
                int novaQtd = item.getQuantidade() - quantidade;
                if (novaQtd <= 0) {
                    itens.remove(i);
                } else {
                    item.setQuantidade(novaQtd);
                }
                return;
            }
        }
    }

    public synchronized void limpar() {
        itens.clear();
    }

    public synchronized List<ItemCarrinho> getItens() {
        return Collections.unmodifiableList(new ArrayList<>(itens));
    }

    public synchronized int getTotalCentavos() {
        int total = 0;
        for (ItemCarrinho item : itens) {
            total += item.getSubtotalCentavos();
        }
        return total;
    }

    public synchronized String getTotalFormatado() {
        return String.format("R$ %.2f", getTotalCentavos() / 100.0);
    }

    public synchronized boolean isVazio() {
        return itens.isEmpty();
    }
}
