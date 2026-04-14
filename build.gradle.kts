plugins {
	// --- Framework & Language ---
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.5"
	id("io.spring.dependency-management") version "1.1.7"

	// --- Quality & Tests (Étape 6 & 7) ---
	jacoco
	id("info.solidsoft.pitest") version "1.19.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// --- Production ---
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation(kotlin("stdlib-jdk8"))

	// --- Testing ---
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	// Kotest : On s'assure d'avoir le moteur Junit5 de Kotest
	testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
	testImplementation("io.kotest:kotest-assertions-core:5.9.1")
	testImplementation("io.kotest:kotest-property:5.9.1")

	testImplementation("io.mockk:mockk:1.13.8")

	// FORCE le launcher pour Gradle 9
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
}
// --- Configuration Kotlin ---
kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

// --- Configuration des Tests Junit ---
tasks.withType<Test> {
	useJUnitPlatform {
		// Force l'utilisation du moteur Kotest
		includeEngines("kotest")
	}

	systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")

	testLogging {
		events("passed", "skipped", "failed")
		showExceptions = true
		exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
	}
}
// --- Étape 6/7 : Couverture de code (JaCoCo) ---
tasks.jacocoTestReport {
	dependsOn(tasks.test) // On génère le rapport seulement après les tests
	reports {
		xml.required.set(true) // Indispensable pour GitHub Actions
		html.required.set(true)
	}
}

// --- Étape 7/7 : Tests de mutation (PITest) ---
pitest {
	// On cible le package racine de ton domaine
	targetClasses.set(listOf("livres.domain.*"))

	// On aide PIT à trouver les tests au bon endroit
	targetTests.set(listOf("livres.domain.usecase.*"))

	testPlugin.set("junit5")
	outputFormats.set(listOf("HTML", "XML"))
	pitestVersion.set("1.16.0")
	timestampedReports.set(false)

	// Force l'activation du moteur Kotest pour PIT
	jvmArgs.set(listOf("-Djunit.jupiter.extensions.autodetection.enabled=true"))
}