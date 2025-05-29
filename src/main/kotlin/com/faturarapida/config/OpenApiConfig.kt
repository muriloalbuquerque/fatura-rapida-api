package com.faturarapida.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Configuração do OpenAPI (Swagger) para documentação da API
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val securitySchemeName = "bearerAuth"
        
        // Configura as classes de data para o Swagger
        SpringDocUtils.getConfig().replaceWithClass(LocalDate::class.java, String::class.java)
        SpringDocUtils.getConfig().replaceWithClass(LocalDateTime::class.java, String::class.java)

        return OpenAPI()
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
            .info(
                Info()
                    .title("Fatura Rápida API")
                    .description("""
                        API para gerenciamento de faturas e boletos.
                        
                        ## Visão Geral
                        
                        A API Fatura Rápida permite:
                        - Criar e gerenciar faturas
                        - Gerar PDFs de faturas
                        - Acompanhar status de pagamento
                        - Gerenciar clientes e suas informações
                        
                        ## Autenticação
                        
                        A API utiliza autenticação JWT. Para autenticar, inclua o token no cabeçalho:
                        ```
                        Authorization: Bearer <seu-token>
                        ```
                        """.trimIndent()
                    )
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Suporte Fatura Rápida")
                            .email("suporte@faturarapida.com.br")
                            .url("https://faturarapida.com.br/suporte")
                    )
                    .license(
                        License()
                            .name("Apache 2.0")
                            .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                    )
            )
            .servers(
                listOf(
                    Server().url("/").description("Servidor Local"),
                    Server().url("http://localhost:8080").description("Servidor Local com Porta"),
                    Server().url("http://localhost:8080/api").description("API Base Path")
                )
            )
    }
}
