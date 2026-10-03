package com.github.matheuscruzsouza.pocketpdv;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.matheuscruzsouza.pocketpdv.security.PasswordHasher;

import org.junit.Test;

public class PasswordHasherTest {

    @Test
    public void testHashPasswordECheckPasswordCorreta() {
        String senha = "minhaSenhaSegura123";
        String hash = PasswordHasher.hashPassword(senha);

        assertNotNull(hash);
        assertTrue(hash.startsWith("PBKDF2$10000$"));

        assertTrue(PasswordHasher.checkPassword(senha, hash));
    }

    @Test
    public void testCheckPasswordIncorretaRetornaFalse() {
        String hash = PasswordHasher.hashPassword("senhaCorreta");

        assertFalse(PasswordHasher.checkPassword("senhaErrada", hash));
        assertFalse(PasswordHasher.checkPassword("senhacorreta", hash));
        assertFalse(PasswordHasher.checkPassword("", hash));
    }

    @Test
    public void testVerificarSenhaLegadaTextoPuro() {
        String senhaLegada = "1234";

        // Senha gravada antigamente como texto puro sem prefixo PBKDF2$
        assertTrue(PasswordHasher.checkPassword("1234", senhaLegada));
        assertFalse(PasswordHasher.checkPassword("errada", senhaLegada));
    }

    @Test
    public void testNeedsRehashIdentificaFormatoLegado() {
        // Texto puro legado deve necessitar de rehash
        assertTrue(PasswordHasher.needsRehash("1234"));
        assertTrue(PasswordHasher.needsRehash("admin"));

        // Hash PBKDF2 atual com 10.000 iterações não necessita de rehash
        String hashAtual = PasswordHasher.hashPassword("qualquerSenha");
        assertFalse(PasswordHasher.needsRehash(hashAtual));

        // Formato corrompido ou com menos iterações deve necessitar de rehash
        assertTrue(PasswordHasher.needsRehash("PBKDF2$1000$salt$hash"));
    }

    @Test
    public void testSaltsDiferentesGeramHashesDiferentesParaMesmaSenha() {
        String senha = "mesmaSenha123";
        String hash1 = PasswordHasher.hashPassword(senha);
        String hash2 = PasswordHasher.hashPassword(senha);

        assertNotEquals(hash1, hash2);
        assertTrue(PasswordHasher.checkPassword(senha, hash1));
        assertTrue(PasswordHasher.checkPassword(senha, hash2));
    }

    @Test
    public void testParametrosNulosOuVazios() {
        assertFalse(PasswordHasher.checkPassword(null, "hash"));
        assertFalse(PasswordHasher.checkPassword("senha", null));
        assertFalse(PasswordHasher.checkPassword("senha", ""));
        assertFalse(PasswordHasher.needsRehash(null));
        assertFalse(PasswordHasher.needsRehash(""));
    }
}
