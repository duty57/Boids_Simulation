plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // JOGL dependencies
    implementation("org.jogamp.gluegen:gluegen-rt:2.3.1")
    implementation("org.jogamp.jogl:jogl-all:2.3.1")
    runtimeOnly("org.jogamp.gluegen:gluegen-rt:2.3.1:natives-windows-amd64")
    runtimeOnly("org.jogamp.jogl:jogl-all:2.3.1:natives-windows-amd64")

    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0") // JPA API
    implementation("org.hibernate.orm:hibernate-core:6.3.1.Final") // Hibernate as JPA provider
    implementation("org.projectlombok:lombok:1.18.30") // Corrected Lombok dependency
    annotationProcessor("org.projectlombok:lombok:1.18.30") // Required for processing annotations
    runtimeOnly("org.postgresql:postgresql:42.6.0") // Example: PostgreSQL driver

    // Add JOML dependency
    implementation("org.joml:joml:1.10.5")

    implementation("io.github.cdimascio:dotenv-java:3.0.0")

    // add jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
}

tasks.test {
    useJUnitPlatform()
}