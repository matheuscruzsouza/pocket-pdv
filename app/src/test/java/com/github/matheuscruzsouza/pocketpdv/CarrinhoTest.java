package com.github.matheuscruzsouza.pocketpdv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.matheuscruzsouza.pocketpdv.domain.model.Carrinho;
import com.github.matheuscruzsouza.pocketpdv.domain.model.ItemCarrinho;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Produto;

import org.junit.Before;
import org.junit.Test;

public class CarrinhoTest {

    private Carrinho carrinho;
    private Produto cafe;
    private Produto acucar;

    @Before
    public void setUp() {
        carrinho = new Carrinho();
        cafe = new Produto(1, "7891000100101", "Cafe Torrado 500g", 1450, 45);
        acucar = new Produto(2, "7891000200202", "Acucar Refinado 1kg", 480, 80);
    }

    @Test
    public void testAdicionarItem() {
        carrinho.adicionar(cafe, 2);

        assertEquals(1, carrinho.getItens().size());
        ItemCarrinho item = carrinho.getItens().get(0);
        assertEquals(2, item.getQuantidade());
        assertEquals(2900, item.getSubtotalCentavos());
        assertEquals(2900, carrinho.getTotalCentavos());
    }

    @Test
    public void testIncrementarMesmoItem() {
        carrinho.adicionar(cafe, 1);
        carrinho.adicionar(cafe, 3);

        assertEquals(1, carrinho.getItens().size());
        assertEquals(4, carrinho.getItens().get(0).getQuantidade());
        assertEquals(5800, carrinho.getTotalCentavos());
    }

    @Test
    public void testMultiplosItens() {
        carrinho.adicionar(cafe, 1); // 1450
        carrinho.adicionar(acucar, 2); // 960

        assertEquals(2, carrinho.getItens().size());
        assertEquals(2410, carrinho.getTotalCentavos());
    }

    @Test
    public void testRemoverItem() {
        carrinho.adicionar(cafe, 1);
        carrinho.adicionar(acucar, 1);

        carrinho.remover(cafe.getId());

        assertEquals(1, carrinho.getItens().size());
        assertEquals("Acucar Refinado 1kg", carrinho.getItens().get(0).getProduto().getNome());
        assertEquals(480, carrinho.getTotalCentavos());
    }

    @Test
    public void testLimparCarrinho() {
        carrinho.adicionar(cafe, 1);
        carrinho.adicionar(acucar, 2);

        carrinho.limpar();

        assertTrue(carrinho.getItens().isEmpty());
        assertEquals(0, carrinho.getTotalCentavos());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdicionarQuantidadeZeroLancaExcecao() {
        carrinho.adicionar(cafe, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdicionarQuantidadeNegativaLancaExcecao() {
        carrinho.adicionar(cafe, -5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDiminuirQuantidadeZeroLancaExcecao() {
        carrinho.adicionar(cafe, 2);
        carrinho.diminuir(cafe.getId(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDiminuirQuantidadeNegativaLancaExcecao() {
        carrinho.adicionar(cafe, 2);
        carrinho.diminuir(cafe.getId(), -3);
    }
}
