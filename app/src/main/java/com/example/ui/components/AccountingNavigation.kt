package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.BilanResultatScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GrandLivreScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.PlanComptableScreen
import com.example.ui.viewmodel.AccountingUiState
import com.example.ui.viewmodel.AccountingViewModel

enum class NavigationScreen(val title: String, val icon: ImageVector) {
    DASHBOARD("Tableau", Icons.Default.Dashboard),
    JOURNAL("Journal", Icons.Default.Book),
    GRAND_LIVRE("Grand Livre", Icons.Default.AccountBalance),
    BILAN("Bilan & Résultat", Icons.Default.PieChart),
    BUDGETS("Budgets", Icons.Default.Flag),
    PLAN("Plan", Icons.Default.MenuBook)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountingNavigation(
    viewModel: AccountingViewModel,
    uiState: AccountingUiState,
    onSignOut: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(NavigationScreen.DASHBOARD) }
    var showNewEntryDialog by remember { mutableStateOf(false) }
    var showAutoSmsDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }

    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Compta Perso SYSCOHADA",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showAutoSmsDialog = true },
                        modifier = Modifier.testTag("top_bar_sms_parser")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Scan SMS",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            signOutUser(
                                context = context,
                                credentialManager = credentialManager,
                                onSignOutComplete = onSignOut,
                                scope = scope
                            )
                        },
                        modifier = Modifier.testTag("top_bar_sign_out")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Déconnexion"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                NavigationScreen.entries.forEach { screen ->
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                NavigationScreen.DASHBOARD -> DashboardScreen(
                    state = uiState,
                    onOpenNewEntry = { showNewEntryDialog = true },
                    onOpenAutoSms = { showAutoSmsDialog = true },
                    onNavigateToJournal = { currentScreen = NavigationScreen.JOURNAL },
                    onSeedDemo = { viewModel.seedDemoDataIfEmpty() }
                )
                NavigationScreen.JOURNAL -> JournalScreen(
                    state = uiState,
                    onOpenNewEntry = { showNewEntryDialog = true },
                    onOpenAutoSms = { showAutoSmsDialog = true },
                    onDeleteEntry = { viewModel.deleteEntry(it) }
                )
                NavigationScreen.GRAND_LIVRE -> GrandLivreScreen(
                    state = uiState
                )
                NavigationScreen.BILAN -> BilanResultatScreen(
                    state = uiState
                )
                NavigationScreen.BUDGETS -> BudgetsScreen(
                    state = uiState,
                    onSaveBudget = { accCode, targetCode, title, limit, period ->
                        viewModel.saveBudget(accCode, targetCode, title, limit, period)
                    },
                    onDeleteBudget = { viewModel.deleteBudget(it) }
                )
                NavigationScreen.PLAN -> PlanComptableScreen(
                    state = uiState,
                    onSaveAccount = { code, name, cl, desc ->
                        viewModel.saveAccount(code, name, cl, desc)
                    }
                )
            }
        }
    }

    if (showNewEntryDialog) {
        NewEntryDialog(
            accounts = uiState.accounts,
            currency = uiState.summary.currency,
            onDismiss = { showNewEntryDialog = false },
            onSubmit = { date, ref, lbl, dCode, dName, cCode, cName, amt, cat, note ->
                viewModel.addEntry(date, ref, lbl, dCode, dName, cCode, cName, amt, cat, note)
            }
        )
    }

    if (showAutoSmsDialog) {
        AutoSmsDialog(
            onDismiss = { showAutoSmsDialog = false },
            onConfirmEntry = { date, ref, lbl, dCode, dName, cCode, cName, amt, cat, note ->
                viewModel.addEntry(date, ref, lbl, dCode, dName, cCode, cName, amt, cat, note)
            }
        )
    }
}
