package com.codesupreme.boxingclub.api.product.controller;


import com.codesupreme.boxingclub.dto.product.ProductDto;
import com.codesupreme.boxingclub.service.impl.product.ProductServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v10/product")
public class ProductController {

    private final ProductServiceImpl service;
    public ProductController(ProductServiceImpl service) {this.service = service;}

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllOrder() {
        List<ProductDto> all = service.getAllOrder();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ProductDto> getOrderById(@PathVariable("orderId") Long id) {
        ProductDto det = service.getOrderById(id);
        if (det != null) {
            return ResponseEntity.ok(det);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ProductDto> createOrder(@RequestBody ProductDto dto) {
        ProductDto created = service.createOrder(dto);
        return ResponseEntity.ok(created);
    }

    // Update
    @PutMapping("/{orderId}")
    public ResponseEntity<?> updateOrder(
            @PathVariable("orderId") Long id,
            @RequestBody ProductDto productDto) {
        ProductDto updatedOrder = service.updateOrder(id, productDto);
        if (updatedOrder != null) {
            return ResponseEntity.ok(updatedOrder);
        }
        return ResponseEntity.notFound().build();
    }


    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable("orderId") Long id) {
        Boolean deleted = service.deleteOrder(id);
        if (deleted) {
            return ResponseEntity.ok("Admin order deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }
}
