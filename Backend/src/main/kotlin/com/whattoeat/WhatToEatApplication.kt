package com.whattoeat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WhatToEatApplication

fun main(args: Array<String>) {
    runApplication<WhatToEatApplication>(*args)
}
