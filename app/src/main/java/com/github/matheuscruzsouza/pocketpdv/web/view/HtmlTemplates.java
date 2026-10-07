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

    public static String formatarDinheiro(long centavos) {
        return com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.formatarDinheiro(centavos);
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
        long totalCentavos = (carrinho != null) ? carrinho.getTotalCentavos() : 0L;
        mav.addObject("totalFormatado", formatarDinheiro(totalCentavos));
        mav.addObject("carrinhoVazio", carrinho == null || carrinho.getItens().isEmpty());

        return mav;
    }

    public static String paginaPdvCaixaFechado(Funcionario operador) {
        String nomeOperador = (operador != null && operador.getNome() != null) ? operador.getNome() : "Carlos Silva";
        return "<!DOCTYPE html><html lang=\"pt-BR\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>PocketPDV - Caixa Fechado</title><link rel=\"stylesheet\" href=\"/assets/style.css\"><script src=\"/assets/htmx.min.js\"></script></head><body style=\"background-color:#f3f4f6;\">" +
               "<div class=\"container\">" +
               "<header class=\"header\">" +
               "<div style=\"display: flex; align-items: center; gap: 0.5rem;\"><span class=\"brand\">PocketPDV</span><span class=\"badge\" style=\"margin-left: 0.5rem; background: #e0e7ff; color: #3730a3;\">👤 " + nomeOperador + "</span></div>" +
               "<div style=\"display: flex; gap: 0.75rem; align-items: center;\"><form method=\"post\" action=\"/logout\" style=\"margin: 0;\"><button type=\"submit\" style=\"background: none; border: none; padding: 0; cursor: pointer; color: #6b7280; font-size: 0.875rem;\">Encerrar Sessão</button></form></div>" +
               "</header>" +
               "<div style=\"display: flex; justify-content: center; align-items: center; min-height: 70vh;\">" +
               "<div class=\"card\" style=\"text-align: center; max-width: 400px; padding: 3rem 2rem;\">" +
               "  <h2 style=\"color: #b91c1c; margin-bottom: 1rem;\">🔒 Caixa Fechado</h2>" +
               "  <p style=\"color: #4b5563; margin-bottom: 2rem;\">Você precisa abrir o caixa para iniciar as vendas do dia.</p>" +
               "  <button class=\"btn btn-primary\" style=\"width: 100%; font-size: 1.1rem; padding: 0.75rem;\" hx-get=\"/caixa/modal-abrir\" hx-target=\"#modal-container\">Abrir Caixa</button>" +
               "</div>" +
               "</div>" +
               "</div>" +
               "<div id=\"modal-container\"></div>" +
               "<script>function fecharModal() { var c = document.getElementById('modal-container'); if(c) c.innerHTML = ''; }</script>" +
               "</body></html>";
    }

    public static String fragmentoModalAberturaCaixa(String erro) {
        String msgErro = (erro != null) ? "<div style=\"color:red; margin-bottom:1rem;\">" + erro + "</div>" : "";
        return "<div class=\"modal\" id=\"modal-caixa\" onclick=\"if(event.target===this) fecharModal()\"><div class=\"modal-content\" style=\"max-width:350px;\">" +
               "<div class=\"modal-header\"><h3 class=\"modal-title\">Abrir Caixa</h3><button class=\"modal-close\" onclick=\"fecharModal()\">&times;</button></div>" +
               msgErro +
               "<form hx-post=\"/caixa/abrir\" hx-target=\"#modal-container\">" +
               "<div class=\"form-group\"><label class=\"form-label\">Troco Inicial na Gaveta (R$)</label>" +
               "<input type=\"number\" step=\"0.01\" name=\"trocoInicial\" class=\"form-input\" required placeholder=\"Ex: 100.00\" autofocus></div>" +
               "<button type=\"submit\" class=\"btn btn-primary\" style=\"width:100%;\">Confirmar Abertura</button>" +
               "</form></div></div>";
    }

    public static String fragmentoModalFechamentoCaixa(String erro) {
        String msgErro = (erro != null) ? "<div style=\"color:red; margin-bottom:1rem;\">" + erro + "</div>" : "";
        return "<div class=\"modal\" id=\"modal-caixa\" onclick=\"if(event.target===this) fecharModal()\"><div class=\"modal-content\" style=\"max-width:350px;\">" +
               "<div class=\"modal-header\"><h3 class=\"modal-title\">Fechar Caixa</h3><button class=\"modal-close\" onclick=\"fecharModal()\">&times;</button></div>" +
               msgErro +
               "<form hx-post=\"/caixa/fechar\" hx-target=\"#modal-container\">" +
               "<p style=\"color:#4b5563; font-size:0.9rem; margin-bottom:1rem;\">Conte o dinheiro da gaveta e informe o total físico.</p>" +
               "<div class=\"form-group\"><label class=\"form-label\">Valor Físico Declarado (R$)</label>" +
               "<input type=\"number\" step=\"0.01\" name=\"valorDeclarado\" class=\"form-input\" required placeholder=\"Ex: 150.00\" autofocus></div>" +
               "<button type=\"submit\" class=\"btn btn-danger\" style=\"width:100%;\">Encerrar Turno</button>" +
               "</form></div></div>";
    }

    public static String fragmentoModalMovimentacaoCaixa(String tipo, String erro) {
        String msgErro = (erro != null) ? "<div style=\"color:red; margin-bottom:1rem;\">" + erro + "</div>" : "";
        String cor = "SANGRIA".equals(tipo) ? "btn-danger" : "btn-primary";
        String titulo = "SANGRIA".equals(tipo) ? "Retirada de Dinheiro (Sangria)" : "Entrada de Troco (Suprimento)";
        return "<div class=\"modal\" id=\"modal-caixa\" onclick=\"if(event.target===this) fecharModal()\"><div class=\"modal-content\" style=\"max-width:350px;\">" +
               "<div class=\"modal-header\"><h3 class=\"modal-title\">" + titulo + "</h3><button class=\"modal-close\" onclick=\"fecharModal()\">&times;</button></div>" +
               msgErro +
               "<form hx-post=\"/caixa/movimentacao\" hx-target=\"#modal-container\">" +
               "<input type=\"hidden\" name=\"tipo\" value=\"" + tipo + "\">" +
               "<div class=\"form-group\"><label class=\"form-label\">Valor (R$)</label>" +
               "<input type=\"number\" step=\"0.01\" name=\"valor\" class=\"form-input\" required placeholder=\"Ex: 50.00\" autofocus></div>" +
               "<div class=\"form-group\"><label class=\"form-label\">Motivo/Descrição</label>" +
               "<input type=\"text\" name=\"descricao\" class=\"form-input\" required placeholder=\"Motivo da operação\"></div>" +
               "<button type=\"submit\" class=\"btn " + cor + "\" style=\"width:100%;\">Confirmar Operação</button>" +
               "</form></div></div>";
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
        long totalCentavos = (carrinho != null) ? carrinho.getTotalCentavos() : 0L;
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
        long totalCentavos = (resumo != null) ? resumo.getTotalCentavos() : 0L;
        int qtdVendas = (resumo != null) ? resumo.getQuantidadeVendas() : 0;
        long ticketMedioCentavos = (resumo != null) ? resumo.getTicketMedioCentavos() : 0L;

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
        return fragmentoCheckoutSucesso(venda, formaPagamento, venda != null ? venda.getTotalCentavos() : 0L, 0L, java.util.Collections.emptyList());
    }

    public static String fragmentoCheckoutSucesso(Venda venda, String formaPagamento, long valorRecebidoCentavos, long trocoCentavos) {
        return fragmentoCheckoutSucesso(venda, formaPagamento, valorRecebidoCentavos, trocoCentavos, java.util.Collections.emptyList());
    }

    public static String fragmentoCheckoutSucesso(Venda venda, String formaPagamento, long valorRecebidoCentavos, long trocoCentavos, List<Produto> atualizados) {
        String formaEscapada = com.github.matheuscruzsouza.pocketpdv.util.HtmlEscaper.escape(formaPagamento);
        String dataHoraEscapada = com.github.matheuscruzsouza.pocketpdv.util.HtmlEscaper.escape(venda != null ? venda.getDataHora() : "");
        long vendaId = venda != null ? venda.getId() : 0;
        long totalCentavos = venda != null ? venda.getTotalCentavos() : 0L;

        StringBuilder detalhePagamento = new StringBuilder();
        detalhePagamento.append("Total: <b>").append(formatarDinheiro(totalCentavos)).append("</b>")
                .append(" &bull; Forma: <b>").append(formaEscapada).append("</b>");

        if ("DINHEIRO".equalsIgnoreCase(formaPagamento) && valorRecebidoCentavos > 0) {
            detalhePagamento.append(" &bull; Recebido: <b>").append(formatarDinheiro(valorRecebidoCentavos)).append("</b>");
            if (trocoCentavos > 0) {
                detalhePagamento.append(" &bull; Troco: <b style=\"color: #047857;\">").append(formatarDinheiro(trocoCentavos)).append("</b>");
            }
        }
        detalhePagamento.append(" &bull; Horário: ").append(dataHoraEscapada);

        StringBuilder jsUpdates = new StringBuilder();
        if (atualizados != null) {
            for (Produto p : atualizados) {
                jsUpdates.append("  var el").append(p.getId()).append(" = document.getElementById('estoque-prod-").append(p.getId()).append("');\n");
                jsUpdates.append("  var btn").append(p.getId()).append(" = document.getElementById('btn-add-prod-").append(p.getId()).append("');\n");
                jsUpdates.append("  if(el").append(p.getId()).append(") { ");
                if (p.getEstoque() <= 0) {
                    jsUpdates.append("el").append(p.getId()).append(".innerText = '0 un (Esgotado)'; el").append(p.getId()).append(".style.background = '#fee2e2'; el").append(p.getId()).append(".style.color = '#b91c1c';");
                } else if (p.getEstoque() <= 5) {
                    jsUpdates.append("el").append(p.getId()).append(".innerText = '").append(p.getEstoque()).append(" un'; el").append(p.getId()).append(".style.background = '#fee2e2'; el").append(p.getId()).append(".style.color = '#b91c1c';");
                } else {
                    jsUpdates.append("el").append(p.getId()).append(".innerText = '").append(p.getEstoque()).append(" un'; el").append(p.getId()).append(".style.background = '#e5e7eb'; el").append(p.getId()).append(".style.color = '#374151';");
                }
                jsUpdates.append(" }\n");
                jsUpdates.append("  if(btn").append(p.getId()).append(") { ");
                if (p.getEstoque() <= 0) {
                    jsUpdates.append("btn").append(p.getId()).append(".disabled = true; btn").append(p.getId()).append(".innerText = 'Esgotado'; btn").append(p.getId()).append(".className = 'btn btn-secondary'; btn").append(p.getId()).append(".style.opacity = '0.5'; btn").append(p.getId()).append(".style.cursor = 'not-allowed';");
                } else {
                    jsUpdates.append("btn").append(p.getId()).append(".disabled = false; btn").append(p.getId()).append(".innerText = '+ Adicionar'; btn").append(p.getId()).append(".className = 'btn btn-primary'; btn").append(p.getId()).append(".style.opacity = '1'; btn").append(p.getId()).append(".style.cursor = 'pointer';");
                }
                jsUpdates.append(" }\n");
            }
        }

        return "<div style=\"background: #d1fae5; color: #065f46; padding: 1rem; border-radius: 0.5rem; margin-bottom: 1rem; border: 1px solid #a7f3d0;\">\n" +
                "  <div class=\"font-bold\" style=\"font-size: 1.1rem;\">✅ Venda #" + vendaId + " finalizada com sucesso!</div>\n" +
                "  <div style=\"margin-top: 0.25rem;\">" + detalhePagamento + "</div>\n" +
                "</div>\n" +
                "<script>\n" +
                "  if(typeof fecharModal === 'function') fecharModal();\n" +
                "  var tbody = document.getElementById('cart-table-body');\n" +
                "  if(tbody) tbody.innerHTML = '<tr><td colspan=\"5\" class=\"text-center\" style=\"color: #9ca3af; padding: 2rem;\">Carrinho vazio</td></tr>';\n" +
                "  var cTot = document.getElementById('cart-total');\n" +
                "  if(cTot) cTot.innerText = 'R$ 0,00';\n" +
                "  var bChk = document.getElementById('btn-checkout');\n" +
                "  if(bChk) { bChk.disabled = true; bChk.style.opacity = '0.5'; bChk.style.cursor = 'not-allowed'; }\n" +
                "  var inp = document.getElementById('input-codigo-barras');\n" +
                "  if(inp) { inp.value = ''; inp.focus(); }\n" +
                jsUpdates.toString() +
                "</script>\n";
    }

    public static String fragmentoCheckoutErro(String erro) {
        String erroEscapado = com.github.matheuscruzsouza.pocketpdv.util.HtmlEscaper.escape(erro);
        return "<div style=\"background: #fee2e2; color: #b91c1c; padding: 1rem; border-radius: 0.5rem; margin-bottom: 1rem; border: 1px solid #fca5a5;\">\n" +
                "  <div class=\"font-bold\">❌ Erro ao finalizar venda</div>\n" +
                "  <div>" + erroEscapado + "</div>\n" +
                "</div>\n" +
                "<script>\n" +
                "  if(typeof fecharModal === 'function') fecharModal();\n" +
                "</script>\n";
    }
}
