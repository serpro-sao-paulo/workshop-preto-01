package br.gov.sifap;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests using a shared PostgreSQL Testcontainer.
 *
 * The container is static (shared across all IT subclasses in the same JVM fork),
 * which amortises the Flyway V1-V5 migration cost over the full IT suite.
 *
 * Usage: extend this class and annotate the subclass with {@code @Testcontainers}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("sifap_test")
                    .withUsername("sifap_app")
                    .withPassword("sifap_test_only")
                    .withReuse(true);

    @DynamicPropertySource
    static void overrideDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        // Disable OAuth2 resource server for integration tests
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> "");
        registry.add("spring.autoconfigure.exclude",
                () -> "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration");
    }
}
