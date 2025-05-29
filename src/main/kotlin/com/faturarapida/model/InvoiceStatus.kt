package com.faturarapida.model

/**
 * Enum que representa os possíveis status de uma fatura
 */
enum class InvoiceStatus(val descricao: String) {
    EMITIDA("Emitida"),
    PAGA("Paga"),
    VENCIDA("Vencida"),
    CANCELADA("Cancelada");

    companion object {
        fun fromString(status: String): InvoiceStatus {
            return values().find { it.name == status } 
                ?: throw IllegalArgumentException("Status de fatura inválido: $status")
        }
    }
}
