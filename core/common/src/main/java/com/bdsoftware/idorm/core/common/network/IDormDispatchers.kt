package com.bdsoftware.idorm.core.common.network

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: IDormDispatchers)

enum class IDormDispatchers {
    Default,
    Main,
    IO,
}
