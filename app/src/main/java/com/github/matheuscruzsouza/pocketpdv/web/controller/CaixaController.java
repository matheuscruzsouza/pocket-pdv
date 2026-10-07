package com.github.matheuscruzsouza.pocketpdv.web.controller;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.GetMethod;
import com.github.matheuscruzsouza.nanospring.annotation.PostMethod;
import com.github.matheuscruzsouza.nanospring.annotation.RequestParam;
import com.github.matheuscruzsouza.nanospring.annotation.RestController;
import com.github.matheuscruzsouza.pocketpdv.domain.model.CaixaTurno;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Session;
import com.github.matheuscruzsouza.pocketpdv.domain.service.CaixaService;
import com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService;
import com.github.matheuscruzsouza.pocketpdv.service.SessionService;
import com.github.matheuscruzsouza.pocketpdv.web.interceptor.AuthInterceptor;
import com.github.matheuscruzsouza.pocketpdv.web.view.HtmlTemplates;

import fi.iki.elonen.NanoHTTPD;

@RestController("/caixa")
public class CaixaController {

    @Autowired
    private CaixaService caixaService;

    @Autowired
    private SessionService sessionService;

    public CaixaController() {}

    private Session obterSessao(NanoHTTPD.IHTTPSession session) {
        String sessionId = AuthInterceptor.extrairSessionId(session);
        if (sessionId == null) return null;
        if (sessionService == null && PocketPdvService.getInstance() != null) {
            sessionService = PocketPdvService.getInstance().getSessionService();
        }
        return sessionService != null ? sessionService.obterSessao(sessionId) : null;
    }

    private CaixaService getCaixaService() {
        if (caixaService != null) return caixaService;
        if (PocketPdvService.getInstance() != null && PocketPdvService.getInstance().getServer() != null) {
            caixaService = (CaixaService) PocketPdvService.getInstance().getServer().getBean(com.github.matheuscruzsouza.pocketpdv.domain.service.CaixaServiceImpl.class);
        }
        return caixaService;
    }

    @GetMethod(value = "/modal-abrir", mimeType = "text/html")
    public String modalAbrir(NanoHTTPD.IHTTPSession session) {
        return HtmlTemplates.fragmentoModalAberturaCaixa(null);
    }

    @PostMethod(value = "/abrir", mimeType = "text/html")
    public String abrirCaixa(@RequestParam("trocoInicial") String trocoStr, NanoHTTPD.IHTTPSession session) {
        Session sessao = obterSessao(session);
        if (sessao == null) return "<script>window.location.href='/login';</script>";

        if (trocoStr == null && session.getParms() != null) {
            trocoStr = session.getParms().get("trocoInicial");
        }

        long trocoCentavos = 0;
        if (trocoStr != null && !trocoStr.isEmpty()) {
            try {
                trocoCentavos = com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.parseToCentavos(trocoStr);
            } catch (Exception e) {
                return HtmlTemplates.fragmentoModalAberturaCaixa("Valor inválido.");
            }
        }

        try {
            getCaixaService().abrirCaixa(sessao.getUserId(), trocoCentavos);
            // Refresh para recarregar a página do PDV
            return "<script>window.location.reload();</script>";
        } catch (Exception e) {
            return HtmlTemplates.fragmentoModalAberturaCaixa(e.getMessage());
        }
    }

    @GetMethod(value = "/modal-fechar", mimeType = "text/html")
    public String modalFechar(NanoHTTPD.IHTTPSession session) {
        return HtmlTemplates.fragmentoModalFechamentoCaixa(null);
    }

    @PostMethod(value = "/fechar", mimeType = "text/html")
    public String fecharCaixa(@RequestParam("valorDeclarado") String valorStr, NanoHTTPD.IHTTPSession session) {
        Session sessao = obterSessao(session);
        if (sessao == null) return "<script>window.location.href='/login';</script>";

        if (valorStr == null && session.getParms() != null) {
            valorStr = session.getParms().get("valorDeclarado");
        }

        long valorCentavos = 0;
        if (valorStr != null && !valorStr.isEmpty()) {
            try {
                valorCentavos = com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.parseToCentavos(valorStr);
            } catch (Exception e) {
                return HtmlTemplates.fragmentoModalFechamentoCaixa("Valor inválido.");
            }
        }

        try {
            CaixaTurno aberto = getCaixaService().obterCaixaAberto(sessao.getUserId());
            if (aberto != null) {
                getCaixaService().fecharCaixa(aberto.getId(), valorCentavos);
            }
            return "<script>window.location.reload();</script>";
        } catch (Exception e) {
            return HtmlTemplates.fragmentoModalFechamentoCaixa(e.getMessage());
        }
    }

    @GetMethod(value = "/modal-movimentacao", mimeType = "text/html")
    public String modalMovimentacao(NanoHTTPD.IHTTPSession session) {
        String tipo = session.getParms() != null ? session.getParms().get("tipo") : "SANGRIA";
        if (tipo == null) tipo = "SANGRIA";
        return HtmlTemplates.fragmentoModalMovimentacaoCaixa(tipo, null);
    }

    @PostMethod(value = "/movimentacao", mimeType = "text/html")
    public String registrarMovimentacao(@RequestParam("tipo") String tipo,
                                        @RequestParam("valor") String valorStr,
                                        @RequestParam("descricao") String descricao,
                                        NanoHTTPD.IHTTPSession session) {
        Session sessao = obterSessao(session);
        if (sessao == null) return "<script>window.location.href='/login';</script>";

        if (session.getParms() != null) {
            if (tipo == null) tipo = session.getParms().get("tipo");
            if (valorStr == null) valorStr = session.getParms().get("valor");
            if (descricao == null) descricao = session.getParms().get("descricao");
        }

        if (tipo == null) tipo = "SANGRIA";

        long valorCentavos = 0;
        if (valorStr != null && !valorStr.isEmpty()) {
            try {
                valorCentavos = com.github.matheuscruzsouza.pocketpdv.util.MoneyParser.parseToCentavos(valorStr);
            } catch (Exception e) {
                return HtmlTemplates.fragmentoModalMovimentacaoCaixa(tipo, "Valor inválido.");
            }
        }

        try {
            CaixaTurno aberto = getCaixaService().obterCaixaAberto(sessao.getUserId());
            if (aberto != null) {
                getCaixaService().registrarMovimentacao(aberto.getId(), tipo.toUpperCase(), valorCentavos, descricao);
            }
            return "<div id=\"alerta-caixa\" hx-swap-oob=\"true\" style=\"background: #d1fae5; color: #065f46; padding: 0.75rem; border-radius: 0.375rem; margin-bottom: 1rem;\">Movimentação registrada com sucesso.</div>" + 
                   "<script>document.getElementById('modal-caixa').style.display = 'none';</script>";
        } catch (Exception e) {
            return HtmlTemplates.fragmentoModalMovimentacaoCaixa(tipo, e.getMessage());
        }
    }
}
