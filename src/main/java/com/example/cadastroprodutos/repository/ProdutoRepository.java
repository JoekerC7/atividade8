package com.example.cadastroprodutos.repository;

import com.example.cadastroprodutos.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByNomeContainingIgnoreCaseOrderByIdAsc(String nome);

    List<Produto> findByCategoriaOrderByIdAsc(String categoria);

    List<Produto> findByNomeContainingIgnoreCaseAndCategoriaOrderByIdAsc(String nome, String categoria);
}
