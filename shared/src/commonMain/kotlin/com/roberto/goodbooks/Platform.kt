package com.roberto.goodbooks

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform