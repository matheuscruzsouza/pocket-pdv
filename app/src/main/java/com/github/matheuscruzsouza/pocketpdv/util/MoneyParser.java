package com.github.matheuscruzsouza.pocketpdv.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public final class MoneyParser {

    // Limite superior de segurança para transações de PDV: R$ 999 bilhões (99.900.000.000.000 centavos)
    public static final long MAX_CENTAVOS = 99_900_000_000_000L;

    private MoneyParser() {
        // Utilitário estático
    }

    /**
     * Converte uma entrada de texto em valor monetário para centavos (long).
     * Rejeita valores nulos, vazios, formatos inválidos (ex: NaN, Infinity),
     * valores negativos (< 0) e valores excessivos.
     */
    public static long parseToCentavos(String input) {
        if (input == null) {
            throw new IllegalArgumentException("O valor monetário não pode ser nulo.");
        }

        String limpo = input.trim();
        if (limpo.isEmpty()) {
            throw new IllegalArgumentException("O valor monetário não pode ser vazio.");
        }

        // Remove prefixos comuns de moeda como "R$", "R$ ", "$"
        limpo = limpo.replace("R$", "").replace("$", "").trim();

        // Rejeita tokens com NaN, Infinity ou caracteres não numéricos óbvios
        String lower = limpo.toLowerCase(Locale.US);
        if (lower.contains("nan") || lower.contains("infinity") || lower.contains("e")) {
            throw new IllegalArgumentException("Formato monetário numérico inválido: " + input);
        }

        // Se houver pontos e vírgula (ex: 1.250,50), remove o separador de milhar
        if (limpo.contains(".") && limpo.contains(",")) {
            limpo = limpo.replace(".", "").replace(",", ".");
        } else if (limpo.contains(",")) {
            // Se houver apenas vírgula (ex: 10,50)
            limpo = limpo.replace(",", ".");
        }

        BigDecimal valor;
        try {
            valor = new BigDecimal(limpo);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato monetário inválido: " + input, e);
        }

        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O valor monetário não pode ser negativo: " + input);
        }

        // Ajusta para 2 casas decimais exatas
        BigDecimal emCentavos = valor.setScale(2, RoundingMode.HALF_UP).movePointRight(2);

        try {
            long centavos = emCentavos.longValueExact();
            if (centavos > MAX_CENTAVOS) {
                throw new IllegalArgumentException("Valor monetário excede o limite permitido: " + input);
            }
            return centavos;
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Valor monetário excede a capacidade numérica: " + input, e);
        }
    }

    /**
     * Formata um valor em centavos para a representação monetária padrão brasileira (R$ X,XX).
     */
    public static String formatarDinheiro(long centavos) {
        BigDecimal bd = BigDecimal.valueOf(centavos).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols(new Locale("pt", "BR"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00", symbols);
        return "R$ " + df.format(bd);
    }
}
