import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	kotlin("jvm") version "2.0.21"
	kotlin("plugin.spring") version "2.0.21"
	id("org.springframework.boot") version "3.3.0"
	id("io.spring.dependency-management") version "1.1.5"
	jacoco
	id("info.solidsoft.pitest") version "1.19.0"
	// Le plugin est déclaré mais on va utiliser notre propre tâche CLI pour éviter les crashs
	id("io.gitlab.arturbosch.detekt") version "1.23.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

// --- 1. GESTION DES SOURCES ---
sourceSets {
	create("testIntegration") {
		compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
		runtimeClasspath += output + compileClasspath
	}
	create("testComponent") {
		compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
		runtimeClasspath += output + compileClasspath
	}
	create("testArchitecture") {
		compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
		runtimeClasspath += output + compileClasspath
	}
}

val testIntegrationImplementation by configurations.getting { extendsFrom(configurations.testImplementation.get()) }
val testComponentImplementation by configurations.getting { extendsFrom(configurations.testImplementation.get()) }
val testArchitectureImplementation by configurations.getting { extendsFrom(configurations.testImplementation.get()) }

repositories {
	mavenCentral()
}

// --- 2. DÉPENDANCES ---
dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.postgresql:postgresql")
	implementation("org.liquibase:liquibase-core")

	// Tests Unitaires
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
	testImplementation("io.kotest:kotest-assertions-core:5.9.1")
	testImplementation("io.mockk:mockk:1.13.10")
	testImplementation("io.kotest:kotest-property:5.9.1")

	// Tests Intégration
	testIntegrationImplementation("org.testcontainers:postgresql:1.19.1")
	testIntegrationImplementation("com.ninja-squad:springmockk:4.0.2")

	// Tests Composants
	val cucumberVersion = "7.14.0"
	testComponentImplementation("io.cucumber:cucumber-java:$cucumberVersion")
	testComponentImplementation("io.cucumber:cucumber-spring:$cucumberVersion")
	testComponentImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")
	testComponentImplementation("io.rest-assured:rest-assured:5.3.2")
	testComponentImplementation("org.junit.platform:junit-platform-suite:1.10.0")
	testComponentImplementation("org.testcontainers:postgresql:1.19.1")

	// ArchUnit
	testArchitectureImplementation("com.tngtech.archunit:archunit-junit5:1.0.1")
}

// --- 3. CONFIGURATION DES TÂCHES DE TEST ---

tasks.register<Test>("testIntegration") {
	group = "verification"
	useJUnitPlatform()
	testClassesDirs = sourceSets["testIntegration"].output.classesDirs
	classpath = sourceSets["testIntegration"].runtimeClasspath
}

tasks.register<Test>("testComponent") {
	group = "verification"
	useJUnitPlatform()
	testClassesDirs = sourceSets["testComponent"].output.classesDirs
	classpath = sourceSets["testComponent"].runtimeClasspath
}

tasks.register<Test>("testArchitecture") {
	group = "verification"
	useJUnitPlatform()
	testClassesDirs = sourceSets["testArchitecture"].output.classesDirs
	classpath = sourceSets["testArchitecture"].runtimeClasspath
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// --- 4. QUALITÉ & DETEKT (MODE CLI ISOLÉ) ---

// Configuration dédiée pour isoler Detekt du reste du projet
val detektCli by configurations.creating

dependencies {
	detektCli("io.gitlab.arturbosch.detekt:detekt-cli:1.23.7")
	detektCli("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
}

// Remplacement de la tâche standard par une version JavaExec (100% isolée)
tasks.named("detekt") {
	enabled = false // On désactive la tâche du plugin qui crash
}

tasks.register<JavaExec>("detektCheck") {
	group = "verification"
	description = "Analyse statique Detekt isolée (évite les conflits Kotlin)."
	mainClass.set("io.gitlab.arturbosch.detekt.cli.Main")
	classpath = detektCli

	val reportDir = file("${buildDir}/reports/detekt")

	args(
		"--input", projectDir.absolutePath,
		"--config", file("config/detekt.yml").absolutePath,
		"--report", "html:${reportDir}/detekt.html"
	)

	doFirst {
		if (!reportDir.exists()) reportDir.mkdirs()
	}
}

// --- 5. RAPPORTS & COMPILATION ---

tasks.jacocoTestReport {
	dependsOn(tasks.test, "testIntegration", "testComponent")
	reports {
		html.required.set(true)
	}
}

pitest {
	targetClasses.set(listOf("livres.domain.*"))
	targetTests.set(listOf("livres.domain.*"))
	outputFormats.set(listOf("HTML", "XML"))
	timestampedReports.set(false)
}

kotlin {
	compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
}