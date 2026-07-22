package org.allservice.controllers;

import org.allservice.dto.request.ProductRequestDTO;
import org.allservice.dto.response.ProductResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.allservice.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> cadastrar(@RequestBody ProductRequestDTO dto) {
        ProductResponseDTO response = productService.cadastrarProduto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> buscarPorId(@PathVariable Long id) {
        ProductResponseDTO response = productService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> listarTodos() {
        List<ProductResponseDTO> response = productService.listarTodosProdutos();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> atualizar(@PathVariable Long id, @RequestBody ProductRequestDTO dto) {
        ProductResponseDTO response = productService.atualizarProduto(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        productService.deletarProduto(id);
        return ResponseEntity.noContent().build();
    }
}