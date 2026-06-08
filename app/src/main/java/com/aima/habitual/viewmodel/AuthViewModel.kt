package com.aima.habitual.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.aima.habitual.data.HabitualDatabase
import com.aima.habitual.data.OfflineAppRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val db = HabitualDatabase.getInstance(application)
    private val repository = OfflineAppRepository(db.habitDao())
    private val prefs = application.getSharedPreferences("habitual_prefs", Context.MODE_PRIVATE)

    private val securePrefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(application)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                application,
                "habitual_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("AuthViewModel", "EncryptedSharedPreferences failed, using fallback", e)
            application.getSharedPreferences("habitual_secure_fallback", Context.MODE_PRIVATE)
        }
    }

    var userName by mutableStateOf(prefs.getString("user_name", "Ritual Specialist") ?: "Ritual Specialist")
        private set

    var profileImageUri by mutableStateOf<Uri?>(null)
        private set

    var isLoggedIn by mutableStateOf(prefs.getBoolean("is_logged_in", false))
        private set

    var isBiometricEnabled by mutableStateOf(prefs.getBoolean("biometric_login_enabled", false))
        private set

    val isBiometricAvailable: Boolean
        get() {
            if (!isBiometricEnabled) return false
            return prefs.getBoolean("has_authenticated_before", false)
        }

    var loginError by mutableStateOf<String?>(null)
        private set
        
    var databaseError by mutableStateOf<String?>(null)
        private set

    fun clearDatabaseError() {
        databaseError = null
    }

    // Callbacks for other ViewModels to clear their caches
    var onUserChanged: (() -> Unit)? = null

    init {
        val savedImage = prefs.getString("user_image", null)
        if (savedImage != null) {
            try {
                profileImageUri = Uri.parse(savedImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateUserName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotBlank()) {
            userName = trimmed
            prefs.edit().putString("user_name", trimmed).apply()
        }
    }

    fun updateProfileImage(uri: Uri) {
        try {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
            getApplication<Application>().contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        profileImageUri = uri
        prefs.edit().putString("user_image", uri.toString()).apply()
    }

    fun toggleBiometricLogin(enabled: Boolean) {
        isBiometricEnabled = enabled
        prefs.edit().putBoolean("biometric_login_enabled", enabled).apply()
    }

    private fun markAuthenticated() {
        prefs.edit().putBoolean("has_authenticated_before", true).apply()
    }

    fun registerUser(name: String, email: String, pass: String, onComplete: (Boolean) -> Unit) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val lastEmail = securePrefs.getString("user_email", null)
                    if (lastEmail != null && lastEmail != email) {
                        viewModelScope.launch {
                            try { repository.deleteAllUserData() } catch (e: Exception) {}
                            onUserChanged?.invoke()
                        }
                    }

                    securePrefs.edit().putString("user_email", email).apply()

                    val user = FirebaseAuth.getInstance().currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    user?.updateProfile(profileUpdates)

                    prefs.edit().apply {
                        putString("user_name", name)
                        putBoolean("is_logged_in", true)
                    }.apply()

                    userName = name
                    isLoggedIn = true
                    loginError = null
                    markAuthenticated()
                    onComplete(true)
                } else {
                    loginError = task.exception?.message ?: "Registration failed."
                    onComplete(false)
                }
            }
    }

    fun logout() {
        prefs.edit().apply {
            putBoolean("is_logged_in", false)
        }.apply()

        FirebaseAuth.getInstance().signOut()
        isLoggedIn = false
    }

    fun deleteProfile() {
        FirebaseAuth.getInstance().currentUser?.delete()
        
        prefs.edit().clear().apply()
        try {
            securePrefs.edit().clear().apply()
        } catch (e: Exception) {
            Log.w("AuthViewModel", "Could not clear secure preferences", e)
        }

        onUserChanged?.invoke()

        viewModelScope.launch {
            try {
                repository.deleteAllUserData()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to clear profile databases", e)
                databaseError = "Profile deletion incomplete. Some database records may still exist locally."
            }
        }

        userName = "Ritual Specialist"
        profileImageUri = null
        isLoggedIn = false
        loginError = null
    }

    fun validateLogin(email: String, pass: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val lastEmail = securePrefs.getString("user_email", null)
                    if (lastEmail != null && lastEmail != email) {
                        viewModelScope.launch {
                            try { repository.deleteAllUserData() } catch (e: Exception) {}
                            onUserChanged?.invoke()
                        }
                    }
                    securePrefs.edit().putString("user_email", email).apply()

                    val user = FirebaseAuth.getInstance().currentUser
                    user?.displayName?.let { name ->
                        userName = name
                        prefs.edit().putString("user_name", name).apply()
                    }
                    loginError = null
                    isLoggedIn = true
                    prefs.edit().putBoolean("is_logged_in", true).apply()
                    markAuthenticated()
                } else {
                    val ex = task.exception
                    loginError = when (ex) {
                        is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "No account found. Please register."
                        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
                        else -> ex?.message ?: "Invalid email or password."
                    }
                }
            }
    }

    fun verifyUserPassword(pass: String, onResult: (Boolean) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && user.email != null) {
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, pass)
            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    onResult(task.isSuccessful)
                }
        } else {
            onResult(false)
        }
    }

    fun clearLoginError() {
        loginError = null
    }

    // --- GOOGLE DRIVE BACKUP / RESTORE ---

    sealed class BackupState {
        object Idle : BackupState()
        object InProgress : BackupState()
        data class Success(val message: String) : BackupState()
        data class Error(val message: String) : BackupState()
    }

    var backupState by mutableStateOf<BackupState>(BackupState.Idle)
        private set

    var lastBackupTime by mutableStateOf<Long?>(null)
        private set

    fun clearBackupState() {
        backupState = BackupState.Idle
    }

    fun handleDriveSignInResult(
        account: GoogleSignInAccount?,
        isBackup: Boolean
    ) {
        if (account == null) {
            backupState = BackupState.Error("Google authorization cancelled.")
            return
        }
        if (isBackup) {
            performBackup(account)
        } else {
            performRestore(account)
        }
    }

    private fun performBackup(account: GoogleSignInAccount) {
        backupState = BackupState.InProgress
        viewModelScope.launch {
            try {
                val backupData = com.aima.habitual.data.BackupData(
                    habits = repository.getAllHabitsSnapshot(),
                    records = repository.getAllRecordsSnapshot(),
                    diaryEntries = repository.getAllDiaryEntriesSnapshot(),
                    wellbeingStats = repository.getAllWellbeingStatsSnapshot(),
                    sleepLogs = repository.getAllSleepLogsSnapshot()
                )

                val gson = com.google.gson.Gson()
                val json = gson.toJson(backupData)

                val success = com.aima.habitual.utils.DriveBackupManager.backup(
                    getApplication(), account, json
                )

                if (success) {
                    lastBackupTime = System.currentTimeMillis()
                    backupState = BackupState.Success("Backup successful!")
                } else {
                    backupState = BackupState.Error("Backup failed. Please try again.")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Backup failed", e)
                backupState = BackupState.Error("Backup failed: ${e.localizedMessage}")
            }
        }
    }

    private fun performRestore(account: GoogleSignInAccount) {
        backupState = BackupState.InProgress
        viewModelScope.launch {
            try {
                val json = com.aima.habitual.utils.DriveBackupManager.restore(
                    getApplication(), account
                )

                if (json == null) {
                    backupState = BackupState.Error("No backup found on Google Drive.")
                    return@launch
                }

                val gson = com.google.gson.Gson()
                val backupData = gson.fromJson(json, com.aima.habitual.data.BackupData::class.java)

                repository.restoreAllUserData(
                    backupData.habits,
                    backupData.records,
                    backupData.diaryEntries,
                    backupData.wellbeingStats,
                    backupData.sleepLogs
                )
                
                onUserChanged?.invoke()

                backupState = BackupState.Success("Restore successful! Your data has been recovered.")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Restore failed", e)
                backupState = BackupState.Error("Restore failed: ${e.localizedMessage}")
            }
        }
    }

    fun refreshLastBackupTime(account: GoogleSignInAccount?) {
        if (account == null) return
        viewModelScope.launch {
            lastBackupTime = com.aima.habitual.utils.DriveBackupManager.getLastBackupTime(
                getApplication(), account
            )
        }
    }

    fun signInWithGoogle(context: Context, webClientId: String) {
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val name = googleIdTokenCredential.displayName ?: "User"
                val email = googleIdTokenCredential.id

                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            viewModelScope.launch {
                                val lastEmail = securePrefs.getString("user_email", null)
                                if (lastEmail != null && lastEmail != email) {
                                    try { repository.deleteAllUserData() } catch (e: Exception) {}
                                    onUserChanged?.invoke()
                                }

                                prefs.edit().apply {
                                    putString("user_name", name)
                                    putBoolean("is_logged_in", true)
                                }.apply()

                                securePrefs.edit().apply {
                                    putString("user_email", email)
                                }.apply()

                                userName = name
                                isLoggedIn = true
                                loginError = null
                                markAuthenticated()
                                Log.d("AuthViewModel", "Google Sign-In successful")
                            }
                        } else {
                            loginError = task.exception?.message ?: "Firebase authentication failed."
                        }
                    }
            } catch (e: GetCredentialCancellationException) {
                Log.d("AuthViewModel", "Google Sign-In cancelled by user")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Google Sign-In failed", e)
                loginError = "Google Sign-In failed. Please try another method."
            }
        }
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                isLoggedIn = true
                prefs.edit().putBoolean("is_logged_in", true).apply()
                loginError = null
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    loginError = errString.toString()
                    onFailure(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val biometricManager = BiometricManager.from(activity)
        val canStrong = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
        val canWeak = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS

        val title = activity.getString(com.aima.habitual.R.string.biometric_prompt_title)
        val subtitle = activity.getString(com.aima.habitual.R.string.biometric_prompt_subtitle)
        val cancelText = activity.getString(com.aima.habitual.R.string.biometric_prompt_cancel)

        val promptInfo = when {
            canStrong -> {
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setNegativeButtonText(cancelText)
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .build()
            }
            canWeak -> {
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setNegativeButtonText(cancelText)
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                    .build()
            }
            else -> {
                onFailure("No biometric hardware available")
                return
            }
        }

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e("AuthViewModel", "BiometricPrompt.authenticate failed", e)
            onFailure("Biometric authentication unavailable: ${e.localizedMessage}")
        }
    }
}
