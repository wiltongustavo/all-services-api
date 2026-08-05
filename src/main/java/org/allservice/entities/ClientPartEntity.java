package org.allservice.entities;
import jakarta.persistence.*;
import org.allservice.enums.PartCondition;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_client_parts")
public class ClientPartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Nome da peça (Ex: Pastilha de Freio)

    private String brand; // Marca da peça (Ex: Bosch, Brembo)

    @Column(name = "serial_number")
    private String serialNumber; // Número de série ou Part Number

    @Enumerated(EnumType.STRING)
    @Column(name = "`condition`", nullable = false)
    private PartCondition condition;

    @Column(columnDefinition = "TEXT")
    private String description; // Observações (Ex: "Na caixa, entregue pelo próprio cliente")

    @Column(name = "declared_value")
    private BigDecimal declaredValue; // Valor estimado (opcional para termo de responsabilidade)

    @Column(name = "is_client_part", nullable = false)
    private Boolean isClientPart;

    public Boolean getClientPart() {
        return isClientPart;
    }

    public void setClientPart(Boolean clientPart) {
        isClientPart = clientPart;
    }

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Relacionamento com a Ordem de Serviço
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.condition == null) {
            this.condition = PartCondition.NEW; // Ajustado para corresponder ao Enum NEW/USED/REFURBISHED
        }
    }

    // Construtores, Getters e Setters
    public ClientPartEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public PartCondition getCondition() { return condition; }
    public void setCondition(PartCondition condition) { this.condition = condition; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getDeclaredValue() { return declaredValue; }
    public void setDeclaredValue(BigDecimal declaredValue) { this.declaredValue = declaredValue; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public OrderEntity getOrder() { return order; }
    public void setOrder(OrderEntity order) { this.order = order; }
}