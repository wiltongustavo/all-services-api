package org.allservice.service;

import org.allservice.dto.request.ClientRequestDTO;
import org.allservice.dto.response.*;
import org.allservice.entities.ClientEntity;
import org.allservice.exceptions.BusinessException;
import org.allservice.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.allservice.repositories.ClientRepository;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    // Injeção por construtor limpa (sem precisar do @Autowired no campo)
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional
    public CreateClientResponseDTO cadastrarClient(ClientRequestDTO clientDTO) {
        // Validação de campo obrigatório
        if (clientDTO.name() == null || clientDTO.name().trim().isEmpty()) {
            throw new BusinessException("O nome do cliente é obrigatório!");
        }

        // Regra de e-mail único
        if (clientDTO.email() != null && clientRepository.findByEmail(clientDTO.email()).isPresent()) {
            throw new BusinessException("Já existe um cliente cadastrado com este e-mail!");
        }

        ClientEntity client = new ClientEntity();
        client.setName(clientDTO.name());
        client.setEmail(clientDTO.email());
        client.setPhone(clientDTO.phone());
        client.setDateOfBirth(clientDTO.dateOfBirth());

        ClientEntity clientSalvo = clientRepository.save(client);

        return new CreateClientResponseDTO(
                clientSalvo.getId(),
                clientSalvo.getName(),
                clientSalvo.getEmail(),
                clientSalvo.getPhone(),
                clientSalvo.getDateOfBirth(),
                clientSalvo.getCreateAt()
        );
    }

    @Transactional
    public UpdateClientResponseDTO atualizarCliente(Long id, ClientRequestDTO clientDTO) {
        // ID não existe -> 404
        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));

        // Validação de e-mail duplicado ao editar -> 400
        if (clientDTO.email() != null && !clientDTO.email().equals(client.getEmail())) {
            clientRepository.findByEmail(clientDTO.email()).ifPresent(c -> {
                throw new BusinessException("Este e-mail já está em uso por outro cliente!");
            });
        }

        client.setName(clientDTO.name());
        client.setEmail(clientDTO.email());
        client.setPhone(clientDTO.phone());
        client.setDateOfBirth(clientDTO.dateOfBirth());

        ClientEntity clientAtualizado = clientRepository.save(client);

        return new UpdateClientResponseDTO(
                clientAtualizado.getId(),
                clientAtualizado.getName(),
                clientAtualizado.getEmail(),
                clientAtualizado.getPhone(),
                clientAtualizado.getDateOfBirth(),
                clientAtualizado.getCreateAt()
        );
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Long id){
        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));
        List<OrderResponseDTO> ordersDto = client.getOrders().stream()
                .map(order -> {

                    List<OrderItemResponseDTO> itemsDTO = order.getItems().stream()
                            .map(item -> new OrderItemResponseDTO(
                                    item.getId(),
                                    item.getQuantity(),
                                    item.getSoldPrice(),
                                    item.getProduct().getId(),
                                    item.getProduct().getName()
                            )).toList();
                   return new OrderResponseDTO(
                        order.getId(),
                        order.getName(),
                        order.getDescription(),
                            order.getValue(),
                            order.getStatus(),
                            order.getCreatedAt(),
                            client.getName(),
                            itemsDTO

                );
                })
                .toList();
            return new ClienteResponseDTO(
                    client.getId(),
                    client.getName(),
                    client.getEmail(),
                    client.getPhone(),
                    client.getDateOfBirth(),
                    client.getCreateAt(),
                    ordersDto
            );

    }


    @Transactional
    public void deletarCliente(Long id) {
        // ID não existe -> 404
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente não encontrado com o ID: " + id);
        }

        clientRepository.deleteById(id);
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
}