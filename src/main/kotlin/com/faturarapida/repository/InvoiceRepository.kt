package com.faturarapida.repository

import com.faturarapida.model.Invoice
import org.springframework.data.jpa.repository.JpaRepository

interface InvoiceRepository : JpaRepository<Invoice, Long>
