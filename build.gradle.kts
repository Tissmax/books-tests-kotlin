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

	// --- Unit Testing (Kotest & MockK) ---
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		// On exclut JUnit Vintage pour éviter les conflits avec Kotest/JUnit 5
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
	testImplementation("io.kotest:kotest-assertions-core:5.9.1")
	testImplementation("io.kotest:kotest-property:5.9.1")
	testImplementation("io.mockk:mockk:1.13.8")

	// --- JUnit 5 Platform (Géré par Spring, on ne force que le launcher) ---
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// --- Configuration Kotlin ---
kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

// --- Configuration des Tests Junit ---
tasks.withType<Test> {
	useJUnitPlatform()
	testLogging {
		events("passed", "skipped", "failed")
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
	targetClasses.set(listOf("livres.domain.*")) // Cible ton code métier uniquement
	testPlugin.set("junit5")                     // Utilise JUnit 5 pour Kotest
	outputFormats.set(listOf("XML", "HTML"))
	timestampedReports.set(false)                // Évite de créer un dossier différent à chaque run
	mutationThreshold.set(0)                     // N'échoue pas le build si des mutants survivent (pour voir le rapport)
}