package com.faturarapida.repository

import com.faturarapida.model.Invoice
import com.faturarapida.model.InvoiceStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * Repositório para operações de banco de dados relacionadas a faturas
 */
@Repository
interface InvoiceRepository : JpaRepository<Invoice, Long> {
    
    /**
     * Busca faturas por status com paginação
     * @param status Status das faturas a serem buscadas
     * @param pageable Configuração de paginação
     * @return Página de faturas com o status especificado
     */
    fun findByStatus(status: InvoiceStatus, pageable: Pageable): Page<Invoice>
    
    /**
     * Busca faturas vencidas antes de uma determinada data e com status diferente do especificado
     * @param data Data de vencimento máxima
     * @param status Status a ser excluído da busca
     * @return Lista de faturas vencidas
     */
    @Query("""
        SELECT i FROM Invoice i 
        WHERE i.vencimento < :data 
        AND i.status <> :status
    """)
    fun findByVencimentoBeforeAndStatusNot(
        @Param("data") data: LocalDate,
        @Param("status") status: InvoiceStatus
    ): List<Invoice>
    
    /**
     * Busca faturas por cliente com paginação
     * @param cliente Nome ou parte do nome do cliente
     * @param pageable Configuração de paginação
     * @return Página de faturas do cliente
     */
    @Query("""
        SELECT i FROM Invoice i 
        WHERE LOWER(i.cliente) LIKE LOWER(CONCAT('%', :cliente, '%'))
    """)
    fun findByClienteContainingIgnoreCase(
        @Param("cliente") cliente: String,
        pageable: Pageable
    ): Page<Invoice>
    
    /**
     * Verifica se existe alguma fatura para um determinado cliente
     * @param cliente Nome do cliente
     * @return true se existir pelo menos uma fatura para o cliente
     */
    fun existsByCliente(cliente: String): Boolean
    
    /**
     * Conta o número de faturas por status
     * @param status Status das faturas a serem contadas
     * @return Número de faturas com o status especificado
     */
    fun countByStatus(status: InvoiceStatus): Long
}
