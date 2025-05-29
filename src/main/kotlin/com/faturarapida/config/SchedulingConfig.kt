package com.faturarapida.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Configuração para habilitar o agendamento de tarefas no Spring
 */
@Configuration
@EnableScheduling
class SchedulingConfig
