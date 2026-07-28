package org.allservice.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_veiculos")
public class VehicleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String plate; // Placa

    @Column(nullable = false, length = 50)
    private String brand; // Marca

    @Column(nullable = false, length = 50)
    private String model; // Modelo

    @Column(nullable = false)
    private Integer year; // Ano

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Column(nullable = false)
    private String color; // Ano


    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    public VehicleEntity() {
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public ClientEntity getClient() {
        return client;
    }

    public void setClient(ClientEntity client) {
        this.client = client;
    }
}