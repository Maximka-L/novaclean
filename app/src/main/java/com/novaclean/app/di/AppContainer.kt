package com.novaclean.app.di

import android.content.Context
import com.novaclean.app.data.datasource.ContactsDataSource
import com.novaclean.app.data.datasource.FileSystemDataSource
import com.novaclean.app.data.datasource.GooglePlayBillingDataSource
import com.novaclean.app.data.datasource.MediaStoreDataSource
import com.novaclean.app.data.datasource.MemoryDataSource
import com.novaclean.app.data.repository.AudioRepositoryImpl
import com.novaclean.app.data.repository.BillingRepositoryImpl
import com.novaclean.app.data.repository.ContactsRepositoryImpl
import com.novaclean.app.data.repository.JunkRepositoryImpl
import com.novaclean.app.data.repository.MemoryRepositoryImpl
import com.novaclean.app.data.repository.PhotoRepositoryImpl
import com.novaclean.app.domain.repository.AudioRepository
import com.novaclean.app.domain.repository.BillingRepository
import com.novaclean.app.domain.repository.ContactsRepository
import com.novaclean.app.domain.repository.JunkRepository
import com.novaclean.app.domain.repository.MemoryRepository
import com.novaclean.app.domain.repository.PhotoRepository
import com.novaclean.app.domain.usecase.CheckBatchCleanAllowedUseCase
import com.novaclean.app.domain.usecase.CleanJunkUseCase
import com.novaclean.app.domain.usecase.DeleteAudioUseCase
import com.novaclean.app.domain.usecase.DeleteContactUseCase
import com.novaclean.app.domain.usecase.DeletePhotosUseCase
import com.novaclean.app.domain.usecase.GetDuplicateAudioUseCase
import com.novaclean.app.domain.usecase.GetDuplicateContactsUseCase
import com.novaclean.app.domain.usecase.GetDuplicatePhotosUseCase
import com.novaclean.app.domain.usecase.GetRamInfoUseCase
import com.novaclean.app.domain.usecase.GetSubscriptionPlansUseCase
import com.novaclean.app.domain.usecase.MergeContactsUseCase
import com.novaclean.app.domain.usecase.ObserveProStatusUseCase
import com.novaclean.app.domain.usecase.OptimizeRamUseCase
import com.novaclean.app.domain.usecase.PurchasePlanUseCase
import com.novaclean.app.domain.usecase.RestorePurchasesUseCase
import com.novaclean.app.domain.usecase.ScanJunkUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Data Sources
    private val mediaStoreDataSource = MediaStoreDataSource(context)
    private val contactsDataSource = ContactsDataSource(context)
    private val fileSystemDataSource = FileSystemDataSource(context)
    private val memoryDataSource = MemoryDataSource(context)
    private val googlePlayBillingDataSource = GooglePlayBillingDataSource(context, applicationScope)

    // Repositories
    val photoRepository: PhotoRepository = PhotoRepositoryImpl(context, mediaStoreDataSource)
    val audioRepository: AudioRepository = AudioRepositoryImpl(context, mediaStoreDataSource)
    val contactsRepository: ContactsRepository = ContactsRepositoryImpl(contactsDataSource)
    val junkRepository: JunkRepository = JunkRepositoryImpl(fileSystemDataSource)
    val memoryRepository: MemoryRepository = MemoryRepositoryImpl(memoryDataSource)
    val billingRepository: BillingRepository = BillingRepositoryImpl(googlePlayBillingDataSource)
    val profileRepository: com.novaclean.app.domain.repository.ProfileRepository = com.novaclean.app.data.repository.ProfileRepositoryImpl(context, billingRepository, applicationScope)

    // Use Cases
    val getRamInfoUseCase = GetRamInfoUseCase(memoryRepository)
    val optimizeRamUseCase = OptimizeRamUseCase(memoryRepository)

    val getDuplicatePhotosUseCase = GetDuplicatePhotosUseCase(photoRepository)
    val deletePhotosUseCase = DeletePhotosUseCase(photoRepository)

    val getDuplicateAudioUseCase = GetDuplicateAudioUseCase(audioRepository)
    val deleteAudioUseCase = DeleteAudioUseCase(audioRepository)

    val getDuplicateContactsUseCase = GetDuplicateContactsUseCase(contactsRepository)
    val mergeContactsUseCase = MergeContactsUseCase(contactsRepository)
    val deleteContactUseCase = DeleteContactUseCase(contactsRepository)

    val scanJunkUseCase = ScanJunkUseCase(junkRepository)
    val cleanJunkUseCase = CleanJunkUseCase(junkRepository)

    val getSubscriptionPlansUseCase = GetSubscriptionPlansUseCase(billingRepository)
    val observeProStatusUseCase = ObserveProStatusUseCase(billingRepository)
    val purchasePlanUseCase = PurchasePlanUseCase(billingRepository)
    val restorePurchasesUseCase = RestorePurchasesUseCase(billingRepository)
    val checkBatchCleanAllowedUseCase = CheckBatchCleanAllowedUseCase(billingRepository)

    val observeUserProfileUseCase = com.novaclean.app.domain.usecase.ObserveUserProfileUseCase(profileRepository)
    val setNotificationsEnabledUseCase = com.novaclean.app.domain.usecase.SetNotificationsEnabledUseCase(profileRepository)
    val addCleanedBytesUseCase = com.novaclean.app.domain.usecase.AddCleanedBytesUseCase(profileRepository)
    val registerProfileUseCase = com.novaclean.app.domain.usecase.RegisterProfileUseCase(profileRepository)

    val passwordGeneratorUseCase = com.novaclean.app.domain.usecase.PasswordGeneratorUseCase()

    // Media Compressor
    val compressorRepository: com.novaclean.app.domain.repository.CompressorRepository =
        com.novaclean.app.data.repository.CompressorRepositoryImpl(context)
    val compressMediaUseCase = com.novaclean.app.domain.usecase.CompressMediaUseCase(compressorRepository, billingRepository)

    // Large Files Finder
    val largeFilesRepository: com.novaclean.app.domain.repository.LargeFilesRepository =
        com.novaclean.app.data.repository.LargeFilesRepositoryImpl(context)
    val scanLargeFilesUseCase = com.novaclean.app.domain.usecase.ScanLargeFilesUseCase(largeFilesRepository)
    val deleteLargeFilesUseCase = com.novaclean.app.domain.usecase.DeleteLargeFilesUseCase(largeFilesRepository)

    // Private Vault
    val vaultRepository: com.novaclean.app.domain.repository.VaultRepository =
        com.novaclean.app.data.repository.VaultRepositoryImpl(context)
    val vaultUseCases = com.novaclean.app.domain.usecase.VaultUseCases(vaultRepository)

    // Password Manager
    val passwordManagerRepository: com.novaclean.app.domain.repository.PasswordManagerRepository =
        com.novaclean.app.data.repository.PasswordManagerRepositoryImpl(context)
    val getSavedPasswordsUseCase = com.novaclean.app.domain.usecase.GetSavedPasswordsUseCase(passwordManagerRepository)
    val savePasswordUseCase = com.novaclean.app.domain.usecase.SavePasswordUseCase(passwordManagerRepository)
    val deletePasswordUseCase = com.novaclean.app.domain.usecase.DeletePasswordUseCase(passwordManagerRepository)
}
