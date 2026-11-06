package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PositiveGreen
import com.example.ui.viewmodel.AccountingUiState
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BilanResultatScreen(
    state: AccountingUiState
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Bilan Personnel, 1: Compte de Résultat
    val summary = state.summary
    val balances = summary.accountBalances
    val numFmt = NumberFormat.getNumberInstance(Locale.FRANCE).apply { maximumFractionDigits = 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Bilan Personnel", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Compte de Résultat", fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            // ==================== BILAN PERSONNEL ====================
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Patrimoine Net Highlight Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "PATRIMOINE NET PERSONNEL",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${numFmt.format(summary.patrimoineNet)} ${summary.currency}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "Actifs (${numFmt.format(summary.totalActifs)}) - Dettes (${numFmt.format(summary.totalDettes)})",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }

                // ACTIF SECTION
                item {
                    Text(
                        text = "ACTIF (Ce que vous possédez)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Immobilisations (Classe 2)
                            val immoTotal = balances.values.filter { it.accountClass == 2 }.sumOf { maxOf(0.0, it.balance) }
                            BilanSectionHeader(title = "Classe 2 — Immobilisations (Biens durables)", total = immoTotal, currency = summary.currency)
                            balances.values.filter { it.accountClass == 2 && it.balance > 0 }.forEach {
                                BilanRow(code = it.code, name = it.name, amount = it.balance, currency = summary.currency)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            // Stocks (Classe 3)
                            val stocksTotal = balances.values.filter { it.accountClass == 3 }.sumOf { maxOf(0.0, it.balance) }
                            BilanSectionHeader(title = "Classe 3 — Stocks éventuels", total = stocksTotal, currency = summary.currency)
                            balances.values.filter { it.accountClass == 3 && it.balance > 0 }.forEach {
                                BilanRow(code = it.code, name = it.name, amount = it.balance, currency = summary.currency)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            // Créances (Classe 4)
                            val creancesTotal = balances.values.filter { it.accountClass == 4 && (it.code.startsWith("41") || it.code.startsWith("42") || it.code.startsWith("46")) }.sumOf { maxOf(0.0, it.balance) }
                            BilanSectionHeader(title = "Classe 4 — Créances à recouvrer", total = creancesTotal, currency = summary.currency)
                            balances.values.filter { it.accountClass == 4 && (it.code.startsWith("41") || it.code.startsWith("42") || it.code.startsWith("46")) && it.balance > 0 }.forEach {
                                BilanRow(code = it.code, name = it.name, amount = it.balance, currency = summary.currency)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            // Trésorerie & Placements (Classe 5)
                            val tresoTotal = balances.values.filter { it.accountClass == 5 }.sumOf { maxOf(0.0, it.balance) }
                            BilanSectionHeader(title = "Classe 5 — Trésorerie et placements", total = tresoTotal, currency = summary.currency)
                            balances.values.filter { it.accountClass == 5 && it.balance > 0 }.forEach {
                                BilanRow(code = it.code, name = it.name, amount = it.balance, currency = summary.currency)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("TOTAL ACTIFS", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("${numFmt.format(summary.totalActifs)} ${summary.currency}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // PASSIF SECTION
                item {
                    Text(
                        text = "PASSIF & DETTES (Ce que vous devez)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Dettes (Classe 4)
                            val dettesTotal = balances.values.filter { it.accountClass == 4 && (it.code.startsWith("40") || it.code.startsWith("43") || it.code.startsWith("45")) }.sumOf { maxOf(0.0, it.balance) }
                            BilanSectionHeader(title = "Classe 4 — Dettes et Emprunts", total = dettesTotal, currency = summary.currency)
                            balances.values.filter { it.accountClass == 4 && (it.code.startsWith("40") || it.code.startsWith("43") || it.code.startsWith("45")) && it.balance > 0 }.forEach {
                                BilanRow(code = it.code, name = it.name, amount = it.balance, currency = summary.currency)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            // Résultat net cumulé
                            BilanSectionHeader(title = "Résultat net de l'exercice", total = summary.capaciteEpargne, currency = summary.currency)

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("TOTAL DETTES", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                Text("${numFmt.format(summary.totalDettes)} ${summary.currency}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        } else {
            // ==================== COMPTE DE RÉSULTAT ====================
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Result Summary Card
                item {
                    val isSurplus = summary.capaciteEpargne >= 0
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSurplus) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isSurplus) "RÉSULTAT NET BÉNÉFICIAIRE (ÉPARGNE)" else "RÉSULTAT NET DÉFICITAIRE",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${numFmt.format(summary.capaciteEpargne)} ${summary.currency}",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSurplus) PositiveGreen else MaterialTheme.colorScheme.error
                                        )
                                    )
                                }
                                Icon(
                                    imageVector = if (isSurplus) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = if (isSurplus) PositiveGreen else MaterialTheme.colorScheme.error
                                )
                            }
                            Text(
                                text = "Capacité d'épargne = Revenus (${numFmt.format(summary.totalProduits)}) - Charges (${numFmt.format(summary.totalCharges)})",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // PRODUITS (Classe 7 + 802/804)
                item {
                    Text(
                        text = "PRODUITS & REVENUS (Classe 7)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val productBalances = balances.values.filter {
                                (it.accountClass == 7 && it.balance > 0) || (it.accountClass == 8 && (it.code == "802" || it.code == "804") && it.balance > 0)
                            }

                            if (productBalances.isEmpty()) {
                                Text("Aucun produit enregistré.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                productBalances.forEach { p ->
                                    BilanRow(code = p.code, name = p.name, amount = p.balance, currency = summary.currency)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("TOTAL REVENUS / PRODUITS", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("${numFmt.format(summary.totalProduits)} ${summary.currency}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // CHARGES (Classe 6 + 801/803/805)
                item {
                    Text(
                        text = "CHARGES DE CONSOMMATION (Classe 6)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val chargeBalances = balances.values.filter {
                                (it.accountClass == 6 && it.balance > 0) || (it.accountClass == 8 && (it.code == "801" || it.code == "803" || it.code == "805") && it.balance > 0)
                            }

                            if (chargeBalances.isEmpty()) {
                                Text("Aucune charge enregistrée.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                chargeBalances.forEach { c ->
                                    BilanRow(code = c.code, name = c.name, amount = c.balance, currency = summary.currency)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("TOTAL CHARGES / DÉPENSES", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                Text("${numFmt.format(summary.totalCharges)} ${summary.currency}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun BilanSectionHeader(title: String, total: Double, currency: String) {
    val numFmt = NumberFormat.getNumberInstance(Locale.FRANCE).apply { maximumFractionDigits = 0 }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "${numFmt.format(total)} $currency",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun BilanRow(code: String, name: String, amount: Double, currency: String) {
    val numFmt = NumberFormat.getNumberInstance(Locale.FRANCE).apply { maximumFractionDigits = 0 }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$code $name",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
        Text(
            text = "${numFmt.format(amount)} $currency",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
        )
    }
}
