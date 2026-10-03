package com.github.matheuscruzsouza.pocketpdv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.github.matheuscruzsouza.pocketpdv.domain.model.Funcionario;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Session;
import com.github.matheuscruzsouza.pocketpdv.service.SessionService;

import org.junit.Before;
import org.junit.Test;

public class SessionServiceTest {

    private SessionService sessionService;
    private Funcionario operador;

    @Before
    public void setUp() {
        sessionService = new SessionService();
        operador = new Funcionario(10L, "Carlos Silva", "Operador de Caixa", "carlossilva", true);
    }

    @Test
    public void testCriarEObterSessaoValida() {
        Session session = sessionService.criarSessao(operador);

        assertNotNull(session);
        assertNotNull(session.getId());
        assertEquals(10L, session.getUserId());
        assertEquals("carlossilva", session.getUsuario());
        assertEquals("Carlos Silva", session.getNome());
        assertEquals("Operador de Caixa", session.getCargo());
        assertFalse(session.isExpired());

        Session recuperada = sessionService.obterSessao(session.getId());
        assertNotNull(recuperada);
        assertEquals(session.getId(), recuperada.getId());
        assertEquals("carlossilva", recuperada.getUsuario());
    }

    @Test
    public void testSessaoComFuncionarioNuloRetornaNull() {
        Session session = sessionService.criarSessao(null);
        assertNull(session);
    }

    @Test
    public void testSessaoInexistenteRetornaNull() {
        assertNull(sessionService.obterSessao(null));
        assertNull(sessionService.obterSessao(""));
        assertNull(sessionService.obterSessao("token-inexistente-123"));
    }

    @Test
    public void testEncerrarSessaoInvalidaImediatamente() {
        Session session = sessionService.criarSessao(operador);
        assertNotNull(sessionService.obterSessao(session.getId()));

        sessionService.encerrarSessao(session.getId());
        assertNull(sessionService.obterSessao(session.getId()));
    }

    @Test
    public void testSessaoExpiradaRetornaNullEEspurgada() {
        Session session = sessionService.criarSessao(operador);
        assertNotNull(session);

        // Força expiração manipulando timestamp
        session.setExpiresAt(System.currentTimeMillis() - 1000L);
        assertTrue(session.isExpired());

        // Ao consultar, deve retornar null e expurgar do map
        assertNull(sessionService.obterSessao(session.getId()));
        assertEquals(0, sessionService.getQuantidadeSessoesAtivas());
    }

    @Test
    public void testMultiplasSessoesConcorrentes() {
        Funcionario gerente = new Funcionario(20L, "Mariana Ramos", "Gerente", "mramos", true);

        Session s1 = sessionService.criarSessao(operador);
        Session s2 = sessionService.criarSessao(gerente);

        assertNotNull(s1);
        assertNotNull(s2);
        assertFalse(s1.getId().equals(s2.getId()));

        assertEquals(2, sessionService.getQuantidadeSessoesAtivas());

        sessionService.encerrarSessao(s1.getId());
        assertEquals(1, sessionService.getQuantidadeSessoesAtivas());
        assertNull(sessionService.obterSessao(s1.getId()));
        assertNotNull(sessionService.obterSessao(s2.getId()));
    }
}
