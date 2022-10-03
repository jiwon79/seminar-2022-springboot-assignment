package com.wafflestudio.seminar.user.database

import javax.persistence.*

@Entity
class UserEntity(
    val name: String,
    val email: String,
    val password: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
}