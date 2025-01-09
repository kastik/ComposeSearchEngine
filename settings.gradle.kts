rootProject.name = "ComposeSearchEngine"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("de.cschubertcs")
            }


        }
        mavenCentral()
        gradlePluginPortal()

    }



}

dependencyResolutionManagement {

    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()

    }

}

include(":searchengineui")
include(":searchengine")
include(":searchenginedocs")
