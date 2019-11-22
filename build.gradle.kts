import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("fabric-loom")
    `maven-publish`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.wynprice.cursemaven") version "1.2.2"
    id("com.matthewprenger.cursegradle") version "1.4.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
fun prop(name: String): String = project.property(name).toString()
val minecraft_version: String by project
val mod_name: String by project

val yarn_mappings: String by project
val loader_version: String by project
val archives_base_name: String by project

val mod_version = prop("mod_version") + "-" + minecraft_version
val maven_group: String by project
val fabric_version: String by project
val fabric_language_kotlin_version: String by project
val loom_version: String by project
val kotlin_version: String by project
val lba_version: String by project
val cardinal_energy_version: String by project
val cardinal_api_version: String by project
val libgui_version: String by project
val rei_version: String by project
val waila_version: String by project
val drawer_version: String by project
val fabric_keybindings_version: String by project
val curseforge_api_key: String by project
val scheduler_version: String by project





base {
    archivesBaseName = archives_base_name
}

version = mod_version
group = maven_group

minecraft {
}

repositories {
    mavenLocal()
    maven(url = "http://maven.fabricmc.net/")
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://mod-buildcraft.com/maven")
    maven(url = "https://minecraft.curseforge.com/api/maven")
    maven(url = "http://tehnut.info/maven")
    maven(url = "https://maven.jamieswhiteshirt.com/libs-release/")
    maven(url = "http://server.bbkr.space:8081/artifactory/libs-snapshot")
    maven(url = "https://maven.abusedmaster.xyz")
    maven(url = "http://server.bbkr.space:8081/artifactory/libs-release/")
    maven(url = "https://dl.bintray.com/shedaniel/autoconfig1u/")
    jcenter()

}


dependencies {

    fabric()

    modDependency("alexiil.mc.lib:libblockattributes-items:$lba_version")
    modDependency("alexiil.mc.lib:libblockattributes-core:$lba_version")

    modDependency("io.github.cottonmc:LibGui:$libgui_version")

    modDependency("net.fabricmc:fabric-language-kotlin:$fabric_language_kotlin_version")

    modDependency("me.shedaniel:RoughlyEnoughItems:$rei_version") {
        exclude(group = "io.github.prospector.modmenu")
    }

    modDependency("com.lettuce.fudge:fabric-drawer:$drawer_version")
    modDependency("com.lettuce.fudge:working-scheduler:$scheduler_version")

    modRuntime("mcp.mobius.waila:Hwyla:$waila_version")
    modRuntime("com.jamieswhiteshirt:developer-mode:1.0.14")
    modRuntime("gamemodeoverhaul:GamemodeOverhaul:1.0.1.0")


}

fun DependencyHandlerScope.fabric() {
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings("net.fabricmc:yarn:$yarn_mappings")
    modImplementation("net.fabricmc:fabric-loader:$loader_version")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")
}


fun DependencyHandlerScope.modDependency(dep: String, dependencyConfiguration: Action<ExternalModuleDependency> = Action {}) {
    modImplementation(dep) {
        exclude(group = "net.fabricmc.fabric-api")
        dependencyConfiguration.execute(this)
    }

    include(dep)
}


tasks.getByName<ProcessResources>("processResources") {
    filesMatching("fabric.mod.json") {
        expand(mutableMapOf("version" to mod_version))
    }
}


// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
// if it is present.
// If you remove this task, sources will not be generated.
val sourcesJar = tasks.create<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val remapJar = tasks.getByName<RemapJarTask>("remapJar")

val remapModpackJar = tasks.create<RemapJarTask>("remapModpackJar") {
    dependsOn(remapJar)
    addNestedDependencies.set(false)
    input.set(remapJar.input)
    archiveFileName.set("$archives_base_name-$mod_version-modpack.jar")
}

val remapStandaloneJar = tasks.create<RemapJarTask>("remapStandaloneJar") {
    dependsOn(remapJar)
    addNestedDependencies.set(true)
    input.set(remapJar.input)
    archiveFileName.set("$archives_base_name-$mod_version-standalone.jar")
}

val remapSourcesJar = tasks.getByName<RemapSourcesJarTask>("remapSourcesJar")

curseforge {
    apiKey = if (project.hasProperty("curseforge_api_key")) curseforge_api_key else ""
    project(closureOf<CurseProject> {
        id = prop("curseforge_id")
        releaseType = "release"
        addGameVersion("Fabric")
        addGameVersion(minecraft_version)
        changelogType = "markdown"
        changelog = file("changelog.md")

        mainArtifact(remapModpackJar, closureOf<CurseArtifact> {
            displayName = "$mod_name $mod_version-Modpack"
        })

        addArtifact(remapStandaloneJar, closureOf<CurseArtifact> {
            displayName = "$mod_name $mod_version-Standalone"
        })
        relations(closureOf<CurseRelation> {
            requiredDependency("fabric-language-kotlin")
            requiredDependency("libblockattributes")
            requiredDependency("fabric-api")
            requiredDependency("libgui")
            requiredDependency("fabric-drawer")
            requiredDependency("working-scheduler")
            optionalDependency("roughly-enough-items")
        })
    })

    options(closureOf<Options> {
        forgeGradleIntegration = false
    })
}


// configure the maven publication
publishing {
    publications {
        create("standalone", MavenPublication::class.java) {
            // add all the jars that should be included when publishing to maven
            artifact(remapStandaloneJar) {
                builtBy(remapStandaloneJar)
            }
            artifact(sourcesJar) {
                builtBy(remapSourcesJar)
            }
            groupId = maven_group
            artifactId = "$archives_base_name-standalone"
            version = mod_version
        }

        create("modpack", MavenPublication::class.java) {
            // add all the jars that should be included when publishing to maven
            artifact(remapModpackJar) {
                builtBy(remapModpackJar)
            }
            artifact(sourcesJar) {
                builtBy(remapSourcesJar)
            }
            groupId = maven_group
            artifactId = "$archives_base_name-modpack"
            version = mod_version
        }
    }

    // select the repositories you want to publish to
    repositories {
        // uncomment to publish to the local maven
        // mavenLocal()
    }
}




tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs = listOf("-XXLanguage:+InlineClasses", "-Xuse-experimental=kotlin.Experimental")
    kotlinOptions.jvmTarget = "1.8"
}
