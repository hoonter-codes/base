package com.example.controller

import com.example.domain.exceptions.CacheUsecaseException
import domain.usecases.CacheUsecase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

class GetCacheController {

    suspend fun invoke(call: ApplicationCall, domain: CacheUsecase) {
        val keyParam = call.parameters["key"]
        if (keyParam == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing key parameter")
            return
        }
        val key = try {
            keyParam.toInt()
        } catch (_: NumberFormatException) {
            call.respond(HttpStatusCode.BadRequest, "Invalid key format")
            return
        }

        val value: String? = try {
            domain.get(key)
        } catch (e: CacheUsecaseException) {
            call.respond(HttpStatusCode.InternalServerError, e.message)
            return
        }

        if (value == null) {
            call.respond(HttpStatusCode.NotFound, "Key not found")
        } else {
            call.respond(HttpStatusCode.OK, value)
        }
    }
}