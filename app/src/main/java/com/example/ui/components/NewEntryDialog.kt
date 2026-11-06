package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.OperationPresetType

@Composable
fun NewEntryDialog(
    accounts: List<Account>,
    currency: String,
    onDismiss: () -> Unit,
    onSubmit: (
        dateMillis: Long,
        reference: String,
        label: String,
        debitAccountCode: String,
        debitAccountName: String,
        creditAccountCode: String,
        creditAccountName: String,
        amount: Double,
        categoryType: String,
        note: String
    ) -> Unit
) {
    val presets = OperationPresetType.entries.toTypedArray()
    var selectedPresetIndex by remember { mutableStateOf(0) }
    val currentPreset = presets[selectedPresetIndex]

    var label by remember(selectedPresetIndex) { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var selectedDebitCode by remember(selectedPresetIndex) {
        mutableStateOf(currentPreset.defaultDebitCode)
    }
    var selectedCreditCode by remember(selectedPresetIndex) {
        mutableStateOf(currentPreset.defaultCreditCode)
    }

    var debitMenuExpanded by remember { mutableStateOf(false) }
    var creditMenuExpanded by remember { mutableStateOf(false) }

    val debitAccount = accounts.firstOrNull { it.code == selectedDebitCode }
        ?: Account.DEFAULT_ACCOUNTS.firstOrNull { it.code == selectedDebitCode }
        ?: Account(code = selectedDebitCode, name = "Compte $selectedDebitCode")

    val creditAccount = accounts.firstOrNull { it.code == selectedCreditCode }
        ?: Account.DEFAULT_ACCOUNTS.firstOrNull { it.code == selectedCreditCode }
        ?: Account(code = selectedCreditCode, name = "Compte $selectedCreditCode")

    val amount = amountText.replace(" ", "").replace(",", ".").toDoubleOrNull() ?: 0.0
    val isValid = amount > 0.0 && (label.isNotBlank() || currentPreset.title.isNotBlank())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nouvelle Écriture Comptable",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Preset Selector Tab Row
                ScrollableTabRow(
                    selectedTabIndex = selectedPresetIndex,
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presets.forEachIndexed { index, preset ->
                        Tab(
                            selected = selectedPresetIndex == index,
                            onClick = {
                                selectedPresetIndex = index
                                selectedDebitCode = preset.defaultDebitCode
                                selectedCreditCode = preset.defaultCreditCode
                            },
                            text = { Text(preset.title, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rule reminder
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentPreset.ruleExplanation,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Montant ($currency)") },
                    placeholder = { Text("Ex: 25000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("entry_amount_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Label
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Libellé de l'opération") },
                    placeholder = { Text(currentPreset.subtitle) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("entry_label_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Débit / Crédit Selection
                Text(
                    text = "Partie Double (Débit / Crédit) :",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Compte Débité
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { debitMenuExpanded = true }
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "DÉBIT (+)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${debitAccount.code} ${debitAccount.name}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    maxLines = 2
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = debitMenuExpanded,
                            onDismissRequest = { debitMenuExpanded = false }
                        ) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text("${acc.code} - ${acc.name} (Cl. ${acc.accountClass})") },
                                    onClick = {
                                        selectedDebitCode = acc.code
                                        debitMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Compte Crédité
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { creditMenuExpanded = true }
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "CRÉDIT (-)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${creditAccount.code} ${creditAccount.name}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    maxLines = 2
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = creditMenuExpanded,
                            onDismissRequest = { creditMenuExpanded = false }
                        ) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text("${acc.code} - ${acc.name} (Cl. ${acc.accountClass})") },
                                    onClick = {
                                        selectedCreditCode = acc.code
                                        creditMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Optional Reference & Note
                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Référence / N° Pièce (optionnel)") },
                    placeholder = { Text("Ex: FACT-2026-01") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Commentaire (optionnel)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalLabel = if (label.isNotBlank()) label else currentPreset.title
                    onSubmit(
                        System.currentTimeMillis(),
                        reference,
                        finalLabel,
                        selectedDebitCode,
                        debitAccount.name,
                        selectedCreditCode,
                        creditAccount.name,
                        amount,
                        currentPreset.categoryKey,
                        note
                    )
                    onDismiss()
                },
                enabled = isValid,
                modifier = Modifier.testTag("submit_entry_button")
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
