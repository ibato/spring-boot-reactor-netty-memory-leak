plugins {
	java
	id("org.springframework.boot") version "2.1.6.RELEASE"
	id("io.spring.dependency-management") version "1.0.10.RELEASE"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks {
	withType<Test> {
		testLogging {
			testLogging.showStandardStreams = true
		}
	}
}