package com.github.matheuscruzsouza.pocketpdv.web.controller;

import com.github.matheuscruzsouza.nanospring.annotation.Autowired;
import com.github.matheuscruzsouza.nanospring.annotation.GetMethod;
import com.github.matheuscruzsouza.nanospring.annotation.PostMethod;
import com.github.matheuscruzsouza.nanospring.annotation.RequestParam;
import com.github.matheuscruzsouza.nanospring.annotation.RestController;
import com.github.matheuscruzsouza.pocketpdv.domain.model.Funcionario;
import com.github.matheuscruzsouza.pocketpdv.persistence.FuncionarioRepository;
import com.github.matheuscruzsouza.pocketpdv.service.PocketPdvService;
import com.github.matheuscruzsouza.pocketpdv.web.view.HtmlTemplates;

@RestController("/login")
public class AuthController {

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    public AuthController() {
    }

    public AuthController(FuncionarioRepository funcionarioRepository) {
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
    public String loginPage() {
        return HtmlTemplates.paginaLogin(null, null);
    }

    @PostMethod(value = "", mimeType = "text/html")
    public String autenticar(
            @RequestParam("usuario") String usuario,
            @RequestParam("senha") String senha) {

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

        if (f.getSenha() != null && f.getSenha().equals(senha.trim())) {
            return "<!DOCTYPE html><html><head><meta http-equiv=\"refresh\" content=\"0;url=/pdv\"></head>" +
                   "<body><script>window.location.href='/pdv';</script><p>Acessando PDV...</p></body></html>";
        }

        return HtmlTemplates.paginaLogin("Senha incorreta. Tente novamente.", null);
    }
}
