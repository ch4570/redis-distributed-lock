package com.redis.example.service

import com.redis.example.common.AbstractServiceTest
import com.redis.example.common.TransactionSupport
import com.redis.example.entity.Product
import com.redis.example.repository.ProductRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import org.springframework.orm.ObjectOptimisticLockingFailureException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.Executors

internal class ProductServiceTest(
    private val productRepository: ProductRepository,
    private val productService: ProductService,
    private val transactionSupport: TransactionSupport,
) : AbstractServiceTest(
) {

    init {

        this.Given("[예외 케이스- Lock 미사용] - 동시에 재고를 차감하려는 상황에서") {
            val product = Product(
                productName = "라부부 인형",
                price = 800_000,
                stock = 100,
            )

            transactionSupport.startWithNewTransaction {
                productRepository.save(product)
            }

            When("100명이 1개씩 재고 차감을 동시에 시도하면") {
                val executor = Executors.newVirtualThreadPerTaskExecutor()

                val futures = (0 until 100).map {
                    CompletableFuture.runAsync(
                        { productService.decreaseQuantity(product.id, 1) },
                        executor
                    )
                }.toTypedArray()

                CompletableFuture.allOf(*futures).join()

                Then("차감된 개수는 100개가 되지 못한다.") {
                    val updatedProduct = productRepository.findByIdOrNull(product.id)
                        ?: throw EntityNotFoundException("Not Found")

                    println("update quantity = [${updatedProduct.stock}]")

                    updatedProduct.stock shouldNotBe 100
                }
            }
        }

        this.Given("[예외 케이스- Optimistic Lock 사용] - 동시에 재고를 차감하려는 상황에서") {
            val product = Product(
                productName = "라부부 인형",
                price = 800_000,
                stock = 100,
            )

            transactionSupport.startWithNewTransaction {
                productRepository.save(product)
            }

            When("100명이 1개씩 재고 차감을 동시에 시도하면") {
                val executor = Executors.newVirtualThreadPerTaskExecutor()

                val futures = (0 until 100).map {
                    CompletableFuture.runAsync(
                        { productService.decreaseQuantityWithOptimisticLock(product.id, 1) },
                        executor
                    )
                }.toTypedArray()

                Then("차감된 개수는 100개가 되지 못한다.") {
                    assertThrows<CompletionException> {
                        CompletableFuture.allOf(*futures).join()
                    }.cause is ObjectOptimisticLockingFailureException

                    val updatedProduct = productRepository.findByIdOrNull(product.id)
                        ?: throw EntityNotFoundException("Not Found")

                    println("update quantity = [${updatedProduct.stock}]")

                    updatedProduct.stock shouldNotBe 100
                }
            }
        }

        this.Given("[성공 케이스- Pessimistic Lock 사용] - 동시에 재고를 차감하려는 상황에서") {
            val product = Product(
                productName = "라부부 인형",
                price = 800_000,
                stock = 100,
            )

            transactionSupport.startWithNewTransaction {
                productRepository.save(product)
            }

            When("100명이 1개씩 재고 차감을 동시에 시도하면") {
                val executor = Executors.newVirtualThreadPerTaskExecutor()

                val futures = (0 until 100).map {
                    CompletableFuture.runAsync(
                        { productService.decreaseQuantityWithPessimisticLock(product.id, 1) },
                        executor
                    )
                }.toTypedArray()

                CompletableFuture.allOf(*futures).join()

                Then("차감된 개수는 100개가 되지 못한다.") {
                    val updatedProduct = productRepository.findByIdOrNull(product.id)
                        ?: throw EntityNotFoundException("Not Found")

                    println("update quantity = [${updatedProduct.stock}]")

                    updatedProduct.stock shouldBe 0
                }
            }
        }
    }
}