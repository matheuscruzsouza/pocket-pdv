package com.github.matheuscruzsouza.pocketpdv.web.controller;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.DeleteMethod;
import com.github.matheuscruzsouza.nanospring.annotation.GetMethod;
import com.github.matheuscruzsouza.nanospring.annotation.PathVariable;
import com.github.matheuscruzsouza.nanospring.annotation.PostMethod;
import com.github.matheuscruzsouza.nanospring.annotation.RequestParam;
import com.github.matheuscruzsouza.nanospring.annotation.RestController;
import com.github.matheuscruzsouza.nanospring.server.Server;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Carrinho;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Produto;
import com.github.matheuscruzsouza.pocketpdv.domain.model.RelatorioDiarioDTO;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Venda;
import com.github.matheuscruzsouza.pocketpdv.domain.service.EstoqueService;
import com.github.matheuscruzsouza.pocketpdv.domain.service.RelatorioService;
import com.github.matheuscruzsouza.pocketpdv.domain.service.VendaService;
import com.github.matheuscruzsouza.nanospring.sse.SseEmitter;
import com.github.matheuscruzsouza.pocketpdv.service.EstoqueSseHub;
import com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService;
import com.github.matheuscruzsouza.pocketpdv.web.view.HtmlTemplates;

import java.util.Date;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;

@RestController("/pdv")
public class PdvController {

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private VendaService vendaService;

    @Autowired
    private RelatorioService relatorioService;

    @Autowired
    private Carrinho carrinho;

    public PdvController() {
    }

    public PdvController(EstoqueService estoqueService, VendaService vendaService,
                         RelatorioService relatorioService, Carrinho carrinho) {
        this.estoqueService = estoqueService;
        this.vendaService = vendaService;
        this.relatorioService = relatorioService;
        this.carrinho = carrinho;
    }

    private Carrinho getCarrinho() {
        if (carrinho != null) return carrinho;
        if (PocketPdvService.getInstance() != null) {
            carrinho = PocketPdvService.getInstance().getCarrinho();
        }
        return carrinho;
    }

    private EstoqueService getEstoqueService() {
        if (estoqueService != null) return estoqueService;
        if (PocketPdvService.getInstance() != null) {
            Server s = PocketPdvService.getInstance().getServer();
            if (s != null) {
                estoqueService = (EstoqueService) s.getBean(com.github.matheuscruzsouza.pocketpdv.domain.service.EstoqueServiceImpl.class);
            }
        }
        return estoqueService;
    }

    private VendaService getVendaService() {
        if (vendaService != null) return vendaService;
        if (PocketPdvService.getInstance() != null) {
            Server s = PocketPdvService.getInstance().getServer();
            if (s != null) {
                vendaService = (VendaService) s.getBean(com.github.matheuscruzsouza.pocketpdv.domain.service.VendaServiceImpl.class);
            }
        }
        return vendaService;
    }

    private RelatorioService getRelatorioService() {
        if (relatorioService != null) return relatorioService;
        if (PocketPdvService.getInstance() != null) {
            Server s = PocketPdvService.getInstance().getServer();
            if (s != null) {
                relatorioService = (RelatorioService) s.getBean(com.github.matheuscruzsouza.pocketpdv.domain.service.RelatorioServiceImpl.class);
            }
        }
        return relatorioService;
    }

    @GetMethod(value = "", mimeType = "text/html")
    public String index() {
        return HtmlTemplates.paginaPdv(
                getEstoqueService() != null ? getEstoqueService().listarCatalogo() : java.util.Collections.emptyList(),
                getCarrinho(), null, null);
    }

