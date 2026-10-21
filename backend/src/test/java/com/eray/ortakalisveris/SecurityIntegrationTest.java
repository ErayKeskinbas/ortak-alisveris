package com.eray.ortakalisveris;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.security.test.web.servlet.request
    .SecurityMockMvcRequestBuilders.formLogin;

import static org.springframework.security.test.web.servlet.request
    .SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.security.test.web.servlet.request
    .SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.security.test.web.servlet.response
    .SecurityMockMvcResultMatchers.authenticated;

import static org.springframework.security.test.web.servlet.response
    .SecurityMockMvcResultMatchers.unauthenticated;

import static org.springframework.test.web.servlet.request
    .MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.request
    .MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result
    .MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    private static final String EMAIL = "security@example.com";
    private static final String PASSWORD = "Test-Sifresi-2026!";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AppUserRepository users;

    @Autowired
    private HouseholdRepository households;

    @Autowired
    private ProductRepository products;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepareUser() {
        Household household = households.save(
            new Household("Güvenlik test evi")
        );

        AppUser appUser = new AppUser(
            "Test kullanıcısı",
            EMAIL,
            passwordEncoder.encode(PASSWORD)
        );

        appUser.setHousehold(household);
        users.saveAndFlush(appUser);
    }

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        mvc.perform(get("/api/products"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login.html"));
    }

    @Test
    void correctPasswordCreatesWorkingSession() throws Exception {
        MockHttpSession session = login();

        mvc.perform(get("/api/products").session(session))
            .andExpect(status().isOk())
            .andExpect(authenticated().withUsername(EMAIL))
            .andExpect(content().json("[]"));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        mvc.perform(
                formLogin("/login")
                    .user(EMAIL)
                    .password("Yanlis-Sifre-2026!")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login.html?error"))
            .andExpect(unauthenticated());
    }

    @Test
    void productCreationWithoutCsrfIsRejected() throws Exception {
        mvc.perform(
                post("/api/products")
                    .with(user(EMAIL))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "Süt",
                          "quantity": "2 litre"
                        }
                        """)
            )
            .andExpect(status().isForbidden());

        assertEquals(0L, products.count());
    }

    @Test
    void productCreationWithSessionAndCsrfSucceeds() throws Exception {
        MockHttpSession session = login();

        mvc.perform(
                post("/api/products")
                    .session(session)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "Süt",
                          "quantity": "2 litre"
                        }
                        """)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Süt"))
            .andExpect(jsonPath("$.completed").value(false));

        assertEquals(1L, products.count());
    }

    @Test
    void logoutInvalidatesSession() throws Exception {
        MockHttpSession session = login();

        mvc.perform(
                post("/logout")
                    .session(session)
                    .with(csrf())
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login.html?logout"))
            .andExpect(unauthenticated());

        assertTrue(session.isInvalid());

        mvc.perform(get("/api/products"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login.html"));
    }

    private MockHttpSession login() throws Exception {
        var result = mvc.perform(
                formLogin("/login")
                    .user(EMAIL)
                    .password(PASSWORD)
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(authenticated().withUsername(EMAIL))
            .andReturn();

        MockHttpSession session =
            (MockHttpSession) result.getRequest().getSession(false);

        assertNotNull(session);

        return session;
    }
}