package com.faturarapida.service.storage

import com.faturarapida.exception.StorageException
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.io.Resource
import org.springframework.mock.web.MockMultipartFile
import org.springframework.util.FileSystemUtils
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.*
import java.util.*

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StorageServiceTest {

    private val testRootDir = Paths.get("build/test-storage")
    private lateinit var storageService: StorageService
    
    @BeforeEach
    fun setUp() {
        // Limpa o diretório de teste antes de cada teste
        FileSystemUtils.deleteRecursively(testRootDir.toFile())
        Files.createDirectories(testRootDir)
        
        // Cria uma instância do serviço com o diretório de teste
        storageService = StorageService(testRootDir.toString())
    }
    
    @AfterEach
    fun tearDown() {
        try {
            FileSystemUtils.deleteRecursively(testRootDir)
        } catch (e: IOException) {
            // Ignora erros de limpeza
        }
    }
    
    @Test
    fun `store should save file and return path`() {
        // Arrange
        val filename = "test.txt"
        val content = "Hello, World!"
        val file = MockMultipartFile(
            "file",
            filename,
            "text/plain",
            content.toByteArray()
        )
        
        // Act
        val result = storageService.store(file)
        
        // Assert
        assertThat(result).isNotEmpty
        assertThat(File(result)).exists()
        assertThat(File(result).readText()).isEqualTo(content)
    }
    
    @Test
    fun `store should generate unique filename`() {
        // Arrange
        val filename = "test.txt"
        val content = "Hello, World!"
        val file1 = MockMultipartFile("file", filename, "text/plain", content.toByteArray())
        val file2 = MockMultipartFile("file", filename, "text/plain", content.toByteArray())
        
        // Act
        val result1 = storageService.store(file1)
        val result2 = storageService.store(file2)
        
        // Assert
        assertThat(result1).isNotEqualTo(result2)
        assertThat(File(result1)).exists()
        assertThat(File(result2)).exists()
    }
    
    @Test
    fun `store should reject empty file`() {
        // Arrange
        val file = MockMultipartFile("file", "empty.txt", "text/plain", ByteArray(0))
        
        // Act & Assert
        assertThatThrownBy { storageService.store(file) }
            .isInstanceOf(StorageException::class.java)
            .hasMessageContaining("vazio")
    }
    
    @Test
    fun `loadAsResource should return resource for existing file`() {
        // Arrange
        val filename = "test.txt"
        val content = "Test content"
        val file = testRootDir.resolve(filename)
        Files.write(file, content.toByteArray())
        
        // Act
        val resource = storageService.loadAsResource(filename)
        
        // Assert
        assertThat(resource).isNotNull
        assertThat(resource.exists()).isTrue
        assertThat(resource.contentAsString(StandardCharsets.UTF_8)).isEqualTo(content)
    }
    
    @Test
    fun `loadAsResource should throw exception for non-existent file`() {
        // Act & Assert
        assertThatThrownBy { storageService.loadAsResource("nonexistent.txt") }
            .isInstanceOf(StorageException::class.java)
            .hasMessageContaining("não encontrado")
    }
    
    @Test
    fun `loadAsBytes should return file content as bytes`() {
        // Arrange
        val filename = "test.bin"
        val content = byteArrayOf(0x01, 0x02, 0x03)
        val file = testRootDir.resolve(filename)
        Files.write(file, content)
        
        // Act
        val result = storageService.loadAsBytes(filename)
        
        // Assert
        assertThat(result).isEqualTo(content)
    }
    
    @Test
    fun `delete should remove file`() {
        // Arrange
        val filename = "to-delete.txt"
        val file = testRootDir.resolve(filename)
        Files.write(file, "Delete me".toByteArray())
        
        // Act
        storageService.delete(filename)
        
        // Assert
        assertThat(file).doesNotExist()
    }
    
    @Test
    fun `delete should not throw for non-existent file`() {
        // This should not throw an exception
        assertDoesNotThrow { storageService.delete("nonexistent.txt") }
    }
    
    @Test
    fun `store with bytes should save file`() {
        // Arrange
        val filename = "test.bin"
        val content = byteArrayOf(0x01, 0x02, 0x03)
        
        // Act
        val result = storageService.store(content, filename)
        
        // Assert
        assertThat(File(result)).exists()
        assertThat(Files.readAllBytes(Paths.get(result))).isEqualTo(content)
    }
    
    @Test
    fun `should reject path traversal attempts`() {
        // Arrange
        val maliciousPath = "../outside.txt"
        val content = "Malicious content".toByteArray()
        
        // Act & Assert
        assertThatThrownBy { storageService.store(content, maliciousPath) }
            .isInstanceOf(StorageException::class.java)
            .hasMessageContaining("Tentativa de acesso a diretório pai não permitida")
    }
    
    @Test
    fun `should handle filenames with special characters`() {
        // Arrange
        val filename = "arquivo com espaços e çãõ.txt"
        val content = "Conteúdo com acentuação".toByteArray()
        
        // Act
        val result = storageService.store(content, filename)
        
        // Assert
        assertThat(File(result)).exists()
        assertThat(Files.readAllBytes(Paths.get(result))).isEqualTo(content)
    }
    
    private fun Resource.contentAsString(charset: java.nio.charset.Charset): String {
        return this.inputStream.bufferedReader(charset).use { it.readText() }
    }
}
