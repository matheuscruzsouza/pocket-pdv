package com.github.matheuscruzsouza.pocketpdv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.github.matheuscruzsouza.pocketpdv.domain.model.Funcionario;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Session;
import com.github.matheuscruzsouza.pocketpdv.service.SessionService;
import com.github.matheuscruzsouza.pocketpdv.web.interceptor.AuthInterceptor;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class AuthInterceptorTest {

    private SessionService sessionService;
    private AuthInterceptor interceptor;
    private Funcionario operador;

    @Before
    public void setUp() {
        sessionService = new SessionService();
        interceptor = new AuthInterceptor(sessionService);
        operador = new Funcionario(1L, "Operador Caixa", "Operador", "operador1", true);
    }

    private NanoHTTPD.IHTTPSession criarSessaoHttp(NanoHTTPD.Method method, String cookieHeader) {
        Map<String, String> headers = new HashMap<>();
        if (cookieHeader != null) {
            headers.put("cookie", cookieHeader);
        }

        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
                if ("getMethod".equals(m.getName())) {
                    return method != null ? method : NanoHTTPD.Method.GET;
                }
                if ("getHeaders".equals(m.getName())) {
                    return headers;
                }
                if ("getCookies".equals(m.getName())) {
                    return null;
                }
                if ("getParms".equals(m.getName())) {
                    return Collections.emptyMap();
                }
                return null;
            }
        };

        return (NanoHTTPD.IHTTPSession) Proxy.newProxyInstance(
                NanoHTTPD.IHTTPSession.class.getClassLoader(),
                new Class<?>[]{NanoHTTPD.IHTTPSession.class},
                handler
        );
    }

    @Test
    public void testRotasPublicasPermitidasSemSessao() {
        NanoHTTPD.IHTTPSession anonimo = criarSessaoHttp(NanoHTTPD.Method.GET, null);

        assertTrue(interceptor.preHandle(anonimo, "/"));
        assertTrue(interceptor.preHandle(anonimo, "/login"));
        assertTrue(interceptor.preHandle(anonimo, "/logout"));
        assertTrue(interceptor.preHandle(anonimo, "/primeiro-acesso"));
        assertTrue(interceptor.preHandle(anonimo, "/assets/style.css"));
        assertTrue(interceptor.preHandle(anonimo, "/assets/htmx.min.js"));
        assertTrue(interceptor.preHandle(anonimo, "/pdv/display"));
        assertTrue(interceptor.preHandle(anonimo, "/pdv/eventos/estoque"));
        assertTrue(interceptor.preHandle(anonimo, "/actuator/health"));
        assertTrue(interceptor.preHandle(anonimo, "/api-docs"));
    }

    @Test
    public void testMutacoesPdvBloqueadasSemSessao() {
        NanoHTTPD.IHTTPSession anonimoPost = criarSessaoHttp(NanoHTTPD.Method.POST, null);
        NanoHTTPD.IHTTPSession anonimoDelete = criarSessaoHttp(NanoHTTPD.Method.DELETE, null);
        NanoHTTPD.IHTTPSession anonimoGet = criarSessaoHttp(NanoHTTPD.Method.GET, null);

        assertFalse(interceptor.preHandle(anonimoPost, "/pdv/carrinho/checkout"));
        assertFalse(interceptor.preHandle(anonimoPost, "/pdv/estoque/ajuste"));
        assertFalse(interceptor.preHandle(anonimoPost, "/pdv/estoque/novo"));
        assertFalse(interceptor.preHandle(anonimoPost, "/pdv/carrinho/item"));
        assertFalse(interceptor.preHandle(anonimoDelete, "/pdv/carrinho"));
        assertFalse(interceptor.preHandle(anonimoGet, "/pdv/relatorio"));
        assertFalse(interceptor.preHandle(anonimoGet, "/pdv/estoque"));
    }

    @Test
    public void testMutacoesPdvPermitidasComSessaoValida() {
        Session sessao = sessionService.criarSessao(operador);
        String cookie = SessionService.COOKIE_NAME + "=" + sessao.getId();
        NanoHTTPD.IHTTPSession autenticadoPost = criarSessaoHttp(NanoHTTPD.Method.POST, cookie);

        assertTrue(interceptor.preHandle(autenticadoPost, "/pdv/carrinho/checkout"));
        assertTrue(interceptor.preHandle(autenticadoPost, "/pdv/estoque/ajuste"));
        assertTrue(interceptor.preHandle(autenticadoPost, "/pdv/estoque/novo"));
        assertTrue(interceptor.preHandle(autenticadoPost, "/pdv/carrinho/item"));
    }

    @Test
    public void testSessaoComTokenInvalidoBloqueada() {
        String cookieInvalido = SessionService.COOKIE_NAME + "=token-falso-999";
        NanoHTTPD.IHTTPSession sessaoInvalida = criarSessaoHttp(NanoHTTPD.Method.POST, cookieInvalido);

        assertFalse(interceptor.preHandle(sessaoInvalida, "/pdv/carrinho/checkout"));
        assertFalse(interceptor.preHandle(sessaoInvalida, "/pdv/estoque/ajuste"));
    }

    @Test
    public void testExtrairSessionId() {
        assertNull(AuthInterceptor.extrairSessionId(null));

        NanoHTTPD.IHTTPSession sSemCookie = criarSessaoHttp(NanoHTTPD.Method.GET, null);
        assertNull(AuthInterceptor.extrairSessionId(sSemCookie));

        NanoHTTPD.IHTTPSession sComCookie = criarSessaoHttp(NanoHTTPD.Method.GET, "tema=escuro; " + SessionService.COOKIE_NAME + "=abc-123; outro=456");
        assertEquals("abc-123", AuthInterceptor.extrairSessionId(sComCookie));
    }
}
