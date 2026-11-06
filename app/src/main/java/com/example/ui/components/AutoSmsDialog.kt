package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.parser.FinancialSmsParser
import com.example.data.parser.ParsedSmsTransaction
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AutoSmsDialog(
    onDismiss: () -> Unit,
    onConfirmEntry: (
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
    var rawSmsText by remember { mutableStateOf("") }
    var parsedResult by remember { mutableStateOf<ParsedSmsTransaction?>(null) }

    fun processText(text: String) {
        rawSmsText = text
        parsedResult = FinancialSmsParser.parse(text)
    }

    val sampleSms = listOf(
        "Orange Money Reçu" to "Vous avez recu 50000 FCFA de 691234567. Nouveau solde: 120000 FCFA. Ref: MP261106.1432.A98765",
        "Orange Money Paiement" to "Paiement de 28500 FCFA effectue avec succes a SUPERMARCHE CARREFOUR. Nouveau solde: 91500 FCFA. Ref: CI261106.0912.B12345",
        "MTN MoMo Transfert" to "Y'ello! Transfert reussi de 15000 FCFA vers PHARMACIE CENTRALE. ID transaction: 987654321. Solde: 45000 FCFA",
        "Virement Épargne" to "Virement de 80000 FCFA effectue vers votre Fonds d'urgence. Ref: VIR-EPARGNE-202611"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Détection Automatique SMS")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Collez le message de votre banque ou opérateur (Orange Money, MTN MoMo) pour générer l'écriture comptable en partie double automatiquement.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = rawSmsText,
                    onValueChange = { processText(it) },
                    label = { Text("Texte du SMS ou notification") },
                    placeholder = { Text("Ex: Vous avez reçu 50 000 FCFA...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("sms_input_field"),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Exemples rapides :",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for ((title, text) in sampleSms) {
                        SuggestionChip(
                            onClick = { processText(text) },
                            label = { Text(title, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Parse Result Preview
                val res = parsedResult
                if (res != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = res.detectedType.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                val numFmt = NumberFormat.getNumberInstance(Locale.FRANCE)
                                Text(
                                    text = "${numFmt.format(res.amount)} ${res.currency}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Libellé : ${res.label}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = "Réf : ${res.reference}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Double Entry Preview
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "DÉBIT (+)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Text(
                                        text = "${res.debitAccountCode} - ${res.debitAccountName}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "CRÉDIT (-)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    )
                                    Text(
                                        text = "${res.creditAccountCode} - ${res.creditAccountName}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Rule explanation
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = res.detectedType.ruleExplanation,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                } else if (rawSmsText.isNotBlank()) {
                    Text(
                        text = "Impossible d'extraire automatiquement le montant ou le sens de l'opération. Vérifiez le format du SMS.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val res = parsedResult ?: return@Button
                    onConfirmEntry(
                        System.currentTimeMillis(),
                        res.reference,
                        res.label,
                        res.debitAccountCode,
                        res.debitAccountName,
                        res.creditAccountCode,
                        res.creditAccountName,
                        res.amount,
                        res.detectedType.categoryKey,
                        "Détecté par SMS : ${res.rawText.take(120)}"
                    )
                    onDismiss()
                },
                enabled = parsedResult != null,
                modifier = Modifier.testTag("confirm_sms_button")
            ) {
                Text("Inscrire au Journal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
