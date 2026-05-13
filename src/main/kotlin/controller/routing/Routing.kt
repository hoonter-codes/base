package com.example.controller.routing

import com.example.controller.PutCacheController
import com.example.controller.SetCacheSizeController
import com.example.controller.SwitchStrategyController
import com.example.controller.GetCacheController
import io.ktor.server.application.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import registry.CacheUsecaseRegistry

fun Application.configureRouting() {

    val domain = CacheUsecaseRegistry.getInstance()

    routing {
        get("/") {
            call.respond("Hello World!")
        }

        get("/get") {
            GetCacheController().invoke(call, domain)
        }

        post("/put") {
            PutCacheController().invoke(call, domain)
        }

        post("/set/size/{size}") {
            SetCacheSizeController().invoke(call, domain)
        }

        post("/switch/strategy") {
            SwitchStrategyController().invoke(call, domain)
        }
    }
}