package com.github.matheuscruzsouza.pocketpdv.util;

/**
 * Utilitário para sanitização e escape de entidades HTML contra vulnerabilidades XSS.
 */
public final class HtmlEscaper {

    private HtmlEscaper() {
        // Utilitário estático
    }

    /**
     * Escapa caracteres sensíveis para HTML seguro: &, <, >, ", '
     * Retorna string vazia se o valor for nulo.
     */
    public static String escape(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#x27;");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }
}
