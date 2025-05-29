package com.faturarapida.service.storage

import com.faturarapida.exception.StorageException
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

/**
 * Serviço responsável por gerenciar o armazenamento de arquivos
 *
 * Este serviço lida com operações de armazenamento de arquivos, incluindo:
 * - Armazenamento de arquivos enviados
 * - Recuperação de arquivos armazenados
 * - Exclusão de arquivos
 * - Gerenciamento de nomes de arquivo únicos
 */
@Service
class StorageService(
    private val rootLocation: Path
) {
    private val logger = LoggerFactory.getLogger(StorageService::class.java)

    constructor(rootLocation: String) : this(Paths.get(rootLocation).toAbsolutePath().normalize())

    init {
        logger.info("Inicializando StorageService com diretório: $rootLocation")
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation)
                logger.info("Diretório de armazenamento criado: $rootLocation")
            } else {
                logger.info("Usando diretório de armazenamento existente: $rootLocation")
            }
        } catch (ex: IOException) {
            throw StorageException("Não foi possível criar o diretório para armazenar os arquivos: $rootLocation", ex)
        }
    }

    /**
     * Armazena um arquivo enviado via MultipartFile no sistema de arquivos
     */
    fun store(file: MultipartFile): String {
        val filename = StringUtils.cleanPath(file.originalFilename ?: "")
        logger.debug("Armazenando arquivo: $filename")

        try {
            if (file.isEmpty) {
                throw StorageException("Falha ao armazenar arquivo vazio: $filename")
            }

            if (filename.contains("..")) {
                throw StorageException("Tentativa de acesso a diretório pai não permitida: $filename")
            }

            val uniqueFilename = generateUniqueFilename(filename)
            val targetLocation = rootLocation.resolve(uniqueFilename)

            logger.debug("Copiando arquivo para: $targetLocation")
            Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)

            logger.info("Arquivo armazenado com sucesso: $uniqueFilename")
            return targetLocation.toString()
        } catch (e: IOException) {
            throw StorageException("Falha ao armazenar o arquivo $filename", e)
        }
    }

    fun store(bytes: ByteArray, filename: String): String {
        logger.debug("Armazenando arquivo a partir de bytes: $filename")

        try {
            if (filename.contains("..")) {
                throw StorageException("Tentativa de acesso a diretório pai não permitida: $filename")
            }
            
            val uniqueFilename = generateUniqueFilename(filename)
            val targetLocation = rootLocation.resolve(uniqueFilename)

            // Verifica se o caminho resolvido está dentro do diretório raiz
            if (!targetLocation.normalize().startsWith(rootLocation)) {
                throw StorageException("Tentativa de acesso a localização não permitida: $filename")
            }

            logger.debug("Escrevendo ${bytes.size} bytes em: $targetLocation")
            Files.createDirectories(targetLocation.parent)
            Files.write(targetLocation, bytes)

            logger.info("Arquivo armazenado com sucesso: $uniqueFilename (${bytes.size} bytes)")
            return targetLocation.toString()
        } catch (e: IOException) {
            throw StorageException("Falha ao armazenar o arquivo $filename", e)
        }
    }

    fun loadAsResource(filename: String): Resource {
        logger.debug("Carregando recurso: $filename")

        try {
            val file = load(filename)
            val resource = UrlResource(file.toUri())

            if (resource.exists() && resource.isReadable) {
                logger.debug("Recurso carregado com sucesso: $filename")
                return resource
            } else {
                throw StorageException("Arquivo existe mas não pode ser lido: $filename")
            }
        } catch (e: MalformedURLException) {
            throw StorageException("Arquivo não encontrado: $filename", e)
        }
    }

    fun loadAsBytes(filename: String): ByteArray {
        logger.debug("Carregando arquivo como bytes: $filename")

        return try {
            val file = load(filename)
            Files.readAllBytes(file)
        } catch (e: IOException) {
            throw StorageException("Falha ao ler o arquivo: $filename", e)
        }
    }

    fun delete(filename: String) {
        logger.info("Solicitada exclusão do arquivo: $filename")

        try {
            val file = try {
                load(filename)
            } catch (e: StorageException) {
                if (e.message?.contains("não encontrado") == true) {
                    logger.info("Arquivo não encontrado, ignorando exclusão: $filename")
                    return
                }
                throw e
            }
            
            if (Files.exists(file)) {
                Files.deleteIfExists(file)
                logger.info("Arquivo excluído com sucesso: $filename")
            } else {
                logger.info("Arquivo não existe, ignorando exclusão: $filename")
            }
        } catch (e: IOException) {
            throw StorageException("Falha ao excluir o arquivo: $filename", e)
        }
    }

    private fun load(filename: String): Path {
        if (filename.contains("..")) {
            throw StorageException("Tentativa de acesso a diretório pai não permitida: $filename")
        }

        val file = rootLocation.resolve(filename).normalize()

        if (!file.startsWith(rootLocation)) {
            throw StorageException("Tentativa de acesso a localização não permitida: $filename")
        }

        if (!Files.exists(file)) {
            throw StorageException("Arquivo não encontrado: $filename")
        }

        return file
    }

    private fun generateUniqueFilename(originalFilename: String): String {
        require(originalFilename.isNotBlank()) { "O nome do arquivo não pode estar vazio" }
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString().substring(0, 8)

        val extension = originalFilename
            .substringAfterLast('.')
            .takeIf { it != originalFilename }
            ?.takeIf { it.length <= 10 }
            ?.lowercase()
            ?: "bin"

        val safeName = (originalFilename
            .substringBeforeLast('.')
            .takeIf { it != originalFilename }
            ?: "file_$timestamp")
            .replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            .take(100)

        return "${safeName}_${timestamp}_${uuid}.${extension}"
    }
}