package com.redis.example.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version

@Entity
@Table(name = "product")
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    val productName: String,

    val price: Long,

    var stock: Long = 0,

    @Version
    var version: Long = 0,

): BaseEntity() {

    fun decrease(quantity: Long) {
        this.stock -= quantity
    }
}