package com.eray.ortakalisveris;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Product> getProducts() {
        return repository.findAll(Sort.by("id").ascending());
    }

    public Product addProduct(String name, String quantity) {
        Product product = new Product(
            name.trim(),
            quantity == null ? "" : quantity.trim()
        );

        return repository.save(product);
    }

    public Product updateProduct(Long id, boolean completed) {
        Product product = findProduct(id);
        product.setCompleted(completed);

        return repository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = findProduct(id);
        repository.delete(product);
    }

    private Product findProduct(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Ürün bulunamadı"
            ));
    }
}