package com.faturarapida.exception

/**
 * Exceção lançada quando ocorre um erro no armazenamento de arquivos
 */
class StorageException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
