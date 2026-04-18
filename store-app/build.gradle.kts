plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    implementation(project(":order-module"))
    implementation(project(":catalog-module"))
    implementation(project(":user-module"))
    implementation(project(":payment-module"))
}

tasks.named<Jar>("jar") {
    enabled = false
}
