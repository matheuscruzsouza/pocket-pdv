package com.github.matheuscruzsouza.pocketpdv;

import static org.junit.Assert.assertEquals;

import com.github.matheuscruzsouza.pocketpdv.util.HtmlEscaper;

import org.junit.Test;

public class HtmlEscaperTest {

    @Test
    public void testEscapeNullRetornaVazio() {
        assertEquals("", HtmlEscaper.escape(null));
    }

    @Test
    public void testEscapeVazioRetornaVazio() {
        assertEquals("", HtmlEscaper.escape(""));
    }

    @Test
    public void testEscapeTextoSeguroPermaneceInalterado() {
        assertEquals("Cafe Torrado 500g", HtmlEscaper.escape("Cafe Torrado 500g"));
        assertEquals("1234567890", HtmlEscaper.escape("1234567890"));
    }

    @Test
    public void testEscapeTagScript() {
        String input = "<script>alert('xss')</script>";
        String esperado = "&lt;script&gt;alert(&#x27;xss&#x27;)&lt;/script&gt;";
        assertEquals(esperado, HtmlEscaper.escape(input));
    }

    @Test
    public void testEscapeAtributosMaliciosos() {
        String input = "\" onmouseover=\"alert(1)\"";
        String esperado = "&quot; onmouseover=&quot;alert(1)&quot;";
        assertEquals(esperado, HtmlEscaper.escape(input));
    }

    @Test
    public void testEscapeEComercial() {
        String input = "Arroz & Feijao";
        String esperado = "Arroz &amp; Feijao";
        assertEquals(esperado, HtmlEscaper.escape(input));
    }

    @Test
    public void testEscapeCaracteresMisturados() {
        String input = "<b>'Teste' & \"Outro\"</b>";
        String esperado = "&lt;b&gt;&#x27;Teste&#x27; &amp; &quot;Outro&quot;&lt;/b&gt;";
        assertEquals(esperado, HtmlEscaper.escape(input));
    }
}
