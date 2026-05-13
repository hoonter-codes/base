package com.example.controller

import com.example.domain.exceptions.CacheUsecaseException
import domain.usecases.CacheUsecase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

class PutCacheController {

    suspend fun invoke(call: ApplicationCall, domain: CacheUsecase) {
        val keyParam = call.parameters["key"]?.toInt()

        if (keyParam == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing key parameter")
            return
         }

        val valueParam = call.parameters["value"]
        if (valueParam == null || valueParam.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, "Missing value parameter")
            return
        }

        try {
            domain.put(keyParam, valueParam)
        } catch (e: CacheUsecaseException) {
            call.respond(HttpStatusCode.InternalServerError, e.message)
            return
        }
        call.respond(HttpStatusCode.OK)
    }
}