Feature: Gestion des books
  Scenario: L'utilisateur crée un livre et le retrouve dans la liste
    Given l'utilisateur crée le livre "L'Anomalie" écrit par "Hervé Le Tellier"
    When l'utilisateur récupère la liste des livres
    Then la liste doit contenir le livre suivant
      | title      | author           | reserved
      | L'Anomalie | Hervé Le Tellier | false

  Scenario: L'utilisateur reserve un livre et le retourne
    Given l'utilisateur reserve le livre "Létranger" écrit par "Albert Camus"
    When l'utilisateur réserve le livre "Létranger"
    Then le livre doit etre reserve
      | title      | author           | reserved |
      | Létranger  | Albert Camus     | true     |
    When l'utilisateur retourne le livre "Létranger"
    Then le livre doit etre retourne
      | title      | author           | reserved |
      | Létranger  | Albert Camus     | false    |

