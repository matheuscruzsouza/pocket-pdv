package com.github.matheuscruzsouza.pocketpdv;

import com.github.matheuscruzsouza.pocketpdv.domain.model.Pagamento;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PagamentoTest {

    @Test
    public void testCriacaoPagamentoValido() {
        Pagamento p = new Pagamento(1, 10, "dinheiro", 5000, 6000, 1000);
        assertEquals(1, p.getId());
        assertEquals(10, p.getVendaId());
        assertEquals("DINHEIRO", p.getTipo());
        assertEquals(5000, p.getValorCentavos());
        assertEquals(6000, p.getValorRecebidoCentavos());
        assertEquals(1000, p.getTrocoCentavos());
        assertEquals("R$ 50,00", p.getValorFormatado());
        assertEquals("R$ 60,00", p.getValorRecebidoFormatado());
        assertEquals("R$ 10,00", p.getTrocoFormatado());
    }

    @Test
    public void testTipoNaoPodeSerVazio() {
        try {
            new Pagamento("", 1000, 1000, 0);
            fail("Deveria lançar IllegalArgumentException para tipo vazio");
        } catch (IllegalArgumentException expected) {
            // Sucesso
        }
    }

    @Test
    public void testValorNaoPodeSerNegativo() {
        try {
            new Pagamento("PIX", -10, 1000, 0);
            fail("Deveria lançar IllegalArgumentException para valor negativo");
        } catch (IllegalArgumentException expected) {
            // Sucesso
        }
    }

    @Test
    public void testValorRecebidoNaoPodeSerNegativo() {
        try {
            new Pagamento("DINHEIRO", 1000, -500, 0);
            fail("Deveria lançar IllegalArgumentException para valor recebido negativo");
        } catch (IllegalArgumentException expected) {
            // Sucesso
        }
    }

    @Test
    public void testTrocoNaoPodeSerNegativo() {
        try {
            new Pagamento("DINHEIRO", 1000, 1000, -100);
            fail("Deveria lançar IllegalArgumentException para troco negativo");
        } catch (IllegalArgumentException expected) {
            // Sucesso
        }
    }
}
