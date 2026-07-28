package org.allservice.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_enderecos")
public class AddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 9)
    private String cep;

    @Column(nullable = false, length = 150)
    private String street; // Logradouro

    @Column(nullable = false, length = 20)
    private String number;

    @Column(length = 100)
    private String complement;

    @Column(nullable = false, length = 100)
    private String neighborhood; // Bairro

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 2)
    private String state; // UF

    @OneToOne
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    public AddressEntity() {
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public String getComplement() { return complement; }
    public void setComplement(String complement) { this.complement = complement; }

    public String getNeighborhood() { return neighborhood; }
    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public ClientEntity getClient() { return client; }
    public void setClient(ClientEntity client) { this.client = client; }
}