package com.faturarapida.config

import com.faturarapida.service.storage.StorageService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Configuração para o serviço de armazenamento de arquivos
 */
@Configuration
@EnableConfigurationProperties(StorageConfig.StorageProperties::class)
class StorageConfig {

    private val logger = LoggerFactory.getLogger(StorageConfig::class.java)

    /**
     * Propriedades de configuração para o armazenamento
     */
    @ConfigurationProperties(prefix = "app.file-storage")
    class StorageProperties {
        /**
         * Diretório raiz para armazenamento de arquivos
         */
        var rootLocation: String = "./storage"

        /**
         * Se deve criar os diretórios automaticamente
         */
        var createDirs: Boolean = true
    }

    /**
     * Inicializa o diretório de armazenamento na inicialização da aplicação
     */
    @Bean
    fun init(storageService: StorageService, properties: StorageProperties): CommandLineRunner {
        return CommandLineRunner {
            if (properties.createDirs) {
                logger.info("Inicializando diretório de armazenamento em: ${properties.rootLocation}")
                try {
                    val rootPath = Paths.get(properties.rootLocation).toAbsolutePath().normalize()
                    Files.createDirectories(rootPath)
                    logger.info("Diretório de armazenamento inicializado com sucesso: $rootPath")
                } catch (e: Exception) {
                    logger.error("Falha ao inicializar diretório de armazenamento", e)
                    throw IllegalStateException("Não foi possível inicializar o diretório de armazenamento", e)
                }
            }
        }
    }

    /**
     * Configura o serviço de armazenamento
     */
    @Bean
    fun storageService(properties: StorageProperties): StorageService {
        return StorageService(properties.rootLocation)
    }
}
