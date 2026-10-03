package com.github.matheuscruzsouza.pocketpdv.web.controller;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.GetMethod;
import com.github.matheuscruzsouza.nanospring.annotation.RestController;
import com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService;
import com.github.matheuscruzsouza.pocketpdv.service.SessionService;
import com.github.matheuscruzsouza.pocketpdv.web.interceptor.AuthInterceptor;

import fi.iki.elonen.NanoHTTPD;

@RestController("")
public class RootController {

    @Autowired
    private SessionService sessionService;

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

    @GetMethod(value = "/", mimeType = "text/html")
    public String index() {
        return "<!DOCTYPE html><html><head><meta http-equiv=\"refresh\" content=\"0;url=/login\"></head>" +
               "<body><script>window.location.href='/login';</script></body></html>";
    }

    @GetMethod(value = "/logout", mimeType = "text/html")
    public Object logout(NanoHTTPD.IHTTPSession session) {
        String sessionId = AuthInterceptor.extrairSessionId(session);
        if (sessionId != null) {
            getSessionService().encerrarSessao(sessionId);
        }
        NanoHTTPD.Response response = NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.REDIRECT,
                "text/html",
                "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">" +
                "<meta http-equiv=\"refresh\" content=\"0;url=/login\">" +
                "<script>window.location.href='/login';</script>" +
                "</head><body><p>Sessão encerrada. Redirecionando...</p></body></html>"
        );
        response.addHeader("Location", "/login");
        response.addHeader("Set-Cookie", SessionService.COOKIE_NAME + "=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0");
        return response;
    }
}
