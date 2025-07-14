package com.redis.example.service

import com.redis.example.common.AbstractServiceTest
import com.redis.example.entity.Product
import com.redis.example.repository.ProductRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

internal class ProductServiceTest(
    private val productRepository: ProductRepository,
    private val productService: ProductService,
) : AbstractServiceTest(
) {

    init {

        this.Given("[예외 케이스- Lock 미사용] - 동시에 재고를 차감하려는 상황에서") {
            val product = Product(
                productName = "라부부 인형",
                price = 800_000,
                stock = 100,
            )


            productRepository.save(product)

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

            productRepository.save(product)

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

                    updatedProduct.stock shouldBe 0
                }
            }
        }

        this.Given("[예외 케이스- 트랜잭션 안에서 Redis Lock 사용] - 동시에 재고를 차감하려는 상황에서") {
            val product = Product(
                productName = "라부부 인형",
                price = 800_000,
                stock = 100,
            )

            productRepository.save(product)

            When("100명이 1개씩 재고 차감을 동시에 시도하면") {
                val executor = Executors.newVirtualThreadPerTaskExecutor()

                val futures = (0 until 100).map {
                    CompletableFuture.runAsync(
                        { productService.decreaseQuantityWithRedissonLock(product.id, 1) },
                        executor
                    )
                }.toTypedArray()

                CompletableFuture.allOf(*futures).join()

                Then("차감된 개수는 100개가 되지 못한다.") {
                    val updatedProduct = productRepository.findByIdOrNull(product.id)
                        ?: throw EntityNotFoundException("Not Found")

                    updatedProduct.stock shouldNotBe 0
                }
            }
        }

        this.Given("[정상 케이스- Redis Lock 안에서 트랜잭션 사용] - 동시에 재고를 차감하려는 상황에서") {
            val product = Product(
                productName = "라부부 인형",
                price = 800_000,
                stock = 100,
            )

            productRepository.save(product)

            When("100명이 1개씩 재고 차감을 동시에 시도하면") {
                val executor = Executors.newVirtualThreadPerTaskExecutor()

                val futures = (0 until 100).map {
                    CompletableFuture.runAsync(
                        { productService.decreaseQuantityWithRedissonLockWithNewTransaction(product.id, 1) },
                        executor
                    )
                }.toTypedArray()

                CompletableFuture.allOf(*futures).join()

                Then("차감된 개수는 100개가 되지 못한다.") {
                    val updatedProduct = productRepository.findByIdOrNull(product.id)
                        ?: throw EntityNotFoundException("Not Found")

                    updatedProduct.stock shouldBe 0
                }
            }
        }
    }
}
