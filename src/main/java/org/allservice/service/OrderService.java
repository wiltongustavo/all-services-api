package org.allservice.service;

import org.allservice.dto.request.ClientPartRequestDTO;
import org.allservice.dto.request.OrderRequestDTO;
import org.allservice.dto.response.*;
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
import java.time.LocalDate;
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

        // 2. Processamento das Peças (Trazidas pelo cliente ou da oficina)
        if (orderDTO.clientParts() != null && !orderDTO.clientParts().isEmpty()) {
            for (ClientPartRequestDTO partDto : orderDTO.clientParts()) {
                ClientPartEntity part = new ClientPartEntity();
                part.setName(partDto.name());
                part.setBrand(partDto.brand());
                part.setSerialNumber(partDto.serialNumber());
                part.setCondition(partDto.condition());
                part.setClientPart(partDto.isClientPart());
                part.setDescription(partDto.description());
                part.setDeclaredValue(partDto.declaredValue());

                pedido.addClientPart(part);
            }
        }

        // 3. Cálculo de Valores no Backend
        // A. Soma dos produtos de estoque (Preço real buscado do banco x quantidade)
        BigDecimal valorTotalItens = itensEntity.stream()
                .map(item -> item.getSoldPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // B. Soma das peças que PERTENCEM À OFICINA (onde isClientPart é false)
        BigDecimal valorTotalPecasOficina = BigDecimal.ZERO;
        if (orderDTO.clientParts() != null) {
            valorTotalPecasOficina = orderDTO.clientParts().stream()
                    .filter(partDto -> Boolean.FALSE.equals(partDto.isClientPart())) // Se não for do cliente, soma
                    .map(partDto -> partDto.declaredValue() != null ? partDto.declaredValue() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // C. Valor Total Geral (Itens + Peças da Oficina + Mão de Obra)
        BigDecimal valorTotalGeral = valorTotalItens
                .add(valorTotalPecasOficina)
                .add(laborValue);

        pedido.setTotalValue(valorTotalGeral); // CORRETO: Usando setter para atribuir o valor calculado

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
    public Page<OrderSummaryResponseDTO> listarResumoOrdens(
            Long id,
            OrderStatus status,
            String clientName,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        // Converte LocalDate para LocalDateTime para abranger o dia completo
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(23, 59, 59) : null;

        Page<OrderEntity> pedidoPage = orderRepository.findFilteredSummaryPaged(
                id, status, clientName, startDateTime, endDateTime, pageable
        );

        return pedidoPage.map(order -> new OrderSummaryResponseDTO(
                order.getId(),
                order.getName(),
                order.getDescription(),
                order.getTotalValue(),
                order.getStatus(),
                order.getLaborValue(),
                order.getCreatedAt(),
                order.getClient() != null ? order.getClient().getName() : null
        ));
    }
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByVehicleId(Long vehicleId) {
        List<OrderEntity> orders = orderRepository.findByVehicleIdWithDetails(vehicleId);

        return orders != null ? orders.stream()
                .map(this::mapearParaResponse)
                .toList() : List.of();
    }

    @Transactional(readOnly = true)
    private OrderResponseDTO mapearParaResponse(OrderEntity pedido) {
        VehicleResponseDTO vehicleDto = null;
        if (pedido.getVehicle() != null) {
            vehicleDto = new VehicleResponseDTO(
                    pedido.getVehicle().getId(),
                    pedido.getVehicle().getPlate(),
                    pedido.getVehicle().getBrand(),
                    pedido.getVehicle().getModel(),
                    pedido.getVehicle().getYear(),
                    pedido.getVehicle().getColor()
            );
        }

        List<OrderItemResponseDTO> itensResponse = pedido.getItems() != null ? pedido.getItems().stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getId(),
                        item.getQuantity(),
                        item.getSoldPrice(),
                        item.getProduct() != null ? item.getProduct().getId() : null,
                        item.getProduct() != null ? item.getProduct().getName() : null
                ))
                .toList() : List.of();

        List<ClientPartResponseDTO> clientPartsResponse = pedido.getClientParts() != null ? pedido.getClientParts().stream()
                .map(part -> new ClientPartResponseDTO(
                        part.getId(),
                        part.getName(),
                        part.getBrand(),
                        part.getSerialNumber(),
                        part.getCondition(),
                        part.getDescription(),
                        part.getClientPart(),
                        part.getDeclaredValue()
                ))
                .toList() : List.of();

        return new OrderResponseDTO(
                pedido.getId(),
                pedido.getName(),
                pedido.getDescription(),
                pedido.getTotalValue(), // CORRETO: Alterado de set para getTotalValue
                pedido.getStatus(),
                pedido.getLaborValue(),
                pedido.getCreatedAt(),
                pedido.getClient() != null ? pedido.getClient().getName() : null,
                itensResponse,
                clientPartsResponse,
                vehicleDto
        );
    }
}