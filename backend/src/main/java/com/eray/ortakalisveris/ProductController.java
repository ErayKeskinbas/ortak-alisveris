package com.eray.ortakalisveris;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Product> getProducts() {
        return repository.findAll(Sort.by("id").ascending());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product addProduct(@Valid @RequestBody CreateProductRequest request) {
        Product product = new Product(
            request.name().trim(),
            request.quantity() == null ? "" : request.quantity().trim()
        );

        return repository.save(product);
    }

    @PatchMapping("/{id}")
    public Product updateProduct(
        @PathVariable("id") Long id,
        @Valid @RequestBody UpdateProductRequest request
    ) {
        Product product = findProduct(id);
        product.setCompleted(request.completed());

        return repository.save(product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable("id") Long id) {
        Product product = findProduct(id);
        repository.delete(product);
    }

    private Product findProduct(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Ürün bulunamadı"
            ));
    }

    public record CreateProductRequest(
        @NotBlank @Size(max = 80) String name,
        @Size(max = 40) String quantity
    ) {}

    public record UpdateProductRequest(
        @NotNull Boolean completed
    ) {}
}