package com.github.matheuscruzsouza.pocketpdv.web.view;

import android.content.Context;
import com.github.matheuscruzsouza.nanospring.server.Server;
import com.github.matheuscruzsouza.nanospring.ui.ModelAndView;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Carrinho;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Funcionario;
import com.github.matheuscruzsouza.pocketpdv.domain.model.ItemCarrinho;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Produto;
import com.github.matheuscruzsouza.pocketpdv.domain.model.RelatorioDiarioDTO;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Venda;
import com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService;
import com.samskivert.mustache.Mustache;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Utilitário de renderização de views e fragmentos utilizando o modelo de template do nano-spring.
 */
public class HtmlTemplates {

    public static String formatarDinheiro(int centavos) {
        return String.format(Locale.GERMANY, "R$ %.2f", centavos / 100.0);
    }

    /**
     * Renderiza um template Mustache localizado em assets/templates/{nomeTemplate}.html
     */
    public static String renderTemplate(String nomeTemplate, Map<String, Object> model) {
        try {
            Context ctx = Server.getContext();
            if (ctx == null) {
                ctx = PocketPdvService.getAppContext();
            }
            if (ctx == null) {
                return "<!-- Contexto Android indisponível para carregar template: " + nomeTemplate + " -->";
            }
            InputStream is = ctx.getAssets().open("templates/" + nomeTemplate + ".html");
            Reader reader = new InputStreamReader(is, "UTF-8");
            return Mustache.compiler().compile(reader).execute(model);
        } catch (Exception e) {
            return "<!-- Erro ao renderizar template " + nomeTemplate + ": " + e.getMessage() + " -->";
        }
    }

    // --- Páginas Principais (ModelAndView) ---

    public static ModelAndView paginaLogin(String erro, String sucesso) {
        ModelAndView mav = new ModelAndView("login");
        if (erro != null && !erro.trim().isEmpty()) {
            mav.addObject("erro", erro);
        }
        if (sucesso != null && !sucesso.trim().isEmpty()) {
            mav.addObject("sucesso", sucesso);
        }
        return mav;
    }

    public static ModelAndView paginaPrimeiroAcesso(String erro, String sucesso) {
        ModelAndView mav = new ModelAndView("primeiro-acesso");
        if (erro != null && !erro.trim().isEmpty()) {
            mav.addObject("erro", erro);
        }
        if (sucesso != null && !sucesso.trim().isEmpty()) {
            mav.addObject("sucesso", sucesso);
        }
        return mav;
    }

    public static ModelAndView paginaPdv(List<Produto> catalogo, Carrinho carrinho, Funcionario operador, String msgSucesso, String msgErro) {
        ModelAndView mav = new ModelAndView("pdv");
        String nomeOperador = (operador != null && operador.getNome() != null) ? operador.getNome() : "Carlos Silva";
        String usuarioOperador = (operador != null && operador.getUsuario() != null) ? operador.getUsuario() : "operador";

        mav.addObject("nomeOperador", nomeOperador);
        mav.addObject("usuarioOperador", usuarioOperador);
        if (msgSucesso != null && !msgSucesso.trim().isEmpty()) mav.addObject("msgSucesso", msgSucesso);
        if (msgErro != null && !msgErro.trim().isEmpty()) mav.addObject("msgErro", msgErro);

        // Prepara lista de produtos para exibição
        List<Map<String, Object>> catalogoList = new ArrayList<>();
        if (catalogo != null) {
            for (Produto p : catalogo) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", p.getId());
                item.put("nome", p.getNome());
                item.put("nomeBusca", p.getNome().toLowerCase());
                item.put("codigoBarras", p.getCodigoBarras());
                item.put("precoFormatado", formatarDinheiro(p.getPrecoCentavos()));
                item.put("estoque", p.getEstoque());
                item.put("estoqueBaixo", p.getEstoque() <= 5);
                item.put("disponivel", p.getEstoque() > 0);
                catalogoList.add(item);
            }
        }
        mav.addObject("catalogo", catalogoList);

