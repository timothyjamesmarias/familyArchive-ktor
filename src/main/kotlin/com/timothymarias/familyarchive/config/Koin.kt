package com.timothymarias.familyarchive.config

import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(appModule(this@configureKoin))
    }
}

fun appModule(application: Application) = module {
    // Config values
    val config = application.environment.config

    // Repositories will be registered here in Phase 2

    // Services will be registered here in Phase 3

    // Storage service (conditional) will be registered here in Phase 3
}
