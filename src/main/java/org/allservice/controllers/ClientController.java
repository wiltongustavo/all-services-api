package org.allservice.controllers;

import org.allservice.dto.request.ClientRequestDTO;
import org.allservice.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.allservice.service.ClientService;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<CreateClientResponseDTO> cadastrar(@RequestBody ClientRequestDTO dto) {
        CreateClientResponseDTO response = clientService.cadastrarClient(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ClientListResponseDTO>> listarTodos(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        // Chama o método atualizado do service passando os filtros
        Page<ClientListResponseDTO> clientes = clientService.listarClientesFiltrados(id, name, email, phone, pageable);

        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        ClienteResponseDTO clienteResponseDTO = clientService.buscarPorId(id);
        return ResponseEntity.ok(clienteResponseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateClientResponseDTO> atualizar(@PathVariable Long id, @RequestBody ClientRequestDTO dto) {
        UpdateClientResponseDTO response = clientService.atualizarCliente(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        clientService.deletarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ClientListResponseDTO>> listarOuFiltrarClientes(
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<ClientListResponseDTO> response = clientService.searchClients(name, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/vehicles")
    public ResponseEntity<List<VehicleResponseDTO>> getVehiclesByClient(@PathVariable Long id) {
        List<VehicleResponseDTO> vehicles = clientService.findVehiclesByClientId(id);
        return ResponseEntity.ok(vehicles);
    }
}