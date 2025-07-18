enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "twitterx"

include(":app")

include(":twitter:api")
include(":twitter:nitter")
include(":twitter:fx")
include(":twitter:impl")
include(":twitter:e2e")

include(":translations:api")
include(":translations:google")
include(":translations:qween")
include(":translations:e2e")

include(":video:api")
include(":video:ytdlp")
include(":video:e2e")

include(":article:api")
include(":article:telegraph")
include(":article:e2e")

include(":localization:api")
include(":localization:impl")

include(":telegram")

include(":ai:api")
include(":ai:google")
include(":ai:e2e")
