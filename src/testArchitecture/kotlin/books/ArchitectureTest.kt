package books

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import io.kotest.core.spec.style.FunSpec

class ArchitectureTest : FunSpec({

    test("L'architecture hexagonale doit être respectée") {
        val basePackage = "books"

        val importedClasses = ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages(basePackage)

        val rule = layeredArchitecture().consideringAllDependencies()
            .layer("Domain").definedBy("$basePackage.domain..")
            .layer("Infrastructure").definedBy("$basePackage.infrastructure..")
            .layer("Application").definedBy("$basePackage.application..")
            .layer("Standard API").definedBy("java..", "kotlin..", "org.springframework..")

            .withOptionalLayers(true)

            .whereLayer("Domain").mayOnlyAccessLayers("Standard API")
            .whereLayer("Infrastructure").mayOnlyAccessLayers("Domain", "Standard API")
            .whereLayer("Application").mayOnlyAccessLayers("Domain", "Infrastructure", "Standard API")

        rule.check(importedClasses)
    }
})