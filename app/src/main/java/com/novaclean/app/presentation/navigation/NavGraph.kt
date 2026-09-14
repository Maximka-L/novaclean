package com.novaclean.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

object Routes {
    const val DASHBOARD = "dashboard"
    const val PHOTOS = "photos"
    const val AUDIO = "audio"
    const val CONTACTS = "contacts"
    const val JUNK = "junk"
    const val PAYWALL = "paywall"
    const val PROFILE = "profile"
    const val PASSWORD_GEN = "password_gen"
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
                onNavigateToPaywall = { navController.navigate(Routes.PAYWALL) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(Routes.PROFILE) {
            val viewModel = remember {
                com.novaclean.app.presentation.viewmodel.ProfileViewModel(
                    observeUserProfileUseCase = container.observeUserProfileUseCase,
                    setNotificationsEnabledUseCase = container.setNotificationsEnabledUseCase
                )
            }
            com.novaclean.app.presentation.screens.ProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToPaywall = { navController.navigate(Routes.PAYWALL) },
                onNavigateToPasswordGen = { navController.navigate(Routes.PASSWORD_GEN) }
            )
        }

        composable(Routes.PASSWORD_GEN) {
            val viewModel = remember {
                com.novaclean.app.presentation.viewmodel.PasswordGeneratorViewModel(
                    generatorUseCase = container.passwordGeneratorUseCase
                )
            }
            com.novaclean.app.presentation.screens.PasswordGeneratorScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
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
                onBack = { navController.popBackStack() }
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
    }
}