        // Itens do carrinho e total
        List<Map<String, Object>> itensModel = extrairItensCarrinho(carrinho);
        mav.addObject("itensCarrinho", itensModel);
        int totalCentavos = (carrinho != null) ? carrinho.getTotalCentavos() : 0;
        mav.addObject("totalFormatado", formatarDinheiro(totalCentavos));
        mav.addObject("carrinhoVazio", carrinho == null || carrinho.getItens().isEmpty());

        return mav;
    }

    public static ModelAndView paginaDisplayCliente(List<Funcionario> caixas, String caixaParam) {
        ModelAndView mav = new ModelAndView("display");
        mav.addObject("caixas", caixas != null ? caixas : java.util.Collections.emptyList());
        mav.addObject("caixaParam", caixaParam != null ? caixaParam : "");
        return mav;
    }

    // --- Fragmentos HTMX ---

    private static List<Map<String, Object>> extrairItensCarrinho(Carrinho carrinho) {
        List<Map<String, Object>> itens = new ArrayList<>();
        if (carrinho != null && !carrinho.getItens().isEmpty()) {
            for (ItemCarrinho ic : carrinho.getItens()) {
                Map<String, Object> map = new HashMap<>();
                map.put("produtoId", ic.getProduto().getId());
                map.put("nome", ic.getProduto().getNome());
                map.put("quantidade", ic.getQuantidade());
                map.put("precoFormatado", formatarDinheiro(ic.getPrecoUnitCentavos()));
                map.put("subtotalFormatado", formatarDinheiro(ic.getSubtotalCentavos()));
                itens.add(map);
            }
        }
        return itens;
    }

    public static String fragmentoItensCarrinho(Carrinho carrinho) {
        Map<String, Object> model = new HashMap<>();
        model.put("itens", extrairItensCarrinho(carrinho));
        return renderTemplate("carrinho-itens", model);
    }

    public static String fragmentoCarrinhoAtualizado(Carrinho carrinho) {
        int totalCentavos = (carrinho != null) ? carrinho.getTotalCentavos() : 0;
        boolean vazio = (carrinho == null || carrinho.getItens().isEmpty());

        return fragmentoItensCarrinho(carrinho) +
                "<div id=\"cart-total\" hx-swap-oob=\"true\" class=\"cart-total-val\">" +
                formatarDinheiro(totalCentavos) + "</div>\n" +
                "<button id=\"btn-checkout\" hx-swap-oob=\"true\" class=\"btn btn-checkout\" onclick=\"abrirCheckout()\" " +
                (vazio ? "disabled style=\"opacity: 0.5; cursor: not-allowed;\"" : "") +
                ">Finalizar Venda</button>\n" +
                "<input type=\"text\" name=\"codigo\" id=\"input-codigo-barras\" hx-swap-oob=\"true\" class=\"form-input\" style=\"flex: 1; min-width: 220px; font-family: monospace; font-size: 1.1rem; font-weight: bold;\" placeholder=\"Bipe ou digite o código de barras (Enter)...\" autofocus autocomplete=\"off\" value=\"\">\n";
    }

    public static String fragmentoModalRelatorio(RelatorioDiarioDTO resumo, List<Venda> vendasRecentes) {
        Map<String, Object> model = new HashMap<>();
        int totalCentavos = (resumo != null) ? resumo.getTotalCentavos() : 0;
        int qtdVendas = (resumo != null) ? resumo.getQuantidadeVendas() : 0;
        int ticketMedioCentavos = (resumo != null) ? resumo.getTicketMedioCentavos() : 0;

        model.put("totalFaturamento", formatarDinheiro(totalCentavos));
        model.put("qtdVendas", qtdVendas);
        model.put("ticketMedio", formatarDinheiro(ticketMedioCentavos));

        List<Map<String, Object>> vendasModel = new ArrayList<>();
        if (vendasRecentes != null) {
            for (Venda v : vendasRecentes) {
                Map<String, Object> vm = new HashMap<>();
                vm.put("id", v.getId());
                vm.put("dataHora", v.getDataHora());
                vm.put("totalFormatado", formatarDinheiro(v.getTotalCentavos()));
                vm.put("status", v.getStatus());
                vendasModel.add(vm);
            }
        }
        model.put("vendas", vendasModel);
        return renderTemplate("modal-relatorio", model);
    }

    public static String fragmentoModalEstoque(List<Produto> catalogo, String alerta) {
        Map<String, Object> model = new HashMap<>();
        if (alerta != null && !alerta.trim().isEmpty()) {
            model.put("alerta", alerta);
        }
        List<Map<String, Object>> catList = new ArrayList<>();
        if (catalogo != null) {
            for (Produto p : catalogo) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", p.getId());
                item.put("nome", p.getNome());
                item.put("codigoBarras", p.getCodigoBarras());
                item.put("precoFormatado", formatarDinheiro(p.getPrecoCentavos()));
                item.put("estoque", p.getEstoque());
                item.put("estoqueBaixo", p.getEstoque() <= 5);
                catList.add(item);
            }
        }
        model.put("catalogo", catList);
        return renderTemplate("modal-estoque", model);
    }

    public static String fragmentoCheckoutSucesso(Venda venda, String formaPagamento) {
        String formaEscapada = com.github.matheuscruzsouza.pocketpdv.util.HtmlEscaper.escape(formaPagamento);
        String dataHoraEscapada = com.github.matheuscruzsouza.pocketpdv.util.HtmlEscaper.escape(venda != null ? venda.getDataHora() : "");
        long vendaId = venda != null ? venda.getId() : 0;
        int totalCentavos = venda != null ? venda.getTotalCentavos() : 0;

        return "<div style=\"background: #d1fae5; color: #065f46; padding: 1rem; border-radius: 0.5rem; margin-bottom: 1rem; border: 1px solid #a7f3d0;\">\n" +
                "  <div class=\"font-bold\" style=\"font-size: 1.1rem;\">✅ Venda #" + vendaId + " finalizada com sucesso!</div>\n" +
                "  <div style=\"margin-top: 0.25rem;\">Total: <b>" + formatarDinheiro(totalCentavos) + "</b> &bull; Forma: <b>" + formaEscapada + "</b> &bull; Horário: " + dataHoraEscapada + "</div>\n" +
                "</div>\n" +
                "<tbody id=\"cart-table-body\" hx-swap-oob=\"true\">\n" +
                "  <tr><td colspan=\"5\" class=\"text-center\" style=\"color: #9ca3af; padding: 2rem;\">Carrinho vazio</td></tr>\n" +
                "</tbody>\n" +
                "<div id=\"cart-total\" hx-swap-oob=\"true\" class=\"cart-total-val\">R$ 0,00</div>\n" +
                "<button id=\"btn-checkout\" hx-swap-oob=\"true\" class=\"btn btn-checkout\" onclick=\"abrirCheckout()\" disabled style=\"opacity: 0.5; cursor: not-allowed;\">Finalizar Venda</button>\n" +
                "<input type=\"text\" name=\"codigo\" id=\"input-codigo-barras\" hx-swap-oob=\"true\" class=\"form-input\" style=\"flex: 1; min-width: 220px; font-family: monospace; font-size: 1.1rem; font-weight: bold;\" placeholder=\"Bipe ou digite o código de barras (Enter)...\" autofocus autocomplete=\"off\" value=\"\">\n";
    }

    public static String fragmentoCheckoutErro(String erro) {
        String erroEscapado = com.github.matheuscruzsouza.pocketpdv.util.HtmlEscaper.escape(erro);
        return "<div style=\"background: #fee2e2; color: #b91c1c; padding: 1rem; border-radius: 0.5rem; margin-bottom: 1rem; border: 1px solid #fca5a5;\">\n" +
                "  <div class=\"font-bold\">❌ Erro ao finalizar venda</div>\n" +
                "  <div>" + erroEscapado + "</div>\n" +
                "</div>";
    }
}
