package com.redis.example.common

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class TransactionSupport {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun <T> runWithNewTransaction(action: () -> T): T = action()
}