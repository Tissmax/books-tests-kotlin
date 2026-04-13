package com.example.demo

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class Test : FunSpec({

	test("Test A doit retourner B") {
		cesar(message = "A", decallage = 1) Hello Quelqu'un a les vraies dates pour le passage de 2027 ? shouldBe "B"
	}

	test("Test A doit retourner C") {
		cesar(message = "A", decallage = 2) shouldBe "C"
	}

	test("Test Z doit retourner A") {
		cesar(message = "Z", decallage = 1) shouldBe "A"
	}

	test("Test A doit retourner B si il est décallé de 27") {
		cesar(message = "A", decallage = 27) shouldBe "B"
	}

	test("Test doit retourner un message d'erreur") {
		shouldThrowMessage("Ne peut pas décaller d'un negatif") {
			cesar(message = "A", decallage = -1)
		}
	}
	test(name = "Doit être en majuscule"){
		shouldThrowMessage("Doit être en majuscule"){
			cesar(message = "a", decallage = 1)
		}
	}
	test(name = "Doit être une lettre"){
		shouldThrow<IllegalArgumentException> {
			cesar(message = "1", decallage = 1)
		}
	}
})