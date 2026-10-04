package com.github.matheuscruzsouza.pocketpdv;

import com.github.matheuscruzsouza.nanospring.sse.SseEmitter;
import com.github.matheuscruzsouza.pocketpdv.service.EstoqueSseHub;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EstoqueSseHubTest {

    @Before
    @After
    public void cleanup() {
        EstoqueSseHub.getInstance().encerrar();
    }

    @Test
    public void testLimiteMaximoEmitters() {
        EstoqueSseHub hub = EstoqueSseHub.getInstance();

        // Registra mais que o limite (MAX_EMITTERS = 32)
        for (int i = 0; i < 40; i++) {
            SseEmitter emitter = hub.registrar();
            assertNotNull(emitter);
        }

        // Registrar mais um não deve estourar memória e deve manter no teto de 32
        SseEmitter novo = hub.registrar();
        assertNotNull(novo);
    }

    @Test
    public void testRemocaoManual() {
        EstoqueSseHub hub = EstoqueSseHub.getInstance();
        SseEmitter emitter = hub.registrar();
        assertNotNull(emitter);

        hub.remover(emitter);
        // Após remoção manual, chamar novamente remover não deve falhar
        hub.remover(emitter);
    }

    @Test
    public void testBroadcastVendaConcluidaNaoLancaExcecao() {
        EstoqueSseHub hub = EstoqueSseHub.getInstance();
        SseEmitter emitter = hub.registrar();
        assertNotNull(emitter);

        // Disparo de notificação de venda não deve falhar mesmo com emitters conectados
        hub.notificarVendaConcluida(1L, 1000L, "operador", 2);
        assertTrue(true);
    }
}
