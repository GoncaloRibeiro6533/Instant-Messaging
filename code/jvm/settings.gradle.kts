plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "talkRooms"


include("domain")
include("service")
include("repository")
include("http-api")
include("repository-jdbi")
