package com.github.matheuscruzsouza.pocketpdv.web.controller;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.DeleteMethod;
import com.github.matheuscruzsouza.nanospring.annotation.GetMethod;
import com.github.matheuscruzsouza.nanospring.annotation.PathVariable;
import com.github.matheuscruzsouza.nanospring.annotation.PostMethod;
import com.github.matheuscruzsouza.nanospring.annotation.RequestParam;
import com.github.matheuscruzsouza.nanospring.annotation.RestController;
import com.github.matheuscruzsouza.nanospring.openapi.annotation.ApiResponse;
import com.github.matheuscruzsouza.nanospring.openapi.annotation.Operation;
import com.github.matheuscruzsouza.nanospring.openapi.annotation.Parameter;
import com.github.matheuscruzsouza.nanospring.openapi.annotation.Tag;
import com.github.matheuscruzsouza.nanospring.server.Server;
import com.github.matheuscruzsouza.nanospring.sse.SseEmitter;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Carrinho;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Funcionario;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Produto;
import com.github.matheuscruzsouza.pocketpdv.domain.model.RelatorioDiarioDTO;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Session;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Venda;
import com.github.matheuscruzsouza.pocketpdv.domain.service.EstoqueService;
import com.github.matheuscruzsouza.pocketpdv.domain.service.RelatorioService;
import com.github.matheuscruzsouza.pocketpdv.domain.service.VendaService;
import com.github.matheuscruzsouza.pocketpdv.persistence.FuncionarioRepository;
import com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService;
import com.github.matheuscruzsouza.pocketpdv.service.SessionService;
import com.github.matheuscruzsouza.pocketpdv.service.EstoqueSseHub;
import com.github.matheuscruzsouza.pocketpdv.web.interceptor.AuthInterceptor;
import com.github.matheuscruzsouza.pocketpdv.web.view.HtmlTemplates;

import java.util.Date;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;

@Tag(name = "Ponto de Venda (PDV)", description = "Operações de venda, catálogo, carrinho, checkout e estoque")
@RestController("/pdv")
public class PdvController {

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private VendaService vendaService;

    @Autowired
    private RelatorioService relatorioService;

    @Autowired
    private SessionService sessionService;

    public PdvController() {
    }

    public PdvController(EstoqueService estoqueService, VendaService vendaService,
                         RelatorioService relatorioService) {
        this(estoqueService, vendaService, relatorioService, null);
    }

    public PdvController(EstoqueService estoqueService, VendaService vendaService,
                         RelatorioService relatorioService,
                         SessionService sessionService) {
        this.estoqueService = estoqueService;
        this.vendaService = vendaService;
        this.relatorioService = relatorioService;
        this.sessionService = sessionService;
    }

    private SessionService getSessionService() {
        if (sessionService != null) return sessionService;
        if (PocketPdvService.getInstance() != null) {
            sessionService = PocketPdvService.getInstance().getSessionService();
        }
        if (sessionService == null) {
            sessionService = new SessionService();
        }
        return sessionService;
    }

    private Session obterSessao(NanoHTTPD.IHTTPSession session) {
        String sessionId = AuthInterceptor.extrairSessionId(session);
        if (sessionId == null) return null;
        SessionService sService = getSessionService();
        return sService != null ? sService.obterSessao(sessionId) : null;
    }

