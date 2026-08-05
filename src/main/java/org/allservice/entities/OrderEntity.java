package org.allservice.entities;

import jakarta.persistence.*;
import org.allservice.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tb_pedidos")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Valor total do pedido (Produtos + Mão de obra)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalValue;

    // Novo campo para armazenar especificamente o valor da mão de obra
    @Column(name = "labor_value", precision = 10, scale = 2)
    private BigDecimal laborValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleEntity vehicle;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClientPartEntity> clientParts = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.laborValue == null) {
            this.laborValue = BigDecimal.ZERO;
        }
    }

    public OrderEntity() {}

    // Getters e Setters
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }


    public BigDecimal getLaborValue() { return laborValue; }
    public void setLaborValue(BigDecimal laborValue) { this.laborValue = laborValue; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public ClientEntity getClient() { return client; }
    public void setClient(ClientEntity client) { this.client = client; }

    public List<OrderItemEntity> setItems(List<OrderItemEntity> items) {
        return this.items = items;
    }

    public List<OrderItemEntity> getItems() {
        return items;
    }

    public VehicleEntity getVehicle() {
        return vehicle;
    }

    public void setVehicle(VehicleEntity vehicle) {
        this.vehicle = vehicle;
    }

    public Set<ClientPartEntity> getClientParts() {
        return clientParts;
    }

    public void setClientParts(Set<ClientPartEntity> clientParts) {
        this.clientParts = clientParts;
    }

    // Método utilitário recomendado para Set (mantém o vínculo bidirecional)
    public void addClientPart(ClientPartEntity part) {
        clientParts.add(part);
        part.setOrder(this);
    }

    public void removeClientPart(ClientPartEntity part) {
        clientParts.remove(part);
        part.setOrder(null);
    }
}