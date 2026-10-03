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
import com.github.matheuscruzsouza.pocketpdv.persistence.FuncionarioRepository;
import com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService;
import com.github.matheuscruzsouza.pocketpdv.web.view.HtmlTemplates;

@Tag(name = "Primeiro Acesso & Recuperação", description = "Definição e recuperação de senha de colaboradores com código")
@RestController("/primeiro-acesso")
public class PrimeiroAcessoController {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    public PrimeiroAcessoController() {
    }

    public PrimeiroAcessoController(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
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

    @Operation(summary = "Exibir formulário de primeiro acesso / recuperação", description = "Renderiza página HTML para validação do código e cadastro de senha")
    @ApiResponse(responseCode = 200, description = "Página de primeiro acesso renderizada")
    @GetMethod(value = "", mimeType = "text/html")
    public Object primeiroAcessoPage() {
        return HtmlTemplates.paginaPrimeiroAcesso(null, null);
    }

    @Operation(summary = "Confirmar código e definir nova senha", description = "Valida o código de 6 dígitos gerado e grava a nova senha do colaborador")
    @ApiResponse(responseCode = 200, description = "Senha cadastrada com sucesso; retorna formulário de login")
    @ApiResponse(responseCode = 400, description = "Código inválido, dados incompletos ou senhas divergentes")
    @PostMethod(value = "", mimeType = "text/html")
    public Object confirmarPrimeiroAcesso(
            @Parameter(description = "Nome de usuário do colaborador", example = "blima") @RequestParam("usuario") String usuario,
            @Parameter(description = "Código de 6 dígitos alfanuméricos gerado pelo admin", example = "K9P2X4") @RequestParam("codigo") String codigo,
            @Parameter(description = "Nova senha do colaborador", example = "senhaForte123") @RequestParam("senha") String senha,
            @Parameter(description = "Confirmação da nova senha", example = "senhaForte123") @RequestParam("confirmaSenha") String confirmaSenha) {

        if (usuario == null || usuario.trim().isEmpty() ||
            codigo == null || codigo.trim().isEmpty() ||
            senha == null || senha.trim().isEmpty()) {
            return HtmlTemplates.paginaPrimeiroAcesso("Todos os campos são obrigatórios.", null);
        }

        if (confirmaSenha != null && !senha.equals(confirmaSenha)) {
            return HtmlTemplates.paginaPrimeiroAcesso("A senha e a confirmação de senha não coincidem.", null);
        }

        FuncionarioRepository repo = getFuncionarioRepository();
        if (repo == null) {
            return HtmlTemplates.paginaPrimeiroAcesso("Sistema indisponível temporariamente.", null);
        }

        boolean sucesso = repo.definirSenhaComCodigo(usuario.trim(), codigo.trim(), senha.trim());
        if (!sucesso) {
            return HtmlTemplates.paginaPrimeiroAcesso("Código de confirmação de 6 dígitos inválido ou usuário inexistente.", null);
        }

        return HtmlTemplates.paginaLogin(null, "Senha cadastrada com sucesso! Faça seu primeiro login com a nova senha.");
    }
}
