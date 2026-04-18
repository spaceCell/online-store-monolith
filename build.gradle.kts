import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.plugins.quality.CheckstyleExtension

plugins {
    id("org.springframework.boot") version "3.1.5" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

allprojects {
    group = "com.example"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "checkstyle")
    apply(plugin = "io.spring.dependency-management")

    configure<CheckstyleExtension> {
        toolVersion = "10.12.5"
        configDirectory = rootProject.file("checkstyle")
        configFile = rootProject.file("checkstyle/checkstyle.xml")
        isIgnoreFailures = false
    }

    configure<DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.1.5")
        }
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Amapstruct.defaultComponentModel=spring")
    }

    dependencies.apply {
        add("implementation", "org.springframework.boot:spring-boot-starter")
        add("implementation", "org.springframework.boot:spring-boot-starter-data-jpa")
        add("implementation", "org.springframework.boot:spring-boot-starter-web")
        add("implementation", "org.mapstruct:mapstruct:1.5.5.Final")
        add("compileOnly", "org.projectlombok:lombok")
        add("annotationProcessor", "org.projectlombok:lombok")
        add("annotationProcessor", "org.projectlombok:lombok-mapstruct-binding:0.2.0")
        add("annotationProcessor", "org.mapstruct:mapstruct-processor:1.5.5.Final")
        add("runtimeOnly", "com.h2database:h2")
        add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
    }
}
