pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "iDorm"
include(":app")

include(":core:network")
include(":core:model")
include(":core:data")
include(":core:common")
include(":core:datastore")
include(":sync")
include(":core:domain")
include(":core:designsystem")
include(":core:ui")
include(":feature:auth")
include(":feature:home")
include(":feature:profile")
include(":feature:feedback")
include(":feature:hcmc")
include(":feature:payment")
include(":feature:invoice")
include(":feature:notification")
include(":feature:rent")
include(":feature:wificonfig")
include(":feature:account")



