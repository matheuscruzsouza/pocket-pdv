package com.github.matheuscruzsouza.pocketpdv.web.interceptor;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.Interceptor;
import com.github.matheuscruzsouza.nanospring.annotation.Order;
import com.github.matheuscruzsouza.nanospring.handler.HandlerInterceptor;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Session;
import com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService;
import com.github.matheuscruzsouza.pocketpdv.service.SessionService;

import fi.iki.elonen.NanoHTTPD;

@Interceptor
@Order(1)
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private SessionService sessionService;

    public AuthInterceptor() {
    }

    public AuthInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    private SessionService getSessionService() {
        if (sessionService != null) return sessionService;
        if (PocketPdvService.getInstance() != null) {
            sessionService = PocketPdvService.getInstance().getSessionService();
        }
        return sessionService;
    }

    public static String extrairSessionId(NanoHTTPD.IHTTPSession session) {
        if (session == null) return null;
        if (session.getCookies() != null) {
            String token = session.getCookies().read(SessionService.COOKIE_NAME);
            if (token != null && !token.trim().isEmpty()) {
                return token.trim();
            }
        }
        if (session.getHeaders() != null) {
            String cookieHeader = session.getHeaders().get("cookie");
            if (cookieHeader == null) {
                cookieHeader = session.getHeaders().get("Cookie");
            }
            if (cookieHeader != null && cookieHeader.contains(SessionService.COOKIE_NAME + "=")) {
                for (String part : cookieHeader.split(";")) {
                    part = part.trim();
                    if (part.startsWith(SessionService.COOKIE_NAME + "=")) {
                        return part.substring((SessionService.COOKIE_NAME + "=").length()).trim();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean preHandle(NanoHTTPD.IHTTPSession session, String uri) {
        if (uri == null) return true;

        String path = uri.trim();
        // Remove barras duplicadas ou trailing slash para normalização
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // Rotas públicas liberadas
        if (isRotaPublica(path)) {
            return true;
        }

        // Proteção das rotas operacionais do PDV
        if (path.startsWith("/pdv")) {
            String sessionId = extrairSessionId(session);
            SessionService service = getSessionService();
            Session sessaoAtiva = (service != null && sessionId != null) ? service.obterSessao(sessionId) : null;

            // Se for requisição GET na raiz do /pdv, permite para o controller emitir redirecionamento HTML amigável
            if (session != null && NanoHTTPD.Method.GET.equals(session.getMethod()) && (path.equals("/pdv") || path.equals("/pdv/"))) {
                return true;
            }

            // Qualquer endpoint de mutação (POST/DELETE/PUT) ou sub-recurso exige sessão estritamente válida
            return sessaoAtiva != null;
        }

        return true;
    }

    private boolean isRotaPublica(String path) {
        return path.equals("") ||
                path.equals("/") ||
                path.equals("/login") ||
                path.startsWith("/login/") ||
                path.equals("/logout") ||
                path.equals("/primeiro-acesso") ||
                path.startsWith("/primeiro-acesso/") ||
                path.startsWith("/assets") ||
                path.equals("/pdv/display") ||
                path.equals("/pdv/eventos/estoque") ||
                path.startsWith("/actuator") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/swagger-ui");
    }
}
