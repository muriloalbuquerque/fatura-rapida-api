package com.faturarapida.exception

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

/**
 * Exceção lançada quando uma fatura não é encontrada
 * @param id ID da fatura não encontrada
 */
class InvoiceNotFoundException(id: Long) : 
    ResponseStatusException(HttpStatus.NOT_FOUND, "Fatura com ID $id não encontrada")
