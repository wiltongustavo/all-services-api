package org.allservice.service;

import org.allservice.dto.request.AddressRequestDTO;
import org.allservice.dto.request.ClientRequestDTO;
import org.allservice.dto.response.*;
import org.allservice.entities.AddressEntity;
import org.allservice.entities.ClientEntity;
import org.allservice.entities.VehicleEntity;
import org.allservice.exceptions.BusinessException;
import org.allservice.exceptions.ResourceNotFoundException;
import org.allservice.repositories.OrderRepository;
import org.allservice.repositories.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.allservice.repositories.ClientRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;

    public ClientService(ClientRepository clientRepository, OrderRepository orderRepository, VehicleRepository vehicleRepository) {
        this.clientRepository = clientRepository;
        this.orderRepository = orderRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public CreateClientResponseDTO cadastrarClient(ClientRequestDTO clientDTO) {
        if (clientDTO.name() == null || clientDTO.name().trim().isEmpty()) {
            throw new BusinessException("O nome do cliente é obrigatório!");
        }

        if (clientDTO.email() != null && clientRepository.findByEmail(clientDTO.email()).isPresent()) {
            throw new BusinessException("Já existe um cliente cadastrado com este e-mail!");
        }

        ClientEntity client = new ClientEntity();
        client.setName(clientDTO.name());
        client.setEmail(clientDTO.email());
        client.setPhone(clientDTO.phone());
        client.setDateOfBirth(clientDTO.dateOfBirth());

        // Mapeia o AddressRequestDTO para AddressEntity
        if (clientDTO.address() != null) {
            AddressRequestDTO addrDto = clientDTO.address();
            AddressEntity address = new AddressEntity();
            address.setCep(addrDto.cep());
            address.setStreet(addrDto.street());
            address.setNumber(addrDto.number());
            address.setComplement(addrDto.complement());
            address.setNeighborhood(addrDto.neighborhood());
            address.setCity(addrDto.city());
            address.setState(addrDto.state());

            address.setClient(client);

            client.setAddress(address);
        }
        // Mapeia a lista de VehicleRequestDTO para VehicleEntity
        if (clientDTO.vehicles() != null && !clientDTO.vehicles().isEmpty()) {
            Set<VehicleEntity> vehicles = clientDTO.vehicles().stream().map(vDto -> {
                VehicleEntity vehicle = new VehicleEntity();
                vehicle.setBrand(vDto.brand());
                vehicle.setModel(vDto.model());
                vehicle.setYear(vDto.year());
                vehicle.setPlate(vDto.plate());
                vehicle.setColor(vDto.color());
                vehicle.setClient(client);
                return vehicle;
            }).collect(Collectors.toSet());

            client.setVehicles(vehicles);
        }
        ClientEntity clientSalvo = clientRepository.save(client);

        // Converte AddressEntity para AddressResponseDTO
        AddressResponseDTO addressResponse = clientSalvo.getAddress() != null ? new AddressResponseDTO(
                clientSalvo.getAddress().getId(),
                clientSalvo.getAddress().getCep(),
                clientSalvo.getAddress().getStreet(),
                clientSalvo.getAddress().getNumber(),
                clientSalvo.getAddress().getComplement(),
                clientSalvo.getAddress().getNeighborhood(),
                clientSalvo.getAddress().getCity(),
                clientSalvo.getAddress().getState()
        ) : null;

        // Converte VehicleEntity para VehicleResponseDTO usando clientSalvo
        List<VehicleResponseDTO> vehicleResponses = clientSalvo.getVehicles().stream().map(v -> new VehicleResponseDTO(
                v.getId(),
                v.getPlate(),
                v.getBrand(),
                v.getModel(),
                v.getYear(),
                v.getColor()
        )).toList();

        return new CreateClientResponseDTO(
                clientSalvo.getId(),
                clientSalvo.getName(),
                clientSalvo.getEmail(),
                clientSalvo.getPhone(),
                clientSalvo.getDateOfBirth(),
                clientSalvo.getCreateAt(),
                addressResponse,
                vehicleResponses
        );
    }

    @Transactional
    public UpdateClientResponseDTO atualizarCliente(Long id, ClientRequestDTO clientDTO) {
        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));

        if (clientDTO.email() != null && !clientDTO.email().equals(client.getEmail())) {
            clientRepository.findByEmail(clientDTO.email()).ifPresent(c -> {
                throw new BusinessException("Este e-mail já está em uso por outro cliente!");
            });
        }

        client.setName(clientDTO.name());
        client.setEmail(clientDTO.email());
        client.setPhone(clientDTO.phone());
        client.setDateOfBirth(clientDTO.dateOfBirth());

        // Atualiza o endereço se vier no request
        if (clientDTO.address() != null) {
            AddressRequestDTO addrDto = clientDTO.address();
            AddressEntity address = client.getAddress();
            if (address == null) {
                address = new AddressEntity();
                client.setAddress(address);

                address.setClient(client);
            }
            address.setCep(addrDto.cep());
            address.setStreet(addrDto.street());
            address.setNumber(addrDto.number());
            address.setComplement(addrDto.complement());
            address.setNeighborhood(addrDto.neighborhood());
            address.setCity(addrDto.city());
            address.setState(addrDto.state());
        }

        ClientEntity clientAtualizado = clientRepository.save(client);

        AddressResponseDTO addressResponse = clientAtualizado.getAddress() != null ? new AddressResponseDTO(
                clientAtualizado.getAddress().getId(),
                clientAtualizado.getAddress().getCep(),
                clientAtualizado.getAddress().getStreet(),
                clientAtualizado.getAddress().getNumber(),
                clientAtualizado.getAddress().getComplement(),
                clientAtualizado.getAddress().getNeighborhood(),
                clientAtualizado.getAddress().getCity(),
                clientAtualizado.getAddress().getState()
        ) : null;

        // Converte VehicleEntity para VehicleResponseDTO usando clientAtualizado
        List<VehicleResponseDTO> vehicleResponses = clientAtualizado.getVehicles().stream().map(v -> new VehicleResponseDTO(
                v.getId(),
                v.getPlate(),
                v.getBrand(),
                v.getModel(),
                v.getYear(),
                v.getColor()
        )).toList();

        return new UpdateClientResponseDTO(
                clientAtualizado.getId(),
                clientAtualizado.getName(),
                clientAtualizado.getEmail(),
                clientAtualizado.getPhone(),
                clientAtualizado.getDateOfBirth(),
                clientAtualizado.getCreateAt(),
                addressResponse,
                vehicleResponses
        );
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Long id) {
        ClientEntity client = clientRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));

        List<OrderResponseDTO> ordersDto = client.getOrders().stream()
                .map(order -> {
                    // Mapeia os itens do estoque do pedido
                    List<OrderItemResponseDTO> itemsDTO = order.getItems().stream()
                            .map(item -> new OrderItemResponseDTO(
                                    item.getId(),
                                    item.getQuantity(),
                                    item.getSoldPrice(),
                                    item.getProduct() != null ? item.getProduct().getId() : null,
                                    item.getProduct() != null ? item.getProduct().getName() : null
                            )).toList();

                    // Mapeia as peças trazidas pelo cliente do pedido
                    List<ClientPartResponseDTO> clientPartsDTO = order.getClientParts() != null ? order.getClientParts().stream()
                            .map(part -> new ClientPartResponseDTO(
                                    part.getId(),
                                    part.getName(),
                                    part.getBrand(),
                                    part.getSerialNumber(),
                                    part.getCondition(),
                                    part.getDescription(),
                                    part.getDeclaredValue()
                            )).toList() : List.of();

                    return new OrderResponseDTO(
                            order.getId(),
                            order.getName(),
                            order.getDescription(),
                            order.getValue(),
                            order.getStatus(),
                            order.getLaborValue(),
                            order.getCreatedAt(),
                            client.getName(),
                            itemsDTO,
                            clientPartsDTO
                    );
                })
                .toList();

        AddressResponseDTO addressResponse = client.getAddress() != null ? new AddressResponseDTO(
                client.getAddress().getId(),
                client.getAddress().getCep(),
                client.getAddress().getStreet(),
                client.getAddress().getNumber(),
                client.getAddress().getComplement(),
                client.getAddress().getNeighborhood(),
                client.getAddress().getCity(),
                client.getAddress().getState()
        ) : null;

        List<VehicleResponseDTO> vehicleResponses = client.getVehicles().stream().map(v -> new VehicleResponseDTO(
                v.getId(),
                v.getPlate(),
                v.getBrand(),
                v.getModel(),
                v.getYear(),
                v.getColor()
        )).toList();

        return new ClienteResponseDTO(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getPhone(),
                client.getDateOfBirth(),
                client.getCreateAt(),
                client.getActive(),
                addressResponse,
                vehicleResponses,
                ordersDto
        );
    }

    @Transactional
    public void deletarCliente(Long id) {
        ClientEntity cliente = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));

        boolean possuiOrdens = orderRepository.existsByClientId(id);
        if (possuiOrdens) {
            throw new BusinessException("Não é possível excluir o cliente pois ele possui ordens de serviço vinculadas.");
        }

        cliente.setActive(false);
        clientRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public Page<ClientListResponseDTO> listarTodosClientes(Pageable pageable) {
        Page<ClientEntity> paginaClientes = clientRepository.findAll(pageable);

        return paginaClientes.map(client -> new ClientListResponseDTO(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getPhone(),
                client.getDateOfBirth(),
                client.getCreateAt()
        ));
    }

    @Transactional(readOnly = true)
    public Page<ClientListResponseDTO> searchClients(String name, Pageable pageable) {
        Page<ClientEntity> clientPage = clientRepository.searchByName(name, pageable);

        return clientPage.map(client -> new ClientListResponseDTO(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getPhone(),
                client.getDateOfBirth(),
                client.getCreateAt()
        ));
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> findVehiclesByClientId(Long clientId) {
        List<VehicleEntity> vehicles = vehicleRepository.findByClientId(clientId);

        return vehicles.stream()
                .map(entity -> new VehicleResponseDTO(
                        entity.getId(),
                        entity.getPlate(),
                        entity.getBrand(),
                        entity.getModel(),
                        entity.getYear(),
                        entity.getColor()
                ))
                .collect(Collectors.toList());
    }
}