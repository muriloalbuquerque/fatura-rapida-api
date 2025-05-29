package com.faturarapida.scheduler

import com.faturarapida.service.InvoiceService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Agendador para execução de tarefas periódicas relacionadas a faturas
 */
@Component
class InvoiceScheduler(
    private val invoiceService: InvoiceService
) {
    
    private val logger = LoggerFactory.getLogger(InvoiceScheduler::class.java)
    
    /**
     * Verifica e atualiza faturas vencidas diariamente às 2h da manhã
     */
    @Scheduled(cron = "0 0 2 * * ?") // Executa todos os dias às 2h da manhã
    fun verificarFaturasVencidas() {
        try {
            logger.info("Iniciando verificação agendada de faturas vencidas")
            val faturasAtualizadas = invoiceService.verificarFaturasVencidas()
            
            if (faturasAtualizadas > 0) {
                logger.info("Verificação concluída: $faturasAtualizadas fatura(s) atualizada(s) para status VENCIDA")
            } else {
                logger.debug("Verificação concluída: Nenhuma fatura vencida encontrada")
            }
        } catch (e: Exception) {
            logger.error("Erro ao verificar faturas vencidas: ${e.message}", e)
        }
    }
    
    // Outras tarefas agendadas podem ser adicionadas aqui
}
