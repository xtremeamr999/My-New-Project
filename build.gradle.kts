
plugins {
    id("fabric-loom")
    id("maven-publish")
    `maven-publish`
    java
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"

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
    maven("https://moulberry.repo.ax/v1") {
        name = "Moulberry's Maven"
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

    mavenCentral()
}

class ModDependencies {
    operator fun get(name: String) = property("deps.$name").toString()
}
//val env = Env()
val deps = ModDependencies()
//val modProperties = ModProperties()
val mcVersion = deps.get("core.mcVersion").toString()

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${mcVersion}")
    mappings("net.fabricmc:yarn:${mcVersion}+build.${deps["yarn_build"]}:v2")
    modImplementation("net.fabricmc:fabric-loader:${deps["fabricLoaderVersion"]}")
//    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")

    implementation("meteordevelopment:orbit:0.2.4")
    include("meteordevelopment:orbit:0.2.4")

    modImplementation("net.hypixel:hypixel-api-transport-apache:4.4")
    include("net.hypixel:hypixel-api-transport-apache:4.4")
    include("net.hypixel:hypixel-api-core:4.4")

    modImplementation("dev.isxander:yet-another-config-lib:${deps["yacl_version"]}")

    modCompileOnly("com.terraformersmc:modmenu:${property("modmenu_version")}")

    // Project Lombok
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    testCompileOnly("org.projectlombok:lombok:1.18.36")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.36")

    //Amecs Reborn
    modCompileOnly("maven.modrinth:amecs-reborn:${property("amecsreborn_version")}+mc${mcVersion}")

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
        inputs.property("mcVersion", mcVersion)

        filesMatching("fabric.mod.json") {
            val expansionProps = project.properties.toMutableMap()

            expansionProps["mcVersion"] = mcVersion
            expansionProps["version"] = project.version

            expand(expansionProps)
        }
    }

    jar {
        from("LICENSE"){
            rename { "${it}_${archiveBaseName.get()}" }
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
publishMods {
    file = tasks.remapJar.get().archiveFile
    additionalFiles.from(tasks.remapSourcesJar.get().archiveFile)
    changelog = "Changelog"
    type = BETA
    modLoaders.add("fabric")
    changelog = rootProject.file("UPDATES.md").readText()
    displayName = "Bazaar Utils"
    dryRun = true

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = "c4u7nzUZ"
        minecraftVersions.add("1.21.1")
        minecraftVersions.add("1.21.4")
        minecraftVersions.add("1.21.5")

        requires {
            id = "P7dR8mSH"
            slug = "fabric-api"
        }
        requires {
            id = "1eAoo2KR"
            slug = "yacl"
        }
        optional {
            id = "mOgUt4GM"
            slug = "modmenu"
        }
        optional {
            id = "IjgEpZeq"
            slug = "amecs-reborn"
        }
    }
    github {
        accessToken = providers.environmentVariable("GITHUB_TOKEN")
        repository = "Bazaar Utils"
        commitish = "modern"
    }
}