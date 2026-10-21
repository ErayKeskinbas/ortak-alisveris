package com.eray.ortakalisveris;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService service;

    @Autowired
    private ProductRepository products;

    @Autowired
    private HouseholdRepository households;

    @Autowired
    private AppUserRepository users;

    private Long firstProductId;

    @BeforeEach
    void prepareData() {
        Household firstHome = households.save(
            new Household("Birinci ev")
        );

        Household secondHome = households.save(
            new Household("İkinci ev")
        );

        createUser("first@example.com", firstHome);
        createUser("member@example.com", firstHome);
        createUser("second@example.com", secondHome);
        createUser("nohome@example.com", null);

        Product firstProduct = products.save(
            new Product("Süt", "2 litre", firstHome)
        );

        firstProductId = firstProduct.getId();

        products.save(
            new Product("Ekmek", "1 adet", secondHome)
        );
    }

    private void createUser(String email, Household household) {
        AppUser user = new AppUser(
            "Test kullanıcısı",
            email,
            "test-only-placeholder"
        );

        user.setHousehold(household);
        users.save(user);
    }

    @Test
    void listsOnlyOwnHouseholdProducts() {
        var result = service.getProducts("second@example.com");

        assertEquals(1, result.size());
        assertEquals("Ekmek", result.getFirst().getName());
    }

    @Test
    void membersOfSameHouseholdSeeSameProducts() {
        var result = service.getProducts("member@example.com");

        assertEquals(1, result.size());
        assertEquals(firstProductId, result.getFirst().getId());
    }

    @Test
    void cannotUpdateAnotherHouseholdProduct() {
        var exception = assertThrows(
            ResponseStatusException.class,
            () -> service.updateProduct(
                "second@example.com",
                firstProductId,
                true
            )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        Product unchanged = products.findById(firstProductId)
            .orElseThrow();

        assertFalse(unchanged.isCompleted());
    }

    @Test
    void cannotDeleteAnotherHouseholdProduct() {
        var exception = assertThrows(
            ResponseStatusException.class,
            () -> service.deleteProduct(
                "second@example.com",
                firstProductId
            )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(products.existsById(firstProductId));
    }

    @Test
    void userWithoutHouseholdCannotListProducts() {
        var exception = assertThrows(
            ResponseStatusException.class,
            () -> service.getProducts("nohome@example.com")
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void newProductBelongsToCreatorsHousehold() {
        Product created = service.addProduct(
            "first@example.com",
            "Elma",
            "1 kg"
        );

        var ownProducts = service.getProducts("member@example.com");
        var otherProducts = service.getProducts("second@example.com");

        assertTrue(
            ownProducts.stream()
                .anyMatch(product -> product.getId().equals(created.getId()))
        );

        assertFalse(
            otherProducts.stream()
                .anyMatch(product -> product.getId().equals(created.getId()))
        );
    }
}