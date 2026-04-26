package com.timothymarias.familyarchive

import com.timothymarias.familyarchive.config.configureAuth
import com.timothymarias.familyarchive.config.configureCors
import com.timothymarias.familyarchive.config.configureDatabase
import com.timothymarias.familyarchive.config.configureKoin
import com.timothymarias.familyarchive.config.configureRouting
import com.timothymarias.familyarchive.config.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.calllogging.CallLogging
import org.slf4j.event.Level

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(CallLogging) {
        level = Level.INFO
    }
    install(AutoHeadResponse)

    configureKoin()
    configureDatabase()
    configureSerialization()
    configureCors()
    configureAuth()
    configureRouting()
}
