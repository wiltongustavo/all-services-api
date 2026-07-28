package org.allservice.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tb_clientes")
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createAt;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VehicleEntity> vehicles = new HashSet<>();

    @OneToMany(mappedBy = "client")
    private Set<OrderEntity> orders = new HashSet<>();

    public Set<OrderEntity> getOrders() {
        return orders;
    }

    @OneToOne(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private AddressEntity address;

    public ClientEntity() {
    }

    public AddressEntity getAddress() {
        return address;
    }

    public void setAddress(AddressEntity address) {
        this.address = address;
    }

    public Set<VehicleEntity> getVehicles() {
        return vehicles;
    }

    public void setVehicles(Set<VehicleEntity> vehicles) {
        this.vehicles = vehicles;
    }

    public void setOrders(Set<OrderEntity> orders) {
        this.orders = orders;
    }

    @PrePersist
    protected void onCreate() {
        this.createAt = LocalDateTime.now();
        this.active = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }


    public LocalDateTime getCreateAt() {
        return createAt;
    }


    public Long getId() {
        return id;
    }


}
