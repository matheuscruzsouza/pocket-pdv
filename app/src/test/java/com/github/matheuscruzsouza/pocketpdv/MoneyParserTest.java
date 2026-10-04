package com.github.matheuscruzsouza.pocketpdv;

import com.github.matheuscruzsouza.pocketpdv.util.MoneyParser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MoneyParserTest {

    @Test
    public void testParseValoresValidos() {
        assertEquals(1050L, MoneyParser.parseToCentavos("10.50"));
        assertEquals(1050L, MoneyParser.parseToCentavos("10,50"));
        assertEquals(1050L, MoneyParser.parseToCentavos("R$ 10,50"));
        assertEquals(1050L, MoneyParser.parseToCentavos(" $ 10.50 "));
        assertEquals(125050L, MoneyParser.parseToCentavos("1.250,50"));
        assertEquals(10000L, MoneyParser.parseToCentavos("100"));
        assertEquals(5L, MoneyParser.parseToCentavos("0.05"));
        assertEquals(0L, MoneyParser.parseToCentavos("0"));
        assertEquals(0L, MoneyParser.parseToCentavos("0,00"));
        // Teste de valor que excede int (~R$ 25 milhões)
        assertEquals(2_500_000_000L, MoneyParser.parseToCentavos("25000000.00"));
    }

    @Test
    public void testRejeicaoValoresNegativos() {
        try {
            MoneyParser.parseToCentavos("-10");
            fail("Deveria rejeitar valor negativo");
        } catch (IllegalArgumentException expected) {
        }

        try {
            MoneyParser.parseToCentavos("-0.01");
            fail("Deveria rejeitar valor negativo");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testRejeicaoEntradasNulasOuVazias() {
        try {
            MoneyParser.parseToCentavos(null);
            fail("Deveria rejeitar nulo");
        } catch (IllegalArgumentException expected) {
        }

        try {
            MoneyParser.parseToCentavos("   ");
            fail("Deveria rejeitar vazio");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testRejeicaoNaNInfinityOuTextoInvalido() {
        try {
            MoneyParser.parseToCentavos("NaN");
            fail("Deveria rejeitar NaN");
        } catch (IllegalArgumentException expected) {
        }

        try {
            MoneyParser.parseToCentavos("Infinity");
            fail("Deveria rejeitar Infinity");
        } catch (IllegalArgumentException expected) {
        }

        try {
            MoneyParser.parseToCentavos("1e5");
            fail("Deveria rejeitar notação científica");
        } catch (IllegalArgumentException expected) {
        }

        try {
            MoneyParser.parseToCentavos("preco");
            fail("Deveria rejeitar texto");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testFormatacaoDinheiro() {
        assertEquals("R$ 10,50", MoneyParser.formatarDinheiro(1050L));
        assertEquals("R$ 0,00", MoneyParser.formatarDinheiro(0L));
        assertEquals("R$ 25.000.000,00", MoneyParser.formatarDinheiro(2_500_000_000L));
    }
}
