package com.example.data.model

enum class OperationPresetType(
    val title: String,
    val subtitle: String,
    val categoryKey: String,
    val defaultDebitCode: String,
    val defaultCreditCode: String,
    val ruleExplanation: String
) {
    DEPENSE_COURANTE(
        title = "Dépense courante",
        subtitle = "Consommation (nourriture, transport, facture...)",
        categoryKey = "EXPENSE",
        defaultDebitCode = "621",
        defaultCreditCode = "531",
        ruleExplanation = "Charge décaissée : Débit Classe 6, Crédit Trésorerie Classe 5."
    ),
    REVENU_SALAIRE(
        title = "Revenu ou Salaire",
        subtitle = "Encaissement (salaire, freelance, vente...)",
        categoryKey = "REVENUE",
        defaultDebitCode = "521",
        defaultCreditCode = "701",
        ruleExplanation = "Produit encaissé : Débit Trésorerie Classe 5, Crédit Classe 7."
    ),
    VIREMENT_EPARGNE(
        title = "Virement vers Épargne",
        subtitle = "Alimenter fonds d'urgence ou projets",
        categoryKey = "SAVINGS_TRANSFER",
        defaultDebitCode = "551",
        defaultCreditCode = "521",
        ruleExplanation = "Règle 1 : L'épargne n'est pas une charge. Débit Compte Épargne (55x), Crédit Trésorerie."
    ),
    TRANSFERT_COMPTES(
        title = "Transfert entre comptes",
        subtitle = "Retrait espèces, recharge Mobile Money...",
        categoryKey = "INTERNAL_TRANSFER",
        defaultDebitCode = "511",
        defaultCreditCode = "531",
        ruleExplanation = "Règle 2 : Les transferts internes ne sont ni des charges ni des revenus."
    ),
    ACHAT_IMMOBILISATION(
        title = "Achat Bien Durable",
        subtitle = "Ordinateur, smartphone, équipement, véhicule...",
        categoryKey = "ASSET_PURCHASE",
        defaultDebitCode = "217",
        defaultCreditCode = "531",
        ruleExplanation = "Règle 5 : Un bien durable augmente votre patrimoine. Débit Classe 2, Crédit Trésorerie."
    ),
    EMPRUNT_RECU(
        title = "Emprunt reçu",
        subtitle = "Crédit bancaire ou emprunt personnel",
        categoryKey = "DEBT_BORROW",
        defaultDebitCode = "521",
        defaultCreditCode = "451",
        ruleExplanation = "Règle 3 : Un emprunt augmente la trésorerie mais crée une dette. Débit 5xx, Crédit 4xx."
    ),
    REMBOURSEMENT_DETTE(
        title = "Remboursement de dette",
        subtitle = "Régler un emprunt ou dette personnelle",
        categoryKey = "DEBT_REPAY",
        defaultDebitCode = "451",
        defaultCreditCode = "521",
        ruleExplanation = "Diminution de dette : Débit 451/431, Crédit Trésorerie."
    ),
    PRET_ACCORDE(
        title = "Prêt accordé à un tiers",
        subtitle = "Prêter de l'argent à un ami ou proche",
        categoryKey = "LOAN_GIVEN",
        defaultDebitCode = "411",
        defaultCreditCode = "531",
        ruleExplanation = "Règle 4 : Un prêt accordé crée une créance, ce n'est pas une dépense perdue. Débit 411, Crédit Trésorerie."
    ),
    RECOUVREMENT_CREANCE(
        title = "Remboursement de prêt reçu",
        subtitle = "Un débiteur vous rembourse",
        categoryKey = "LOAN_RECOVERED",
        defaultDebitCode = "531",
        defaultCreditCode = "411",
        ruleExplanation = "Récupération de créance : Débit Trésorerie, Crédit 411."
    ),
    ACHAT_STOCKS(
        title = "Achat de stocks",
        subtitle = "Réserves alimentaires ou fournitures",
        categoryKey = "STOCK_PURCHASE",
        defaultDebitCode = "311",
        defaultCreditCode = "511",
        ruleExplanation = "Constitution de stocks : Débit Classe 3, Crédit Trésorerie."
    ),
    DON_EXCEPTIONNEL(
        title = "Don ou Événement exceptionnel",
        subtitle = "Dons accordés, reçus, pertes ou gains",
        categoryKey = "EXCEPTIONAL",
        defaultDebitCode = "805",
        defaultCreditCode = "531",
        ruleExplanation = "Événements particuliers : Classe 8."
    )
}