    @PostMethod(value = "/carrinho/item", mimeType = "text/html")
    public String adicionarItem(
            @RequestParam("produtoId") String produtoIdStr,
            @RequestParam("quantidade") String qtdStr,
            NanoHTTPD.IHTTPSession session) {

        long produtoId = 0;
        int quantidade = 1;

        if (produtoIdStr != null && !produtoIdStr.isEmpty()) {
            try {
                produtoId = Long.parseLong(produtoIdStr);
            } catch (NumberFormatException ignored) {}
        }
        if (qtdStr != null && !qtdStr.isEmpty()) {
            try {
                quantidade = Integer.parseInt(qtdStr);
            } catch (NumberFormatException ignored) {}
        }

        if (produtoId == 0 && session != null && session.getParms() != null) {
            String pId = session.getParms().get("produtoId");
            if (pId != null) {
                try {
                    produtoId = Long.parseLong(pId);
                } catch (NumberFormatException ignored) {}
            }
            String q = session.getParms().get("quantidade");
            if (q != null) {
                try {
                    quantidade = Integer.parseInt(q);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (produtoId > 0 && getEstoqueService() != null) {
            Produto produto = getEstoqueService().buscarPorId(produtoId);
            if (produto != null && produto.getEstoque() > 0 && getCarrinho() != null) {
                getCarrinho().adicionar(produto, quantidade);
            }
        }

        return HtmlTemplates.fragmentoCarrinhoAtualizado(getCarrinho());
    }

    @PostMethod(value = "/carrinho/item/diminuir", mimeType = "text/html")
    public String diminuirItem(
            @RequestParam("produtoId") String produtoIdStr,
            @RequestParam("quantidade") String qtdStr,
            NanoHTTPD.IHTTPSession session) {

        long produtoId = 0;
        int quantidade = 1;

        if (produtoIdStr != null && !produtoIdStr.isEmpty()) {
            try {
                produtoId = Long.parseLong(produtoIdStr);
            } catch (NumberFormatException ignored) {}
        }
        if (qtdStr != null && !qtdStr.isEmpty()) {
            try {
                quantidade = Integer.parseInt(qtdStr);
            } catch (NumberFormatException ignored) {}
        }

        if (produtoId == 0 && session != null && session.getParms() != null) {
            String pId = session.getParms().get("produtoId");
            if (pId != null) {
                try {
                    produtoId = Long.parseLong(pId);
                } catch (NumberFormatException ignored) {}
            }
            String q = session.getParms().get("quantidade");
            if (q != null) {
                try {
                    quantidade = Integer.parseInt(q);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (produtoId > 0 && getCarrinho() != null) {
            getCarrinho().diminuir(produtoId, quantidade);
        }

        return HtmlTemplates.fragmentoCarrinhoAtualizado(getCarrinho());
    }

    @PostMethod(value = "/carrinho/codigo", mimeType = "text/html")
    public String adicionarPorCodigo(
            @RequestParam("codigo") String codigo,
            NanoHTTPD.IHTTPSession session) {

        if (codigo == null && session != null && session.getParms() != null) {
            codigo = session.getParms().get("codigo");
            if (codigo == null) {
                codigo = session.getParms().get("codigoBarras");
            }
        }

        if (codigo != null) {
            codigo = codigo.trim();
        }

        String alertaHtml = "";
        if (codigo == null || codigo.isEmpty()) {
            alertaHtml = "<div id=\"alerta-area\" hx-swap-oob=\"true\" style=\"background: #fef3c7; color: #92400e; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">Informe um código de barras.</div>";
            return HtmlTemplates.fragmentoCarrinhoAtualizado(getCarrinho()) + alertaHtml;
        }

        Produto produto = null;
        if (getEstoqueService() != null) {
            produto = getEstoqueService().buscarPorCodigoBarras(codigo);
            if (produto == null) {
                try {
                    long id = Long.parseLong(codigo);
                    produto = getEstoqueService().buscarPorId(id);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (produto == null) {
            alertaHtml = "<div id=\"alerta-area\" hx-swap-oob=\"true\" style=\"background: #fee2e2; color: #b91c1c; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">Produto não encontrado com código: <b>" + codigo + "</b></div>";
        } else if (produto.getEstoque() <= 0) {
            alertaHtml = "<div id=\"alerta-area\" hx-swap-oob=\"true\" style=\"background: #fee2e2; color: #b91c1c; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">Produto sem estoque: <b>" + produto.getNome() + "</b></div>";
        } else {
            if (getCarrinho() != null) {
                getCarrinho().adicionar(produto, 1);
                alertaHtml = "<div id=\"alerta-area\" hx-swap-oob=\"true\" style=\"background: #d1fae5; color: #065f46; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">Item adicionado: <b>" + produto.getNome() + "</b> (" + HtmlTemplates.formatarDinheiro(produto.getPrecoCentavos()) + ")</div>";
            }
        }

        return HtmlTemplates.fragmentoCarrinhoAtualizado(getCarrinho()) + alertaHtml;
    }

    @DeleteMethod(value = "/carrinho/item/:id", mimeType = "text/html")
    public String removerItem(@PathVariable("id") String idStr) {
        if (idStr != null && !idStr.isEmpty() && getCarrinho() != null) {
            try {
                long produtoId = Long.parseLong(idStr);
                getCarrinho().remover(produtoId);
            } catch (NumberFormatException ignored) {}
        }
        return HtmlTemplates.fragmentoCarrinhoAtualizado(getCarrinho());
    }

    @DeleteMethod(value = "/carrinho", mimeType = "text/html")
    public String limparCarrinho() {
        if (getCarrinho() != null) {
            getCarrinho().limpar();
        }
        String alertaHtml = "<div id=\"alerta-area\" hx-swap-oob=\"true\" style=\"background: #f3f4f6; color: #4b5563; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">Carrinho esvaziado.</div>";
        return HtmlTemplates.fragmentoCarrinhoAtualizado(getCarrinho()) + alertaHtml;
    }

    @GetMethod(value = "/relatorio", mimeType = "text/html")
    public String relatorioVendas() {
        RelatorioDiarioDTO resumo = null;
        if (getRelatorioService() != null) {
            resumo = getRelatorioService().obterResumoDoDia(new Date());
        }
        List<Venda> recentes = java.util.Collections.emptyList();
        if (getVendaService() != null) {
            recentes = getVendaService().listarVendasRecentes(10);
        }
        return HtmlTemplates.fragmentoModalRelatorio(resumo, recentes);
    }

    @PostMethod(value = "/carrinho/checkout", mimeType = "text/html")
    public String finalizarVenda(
            @RequestParam("formaPagamento") String formaPagamento,
            NanoHTTPD.IHTTPSession session) {
        try {
            if (getVendaService() == null || getCarrinho() == null) {
                return HtmlTemplates.fragmentoCheckoutErro("Serviço de venda indisponível.");
            }
            if (formaPagamento == null && session != null && session.getParms() != null) {
                formaPagamento = session.getParms().get("formaPagamento");
            }
            if (formaPagamento == null || formaPagamento.trim().isEmpty()) {
                formaPagamento = "DINHEIRO";
            }
            Venda venda = getVendaService().finalizarVenda(getCarrinho());
            return HtmlTemplates.fragmentoCheckoutSucesso(venda, formaPagamento);
        } catch (Exception e) {
            return HtmlTemplates.fragmentoCheckoutErro(e.getMessage());
        }
    }

    @GetMethod(value = "/eventos/estoque", mimeType = "text/event-stream")
    public SseEmitter streamEstoque() {
        return EstoqueSseHub.getInstance().registrar();
    }

    @GetMethod(value = "/estoque", mimeType = "text/html")
    public String modalEstoque() {
        List<Produto> catalogo = (getEstoqueService() != null) ? getEstoqueService().listarCatalogo() : java.util.Collections.emptyList();
        return HtmlTemplates.fragmentoModalEstoque(catalogo, null);
    }

    @PostMethod(value = "/estoque/ajuste", mimeType = "text/html")
    public String ajustarEstoque(
            @RequestParam("produtoId") String produtoIdStr,
            @RequestParam("novoEstoque") String novoEstoqueStr,
            NanoHTTPD.IHTTPSession session) {

        long produtoId = 0;
        int novoEstoque = 0;

        if (produtoIdStr != null && !produtoIdStr.isEmpty()) {
            try { produtoId = Long.parseLong(produtoIdStr); } catch (Exception ignored) {}
        }
        if (novoEstoqueStr != null && !novoEstoqueStr.isEmpty()) {
            try { novoEstoque = Integer.parseInt(novoEstoqueStr); } catch (Exception ignored) {}
        }

        if (session != null && session.getParms() != null) {
            if (produtoId == 0 && session.getParms().containsKey("produtoId")) {
                try { produtoId = Long.parseLong(session.getParms().get("produtoId")); } catch (Exception ignored) {}
            }
            if (novoEstoque == 0 && session.getParms().containsKey("novoEstoque")) {
                try { novoEstoque = Integer.parseInt(session.getParms().get("novoEstoque")); } catch (Exception ignored) {}
            }
        }

        String msg = null;
        if (produtoId > 0 && getEstoqueService() != null) {
            boolean ok = getEstoqueService().ajustarEstoque(produtoId, novoEstoque);
            if (ok) {
                msg = "Estoque atualizado com sucesso para " + novoEstoque + " un!";
            }
        }

        List<Produto> catalogo = (getEstoqueService() != null) ? getEstoqueService().listarCatalogo() : java.util.Collections.emptyList();
        return HtmlTemplates.fragmentoModalEstoque(catalogo, msg);
    }

    @PostMethod(value = "/estoque/novo", mimeType = "text/html")
    public String novoProduto(
            @RequestParam("nome") String nome,
            @RequestParam("codigoBarras") String codigoBarras,
            @RequestParam("preco") String precoStr,
            @RequestParam("estoque") String estoqueStr,
            NanoHTTPD.IHTTPSession session) {

        if (session != null && session.getParms() != null) {
            if (nome == null) nome = session.getParms().get("nome");
            if (codigoBarras == null) codigoBarras = session.getParms().get("codigoBarras");
            if (precoStr == null) precoStr = session.getParms().get("preco");
            if (estoqueStr == null) estoqueStr = session.getParms().get("estoque");
        }

        String msg = null;
        try {
            if (nome != null && codigoBarras != null && precoStr != null && getEstoqueService() != null) {
                int precoCentavos = Math.round(Float.parseFloat(precoStr.replace(",", ".")) * 100);
                int estoque = (estoqueStr != null && !estoqueStr.isEmpty()) ? Integer.parseInt(estoqueStr) : 0;
                Produto p = new Produto(0, codigoBarras.trim(), nome.trim(), precoCentavos, estoque);
                long id = getEstoqueService().salvarProduto(p);
                if (id > 0) {
                    msg = "Produto '" + p.getNome() + "' cadastrado com sucesso!";
                }
            }
        } catch (Exception e) {
            msg = "Erro ao cadastrar produto: " + e.getMessage();
        }

        List<Produto> catalogo = (getEstoqueService() != null) ? getEstoqueService().listarCatalogo() : java.util.Collections.emptyList();
        return HtmlTemplates.fragmentoModalEstoque(catalogo, msg);
    }
}
