package com.eray.ortakalisveris;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(0);

    @GetMapping
    public List<Product> getProducts() {
        return products.values().stream()
            .sorted(Comparator.comparing(Product::id))
            .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product addProduct(@Valid @RequestBody CreateProductRequest request) {
        long id = nextId.incrementAndGet();

        Product product = new Product(
            id,
            request.name().trim(),
            request.quantity() == null ? "" : request.quantity().trim(),
            false
        );

        products.put(id, product);
        return product;
    }

    @PatchMapping("/{id}")
    public Product updateProduct(
        @PathVariable("id") Long id,
        @Valid @RequestBody UpdateProductRequest request
    ) {
        Product updated = products.computeIfPresent(
            id,
            (key, current) -> new Product(
                current.id(),
                current.name(),
                current.quantity(),
                request.completed()
            )
        );

        if (updated == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Ürün bulunamadı"
            );
        }

        return updated;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable("id") Long id) {
        if (products.remove(id) == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Ürün bulunamadı"
            );
        }
    }

    public record Product(
        Long id,
        String name,
        String quantity,
        boolean completed
    ) {}

    public record CreateProductRequest(
        @NotBlank @Size(max = 80) String name,
        @Size(max = 40) String quantity
    ) {}

    public record UpdateProductRequest(
        @jakarta.validation.constraints.NotNull Boolean completed
    ) {}
}