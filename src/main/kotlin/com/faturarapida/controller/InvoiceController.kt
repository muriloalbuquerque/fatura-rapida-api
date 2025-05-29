package com.faturarapida.controller

import com.faturarapida.dto.CreateInvoiceRequest
import com.faturarapida.dto.InvoiceResponse
import com.faturarapida.exception.InvoiceGenerationException
import com.faturarapida.model.Invoice
import com.faturarapida.model.InvoiceStatus
import com.faturarapida.service.InvoiceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Controlador responsável por gerenciar as operações relacionadas a faturas
 */
@Tag(name = "Faturas", description = "API para gerenciamento de faturas")
@RestController
@RequestMapping("/faturas", produces = [MediaType.APPLICATION_JSON_VALUE])
class InvoiceController(
    private val invoiceService: InvoiceService
) {
    
    private val logger = LoggerFactory.getLogger(InvoiceController::class.java)
    
    @Operation(summary = "Cria uma nova fatura")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "201",
            description = "Fatura criada com sucesso",
            content = [
                (Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = InvoiceResponse::class)
                ))
            ]
        ),
        ApiResponse(responseCode = "400", description = "Dados da fatura inválidos"),
        ApiResponse(responseCode = "500", description = "Erro interno ao processar a fatura")
    ])
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun criarFatura(
        @Valid @RequestBody request: CreateInvoiceRequest,
        uriBuilder: UriComponentsBuilder
    ): ResponseEntity<InvoiceResponse> {
        logger.info("Recebida requisição para criar fatura para o cliente: ${request.cliente}")
        
        return try {
            val response = invoiceService.criarFatura(request)
            val uri = uriBuilder.path("/api/faturas/{id}").buildAndExpand(response.id).toUri()
            
            logger.info("Fatura criada com sucesso - ID: ${response.id}")
            
            ResponseEntity.created(uri).body(response)
        } catch (e: InvoiceGenerationException) {
            logger.error("Erro ao gerar fatura: ${e.message}", e)
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro ao gerar a fatura: ${e.message}",
                e
            )
        } catch (e: Exception) {
            logger.error("Erro inesperado ao criar fatura", e)
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro inesperado ao processar a fatura",
                e
            )
        }
    }
    
    @Operation(summary = "Lista todas as faturas com paginação")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Lista de faturas retornada com sucesso"
        )
    ])
    @GetMapping
    fun listarFaturas(
        @PageableDefault(size = 20, sort = ["dataCriacao"])
        @Parameter(hidden = true)
        pageable: Pageable
    ): ResponseEntity<Page<Invoice>> {
        logger.debug("Listando faturas - página ${pageable.pageNumber}, tamanho ${pageable.pageSize}")
        val faturas = invoiceService.listarTodas(pageable)
        return ResponseEntity.ok(faturas)
    }
    
    @Operation(summary = "Busca uma fatura pelo ID")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Fatura encontrada",
            content = [
                (Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Invoice::class)
                ))
            ]
        ),
        ApiResponse(responseCode = "404", description = "Fatura não encontrada")
    ])
    @GetMapping("/{id}")
    fun buscarFaturaPorId(
        @Parameter(description = "ID da fatura a ser buscada")
        @PathVariable id: Long
    ): ResponseEntity<Invoice> {
        logger.debug("Buscando fatura com ID: $id")
        val invoice = invoiceService.buscarPorId(id)
        return ResponseEntity.ok(invoice)
    }
    
    @Operation(summary = "Lista faturas por status")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Lista de faturas retornada com sucesso"
        )
    ])
    @GetMapping("/status/{status}")
    fun listarFaturasPorStatus(
        @Parameter(description = "Status das faturas a serem listadas")
        @PathVariable status: InvoiceStatus,
        @PageableDefault(size = 20, sort = ["dataCriacao"])
        @Parameter(hidden = true)
        pageable: Pageable
    ): ResponseEntity<Page<Invoice>> {
        logger.debug("Listando faturas com status: $status")
        val faturas = invoiceService.listarPorStatus(status, pageable)
        return ResponseEntity.ok(faturas)
    }
    
    @Operation(summary = "Atualiza o status de uma fatura")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Status da fatura atualizado com sucesso"
        ),
        ApiResponse(responseCode = "404", description = "Fatura não encontrada")
    ])
    @PatchMapping("/{id}/status/{novoStatus}")
    fun atualizarStatusFatura(
        @Parameter(description = "ID da fatura")
        @PathVariable id: Long,
        @Parameter(description = "Novo status da fatura")
        @PathVariable novoStatus: InvoiceStatus
    ): ResponseEntity<InvoiceResponse> {
        logger.info("Atualizando status da fatura $id para $novoStatus")
        val response = invoiceService.atualizarStatus(id, novoStatus)
        return ResponseEntity.ok(response)
    }
    
    @Operation(summary = "Baixa o PDF de uma fatura")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "PDF da fatura retornado com sucesso",
            content = [
                (Content(
                    mediaType = "application/pdf",
                    schema = Schema(type = "string", format = "binary")
                ))
            ]
        ),
        ApiResponse(responseCode = "404", description = "Fatura não encontrada"),
        ApiResponse(responseCode = "500", description = "Erro ao gerar o PDF")
    ])
    @GetMapping(
        value = ["/{id}/pdf"],
        produces = [MediaType.APPLICATION_PDF_VALUE]
    )
    fun baixarPdfFatura(
        @Parameter(description = "ID da fatura")
        @PathVariable id: Long
    ): ResponseEntity<ByteArray> {
        logger.debug("Solicitado download do PDF da fatura: $id")
        
        return try {
            val pdfBytes = invoiceService.obterPdf(id)
            val invoice = invoiceService.buscarPorId(id)
            
            val filename = "fatura_${invoice.id}.pdf"
            val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
            
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"; filename*=UTF-8''$encodedFilename")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes)
        } catch (e: Exception) {
            logger.error("Erro ao baixar PDF da fatura $id", e)
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro ao baixar o PDF da fatura",
                e
            )
        }
    }
    
    @Operation(summary = "Verifica e atualiza faturas vencidas")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Verificação de faturas vencidas concluída",
            content = [
                (Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = Map::class)
                ))
            ]
        )
    ])
    @PostMapping("/verificar-vencidas")
    fun verificarFaturasVencidas(): ResponseEntity<Map<String, Any>> {
        logger.info("Iniciando verificação de faturas vencidas")
        val faturasAtualizadas = invoiceService.verificarFaturasVencidas()
        
        return ResponseEntity.ok(
            mapOf(
                "message" to "Verificação de faturas vencidas concluída",
                "faturasAtualizadas" to faturasAtualizadas
            )
        )
    }
    
    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(ex: ResponseStatusException): ResponseEntity<Map<String, String>> {
        val reason = ex.reason ?: "Erro desconhecido"
        logger.warn("Erro na requisição: $reason")
        val body = mapOf(
            "status" to ex.statusCode.value().toString(),
            "error" to reason,
            "message" to reason
        )
        return ResponseEntity.status(ex.statusCode).body(body)
    }
    
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<Map<String, String>> {
        logger.error("Erro inesperado: ${ex.message}", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(mapOf(
                "status" to HttpStatus.INTERNAL_SERVER_ERROR.value().toString(),
                "error" to "Erro interno do servidor",
                "message" to "Ocorreu um erro inesperado: ${ex.message}"
            ))
    }
}
