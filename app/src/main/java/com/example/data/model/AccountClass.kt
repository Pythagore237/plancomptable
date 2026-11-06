package com.example.data.model

enum class AccountClass(val classNumber: Int, val title: String, val description: String) {
    CLASSE_1(1, "Patrimoine et capitaux", "Suivre ta richesse nette et capitaux propres"),
    CLASSE_2(2, "Immobilisations", "Suivre tes biens durables (matériel, véhicule, logement)"),
    CLASSE_3(3, "Stocks", "Suivre tes stocks éventuels (alimentation, fournitures)"),
    CLASSE_4(4, "Tiers", "Suivre créances accordées et dettes à payer"),
    CLASSE_5(5, "Trésorerie et placements", "Suivre ton argent disponible (espèces, banque, Mobile Money, épargne)"),
    CLASSE_6(6, "Charges", "Suivre tes dépenses de consommation"),
    CLASSE_7(7, "Produits", "Suivre tes revenus et gains habituels"),
    CLASSE_8(8, "Comptes exceptionnels", "Enregistrer les événements exceptionnels et dons"),
    CLASSE_9(9, "Budgets et contrôle", "Piloter tes prévisions et objectifs financiers");

    companion object {
        fun fromNumber(number: Int): AccountClass {
            return entries.firstOrNull { it.classNumber == number } ?: CLASSE_6
        }

        fun fromCode(code: String): AccountClass {
            val firstDigit = code.firstOrNull()?.digitToIntOrNull() ?: 6
            return fromNumber(firstDigit)
        }
    }
}
