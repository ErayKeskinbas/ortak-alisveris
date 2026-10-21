package com.eray.ortakalisveris;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ProductService {

    private final ProductRepository products;
    private final AppUserRepository users;

    public ProductService(
        ProductRepository products,
        AppUserRepository users
    ) {
        this.products = products;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<Product> getProducts(String email) {
        Household household = requireHousehold(email);

        return products.findByHousehold_IdOrderByIdAsc(
            household.getId()
        );
    }

    public Product addProduct(
        String email,
        String name,
        String quantity
    ) {
        Household household = requireHousehold(email);

        Product product = new Product(
            name.trim(),
            quantity == null ? "" : quantity.trim(),
            household
        );

        return products.save(product);
    }

    public Product updateProduct(
        String email,
        Long id,
        boolean completed
    ) {
        Household household = requireHousehold(email);
        Product product = findProduct(id, household.getId());

        product.setCompleted(completed);

        return products.save(product);
    }

    public void deleteProduct(String email, Long id) {
        Household household = requireHousehold(email);
        Product product = findProduct(id, household.getId());

        products.delete(product);
    }

    private Household requireHousehold(String email) {
        AppUser user = users.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Kullanıcı bulunamadı"
            ));

        if (user.getHousehold() == null) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Önce bir ev oluşturmalı veya eve katılmalısın"
            );
        }

        return user.getHousehold();
    }

    private Product findProduct(Long id, Long householdId) {
        return products.findByIdAndHousehold_Id(id, householdId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Ürün bulunamadı"
            ));
    }
}