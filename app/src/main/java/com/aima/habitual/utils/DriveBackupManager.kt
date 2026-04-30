package com.aima.habitual.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * DriveBackupManager handles all Google Drive communication for backup/restore.
 *
 * Uses the Drive REST API v3 with the `appDataFolder` — a hidden folder
 * on the user's Drive that only this app can access. The backup file is
 * invisible to the user in their Drive UI.
 *
 * Auth strategy: Uses the legacy GoogleSignIn API (not Credential Manager)
 * because Credential Manager does not support OAuth scopes. This is a
 * separate flow from the main app login — it only requests Drive access
 * when the user explicitly taps Backup or Restore.
 */
object DriveBackupManager {

    private const val TAG = "DriveBackupManager"
    private const val BACKUP_FILE_NAME = "habitual_backup.json"
    private const val MIME_TYPE = "application/json"

    /** The OAuth scope granting access to the hidden appDataFolder only. */
    private val DRIVE_SCOPE = Scope(DriveScopes.DRIVE_APPDATA)

    // ─── AUTH ─────────────────────────────────────────────────────────

    /**
     * Builds the GoogleSignInOptions requesting Drive appdata scope.
     * This is separate from the app's main Credential Manager login.
     */
    fun getSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(DRIVE_SCOPE)
            .build()
    }

    /**
     * Returns true if the user has already granted Drive access in this session.
     */
    fun isAuthorized(context: Context): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && GoogleSignIn.hasPermissions(account, DRIVE_SCOPE)
    }

    /**
     * Builds a Drive service instance from a signed-in Google account.
     * Must be called on a background thread.
     */
    private fun buildDriveService(context: Context, account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Habitual")
            .build()
    }

    // ─── BACKUP (UPLOAD) ──────────────────────────────────────────────

    /**
     * Uploads a JSON string to the appDataFolder on Google Drive.
     * If a backup file already exists, it is updated (not duplicated).
     *
     * @param context Application context for credential access.
     * @param account The authorized Google account.
     * @param jsonContent The serialized backup JSON string.
     * @return true on success, false on failure.
     */
    suspend fun backup(
        context: Context,
        account: GoogleSignInAccount,
        jsonContent: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val driveService = buildDriveService(context, account)
            val existingFileId = findBackupFileId(driveService)

            val content = ByteArrayContent.fromString(MIME_TYPE, jsonContent)

            if (existingFileId != null) {
                // Update existing file (avoids duplicate backups)
                driveService.files().update(existingFileId, null, content).execute()
                Log.d(TAG, "Backup updated: $existingFileId")
            } else {
                // Create new file in appDataFolder
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = BACKUP_FILE_NAME
                    parents = listOf("appDataFolder")
                }
                val created = driveService.files().create(fileMetadata, content)
                    .setFields("id")
                    .execute()
                Log.d(TAG, "Backup created: ${created.id}")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed", e)
            false
        }
    }

    // ─── RESTORE (DOWNLOAD) ───────────────────────────────────────────

    /**
     * Downloads the backup JSON string from appDataFolder.
     *
     * @return The JSON string, or null if no backup exists or on failure.
     */
    suspend fun restore(
        context: Context,
        account: GoogleSignInAccount
    ): String? = withContext(Dispatchers.IO) {
        try {
            val driveService = buildDriveService(context, account)
            val fileId = findBackupFileId(driveService)

            if (fileId == null) {
                Log.d(TAG, "No backup found in appDataFolder")
                return@withContext null
            }

            val inputStream = driveService.files().get(fileId)
                .executeMediaAsInputStream()
            val json = inputStream.bufferedReader().use { it.readText() }
            Log.d(TAG, "Restore downloaded ${json.length} chars")
            json
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            null
        }
    }

    // ─── LAST BACKUP TIME ─────────────────────────────────────────────

    /**
     * Retrieves the last modified time of the backup file.
     *
     * @return Epoch millis of last backup, or null if no backup exists.
     */
    suspend fun getLastBackupTime(
        context: Context,
        account: GoogleSignInAccount
    ): Long? = withContext(Dispatchers.IO) {
        try {
            val driveService = buildDriveService(context, account)
            val result = driveService.files().list()
                .setSpaces("appDataFolder")
                .setFields("files(id, name, modifiedTime)")
                .setQ("name = '$BACKUP_FILE_NAME'")
                .execute()

            val file = result.files?.firstOrNull() ?: return@withContext null
            file.modifiedTime?.value
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get last backup time", e)
            null
        }
    }

    // ─── HELPERS ──────────────────────────────────────────────────────

    /**
     * Finds the file ID of the existing backup in appDataFolder.
     * Returns null if no backup exists yet.
     */
    private fun findBackupFileId(driveService: Drive): String? {
        val result = driveService.files().list()
            .setSpaces("appDataFolder")
            .setFields("files(id, name)")
            .setQ("name = '$BACKUP_FILE_NAME'")
            .execute()

        return result.files?.firstOrNull()?.id
    }
}
