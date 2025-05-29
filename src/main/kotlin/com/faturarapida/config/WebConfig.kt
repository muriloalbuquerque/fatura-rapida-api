package com.faturarapida.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Configurações gerais da aplicação web
 */
@Configuration
@EnableWebMvc
class WebConfig : WebMvcConfigurer {

    /**
     * Configura o CORS globalmente para a aplicação
     * 
     * Esta configuração permite requisições de qualquer origem (origins) para facilitar o desenvolvimento.
     * Em produção, é recomendado restringir as origens permitidas.
     */
    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOrigins(
                        "http://localhost:3000",  // React
                        "http://localhost:4200",  // Angular
                        "http://localhost:8081",  // Outro servidor
                        "http://localhost:8080"   // Mesmo servidor (para testes)
                    )
                    .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600)
            }
        }
    }
    
    /**
     * Configuração adicional para o tratamento de requisições multipart
     * (Já configurado no application.yml, mas pode ser sobrescrito aqui se necessário)
     */
    // @Bean
    // fun multipartConfigElement(): MultipartConfigElement {
    //     return MultipartConfigFactory()
    //         .createMultipartConfig()
    // }
}
