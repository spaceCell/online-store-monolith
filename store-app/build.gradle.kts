plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":order-module"))
    implementation(project(":catalog-module"))
    implementation(project(":user-module"))
    implementation(project(":payment-module"))
}

tasks.named<Jar>("jar") {
    enabled = false
}
