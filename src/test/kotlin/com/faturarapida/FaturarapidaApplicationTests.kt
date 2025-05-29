package com.faturarapida

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FaturarapidaApplicationTests {
    @Test
    fun contextLoads() {
        // Test will pass if the application context loads successfully
    }
}
