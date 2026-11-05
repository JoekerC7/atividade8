package com.example.cadastroprodutos.service;

import com.example.cadastroprodutos.model.Produto;
import com.example.cadastroprodutos.repository.ProdutoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Camada de serviço: concentra a regra de negócio e o acesso aos dados,
 * deixando o Controller responsável apenas pelo fluxo web.
 */
@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public Produto salvar(Produto produto) {
        return repository.save(produto);
    }

    public void remover(Long id) {
        repository.deleteById(id);
    }

    /** Lista todos os produtos, com filtro opcional por nome e/ou categoria. */
    public List<Produto> listar(String busca, String categoria) {
        boolean temBusca = busca != null && !busca.isBlank();
        boolean temCategoria = categoria != null && !categoria.isBlank();

        if (temBusca && temCategoria) {
            return repository.findByNomeContainingIgnoreCaseAndCategoriaOrderByIdAsc(busca.trim(), categoria);
        }
        if (temBusca) {
            return repository.findByNomeContainingIgnoreCaseOrderByIdAsc(busca.trim());
        }
        if (temCategoria) {
            return repository.findByCategoriaOrderByIdAsc(categoria);
        }
        return repository.findAll(Sort.by("id"));
    }
}
