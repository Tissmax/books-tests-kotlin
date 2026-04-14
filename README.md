# TP : Tests Unitaires et Qualité logicielle - Gestion de Livres

Ce projet implémente un cas d'utilisation de gestion de bibliothèque en suivant les principes du **Domain-Driven Design (DDD)** et du **Clean Code**. L'objectif principal était de mettre en place une suite de tests robuste alliant tests unitaires classiques, Property-Based Testing et tests de mutation.

## 🚀 Stack Technique
* **Langage :** Kotlin 2.0.0
* **JDK :** 21 (Temurin)
* **Build Tool :** Gradle 9.4.1
* **Framework de Test :** Kotest & MockK
* **Qualité :** JaCoCo (Couverture) & PITest (Mutation Testing)

## 🧪 Stratégie de Test

### 1. Tests Unitaires & Mocking
Utilisation de **MockK** pour isoler la logique métier (`GestionLivres`) de la persistance (`LivreRepository`). 
* Validation des règles métier (titre/auteur non vides).
* Vérification des interactions avec le repository.

### 2. Property-Based Testing
Utilisation de **Kotest Property** pour valider l'algorithme de tri. Au lieu de tester un cas unique, le test génère des listes de livres aléatoires pour garantir que le tri par titre reste correct quel que soit le contenu.

### 3. Tests de Mutation (PITest)
Pour valider l'efficacité des tests, j'ai mis en place **PITest**. 
* **Score obtenu :** 78% de Mutation Strength.
* **Résultat :** 7 mutants tués sur 9. Les mutants survivants concernent des vérifications intrinsèques au bytecode Kotlin (null-checks générés) qui n'impactent pas la logique métier.

## 📊 Rapports de Qualité
Les rapports sont générés localement via les commandes suivantes :
* **JaCoCo :** `./gradlew jacocoTestReport` -> `build/reports/jacoco/test/html/index.html`
* **PITest :** `./gradlew pitest` -> `build/reports/pitest/index.html`

## ⚠️ Note sur l'Intégration Continue (GitHub Actions)
Le pipeline CI peut afficher un état "Failed" sur la tâche finale `:bootJar`. 
Il s'agit d'un problème de compatibilité connu entre **Gradle 9.4** et le plugin **Spring Boot 3.3** lors de la création de l'archive exécutable. 

**Cependant, toutes les étapes de qualité (Tests, JaCoCo, PITest) sont exécutées avec succès avant cette étape.** Les résultats de tests et les rapports de mutation sont disponibles dans les artefacts de la session GitHub Actions.
