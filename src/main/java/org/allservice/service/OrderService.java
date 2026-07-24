package org.allservice.service;

import org.allservice.dto.request.OrderRequestDTO;
import org.allservice.dto.response.OrderItemResponseDTO;
import org.allservice.dto.response.OrderResponseDTO;
import org.allservice.entities.ClientEntity;
import org.allservice.entities.OrderEntity;
import org.allservice.entities.OrderItemEntity;
import org.allservice.entities.ProductEntity;
import org.allservice.enums.OrderStatus;
import org.allservice.exceptions.BusinessException;
import org.allservice.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.allservice.repositories.ClientRepository;
import org.allservice.repositories.OrderRepository;
import org.allservice.repositories.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        ClientRepository clientRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponseDTO criarOrdemServico(OrderRequestDTO orderDTO) {
        // 1. Busca do cliente pelo clientId do DTO
        ClientEntity client = clientRepository.findById(orderDTO.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + orderDTO.clientId()));

        OrderEntity pedido = new OrderEntity();
        pedido.setClient(client);
        pedido.setName(orderDTO.name());
        pedido.setDescription(orderDTO.description());
        pedido.setStatus(orderDTO.status() != null ? orderDTO.status() : OrderStatus.PENDING);

        // 2. Mapeamento dos itens, validação e baixa de estoque
        List<OrderItemEntity> itensEntity = orderDTO.items().stream()
                .map(itemDto -> {
                    ProductEntity produto = productRepository.findById(itemDto.productId())
                            .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com o ID: " + itemDto.productId()));

                    // Validação de estoque
                    if (produto.getStock() < itemDto.quantity()) {
                        throw new BusinessException("Estoque insuficiente para o produto: " + produto.getName()
                                + ". Disponível: " + produto.getStock() + ", Solicitado: " + itemDto.quantity());
                    }

                    // Abatimento no estoque e atualização no banco
                    produto.setStock(produto.getStock() - itemDto.quantity());
                    productRepository.save(produto);

                    return new OrderItemEntity(
                            itemDto.quantity(),
                            produto.getValue(),
                            pedido,
                            produto
                    );
                })
                .toList();

        pedido.getItems().addAll(itensEntity);

        // 3. Cálculo do valor total
        BigDecimal valorTotalPedido = itensEntity.stream()
                .map(item -> item.getSoldPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        pedido.setValue(valorTotalPedido);
        OrderEntity pedidoSalvo = orderRepository.save(pedido);

        // 4. Mapeamento para DTO de resposta
        List<OrderItemResponseDTO> itensResponse = pedidoSalvo.getItems().stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getId(),
                        item.getQuantity(),
                        item.getSoldPrice(),
                        item.getProduct().getId(),
                        item.getProduct().getName()
                ))
                .toList();

        return new OrderResponseDTO(
                pedidoSalvo.getId(),
                pedidoSalvo.getName(),
                pedidoSalvo.getDescription(),
                pedidoSalvo.getValue(),
                pedidoSalvo.getStatus(),
                pedidoSalvo.getCreatedAt(),
                pedidoSalvo.getClient() != null ? pedidoSalvo.getClient().getName() : null,
                itensResponse
        );
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO buscarPorId(Long id) {
        OrderEntity pedido = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de serviço não encontrada com o ID: " + id));

        List<OrderItemResponseDTO> itensResponse = pedido.getItems().stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getId(),
                        item.getQuantity(),
                        item.getSoldPrice(),
                        item.getProduct().getId(),
                        item.getProduct().getName()
                ))
                .toList();

        return new OrderResponseDTO(
                pedido.getId(),
                pedido.getName(),
                pedido.getDescription(),
                pedido.getValue(),
                pedido.getStatus(),
                pedido.getCreatedAt(),
                pedido.getClient() != null ? pedido.getClient().getName() : null,
                itensResponse
        );
    }

    @Transactional
    public OrderResponseDTO atualizarStatus(Long id, OrderStatus novoStatus) {
        OrderEntity pedido = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de serviço não encontrada com o ID: " + id));

        // Regra de negócio: não permite alterar ordens já concluídas (ou canceladas, se você tiver esse status)
        if (pedido.getStatus() == OrderStatus.COMPLETED) {
            throw new BusinessException("Não é possível alterar o status de uma ordem já finalizada!");
        }

        // Atribuindo o novo status diretamente (já que novoStatus é do tipo OrderStatus vindo do DTO)
        pedido.setStatus(novoStatus);
        OrderEntity pedidoAtualizado = orderRepository.save(pedido);

        List<OrderItemResponseDTO> itensResponse = pedidoAtualizado.getItems().stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getId(),
                        item.getQuantity(),
                        item.getSoldPrice(),
                        item.getProduct().getId(),
                        item.getProduct().getName()
                ))
                .toList();

        return new OrderResponseDTO(
                pedidoAtualizado.getId(),
                pedidoAtualizado.getName(),
                pedidoAtualizado.getDescription(),
                pedidoAtualizado.getValue(),
                pedidoAtualizado.getStatus(),
                pedidoAtualizado.getCreatedAt(),
                pedidoAtualizado.getClient() != null ? pedidoAtualizado.getClient().getName() : null,
                itensResponse
        );
    }

    @Transactional
    public void deletarOrdemServico(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ordem de serviço não encontrada com o ID: " + id);
        }

        orderRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> listAllOrderService(Pageable pageable) {

        Page<OrderEntity> pedidoPage = orderRepository.findAllPaged(pageable);

        // 2. Usamos o .map() nativo do Page para converter a entidade para DTO mantendo a paginação
        return pedidoPage.map(pedido -> {
            List<OrderItemResponseDTO> itensResponse = pedido.getItems().stream()
                    .map(item -> new OrderItemResponseDTO(
                            item.getId(),
                            item.getQuantity(),
                            item.getSoldPrice(),
                            item.getProduct().getId(),
                            item.getProduct().getName()
                    ))
                    .toList();

            return new OrderResponseDTO(
                    pedido.getId(),
                    pedido.getName(),
                    pedido.getDescription(),
                    pedido.getValue(),
                    pedido.getStatus(),
                    pedido.getCreatedAt(),
                    pedido.getClient() != null ? pedido.getClient().getName() : null,
                    itensResponse
            );
        });
    }
}