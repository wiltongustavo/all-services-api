package org.allservice.service;

import org.allservice.dto.request.ProductRequestDTO;
import org.allservice.dto.response.ProductResponseDTO;
import org.allservice.entities.ProductEntity;
import org.allservice.exceptions.BusinessException;
import org.allservice.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.allservice.repositories.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 1. CADASTRAR PRODUTO (HTTP 201)
    @Transactional
    public ProductResponseDTO cadastrarProduto(ProductRequestDTO productDTO) {

        if (productDTO.name() == null || productDTO.name().trim().isEmpty()) {
            throw new BusinessException("O nome do produto é obrigatório!");
        }

        if (productRepository.existsByName(productDTO.name())) {
            throw new BusinessException("Já existe um produto cadastrado com este nome: " + productDTO.name());
        }

        if (productDTO.value() == null || productDTO.value().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor do produto deve ser maior que zero!");
        }

        if (productDTO.stock() == null || productDTO.stock() < 0) {
            throw new BusinessException("O estoque do produto não pode ser nulo ou negativo!");
        }
        if (productDTO.stock() == null) {
            throw new BusinessException("O estoque do produto é obrigatório!");
        }

        ProductEntity product = new ProductEntity();
        product.setName(productDTO.name());
        product.setDescription(productDTO.description());
        product.setValue(productDTO.value());
        product.setStock(productDTO.stock());

        ProductEntity productSalvo = productRepository.save(product);

        return new ProductResponseDTO(
                productSalvo.getId(),
                productSalvo.getName(),
                productSalvo.getDescription(),
                productSalvo.getValue(),
                productSalvo.getCreatedAt(),
                productSalvo.getStock()
        );
    }

    // 2. BUSCAR POR ID (HTTP 200 / 404)
    @Transactional(readOnly = true)
    public ProductResponseDTO buscarPorId(Long id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com o ID: " + id));

        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getValue(),
                product.getCreatedAt(),
                product.getStock()
        );
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> listarTodosProdutos(Pageable pageable) {
        Page<ProductEntity> produtosPage = productRepository.findAllPaged(pageable);

        return produtosPage.map(product -> new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getValue(),
                product.getCreatedAt(),
                product.getStock()
        ));
    }

    // 4. ATUALIZAR PRODUTO (HTTP 200 / 404 / 400)
    @Transactional
    public ProductResponseDTO atualizarProduto(Long id, ProductRequestDTO productDTO) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com o ID: " + id));

        if (productDTO.name() == null || productDTO.name().trim().isEmpty()) {
            throw new BusinessException("O nome do produto é obrigatório!");
        }

        if (productDTO.value() == null || productDTO.value().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor do produto deve ser maior que zero!");
        }

        if (productDTO.stock() == null || productDTO.stock() < 0) {
            throw new BusinessException("O estoque do produto não pode ser nulo ou negativo!");
        }

        product.setName(productDTO.name());
        product.setDescription(productDTO.description());
        product.setValue(productDTO.value());
        product.setStock(productDTO.stock());

        ProductEntity productAtualizado = productRepository.save(product);

        return new ProductResponseDTO(
                productAtualizado.getId(),
                productAtualizado.getName(),
                productAtualizado.getDescription(),
                productAtualizado.getValue(),
                productAtualizado.getCreatedAt(),
                productAtualizado.getStock()
        );
    }

    // 5. DELETAR PRODUTO (HTTP 204 / 404)
    @Transactional
    public void deletarProduto(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Produto não encontrado com o ID: " + id);
        }

        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> searchProducts(String name, Pageable pageable) {
        Page<ProductEntity> productPage = productRepository.searchByName(name != null ? name : "", pageable);

        return productPage.map(product -> new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getValue(),
                product.getCreatedAt(),
                product.getStock()
        ));
    }
}