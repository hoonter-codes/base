package com.example.controller

import domain.usecases.CacheUsecase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

class SwitchStrategyController {

    suspend fun invoke(call: ApplicationCall, domain: CacheUsecase) {
        domain.switchStrategy()
        call.respond(HttpStatusCode.OK, "")
    }
}