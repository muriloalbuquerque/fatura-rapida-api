package com.faturarapida.controller

import com.faturarapida.repository.InvoiceRepository
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/invoices")
class InvoiceController(
    private val invoiceService: InvoiceService
) {

    @PostMapping
    fun criarFatura(@Valid @RequestBody request: InvoiceRequest): ResponseEntity<Any> {
        val invoice = invoiceService.criarFatura(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice)
    }

    @GetMapping
    fun listarFaturas(): List<Invoice> = invoiceService.listarTodas()
}
