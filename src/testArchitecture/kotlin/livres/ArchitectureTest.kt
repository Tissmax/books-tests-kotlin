package livres

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import io.kotest.core.spec.style.FunSpec

class ArchitectureTest : FunSpec({

    test("L'architecture hexagonale doit être respectée") {
        // ON CHANGE LE PACKAGE RACINE ICI
        val basePackage = "livres"

        val importedClasses = ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages(basePackage)

        val rule = layeredArchitecture().consideringAllDependencies()
            // On définit les couches en fonction de tes dossiers réels
            .layer("Domain").definedBy("$basePackage.domain..")
            .layer("Infrastructure").definedBy("$basePackage.infrastructure..")
            .layer("Application").definedBy("$basePackage.application..")
            .layer("Standard API").definedBy("java..", "kotlin..", "org.springframework..")

            // On autorise les couches optionnelles au cas où l'une d'elles est vide
            .withOptionalLayers(true)

            // Les règles de dépendances
            .whereLayer("Domain").mayOnlyAccessLayers("Standard API")
            .whereLayer("Infrastructure").mayOnlyAccessLayers("Domain", "Standard API")
            .whereLayer("Application").mayOnlyAccessLayers("Domain", "Infrastructure", "Standard API")

        rule.check(importedClasses)
    }
})