package com.smartcare.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/smartcare_test",
        "spring.datasource.username=test",
        "spring.datasource.password=test",
        "spring.datasource.hikari.initialization-fail-timeout=-1",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect"
})
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
