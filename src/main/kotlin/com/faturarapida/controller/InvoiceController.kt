package com.faturarapida.controller

import com.faturarapida.dto.InvoiceRequest
import com.faturarapida.dto.InvoiceResponse
import com.faturarapida.exception.InvoiceGenerationException
import com.faturarapida.model.Invoice
import com.faturarapida.service.InvoiceService
import jakarta.validation.Valid
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api/invoices")
class InvoiceController(
    private val invoiceService: InvoiceService
) {

    @PostMapping
    fun criarFatura(@Valid @RequestBody request: InvoiceRequest): ResponseEntity<InvoiceResponse> {
        try {
            val response = invoiceService.criarFatura(request)
            return ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: InvoiceGenerationException) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro ao gerar a fatura: ${e.message}",
                e
            )
        }
    }

    @GetMapping
    fun listarFaturas(): List<Invoice> = invoiceService.listarTodas()
    
    @GetMapping("/{id}")
    fun buscarFaturaPorId(@PathVariable id: Long): ResponseEntity<Invoice> {
        return try {
            val invoice = invoiceService.buscarPorId(id)
            ResponseEntity.ok(invoice)
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro ao buscar fatura",
                e
            )
        }
    }
    
    @GetMapping("/{id}/pdf", produces = [MediaType.APPLICATION_PDF_VALUE])
    fun baixarPdfFatura(@PathVariable id: Long): ResponseEntity<Resource> {
        return try {
            val resource = invoiceService.obterPdf(id)
            val invoice = invoiceService.buscarPorId(id)
            
            val filename = "fatura_${invoice.id}.pdf"
            val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
            
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"; filename*=UTF-8''$encodedFilename")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .body(resource)
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro ao baixar o PDF da fatura",
                e
            )
        }
    }
    
    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(ex: ResponseStatusException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(ex.statusCode).body(mapOf("message" to (ex.reason ?: "Erro desconhecido")))
    }
    
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(mapOf("message" to "Ocorreu um erro inesperado: ${ex.message}"))
    }
}
