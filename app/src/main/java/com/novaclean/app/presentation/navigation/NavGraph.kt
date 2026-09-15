package com.novaclean.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.novaclean.app.di.AppContainer
import com.novaclean.app.presentation.screens.AudioDuplicatesScreen
import com.novaclean.app.presentation.screens.ContactsCleanerScreen
import com.novaclean.app.presentation.screens.DashboardScreen
import com.novaclean.app.presentation.screens.JunkCleanerScreen
import com.novaclean.app.presentation.screens.PaywallScreen
import com.novaclean.app.presentation.screens.PhotoDuplicatesScreen
import com.novaclean.app.presentation.viewmodel.AudioDuplicatesViewModel
import com.novaclean.app.presentation.viewmodel.ContactsCleanerViewModel
import com.novaclean.app.presentation.viewmodel.DashboardViewModel
import com.novaclean.app.presentation.viewmodel.JunkCleanerViewModel
import com.novaclean.app.presentation.viewmodel.PaywallViewModel
import com.novaclean.app.presentation.viewmodel.PhotoDuplicatesViewModel
import kotlinx.coroutines.launch

object Routes {
    const val DASHBOARD = "dashboard"
    const val PHOTOS = "photos"
    const val AUDIO = "audio"
    const val CONTACTS = "contacts"
    const val JUNK = "junk"
    const val PAYWALL = "paywall"
    const val PROFILE = "profile"
    const val PASSWORD_GEN = "password_gen"
    const val COMPRESSOR = "compressor"
    const val LARGE_FILES = "large_files"
    const val VAULT = "vault"
    const val PASSWORD_MANAGER = "password_manager"
}

