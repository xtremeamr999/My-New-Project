
plugins {
    id("fabric-loom")
    id("maven-publish")
    `maven-publish`
    java

//	id 'org.jetbrains.kotlin.jvm' version '2.0.0'
}
group = property("maven_group")!!
version = property("mod_version") as String + "+mc" + property("deps.core.mcVersion") as String

base { archivesName.set(property("mod.id").toString()) }
repositories {
    maven { url = uri("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") }
    maven {
        name = "meteor-maven"
        url = uri("https://maven.meteordev.org/releases")
    }
    maven {
        name = "Hypixel"
        url = uri("https://repo.hypixel.net/repository/Hypixel/")
    }
    maven {
        name = "YACL"
        url = uri("https://maven.isxander.dev/releases")
    }
    maven {
        name = "Terraformers (for gui)"
        url = uri("https://maven.terraformersmc.com/")
    }

    exclusiveContent {
        forRepository {
            maven {
                url = uri("https://cursemaven.com")
                name = "CurseMaven" // Repository name is often required for exclusiveContent
            }
        }
        filter {
            includeGroup("curse.maven")
        }
        forRepository {
            maven {
                url = uri("https://api.modrinth.com/maven")
                name = "Modrinth"
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }

    mavenCentral() // Good practice to include mavenCentral
}

//class Env {
//    val mcVersion = get("core.mc.version_range")
//    val fabricLoaderVersion = get("core.fabric.loader.version_range")
//    operator fun get(name: String) = property("deps.$name").toString()
//}
//class ModProperties {
//    val id = property("mod.id").toString()
//    val displayName = property("mod.display_name").toString()
//    val version = property("version").toString()
//    val description = property("mod.description")
//    val authors = property("mod.authors").toString()
//    val license = property("mod.license")
//    val sourceUrl = property("mod.source_url")
//    val generalWebsite = property("mod.general_website")
//}
class ModDependencies {
    operator fun get(name: String) = property("deps.$name").toString()
}
//val env = Env()
val deps = ModDependencies()
//val modProperties = ModProperties()
dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${deps["core.mcVersion"]}")
    mappings("net.fabricmc:yarn:${deps["core.mcVersion"]}+build.${deps["yarn_build"]}:v2")
    modImplementation("net.fabricmc:fabric-loader:${deps["fabricLoaderVersion"]}")
//    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")

    implementation("meteordevelopment:orbit:0.2.4")
    include("meteordevelopment:orbit:0.2.4")

    modImplementation("net.hypixel:hypixel-api-transport-apache:4.4")
    include("net.hypixel:hypixel-api-transport-apache:4.4")
    include("net.hypixel:hypixel-api-core:4.4")

    modImplementation("dev.isxander:yet-another-config-lib:${deps["yacl_version"]}")

    modImplementation("com.terraformersmc:modmenu:${property("modmenu_version")}")

    // Project Lombok
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    testCompileOnly("org.projectlombok:lombok:1.18.36")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.36")

    // Modern Keybinding
//    modImplementation("curse.maven:modernkeybinding-695433:6016136")
//    include("curse.maven:modernkeybinding-695433:6016136")

    //Amecs Reborn
    modCompileOnly("maven.modrinth:amecs-reborn:${property("amecsreborn_version")}+mc${deps["core.mcVersion"]}")

    // Mixin Constraints
    include(implementation("com.moulberry:mixinconstraints:1.0.8")!!)


    // Skyblocker for compatibility
//    modCompileOnly("maven.modrinth:y6DuFGwJ:v${property("skyblocker_version")}+${deps["core.mcVersion"]}")
    modCompileOnly("maven.modrinth:skyblocker-liap:v5.2.0+1.21.5")

}
tasks {

    processResources {
        inputs.property("version", project.version)
        inputs.property("minecraft", stonecutter.current.version)

        filesMatching("fabric.mod.json") {
            expand(getProperties())
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE"){
            rename { "${it}_${archiveBaseName.get()}" }
        }
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifact(remapJar) {
                    builtBy(remapJar)
                }
            }
        }

        // select the repositories you want to publish to
        repositories {
            // uncomment to publish to the local maven
            // mavenLocal()
        }
    }


}
loom {
    runConfigs.all {
        ideConfigGenerated(true) // Run configurations are not created for subprojects by default
        runDir = "../../run" // Use a shared run folder and create separate worlds
    }
}
java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}