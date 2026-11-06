package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Account(
    @DocumentId val id: String = "",
    val userId: String = "",
    val code: String = "",
    val name: String = "",
    val accountClass: Int = 1,
    val description: String = "",
    val isCustom: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    companion object {
        val DEFAULT_ACCOUNTS = listOf(
            // Classe 1 — Patrimoine et capitaux
            Account(id = "101", code = "101", name = "Patrimoine initial", accountClass = 1, description = "Capital de départ au lancement du système"),
            Account(id = "102", code = "102", name = "Apports personnels", accountClass = 1, description = "Injections de fonds personnels supplémentaires"),
            Account(id = "103", code = "103", name = "Résultat cumulé", accountClass = 1, description = "Économies et bénéfices cumulés des périodes précédentes"),
            Account(id = "104", code = "104", name = "Patrimoine net", accountClass = 1, description = "Valeur nette globale de ton patrimoine"),

            // Classe 2 — Immobilisations
            Account(id = "211", code = "211", name = "Terrains", accountClass = 2, description = "Biens fonciers et parcelles"),
            Account(id = "213", code = "213", name = "Logement", accountClass = 2, description = "Biens immobiliers possédés"),
            Account(id = "214", code = "214", name = "Mobilier", accountClass = 2, description = "Meubles et ameublement durable"),
            Account(id = "215", code = "215", name = "Équipements domestiques", accountClass = 2, description = "Électroménager et gros appareils"),
            Account(id = "216", code = "216", name = "Moyens de transport", accountClass = 2, description = "Véhicule, moto ou vélo"),
            Account(id = "217", code = "217", name = "Matériel informatique", accountClass = 2, description = "Ordinateur, smartphone, tablettes, périphériques"),
            Account(id = "218", code = "218", name = "Autres biens durables", accountClass = 2, description = "Autres actifs matériels d'usage durable"),

            // Classe 3 — Stocks
            Account(id = "311", code = "311", name = "Stocks alimentaires", accountClass = 3, description = "Réserves et provisions alimentaires de longue durée"),
            Account(id = "312", code = "312", name = "Stocks de fournitures", accountClass = 3, description = "Fournitures de bureau, consommables"),
            Account(id = "318", code = "318", name = "Autres stocks", accountClass = 3, description = "Autres matières et stocks valorisables"),

            // Classe 4 — Comptes de tiers
            Account(id = "401", code = "401", name = "Fournisseurs à payer", accountClass = 4, description = "Factures ou achats à terme en attente de règlement"),
            Account(id = "411", code = "411", name = "Créances sur des tiers", accountClass = 4, description = "Prêts ou avances accordés à des amis, famille ou collègues"),
            Account(id = "421", code = "421", name = "Avances accordées", accountClass = 4, description = "Avances sur prestations ou commandes"),
            Account(id = "431", code = "431", name = "Dettes personnelles", accountClass = 4, description = "Dettes envers proches ou particuliers"),
            Account(id = "451", code = "451", name = "Emprunts et crédits", accountClass = 4, description = "Crédits bancaires, microcrédits ou emprunts formels"),
            Account(id = "461", code = "461", name = "Dépôts et cautions", accountClass = 4, description = "Cautions de loyer ou garanties récupérables"),

            // Classe 5 — Trésorerie et placements
            Account(id = "511", code = "511", name = "Espèces", accountClass = 5, description = "Argent liquide disponible en poche ou coffre"),
            Account(id = "521", code = "521", name = "Comptes bancaires", accountClass = 5, description = "Comptes courants bancaires"),
            Account(id = "531", code = "531", name = "Orange Money", accountClass = 5, description = "Portefeuille électronique Orange Money"),
            Account(id = "532", code = "532", name = "MTN Mobile Money", accountClass = 5, description = "Portefeuille électronique MTN Mobile Money"),
            Account(id = "541", code = "541", name = "Épargne disponible", accountClass = 5, description = "Épargne liquide non bloquée"),
            Account(id = "551", code = "551", name = "Fonds d'urgence", accountClass = 5, description = "Fonds de sécurité pour imprévus"),
            Account(id = "552", code = "552", name = "Épargne projet personnel", accountClass = 5, description = "Épargne dédiée aux projets personnels"),
            Account(id = "553", code = "553", name = "Épargne immigration", accountClass = 5, description = "Épargne dédiée aux démarches d'immigration et voyage"),
            Account(id = "554", code = "554", name = "Épargne certifications et formations", accountClass = 5, description = "Épargne dédiée à la montée en compétences"),
            Account(id = "555", code = "555", name = "Placements financiers", accountClass = 5, description = "Tontines, actions, obligations ou cryptos"),

            // Classe 6 — Charges
            Account(id = "601", code = "601", name = "Logement", accountClass = 6, description = "Loyer et charges locatives périodiques"),
            Account(id = "611", code = "611", name = "Eau et électricité", accountClass = 6, description = "Factures d'utilités publiques (eau, électricité, gaz)"),
            Account(id = "621", code = "621", name = "Alimentation", accountClass = 6, description = "Repas quotidiens, courses et nourriture"),
            Account(id = "631", code = "631", name = "Transport", accountClass = 6, description = "Taxi, bus, carburant, péages"),
            Account(id = "641", code = "641", name = "Habillement", accountClass = 6, description = "Vêtements, chaussures, accessoires"),
            Account(id = "651", code = "651", name = "Santé", accountClass = 6, description = "Consultations médicales, pharmacie, soins"),
            Account(id = "661", code = "661", name = "Loisirs et divertissements", accountClass = 6, description = "Sorties, restaurants, abonnements streaming"),
            Account(id = "671", code = "671", name = "Éducation et formations", accountClass = 6, description = "Livres, cours, frais de scolarité"),
            Account(id = "681", code = "681", name = "Entretien et réparations", accountClass = 6, description = "Réparations d'équipements ou véhicules"),
            Account(id = "691", code = "691", name = "Télécommunications", accountClass = 6, description = "Forfaits internet, crédit d'appel, data mobile"),
            Account(id = "699", code = "699", name = "Autres dépenses", accountClass = 6, description = "Dépenses diverses courantes non classées"),

            // Classe 7 — Produits et revenus
            Account(id = "701", code = "701", name = "Salaire", accountClass = 7, description = "Rémunération principale d'emploi salarié"),
            Account(id = "711", code = "711", name = "Revenus d'activités indépendantes", accountClass = 7, description = "Freelancing, missions de consulting, prestations"),
            Account(id = "721", code = "721", name = "Revenus financiers", accountClass = 7, description = "Intérêts, dividendes, plus-values de placement"),
            Account(id = "731", code = "731", name = "Revenus locatifs", accountClass = 7, description = "Loyers perçus sur des biens loués"),
            Account(id = "741", code = "741", name = "Revenus exceptionnels", accountClass = 7, description = "Primes inattendues, ventes exceptionnelles"),
            Account(id = "751", code = "751", name = "Autres revenus", accountClass = 7, description = "Autres rentrées financières diverses"),

            // Classe 8 — Comptes exceptionnels
            Account(id = "801", code = "801", name = "Pertes exceptionnelles", accountClass = 8, description = "Pertes fortuites hors exploitation courante"),
            Account(id = "802", code = "802", name = "Gains exceptionnels", accountClass = 8, description = "Gains exceptionnels ou gains de loterie"),
            Account(id = "803", code = "803", name = "Vols et pertes de biens", accountClass = 8, description = "Sinistres, vols de matériel ou dégradations"),
            Account(id = "804", code = "804", name = "Dons reçus", accountClass = 8, description = "Cadeaux en espèces ou aides reçues de proches"),
            Account(id = "805", code = "805", name = "Dons accordés", accountClass = 8, description = "Aides apportées à la famille, aumône, dons caritatifs"),

            // Classe 9 — Budgets et contrôle
            Account(id = "901", code = "901", name = "Budget mensuel global", accountClass = 9, description = "Enveloppe budgétaire globale pour le mois"),
            Account(id = "902", code = "902", name = "Budget alimentation", accountClass = 9, description = "Objectif mensuel dépenses alimentation (621)"),
            Account(id = "903", code = "903", name = "Budget transport", accountClass = 9, description = "Objectif mensuel dépenses transport (631)"),
            Account(id = "904", code = "904", name = "Budget logement", accountClass = 9, description = "Objectif mensuel dépenses logement (601)"),
            Account(id = "905", code = "905", name = "Budget formation", accountClass = 9, description = "Objectif mensuel éducation & formation (671)"),
            Account(id = "906", code = "906", name = "Budget loisirs", accountClass = 9, description = "Objectif mensuel loisirs & divertissement (661)"),
            Account(id = "907", code = "907", name = "Objectifs d'épargne", accountClass = 9, description = "Cible d'épargne globale mensuelle"),
            Account(id = "908", code = "908", name = "Écarts budgétaires", accountClass = 9, description = "Suivi des dépassements ou économies réalisées")
        )
    }
}
