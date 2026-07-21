package com.wafa.miserenim

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@OpenAPIDefinition(
    info =
        Info(
            title = "Misère Nim API",
            version = "v1",
            description = "Play one-heap Misère Nim against the computer.",
        ),
)
@SpringBootApplication
class MisereNimApiApplication

fun main(args: Array<String>) {
    runApplication<MisereNimApiApplication>(*args)
}
