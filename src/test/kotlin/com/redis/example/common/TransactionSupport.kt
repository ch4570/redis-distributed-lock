package com.redis.example.common

import org.springframework.boot.test.context.TestComponent
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@TestComponent
class TransactionSupport {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun <T> startWithNewTransaction(action: () -> T): T = action()
}