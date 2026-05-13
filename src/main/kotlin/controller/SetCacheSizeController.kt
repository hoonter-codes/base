package com.example.controller

import com.example.domain.exceptions.CacheUsecaseException
import domain.usecases.CacheUsecase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import registry.CacheUsecaseRegistry

class SetCacheSizeController {

    suspend fun invoke(call: ApplicationCall, domain: CacheUsecase) {
        val sizeParam = call.parameters["size"]?.toInt()
        if (sizeParam == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing size parameter")
            return
        }

        try {
            domain.setSize(sizeParam)
        } catch (e: CacheUsecaseException) {
            call.respond(HttpStatusCode.UnprocessableEntity, e.message)
        }
        call.respond(HttpStatusCode.OK, "Size is updated")
    }
}