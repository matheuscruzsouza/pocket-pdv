package com.github.matheuscruzsouza.pocketpdv.web.controller;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.GetMethod;
import com.github.matheuscruzsouza.nanospring.annotation.PostMethod;
import com.github.matheuscruzsouza.nanospring.annotation.RequestParam;
import com.github.matheuscruzsouza.nanospring.annotation.RestController;
import com.github.matheuscruzsouza.nanospring.openapi.annotation.ApiResponse;
import com.github.matheuscruzsouza.nanospring.openapi.annotation.Operation;
import com.github.matheuscruzsouza.nanospring.openapi.annotation.Parameter;
import com.github.matheuscruzsouza.nanospring.openapi.annotation.Tag;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Funcionario;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Session;
import com.github.matheuscruzsouza.pocketpdv.persistence.FuncionarioRepository;
import com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService;
import com.github.matheuscruzsouza.pocketpdv.service.SessionService;
import com.github.matheuscruzsouza.pocketpdv.web.view.HtmlTemplates;

import fi.iki.elonen.NanoHTTPD;

@Tag(name = "Autenticação", description = "Endpoints de login e controle de acesso dos operadores")
@RestController("/login")
public class AuthController {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private SessionService sessionService;

    public AuthController() {
    }

    public AuthController(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    public AuthController(FuncionarioRepository funcionarioRepository, SessionService sessionService) {
        this.funcionarioRepository = funcionarioRepository;
        this.sessionService = sessionService;
    }

    private FuncionarioRepository getFuncionarioRepository() {
        if (funcionarioRepository != null) return funcionarioRepository;
        if (PocketPdvService.getInstance() != null && PocketPdvService.getInstance().getServer() != null) {
            funcionarioRepository = (FuncionarioRepository) PocketPdvService.getInstance().getServer().getBean(FuncionarioRepository.class);
        }
        if (funcionarioRepository == null && PocketPdvService.getInstance() != null) {
            funcionarioRepository = new FuncionarioRepository(PocketPdvService.getInstance().getDbHelper());
        }
        return funcionarioRepository;
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

    @Operation(summary = "Exibir formulário de login", description = "Renderiza página HTML com campos de usuário e senha")
    @ApiResponse(responseCode = 200, description = "Página de login renderizada")
    @GetMethod(value = "", mimeType = "text/html")
    public Object loginPage() {
        return HtmlTemplates.paginaLogin(null, null);
    }

    @Operation(summary = "Autenticar operador", description = "Valida usuário e senha do colaborador e cria sessão server-side com cookie HttpOnly")
    @ApiResponse(responseCode = 302, description = "Autenticado com sucesso; redireciona para o PDV com cookie de sessão")
    @ApiResponse(responseCode = 401, description = "Credenciais inválidas ou colaborador inativo")
    @PostMethod(value = "", mimeType = "text/html")
    public Object autenticar(
            @Parameter(description = "Nome de usuário do operador", example = "operador") @RequestParam("usuario") String usuario,
            @Parameter(description = "Senha do operador", example = "123456") @RequestParam("senha") String senha) {

        if (usuario == null || usuario.trim().isEmpty() || senha == null) {
            return HtmlTemplates.paginaLogin("Informe usuário e senha.", null);
        }

        FuncionarioRepository repo = getFuncionarioRepository();
        if (repo == null) {
            return HtmlTemplates.paginaLogin("Sistema indisponível temporariamente.", null);
        }

        Funcionario f = repo.buscarPorUsuario(usuario.trim());
        if (f == null || !f.isAtivo()) {
            return HtmlTemplates.paginaLogin("Usuário não encontrado ou inativo.", null);
        }

        if (!f.isSenhaDefinida()) {
            return HtmlTemplates.paginaLogin("Primeiro acesso pendente! Por favor, utilize o botão 'Primeiro Acesso' com o código fornecido.", null);
        }

        if (com.github.matheuscruzsouza.pocketpdv.security.PasswordHasher.checkPassword(senha.trim(), f.getSenha())) {
            // Migração transparente: se a senha for legada, atualiza para PBKDF2 com salt no banco
            if (com.github.matheuscruzsouza.pocketpdv.security.PasswordHasher.needsRehash(f.getSenha())) {
                String novoHash = com.github.matheuscruzsouza.pocketpdv.security.PasswordHasher.hashPassword(senha.trim());
                repo.atualizarSenha(f.getId(), novoHash);
                f.setSenha(novoHash);
            }

            Session session = getSessionService().criarSessao(f);
            String token = session != null ? session.getId() : "";

            NanoHTTPD.Response response = NanoHTTPD.newFixedLengthResponse(
                    NanoHTTPD.Response.Status.REDIRECT_SEE_OTHER,
                    "text/html",
                    "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">" +
                    "<meta http-equiv=\"refresh\" content=\"0;url=/pdv\">" +
                    "<script>window.location.href='/pdv';</script>" +
                    "</head>" +
                    "<body><p>Acessando PDV como " + (f.getNome() != null ? f.getNome() : f.getUsuario()) + "...</p></body></html>"
            );
            response.addHeader("Location", "/pdv");
            response.addHeader("Set-Cookie", SessionService.COOKIE_NAME + "=" + token + "; Path=/; HttpOnly; SameSite=Strict; Max-Age=86400");
            return response;
        }

        return HtmlTemplates.paginaLogin("Senha incorreta. Tente novamente.", null);
    }
}
