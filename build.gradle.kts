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

    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0") // JPA API
    implementation("org.hibernate.orm:hibernate-core:6.3.1.Final") // Hibernate as JPA provider
    runtimeOnly("org.postgresql:postgresql:42.6.0") // Example: PostgreSQL driver

}

tasks.test {
    useJUnitPlatform()
}