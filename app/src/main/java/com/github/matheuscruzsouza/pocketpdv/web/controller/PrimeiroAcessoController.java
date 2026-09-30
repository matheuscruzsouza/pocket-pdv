package com.github.matheuscruzsouza.pocketpdv.web.controller;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.GetMethod;
import com.github.matheuscruzsouza.nanospring.annotation.PostMethod;
import com.github.matheuscruzsouza.nanospring.annotation.RequestParam;
import com.github.matheuscruzsouza.nanospring.annotation.RestController;
import com.github.matheuscruzsouza.pocketpdv.persistence.FuncionarioRepository;
import com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService;
import com.github.matheuscruzsouza.pocketpdv.web.view.HtmlTemplates;

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

    @GetMethod(value = "", mimeType = "text/html")
    public String primeiroAcessoPage() {
        return HtmlTemplates.paginaPrimeiroAcesso(null, null);
    }

    @PostMethod(value = "", mimeType = "text/html")
    public String confirmarPrimeiroAcesso(
            @RequestParam("usuario") String usuario,
            @RequestParam("codigo") String codigo,
            @RequestParam("senha") String senha,
            @RequestParam("confirmaSenha") String confirmaSenha) {

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