    private Carrinho getCarrinho(Session sessao) {
        if (sessao == null) {
            return new Carrinho();
        }
        return getSessionService().obterCarrinho(sessao.getId());
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

    private FuncionarioRepository getFuncionarioRepository() {
        if (PocketPdvService.getInstance() != null) {
            Server s = PocketPdvService.getInstance().getServer();
            if (s != null) {
                try {
                    return (FuncionarioRepository) s.getBean(FuncionarioRepository.class);
                } catch (Exception ignored) {}
            }
            if (PocketPdvService.getInstance().getDbHelper() != null) {
                return new FuncionarioRepository(PocketPdvService.getInstance().getDbHelper());
            }
        }
        return null;
    }

    @Operation(summary = "Página principal do PDV", description = "Renderiza catálogo de produtos, carrinho e informações do operador autenticado")
    @ApiResponse(responseCode = 200, description = "Interface do PDV renderizada")
    @ApiResponse(responseCode = 302, description = "Redireciona para /login caso não autenticado")
    @GetMethod(value = "", mimeType = "text/html")
    public Object index(
            @Parameter(description = "Identificador do operador de caixa", example = "blima") @RequestParam("operador") String operador,
            NanoHTTPD.IHTTPSession session) {
        Session sessaoAtiva = obterSessao(session);
        if (sessaoAtiva == null) {
            NanoHTTPD.Response redirect = NanoHTTPD.newFixedLengthResponse(
                    NanoHTTPD.Response.Status.REDIRECT,
                    "text/html",
                    "<!DOCTYPE html><html><head><meta http-equiv=\"refresh\" content=\"0;url=/login\"></head><body><script>window.location.href='/login';</script></body></html>"
            );
            redirect.addHeader("Location", "/login");
            return redirect;
        }

        String usuario = sessaoAtiva.getUsuario();
        Funcionario op = null;
        if (getFuncionarioRepository() != null) {
            op = getFuncionarioRepository().buscarPorUsuario(usuario);
        }
        Carrinho c = getCarrinho(sessaoAtiva);
        return HtmlTemplates.paginaPdv(
                getEstoqueService() != null ? getEstoqueService().listarCatalogo() : java.util.Collections.emptyList(),
                c, op, null, null);
    }

    @Operation(summary = "Adicionar item ao carrinho por ID", description = "Incrementa o produto no carrinho e emite atualização SSE para o display")
    @ApiResponse(responseCode = 200, description = "Fragmento HTML do carrinho atualizado")
    @PostMethod(value = "/carrinho/item", mimeType = "text/html")
    public String adicionarItem(
            @Parameter(description = "ID do produto", example = "1") @RequestParam("produtoId") String produtoIdStr,
            @Parameter(description = "Quantidade a adicionar", example = "1") @RequestParam("quantidade") String qtdStr,
            NanoHTTPD.IHTTPSession session) {

        Session sessaoAtiva = obterSessao(session);
        if (sessaoAtiva == null) {
            return "<div id=\"alerta-area\" hx-swap-oob=\"true\" style=\"background: #fee2e2; color: #b91c1c; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">Sessão expirada. Faça login novamente.</div>";
        }

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

        String op = sessaoAtiva.getUsuario();
        Carrinho c = getCarrinho(sessaoAtiva);

        if (quantidade <= 0) {
            String alertaHtml = "<div id=\"alerta-area\" hx-swap-oob=\"true\" style=\"background: #fee2e2; color: #b91c1c; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">A quantidade deve ser maior que zero.</div>";
            return HtmlTemplates.fragmentoCarrinhoAtualizado(c) + alertaHtml;
        }

        if (produtoId > 0 && getEstoqueService() != null) {
            Produto produto = getEstoqueService().buscarPorId(produtoId);
            if (produto != null && produto.getEstoque() > 0 && c != null) {
                c.adicionar(produto, quantidade);
                EstoqueSseHub.getInstance().notificarCarrinho(op, c);
            }
        }

        return HtmlTemplates.fragmentoCarrinhoAtualizado(c);
    }

    @Operation(summary = "Diminuir quantidade de item no carrinho", description = "Decrementa a quantidade ou remove o item se zerar")
    @ApiResponse(responseCode = 200, description = "Fragmento HTML do carrinho atualizado")
    @PostMethod(value = "/carrinho/item/diminuir", mimeType = "text/html")
    public String diminuirItem(
            @Parameter(description = "ID do produto", example = "1") @RequestParam("produtoId") String produtoIdStr,
            @Parameter(description = "Quantidade a diminuir", example = "1") @RequestParam("quantidade") String qtdStr,
            NanoHTTPD.IHTTPSession session) {

        Session sessaoAtiva = obterSessao(session);
        if (sessaoAtiva == null) {
            return "<div id=\"alerta-area\" hx-swap-oob=\"true\" style=\"background: #fee2e2; color: #b91c1c; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">Sessão expirada. Faça login novamente.</div>";
        }

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

        String op = sessaoAtiva.getUsuario();
        Carrinho c = getCarrinho(sessaoAtiva);

        if (quantidade <= 0) {
            String alertaHtml = "<div id=\"alerta-area\" hx-swap-oob=\"true\" style=\"background: #fee2e2; color: #b91c1c; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">A quantidade deve ser maior que zero.</div>";
            return HtmlTemplates.fragmentoCarrinhoAtualizado(c) + alertaHtml;
        }

        if (produtoId > 0 && c != null) {
            c.diminuir(produtoId, quantidade);
            EstoqueSseHub.getInstance().notificarCarrinho(op, c);
        }

        return HtmlTemplates.fragmentoCarrinhoAtualizado(c);
    }

    @Operation(summary = "Adicionar item por código de barras", description = "Busca produto por código de barras ou ID e insere no carrinho")
    @ApiResponse(responseCode = 200, description = "Fragmento HTML do carrinho atualizado com mensagem de alerta")
    @PostMethod(value = "/carrinho/codigo", mimeType = "text/html")
    public String adicionarPorCodigo(
            @Parameter(description = "Código de barras ou ID numérico do produto", example = "7891234567890") @RequestParam("codigo") String codigo,
            NanoHTTPD.IHTTPSession session) {

        Session sessaoAtiva = obterSessao(session);
        if (sessaoAtiva == null) {
            return "<div id=\"alerta-area\" hx-swap-oob=\"true\" style=\"background: #fee2e2; color: #b91c1c; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">Sessão expirada. Faça login novamente.</div>";
        }

        if (codigo == null && session != null && session.getParms() != null) {
            codigo = session.getParms().get("codigo");
            if (codigo == null) {
                codigo = session.getParms().get("codigoBarras");
            }
        }

        if (codigo != null) {
            codigo = codigo.trim();
        }

        String op = sessaoAtiva.getUsuario();
        Carrinho c = getCarrinho(sessaoAtiva);

        String alertaHtml = "";
        if (codigo == null || codigo.isEmpty()) {
            alertaHtml = "<div id=\"alerta-area\" hx-swap-oob=\"true\" style=\"background: #fef3c7; color: #92400e; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">Informe um código de barras.</div>";
            return HtmlTemplates.fragmentoCarrinhoAtualizado(c) + alertaHtml;
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
            String codigoEscapado = com.github.matheuscruzsouza.pocketpdv.util.HtmlEscaper.escape(codigo);
            alertaHtml = "<div id=\"alerta-area\" hx-swap-oob=\"true\" style=\"background: #fee2e2; color: #b91c1c; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">Produto não encontrado com código: <b>" + codigoEscapado + "</b></div>";
        } else if (produto.getEstoque() <= 0) {
            String nomeEscapado = com.github.matheuscruzsouza.pocketpdv.util.HtmlEscaper.escape(produto.getNome());
            alertaHtml = "<div id=\"alerta-area\" hx-swap-oob=\"true\" style=\"background: #fee2e2; color: #b91c1c; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">Produto sem estoque: <b>" + nomeEscapado + "</b></div>";
        } else {
            if (c != null) {
                c.adicionar(produto, 1);
                EstoqueSseHub.getInstance().notificarCarrinho(op, c);
                String nomeEscapado = com.github.matheuscruzsouza.pocketpdv.util.HtmlEscaper.escape(produto.getNome());
                alertaHtml = "<div id=\"alerta-area\" hx-swap-oob=\"true\" style=\"background: #d1fae5; color: #065f46; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">Item adicionado: <b>" + nomeEscapado + "</b> (" + HtmlTemplates.formatarDinheiro(produto.getPrecoCentavos()) + ")</div>";
            }
        }

        return HtmlTemplates.fragmentoCarrinhoAtualizado(c) + alertaHtml;
    }

    @Operation(summary = "Remover item do carrinho", description = "Remove completamente um produto do carrinho por ID")
    @ApiResponse(responseCode = 200, description = "Fragmento HTML do carrinho atualizado")
    @DeleteMethod(value = "/carrinho/item/:id", mimeType = "text/html")
    public String removerItem(
            @Parameter(description = "ID do produto a ser removido", example = "1") @PathVariable("id") String idStr,
            NanoHTTPD.IHTTPSession session) {
        Session sessaoAtiva = obterSessao(session);
        if (sessaoAtiva == null) {
            return "";
        }
        String op = sessaoAtiva.getUsuario();
        Carrinho c = getCarrinho(sessaoAtiva);
        if (idStr != null && !idStr.isEmpty() && c != null) {
            try {
                long produtoId = Long.parseLong(idStr);
                c.remover(produtoId);
                EstoqueSseHub.getInstance().notificarCarrinho(op, c);
            } catch (NumberFormatException ignored) {}
        }
        return HtmlTemplates.fragmentoCarrinhoAtualizado(c);
    }

    @Operation(summary = "Limpar carrinho", description = "Esvazia todos os itens do carrinho atual")
    @ApiResponse(responseCode = 200, description = "Fragmento HTML do carrinho vazio")
    @DeleteMethod(value = "/carrinho", mimeType = "text/html")
    public String limparCarrinho(NanoHTTPD.IHTTPSession session) {
        Session sessaoAtiva = obterSessao(session);
        if (sessaoAtiva == null) {
            return "<div id=\"alerta-area\" hx-swap-oob=\"true\" style=\"background: #fee2e2; color: #b91c1c; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">Sessão expirada. Faça login novamente.</div>";
        }
        String op = sessaoAtiva.getUsuario();
        Carrinho c = getCarrinho(sessaoAtiva);
        if (c != null) {
            c.limpar();
            EstoqueSseHub.getInstance().notificarCarrinho(op, c);
        }
        String alertaHtml = "<div id=\"alerta-area\" hx-swap-oob=\"true\" style=\"background: #f3f4f6; color: #4b5563; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">Carrinho esvaziado.</div>";
        return HtmlTemplates.fragmentoCarrinhoAtualizado(c) + alertaHtml;
    }

    @Operation(summary = "Relatório de vendas do dia", description = "Retorna o modal HTML com resumo de vendas do dia e últimas 10 vendas")
    @ApiResponse(responseCode = 200, description = "Fragmento HTML do modal de relatório")
    @GetMethod(value = "/relatorio", mimeType = "text/html")
    public String relatorioVendas(NanoHTTPD.IHTTPSession session) {
        Session sessaoAtiva = obterSessao(session);
        if (sessaoAtiva == null) {
            return "<div style=\"padding: 1.5rem; text-align: center; color: #b91c1c;\">Acesso não autorizado. Por favor, efetue login.</div>";
        }
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

    @Operation(summary = "Finalizar venda (Checkout)", description = "Processa o pagamento do carrinho, baixa o estoque e registra a venda")
    @ApiResponse(responseCode = 200, description = "Fragmento HTML de sucesso ou erro do checkout")
    @PostMethod(value = "/carrinho/checkout", mimeType = "text/html")
    public String finalizarVenda(
            @Parameter(description = "Forma de pagamento (DINHEIRO, CARTAO_CREDITO, CARTAO_DEBITO, PIX)", example = "DINHEIRO") @RequestParam("formaPagamento") String formaPagamento,
            @Parameter(description = "Identificador do operador de caixa", example = "blima") @RequestParam("operador") String operador,
            @Parameter(description = "Valor em dinheiro entregue pelo cliente", example = "50.00") @RequestParam("valorRecebido") String valorRecebidoStr,
            NanoHTTPD.IHTTPSession session) {
        Session sessaoAtiva = obterSessao(session);
        if (sessaoAtiva == null) {
            return HtmlTemplates.fragmentoCheckoutErro("Sessão não autorizada ou expirada. Efetue login novamente.");
        }
        try {
            String usuario = sessaoAtiva.getUsuario();
            Carrinho c = getCarrinho(sessaoAtiva);

            if (getVendaService() == null || c == null) {
                return HtmlTemplates.fragmentoCheckoutErro("Serviço de venda indisponível.");
            }
            if (c.isVazio()) {
                return HtmlTemplates.fragmentoCheckoutErro("O carrinho está vazio.");
            }

            if (formaPagamento == null || formaPagamento.trim().isEmpty()) {
                formaPagamento = "DINHEIRO";
            } else {
                formaPagamento = formaPagamento.trim().toUpperCase();
            }

            if (valorRecebidoStr == null && session != null && session.getParms() != null) {
                valorRecebidoStr = session.getParms().get("valorRecebido");
            }

            long totalCentavos = c.getTotalCentavos();
            long valorRecebidoCentavos = 0L;
            long trocoCentavos = 0L;

            if ("DINHEIRO".equals(formaPagamento)) {
                if (valorRecebidoStr == null || valorRecebidoStr.trim().isEmpty()) {
                    return HtmlTemplates.fragmentoCheckoutErro("Informe o valor recebido em dinheiro.");
                }
                try {
                    valorRecebidoCentavos = com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.parseToCentavos(valorRecebidoStr);
                } catch (IllegalArgumentException e) {
                    return HtmlTemplates.fragmentoCheckoutErro("Valor recebido inválido: " + com.github.matheuscruzsouza.pocketpdv.util.HtmlEscaper.escape(valorRecebidoStr));
                }

                if (valorRecebidoCentavos < totalCentavos) {
                    return HtmlTemplates.fragmentoCheckoutErro("Valor recebido (" + HtmlTemplates.formatarDinheiro(valorRecebidoCentavos) +
                            ") insuficiente para cobrir o total (" + HtmlTemplates.formatarDinheiro(totalCentavos) + ").");
                }
                trocoCentavos = valorRecebidoCentavos - totalCentavos;
            } else {
                // Para PIX, DEBITO, CREDITO o valor recebido é o total exato
                valorRecebidoCentavos = totalCentavos;
                trocoCentavos = 0L;
            }

            long funcionarioId = sessaoAtiva.getUserId();
            Venda venda = getVendaService().finalizarVenda(c, funcionarioId, formaPagamento, valorRecebidoCentavos, trocoCentavos);
            EstoqueSseHub.getInstance().notificarCarrinho(usuario, c);
            return HtmlTemplates.fragmentoCheckoutSucesso(venda, formaPagamento, valorRecebidoCentavos, trocoCentavos);
        } catch (Exception e) {
            return HtmlTemplates.fragmentoCheckoutErro(e.getMessage());
        }
    }

    @Operation(summary = "Página de Display do Cliente", description = "Renderiza a tela voltada para o cliente com suporte a pareamento de caixa")
    @ApiResponse(responseCode = 200, description = "Interface HTML do Display do Cliente")
    @GetMethod(value = "/display", mimeType = "text/html")
    public Object displayCliente(
            @Parameter(description = "Identificador do caixa a monitorar", example = "carlos") @RequestParam("caixa") String caixaParam,
            NanoHTTPD.IHTTPSession session) {
        if (caixaParam == null && session != null && session.getParms() != null) {
            caixaParam = session.getParms().get("caixa");
        }
        List<Funcionario> caixas = (getFuncionarioRepository() != null) ?
                getFuncionarioRepository().listarAtivos() : java.util.Collections.emptyList();
        return HtmlTemplates.paginaDisplayCliente(caixas, caixaParam);
    }

    @Operation(summary = "Stream SSE de eventos de estoque e carrinho", description = "Canal Server-Sent Events para atualização em tempo real de carrinho e catálogo")
    @ApiResponse(responseCode = 200, description = "Stream SSE conectado")
    @GetMethod(value = "/eventos/estoque", mimeType = "text/event-stream")
    public SseEmitter streamEstoque() {
        return EstoqueSseHub.getInstance().registrar();
    }

    @Operation(summary = "Modal de gerenciamento de estoque", description = "Retorna lista de produtos com opções de ajuste e cadastro")
    @ApiResponse(responseCode = 200, description = "Fragmento HTML do modal de estoque")
    @GetMethod(value = "/estoque", mimeType = "text/html")
    public String modalEstoque(NanoHTTPD.IHTTPSession session) {
        Session sessaoAtiva = obterSessao(session);
        if (sessaoAtiva == null) {
            return "<div style=\"padding: 1.5rem; text-align: center; color: #b91c1c;\">Acesso não autorizado. Por favor, efetue login.</div>";
        }
        List<Produto> catalogo = (getEstoqueService() != null) ? getEstoqueService().listarCatalogo() : java.util.Collections.emptyList();
        return HtmlTemplates.fragmentoModalEstoque(catalogo, null);
    }

    @Operation(summary = "Ajustar quantidade em estoque", description = "Atualiza a quantidade disponível de um produto existente")
    @ApiResponse(responseCode = 200, description = "Fragmento HTML atualizado do modal de estoque")
    @PostMethod(value = "/estoque/ajuste", mimeType = "text/html")
    public String ajustarEstoque(
            @Parameter(description = "ID do produto", example = "1") @RequestParam("produtoId") String produtoIdStr,
            @Parameter(description = "Nova quantidade em estoque", example = "20") @RequestParam("novoEstoque") String novoEstoqueStr,
            NanoHTTPD.IHTTPSession session) {

        Session sessaoAtiva = obterSessao(session);
        if (sessaoAtiva == null) {
            return "<div style=\"padding: 1.5rem; text-align: center; color: #b91c1c;\">Acesso não autorizado. Por favor, efetue login.</div>";
        }

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

    @Operation(summary = "Cadastrar novo produto", description = "Adiciona um novo produto ao catálogo do estoque")
    @ApiResponse(responseCode = 200, description = "Fragmento HTML atualizado do modal de estoque")
    @PostMethod(value = "/estoque/novo", mimeType = "text/html")
    public String novoProduto(
            @Parameter(description = "Nome do produto", example = "Coca-Cola 350ml") @RequestParam("nome") String nome,
            @Parameter(description = "Código de barras", example = "7894900010015") @RequestParam("codigoBarras") String codigoBarras,
            @Parameter(description = "Preço unitário em reais", example = "5.50") @RequestParam("preco") String precoStr,
            @Parameter(description = "Estoque inicial", example = "50") @RequestParam("estoque") String estoqueStr,
            NanoHTTPD.IHTTPSession session) {

        Session sessaoAtiva = obterSessao(session);
        if (sessaoAtiva == null) {
            return "<div style=\"padding: 1.5rem; text-align: center; color: #b91c1c;\">Acesso não autorizado. Por favor, efetue login.</div>";
        }

        if (session != null && session.getParms() != null) {
            if (nome == null) nome = session.getParms().get("nome");
            if (codigoBarras == null) codigoBarras = session.getParms().get("codigoBarras");
            if (precoStr == null) precoStr = session.getParms().get("preco");
            if (estoqueStr == null) estoqueStr = session.getParms().get("estoque");
        }

        String msg = null;
        try {
            if (nome != null && codigoBarras != null && precoStr != null && getEstoqueService() != null) {
                long precoCentavos = com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.parseToCentavos(precoStr);
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
