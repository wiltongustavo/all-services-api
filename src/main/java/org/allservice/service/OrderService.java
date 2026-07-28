package org.allservice.service;

import org.allservice.dto.request.ClientPartRequestDTO;
import org.allservice.dto.request.OrderRequestDTO;
import org.allservice.dto.response.ClientPartResponseDTO;
import org.allservice.dto.response.OrderItemResponseDTO;
import org.allservice.dto.response.OrderSummaryResponseDTO;
import org.allservice.dto.response.OrderResponseDTO;
import org.allservice.entities.*;
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
import org.allservice.repositories.VehicleRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final VehicleRepository vehicleRepository;

    public OrderService(OrderRepository orderRepository,
                        ClientRepository clientRepository,
                        ProductRepository productRepository,
                        VehicleRepository vehicleRepository) {
        this.orderRepository = orderRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public OrderResponseDTO criarOrdemServico(OrderRequestDTO orderDTO) {
        ClientEntity client = clientRepository.findById(orderDTO.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + orderDTO.clientId()));

        VehicleEntity vehicle = vehicleRepository.findById(orderDTO.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com o ID: " + orderDTO.vehicleId()));

        if (!vehicle.getClient().getId().equals(client.getId())) {
            throw new BusinessException("O veículo informado não pertence ao cliente selecionado!");
        }

        OrderEntity pedido = new OrderEntity();
        pedido.setClient(client);
        pedido.setVehicle(vehicle);
        pedido.setName(orderDTO.name());
        pedido.setDescription(orderDTO.description());

        BigDecimal laborValue = orderDTO.laborValue() != null ? orderDTO.laborValue() : BigDecimal.ZERO;
        pedido.setLaborValue(laborValue);

        pedido.setStatus(orderDTO.status() != null ? orderDTO.status() : OrderStatus.PENDING);

        // 1. Processamento dos Itens do Estoque
        List<OrderItemEntity> itensEntity = orderDTO.items().stream()
                .map(itemDto -> {
                    ProductEntity produto = productRepository.findById(itemDto.productId())
                            .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com o ID: " + itemDto.productId()));

                    if (produto.getStock() < itemDto.quantity()) {
                        throw new BusinessException("Estoque insuficiente para o produto: " + produto.getName());
                    }

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

        // 2. Processamento das Peças Trazidas pelo Cliente (ClientParts)
        if (orderDTO.clientParts() != null && !orderDTO.clientParts().isEmpty()) {
            for (ClientPartRequestDTO partDto : orderDTO.clientParts()) {
                ClientPartEntity part = new ClientPartEntity();
                part.setName(partDto.name());
                part.setBrand(partDto.brand());
                part.setSerialNumber(partDto.serialNumber());
                part.setCondition(partDto.condition());
                part.setDescription(partDto.description());
                part.setDeclaredValue(partDto.declaredValue());

                // Vincula corretamente usando o método auxiliar da OrderEntity
                pedido.addClientPart(part);
            }
        }

        // 3. Cálculo de Valores
        BigDecimal valorTotalItens = itensEntity.stream()
                .map(item -> item.getSoldPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorTotalGeral = valorTotalItens.add(laborValue);
        pedido.setValue(valorTotalGeral);

        OrderEntity pedidoSalvo = orderRepository.save(pedido);

        return mapearParaResponse(pedidoSalvo);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO buscarPorId(Long id) {
        OrderEntity pedido = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de serviço não encontrada com o ID: " + id));

        return mapearParaResponse(pedido);
    }

    @Transactional
    public OrderResponseDTO atualizarStatus(Long id, OrderStatus novoStatus) {
        OrderEntity pedido = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de serviço não encontrada com o ID: " + id));

        if (pedido.getStatus() == OrderStatus.COMPLETED) {
            throw new BusinessException("Não é possível alterar o status de uma ordem já finalizada!");
        }

        pedido.setStatus(novoStatus);
        OrderEntity pedidoAtualizado = orderRepository.save(pedido);

        return mapearParaResponse(pedidoAtualizado);
    }

    @Transactional
    public void deletarOrdemServico(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ordem de serviço não encontrada com o ID: " + id);
        }

        orderRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponseDTO> listarResumoOrdens(Pageable pageable) {
        Page<OrderEntity> pedidoPage = orderRepository.findAllSummaryPaged(pageable);

        return pedidoPage.map(order -> new OrderSummaryResponseDTO(
                order.getId(),
                order.getName(),
                order.getDescription(),
                order.getValue(),
                order.getStatus(),
                order.getLaborValue(),
                order.getCreatedAt(),
                order.getClient() != null ? order.getClient().getName() : null
        ));
    }

    private OrderResponseDTO mapearParaResponse(OrderEntity pedido) {
        List<OrderItemResponseDTO> itensResponse = pedido.getItems().stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getId(),
                        item.getQuantity(),
                        item.getSoldPrice(),
                        item.getProduct() != null ? item.getProduct().getId() : null,
                        item.getProduct() != null ? item.getProduct().getName() : null
                ))
                .toList();

        List<ClientPartResponseDTO> clientPartsResponse = pedido.getClientParts() != null ? pedido.getClientParts().stream()
                .map(part -> new ClientPartResponseDTO(
                        part.getId(),
                        part.getName(),
                        part.getBrand(),
                        part.getSerialNumber(),
                        part.getCondition(),
                        part.getDescription(),
                        part.getDeclaredValue()
                ))
                .toList() : List.of();

        return new OrderResponseDTO(
                pedido.getId(),
                pedido.getName(),
                pedido.getDescription(),
                pedido.getValue(),
                pedido.getStatus(),
                pedido.getLaborValue(),
                pedido.getCreatedAt(),
                pedido.getClient() != null ? pedido.getClient().getName() : null,
                itensResponse,
                clientPartsResponse
        );
    }

    public List<OrderResponseDTO> getOrdersByVehicleId(Long vehicleId) {

        List<OrderEntity> orders = orderRepository.findByVehicleIdWithDetails(vehicleId);

        return orders != null ? orders.stream()
                .map(pedido -> new OrderResponseDTO(
                        pedido.getId(),
                        pedido.getName(),
                        pedido.getDescription(),
                        pedido.getValue(),
                        pedido.getStatus(),
                        pedido.getLaborValue(),
                        pedido.getCreatedAt(),
                        pedido.getClient() != null ? pedido.getClient().getName() : null,

                        // Mapeamento dos itens da ordem
                        pedido.getItems() != null ? pedido.getItems().stream()
                                .map(item -> new OrderItemResponseDTO(
                                        item.getId(),
                                        item.getQuantity(),
                                        item.getSoldPrice(),
                                        item.getProduct().getId(),
                                        item.getProduct().getName()
                                ))
                                .toList() : List.of(),

                        // Mapeamento das peças do cliente
                        pedido.getClientParts() != null ? pedido.getClientParts().stream()
                                .map(part -> new ClientPartResponseDTO(
                                        part.getId(),
                                        part.getName(),
                                        part.getBrand(),
                                        part.getSerialNumber(),
                                        part.getCondition(),
                                        part.getDescription(),
                                        part.getDeclaredValue()
                                ))
                                .toList() : List.of()
                ))
                .toList() : List.of();
    }
}