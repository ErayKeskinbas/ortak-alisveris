package com.eray.ortakalisveris;

import java.security.Principal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<Product> getProducts(Principal principal) {
        return service.getProducts(principal.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product addProduct(
        Principal principal,
        @Valid @RequestBody CreateProductRequest request
    ) {
        return service.addProduct(
            principal.getName(),
            request.name(),
            request.quantity()
        );
    }

    @PatchMapping("/{id}")
    public Product updateProduct(
        Principal principal,
        @PathVariable("id") Long id,
        @Valid @RequestBody UpdateProductRequest request
    ) {
        return service.updateProduct(
            principal.getName(),
            id,
            request.completed()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(
        Principal principal,
        @PathVariable("id") Long id
    ) {
        service.deleteProduct(principal.getName(), id);
    }

    public record CreateProductRequest(
        @NotBlank @Size(max = 80) String name,
        @Size(max = 40) String quantity
    ) {}

    public record UpdateProductRequest(
        @NotNull Boolean completed
    ) {}
}