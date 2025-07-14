package com.redis.example.entity

import jakarta.persistence.*

@Entity
@Table(name = "product")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    val productName: String,

    val price: Long,

    var stock: Long = 0,

): BaseEntity() {

    fun decrease(quantity: Long) {
        this.stock -= quantity
    }
}