@Composable
fun NovaCleanNavGraph(
    navController: NavHostController,
    container: AppContainer
) {
    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD
    ) {
        composable(Routes.DASHBOARD) {
            val viewModel = remember {
                DashboardViewModel(
                    getRamInfoUseCase = container.getRamInfoUseCase,
                    optimizeRamUseCase = container.optimizeRamUseCase,
                    observeProStatusUseCase = container.observeProStatusUseCase
                )
            }
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToPhotos = { navController.navigate(Routes.PHOTOS) },
                onNavigateToAudio = { navController.navigate(Routes.AUDIO) },
                onNavigateToContacts = { navController.navigate(Routes.CONTACTS) },
                onNavigateToJunk = { navController.navigate(Routes.JUNK) },
                onNavigateToCompressor = { navController.navigate(Routes.COMPRESSOR) },
                onNavigateToLargeFiles = { navController.navigate(Routes.LARGE_FILES) },
                onNavigateToVault = { navController.navigate(Routes.VAULT) },
                onNavigateToPaywall = { navController.navigate(Routes.PAYWALL) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                interstitialAdManager = container.interstitialAdManager
            )
        }

        composable(Routes.PROFILE) {
            val viewModel = remember {
                com.novaclean.app.presentation.viewmodel.ProfileViewModel(
                    observeUserProfileUseCase = container.observeUserProfileUseCase,
                    setNotificationsEnabledUseCase = container.setNotificationsEnabledUseCase,
                    registerProfileUseCase = container.registerProfileUseCase
                )
            }
            com.novaclean.app.presentation.screens.ProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToPaywall = { navController.navigate(Routes.PAYWALL) },
                onNavigateToPasswordGen = { navController.navigate(Routes.PASSWORD_GEN) },
                onNavigateToPasswordManager = { navController.navigate(Routes.PASSWORD_MANAGER) }
            )
        }

        composable(Routes.PASSWORD_GEN) {
            val viewModel = remember {
                com.novaclean.app.presentation.viewmodel.PasswordGeneratorViewModel(
                    generatorUseCase = container.passwordGeneratorUseCase
                )
            }
            val scope = rememberCoroutineScope()
            com.novaclean.app.presentation.screens.PasswordGeneratorScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSavePassword = { service, login, pass, cat, notes ->
                    scope.launch {
                        val item = com.novaclean.app.domain.model.SavedPassword(
                            id = java.util.UUID.randomUUID().toString(),
                            serviceName = service,
                            login = login,
                            password = pass,
                            category = cat,
                            notes = notes
                        )
                        container.savePasswordUseCase.execute(item)
                    }
                },
                onNavigateToManager = { navController.navigate(Routes.PASSWORD_MANAGER) }
            )
        }

        composable(Routes.PHOTOS) {
            val viewModel = remember {
                PhotoDuplicatesViewModel(
                    getDuplicatePhotosUseCase = container.getDuplicatePhotosUseCase,
                    deletePhotosUseCase = container.deletePhotosUseCase,
                    observeProStatusUseCase = container.observeProStatusUseCase,
                    checkBatchCleanAllowedUseCase = container.checkBatchCleanAllowedUseCase
                )
            }
            PhotoDuplicatesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToPaywall = { navController.navigate(Routes.PAYWALL) }
            )
        }

        composable(Routes.AUDIO) {
            val viewModel = remember {
                AudioDuplicatesViewModel(
                    getDuplicateAudioUseCase = container.getDuplicateAudioUseCase,
                    deleteAudioUseCase = container.deleteAudioUseCase,
                    checkBatchCleanAllowedUseCase = container.checkBatchCleanAllowedUseCase
                )
            }
            AudioDuplicatesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToPaywall = { navController.navigate(Routes.PAYWALL) }
            )
        }

        composable(Routes.CONTACTS) {
            val viewModel = remember {
                ContactsCleanerViewModel(
                    getDuplicateContactsUseCase = container.getDuplicateContactsUseCase,
                    mergeContactsUseCase = container.mergeContactsUseCase,
                    deleteContactUseCase = container.deleteContactUseCase,
                    observeProStatusUseCase = container.observeProStatusUseCase
                )
            }
            ContactsCleanerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToPaywall = { navController.navigate(Routes.PAYWALL) }
            )
        }

        composable(Routes.JUNK) {
            val viewModel = remember {
                JunkCleanerViewModel(
                    scanJunkUseCase = container.scanJunkUseCase,
                    cleanJunkUseCase = container.cleanJunkUseCase
                )
            }
            JunkCleanerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                isProUser = container.billingRepository.isProUser.value,
                interstitialAdManager = container.interstitialAdManager
            )
        }

        composable(Routes.PAYWALL) {
            val viewModel = remember {
                PaywallViewModel(
                    getSubscriptionPlansUseCase = container.getSubscriptionPlansUseCase,
                    purchasePlanUseCase = container.purchasePlanUseCase,
                    restorePurchasesUseCase = container.restorePurchasesUseCase
                )
            }
            PaywallScreen(
                viewModel = viewModel,
                onClose = { navController.popBackStack() }
            )
        }

        composable(Routes.COMPRESSOR) {
            val viewModel = remember {
                com.novaclean.app.presentation.viewmodel.MediaCompressorViewModel(
                    compressMediaUseCase = container.compressMediaUseCase,
                    observeProStatusUseCase = container.observeProStatusUseCase
                )
            }
            com.novaclean.app.presentation.screens.MediaCompressorScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigatePaywall = { navController.navigate(Routes.PAYWALL) },
                rewardedAdManager = container.rewardedAdManager
            )
        }

        composable(Routes.LARGE_FILES) {
            val viewModel = remember {
                com.novaclean.app.presentation.viewmodel.LargeFilesViewModel(
                    scanUseCase = container.scanLargeFilesUseCase,
                    deleteUseCase = container.deleteLargeFilesUseCase
                )
            }
            com.novaclean.app.presentation.screens.LargeFilesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.VAULT) {
            val viewModel = remember {
                com.novaclean.app.presentation.viewmodel.VaultViewModel(
                    vaultUseCases = container.vaultUseCases,
                    observeProStatusUseCase = container.observeProStatusUseCase
                )
            }
            com.novaclean.app.presentation.screens.VaultScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigatePaywall = { navController.navigate(Routes.PAYWALL) }
            )
        }

        composable(Routes.PASSWORD_MANAGER) {
            val viewModel = remember {
                com.novaclean.app.presentation.viewmodel.PasswordManagerViewModel(
                    getSavedPasswordsUseCase = container.getSavedPasswordsUseCase,
                    savePasswordUseCase = container.savePasswordUseCase,
                    deletePasswordUseCase = container.deletePasswordUseCase
                )
            }
            com.novaclean.app.presentation.screens.PasswordManagerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
