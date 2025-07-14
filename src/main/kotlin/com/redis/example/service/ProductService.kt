package com.redis.example.service

import com.redis.example.common.TransactionSupport
import com.redis.example.repository.ProductRepository
import jakarta.persistence.EntityNotFoundException
import org.redisson.api.RedissonClient
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val redissonClient: RedissonClient,
    private val transactionSupport: TransactionSupport,
) {

    @Transactional
    fun decreaseQuantity(productId: Long, quantity: Long) {
        val product = productRepository.findByIdOrNull(productId)
            ?: throw EntityNotFoundException("차감하려는 상품이 없습니다.")

        product.decrease(quantity)
    }

    @Transactional
    fun decreaseQuantityWithPessimisticLock(productId: Long, quantity: Long) {
        val product = productRepository.findProductWithPessimisticLock(productId)
            ?: throw EntityNotFoundException("차감하려는 상품이 없습니다.")

        product.decrease(quantity)
    }

    /**
     *  트랜잭션 안에서 Lock 사용
     */
    @Transactional
    fun decreaseQuantityWithRedissonLock(productId: Long, quantity: Long) {
        val lock = redissonClient.getLock(productId.toString())

        lock.tryLock(3, 3, TimeUnit.SECONDS)

        val product = productRepository.findByIdOrNull(productId)
            ?: throw EntityNotFoundException("차감하려는 상품이 없습니다.")

        product.decrease(quantity)

        lock.unlock()
    }

    /**
     *  Lock 안에서 새로운 트랜잭션 추가
     */
    fun decreaseQuantityWithRedissonLockWithNewTransaction(productId: Long, quantity: Long) {
        val lock = redissonClient.getLock(productId.toString())

        lock.tryLock(3, 3, TimeUnit.SECONDS)

        transactionSupport.runWithNewTransaction {
            val product = productRepository.findProductWithPessimisticLock(productId)
                ?: throw EntityNotFoundException("차감하려는 상품이 없습니다.")

            product.decrease(quantity)
        }

        lock.unlock()
    }
}
