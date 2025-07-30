package com.codesupreme.boxingclub.service.impl.product;

import com.codesupreme.boxingclub.dao.product.ProductRepository;
import com.codesupreme.boxingclub.dto.product.ProductDto;
import com.codesupreme.boxingclub.model.product.Product;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public ProductServiceImpl(ProductRepository productRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    //ALL
    public List<ProductDto> getAllProduct() {
        List<Product> list = productRepository.findAll();
        return list.stream()
                .map(det -> modelMapper.map(det, ProductDto.class))
                .toList();
    }

    //ById
    public ProductDto getProductById(Long id) {
        Optional<Product> optional = productRepository.findById(id);
        return optional.map(det -> modelMapper.map(det, ProductDto.class)).orElse(null);
    }

    //Create
    public ProductDto createProduct(ProductDto dto) {
        Product det = modelMapper.map(dto, Product.class);
        det = productRepository.save(det);
        return modelMapper.map(det, ProductDto.class);
    }

    //Update
    public ProductDto updateProduct(Long productId, ProductDto productDto) {
        Optional<Product> optional = productRepository.findById(productId);
        if (optional.isPresent()) {
            Product product = optional.get();

            if (productDto.getTitle() != null) {
                productDto.setTitle(productDto.getTitle());
            }

            if (productDto.getPrice() != null) {
                productDto.setPrice(productDto.getPrice());
            }

            if (productDto.getImageUrl() != null) {
                productDto.setImageUrl(productDto.getImageUrl());
            }

            if (productDto.getIsDisable() != null) {
                productDto.setIsDisable(productDto.getIsDisable());
            }

            product = productRepository.save(product);

            return modelMapper.map(product, ProductDto.class);
        }
        return null;
    }

    //Delete
    public Boolean deleteProduct(Long id) {
        Optional<Product> optional = productRepository.findById(id);
        if (optional.isPresent()) {
            Product det = optional.get();
            productRepository.delete(det);
            return true;
        }
        return false;
    }


}


