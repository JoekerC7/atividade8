package com.example.cadastroprodutos.controller;

import com.example.cadastroprodutos.model.Produto;
import com.example.cadastroprodutos.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    /** Disponível em todas as views deste controller (usado nos selects). */
    @ModelAttribute("categorias")
    public List<String> categorias() {
        return List.of("Eletrônico", "Vestuário", "Alimento", "Outros");
    }

    // GET / → redireciona para /cadastrar
    @GetMapping("/")
    public String index() {
        return "redirect:/cadastrar";
    }

    // GET /cadastrar → exibe o formulário vazio
    @GetMapping("/cadastrar")
    public String exibirFormulario(Model model) {
        model.addAttribute("produto", new Produto());
        return "cadastrar";
    }

    // POST /cadastrar → valida, salva e redireciona (padrão PRG)
    @PostMapping("/cadastrar")
    public String cadastrarProduto(@Valid @ModelAttribute("produto") Produto produto,
                                   BindingResult result,
                                   RedirectAttributes redirect) {
        if (result.hasErrors()) {
            // Não salva: reexibe o formulário com as mensagens de erro
            return "cadastrar";
        }
        service.salvar(produto);
        redirect.addFlashAttribute("sucesso", "Produto \"" + produto.getNome() + "\" cadastrado com sucesso!");
        return "redirect:/lista";
    }

    // GET /lista?busca=...&categoria=... → exibe (e filtra) os produtos
    @GetMapping("/lista")
    public String exibirLista(@RequestParam(required = false) String busca,
                              @RequestParam(required = false) String categoria,
                              Model model) {
        model.addAttribute("produtos", service.listar(busca, categoria));
        model.addAttribute("busca", busca);
        model.addAttribute("categoriaSelecionada", categoria);
        return "lista";
    }

    // POST /remover/{id} → exclui o produto
    @PostMapping("/remover/{id}")
    public String remover(@PathVariable Long id, RedirectAttributes redirect) {
        service.remover(id);
        redirect.addFlashAttribute("sucesso", "Produto removido com sucesso.");
        return "redirect:/lista";
    }
}
