import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	kotlin("jvm") version "2.0.21"
	kotlin("plugin.spring") version "2.0.21"
	id("org.springframework.boot") version "3.3.0"
	id("io.spring.dependency-management") version "1.1.5"
	jacoco
	id("info.solidsoft.pitest") version "1.19.0"
	id("io.gitlab.arturbosch.detekt") version "1.23.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

// --- 1. GESTION DES SOURCES & HÉRITAGE DES CONFIGURATIONS ---
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

// CETTE PARTIE EST CRUCIALE POUR GITHUB ACTIONS :
// On dit à Gradle que nos nouveaux types de tests héritent des dépendances de 'test'
configurations {
	val testImplementation = testImplementation.get()
	val testRuntimeOnly = testRuntimeOnly.get()

	getByName("testIntegrationImplementation") { extendsFrom(testImplementation) }
	getByName("testIntegrationRuntimeOnly") { extendsFrom(testRuntimeOnly) }

	getByName("testComponentImplementation") { extendsFrom(testImplementation) }
	getByName("testComponentRuntimeOnly") { extendsFrom(testRuntimeOnly) }

	getByName("testArchitectureImplementation") { extendsFrom(testImplementation) }
}

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

	// TESTS UNITAIRES (Base pour tous les autres)
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
	testImplementation("io.kotest:kotest-assertions-core:5.9.1")
	testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")
	testImplementation("io.mockk:mockk:1.13.10")

	// TESTS D'INTÉGRATION
	"testIntegrationImplementation"("org.testcontainers:postgresql:1.19.1")
	"testIntegrationImplementation"("com.ninja-squad:springmockk:4.0.2")

	// TESTS DE COMPOSANTS
	val cucumberVersion = "7.14.0"
	"testComponentImplementation"("io.cucumber:cucumber-java:$cucumberVersion")
	"testComponentImplementation"("io.cucumber:cucumber-spring:$cucumberVersion")
	"testComponentImplementation"("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")
	"testComponentImplementation"("io.rest-assured:rest-assured:5.3.2")
	"testComponentImplementation"("org.junit.platform:junit-platform-suite:1.10.0")

	// ARCHITECTURE
	"testArchitectureImplementation"("com.tngtech.archunit:archunit-junit5:1.0.1")
}

// --- 3. TÂCHES DE TEST ---
tasks.withType<Test> {
	useJUnitPlatform()
}

val testIntegration = tasks.register<Test>("testIntegration") {
	group = "verification"
	testClassesDirs = sourceSets["testIntegration"].output.classesDirs
	classpath = sourceSets["testIntegration"].runtimeClasspath
}

val testComponent = tasks.register<Test>("testComponent") {
	group = "verification"
	testClassesDirs = sourceSets["testComponent"].output.classesDirs
	classpath = sourceSets["testComponent"].runtimeClasspath
}

val testArchitecture = tasks.register<Test>("testArchitecture") {
	group = "verification"
	testClassesDirs = sourceSets["testArchitecture"].output.classesDirs
	classpath = sourceSets["testArchitecture"].runtimeClasspath
}

// --- 4. QUALITÉ & DETEKT (MODE CLI) ---
val detektCli by configurations.creating
dependencies {
	detektCli("io.gitlab.arturbosch.detekt:detekt-cli:1.23.7")
	detektCli("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
}

tasks.named("detekt") { enabled = false }

tasks.register<JavaExec>("detektCheck") {
	group = "verification"
	mainClass.set("io.gitlab.arturbosch.detekt.cli.Main")
	classpath = detektCli
	args("--input", projectDir.absolutePath, "--config", file("config/detekt.yml").absolutePath, "--report", "html:${buildDir}/reports/detekt/detekt.html")
}

// --- 5. RAPPORTS ---
tasks.jacocoTestReport {
	dependsOn(tasks.test, testIntegration, testComponent)
	reports { html.required.set(true) }
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