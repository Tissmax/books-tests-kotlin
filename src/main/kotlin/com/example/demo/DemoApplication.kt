package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}
	private val alphabet = ('A'..'Z').toList()

fun cesar(message: String, decallage: Int): String {
	// Utilisation de listOf ou de l'intervalle 'A'..'Z'
	val messageUpperCase = message.uppercase() // uppercase() sans CamelCase et avec 'e'
	require(message.all { it.isLetter() || it.isWhitespace() })
	if (decallage <0) {
		error("Ne peut pas décaller d'un negatif")
	}
	if (message != messageUpperCase) {
		error("Doit être en majuscule")
	}
	return messageUpperCase.map { char -> // On utilise '->' pour définir le nom du paramètre dans le map
		if (char in alphabet) {
			val currentIndex = alphabet.indexOf(char)
			val newIndex = (currentIndex + decallage) % 26
			// Gestion du décalage négatif pour le déchiffrement
			alphabet[if (newIndex < 0) newIndex + 26 else newIndex] }
		else {
			char
		}
	}.joinToString("") // .joinToString("") au lieu de .toString("") pour transformer la liste en String
}

