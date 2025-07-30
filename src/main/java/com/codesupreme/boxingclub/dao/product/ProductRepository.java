package com.codesupreme.boxingclub.dao.product;

import com.codesupreme.boxingclub.model.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}