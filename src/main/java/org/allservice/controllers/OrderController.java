package org.allservice.controllers;

import org.allservice.dto.response.OrderResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.allservice.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> criarOrdem(@RequestBody OrderResponseDTO dto) {
        OrderResponseDTO response = orderService.criarOrdemServico(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> buscarPorId(@PathVariable Long id) {
        OrderResponseDTO response = orderService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> listarTodas() {
        List<OrderResponseDTO> response = orderService.listAllOrderService();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> atualizarStatus(@PathVariable Long id, @RequestParam String status) {
        OrderResponseDTO response = orderService.atualizarStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        orderService.deletarOrdemServico(id);
        return ResponseEntity.noContent().build();
    }
}