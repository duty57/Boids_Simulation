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

    // Replace PostgreSQL driver with SQLite
    runtimeOnly("org.xerial:sqlite-jdbc:3.45.1")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("org.hibernate.orm:hibernate-community-dialects:6.4.4.Final")


    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0") // JPA API
    implementation("org.hibernate.orm:hibernate-core:6.3.1.Final") // Hibernate as JPA provider
    implementation("org.projectlombok:lombok:1.18.30") // Corrected Lombok dependency
    annotationProcessor("org.projectlombok:lombok:1.18.30") // Required for processing annotations
//    runtimeOnly("org.postgresql:postgresql:42.6.0") // Example: PostgreSQL driver

    // Add JOML dependency
    implementation("org.joml:joml:1.10.5")

    implementation("io.github.cdimascio:dotenv-java:3.0.0")

    // add jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    // add apache commons
    implementation("org.apache.commons:commons-lang3:3.12.0")
}

tasks.test {
    useJUnitPlatform()
}