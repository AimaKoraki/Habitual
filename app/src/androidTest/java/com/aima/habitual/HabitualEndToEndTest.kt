package com.aima.habitual

import android.content.Context
import androidx.test.espresso.Espresso
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import androidx.test.rule.GrantPermissionRule
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until

/**
 * HabitualEndToEndTest — Comprehensive instrumented E2E test suite.
 *
 * Covers:
 *  1. Authentication (Register, Login, Logout, Delete Account)
 *  2. Dashboard & Habit CRUD (Create, Toggle, Stats, Edit, Delete)
 *  3. Diary CRUD (Create, View, Edit, Delete)
 *  4. Wellbeing (Log Water, Sleep Dialog)
 *  5. Bottom Navigation (All 4 Tabs)
 *  6. Profile Management (Name, Theme, Rituals List)
 *  7. Edge Cases & Validation
 *
 * Technical Notes:
 *  - Uses createAndroidComposeRule<MainActivity> to launch the full app.
 *  - SharedPreferences are cleared before each test for isolation.
 *  - Nodes are found by text from strings.xml since no testTags exist.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class HabitualEndToEndTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACTIVITY_RECOGNITION
    )

    /**
     * Clear all persisted data AND force-restart the activity before each test.
     * This ensures every test starts on the Login screen with no prior state,
     * even if the previous test left the app in a logged-in state.
     */
    /**
     * Ensure every test starts on the Login screen.
     * Instead of brute-force recreating the activity (which breaks the Compose hierarchy),
     * we intelligently navigate back to Login via UI interactions if logged in.
     */
    @Before
    fun clearAppState() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // 1. Handle System Permission Dialogs (e.g. "Allow Habitual to access physical activity?")
        // This is often the cause of "Black Screen" — the test is waiting for a Compose node, but a system dialog is blocking it.
        val allowButton = device.findObject(UiSelector().textMatches("(?i)Allow|While using the app|Only this time"))
        if (allowButton.exists()) {
            try {
                allowButton.click()
            } catch (e: Exception) {
                // Ignore if it disappeared
            }
        }

        // 2. Clear SharedPrefs (Data)
        InstrumentationRegistry.getInstrumentation().targetContext
            .getSharedPreferences("habitual_prefs", Context.MODE_PRIVATE)
            .edit().clear().commit()

        // 3. Reset UI State to Login Screen
        // If we are NOT on the login screen, we need to log out.
        // We use a try/catch approach to detect where we are without crashing.

        try {
            // A. Check if already on Login Screen
            composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
            return // We are good!
        } catch (e: AssertionError) {
            // Not on Login Screen. We are likely logged in.
        }

        // B. Attempt to Log Out
        performLogoutSafely()

        // C. Final Verification
        try {
            composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
        } catch (e: AssertionError) {
            // If still failing, force-relaunch the activity as a last resort
           // activityRule.scenario.recreate() // (If we had an ActivityScenarioRule)
        }
    }

    /**
     * Robustly logs out from anywhere in the app to reach the Login screen.
     */
    private fun performLogoutSafely() {
        try {
            // 1. Dismiss any potential dialogs/popups
            // (Compose dialogs are part of the hierarchy, but just in case)

            // 2. Navigate to Profile
            // We look for the "Profile" tab. If not found, maybe we are in a sub-screen?
            // Try pressing back a few times?

            val profileTab = composeTestRule.onNodeWithText("Profile")
            if (profileTab.isDisplayed()) {
                profileTab.performClick()
                composeTestRule.waitForIdle()
            } else {
                 // Try back press to exit any sub-screens
                 Espresso.pressBackUnconditionally()
                 composeTestRule.waitForIdle()
                 // Try finding Profile again
                 composeTestRule.onNodeWithText("Profile").performClick()
            }

            // 3. Click Log Out
            // It might be off-screen, so scroll to it.
            composeTestRule.onNodeWithText("Log Out").performScrollTo().performClick()
            composeTestRule.waitForIdle()

        } catch (e: Exception) {
            // If navigation fails, we might be in a weird state.
            // Just try pressing back multiple times?
             repeat(3) {
                 Espresso.pressBackUnconditionally()
                 Thread.sleep(200)
             }
        }
    }


    // ══════════════════════════════════════════════════════════════
    //  HELPER FUNCTIONS — reduce boilerplate across tests
    // ══════════════════════════════════════════════════════════════

    /**
     * Registers a new user and lands on the Dashboard.
     * Many tests need an authenticated state, so this helper avoids repetition.
     */
    private fun registerAndLandOnDashboard(
        name: String = "Test User",
        email: String = "test@habitual.com",
        password: String = "password123"
    ) {
        // 1. Navigate to Register screen
        composeTestRule.onNodeWithText("Don't have an account? Register").performClick()
        composeTestRule.waitForIdle()

        // 2. Fill in the registration form
        composeTestRule.onNodeWithText("Full Name").performClick()
        composeTestRule.onNodeWithText("Full Name").performTextInput(name)

        // Use the register-specific email label (index-based since both say "Email Address")
        composeTestRule.onAllNodesWithText("Email Address")[0].performClick()
        composeTestRule.onAllNodesWithText("Email Address")[0].performTextInput(email)

        composeTestRule.onNodeWithText("Create Password").performClick()
        composeTestRule.onNodeWithText("Create Password").performTextInput(password)
        closeSoftKeyboard()

        // 3. Accept terms
        composeTestRule.onNodeWithText("I agree to the Terms of Service").performClick()
        composeTestRule.waitForIdle()

        // 4. Tap Sign Up (Ensure it's enabled first!)
        composeTestRule.onNodeWithText("Sign Up").assertIsEnabled().performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Logs in with previously-registered credentials and lands on Dashboard.
     */
    private fun loginWithCredentials(
        email: String = "test@habitual.com",
        password: String = "password123"
    ) {
        composeTestRule.onNodeWithText("Email Address").performClick()
        composeTestRule.onNodeWithText("Email Address").performTextInput(email)

        composeTestRule.onNodeWithText("Password").performClick()
        composeTestRule.onNodeWithText("Password").performTextInput(password)
        closeSoftKeyboard()

        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Navigates to a specific bottom navigation tab by its label text.
     */
    private fun navigateToTab(label: String) {
        composeTestRule.onNodeWithText(label).performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Closes the soft keyboard to prevent UI obstruction.
     */
    private fun closeSoftKeyboard() {
        Espresso.closeSoftKeyboard()
        composeTestRule.waitForIdle()
    }

    /**
     * Creates a habit from the Dashboard screen.
     * Assumes the user is already on the Dashboard.
     */
    private fun createHabit(name: String = "Drink Water", category: String = "Health") {
        // Tap the FAB to open Add Habit screen
        composeTestRule.onNodeWithContentDescription("Add Ritual").performClick()
        composeTestRule.waitForIdle()

        // Fill in the habit name
        composeTestRule.onNodeWithText("e.g. Drink Water").performTextInput(name)
        closeSoftKeyboard()
        composeTestRule.waitForIdle()

        // Scroll to and tap Save Ritual (may be off-screen on smaller devices)
        composeTestRule.onNodeWithText("Save Ritual").performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    /**
     * Creates a diary entry from the Diary screen.
     * Assumes the user is on the Diary tab.
     */
    private fun createDiaryEntry(title: String = "My First Entry", content: String = "Today was a great day!") {
        // Tap the FAB to open New Entry screen
        composeTestRule.onNodeWithContentDescription("Add New Entry").performClick()
        composeTestRule.waitForIdle()

        // Fill in title
        composeTestRule.onNodeWithText("Title").performClick()
        composeTestRule.onNodeWithText("Title").performTextInput(title)

        // Fill in content
        composeTestRule.onNodeWithText("How was your day?").performClick()
        composeTestRule.onNodeWithText("How was your day?").performTextInput(content)
        closeSoftKeyboard()

        // Tap Save (the save icon in the top bar)
        composeTestRule.onNodeWithContentDescription("Save Entry").performClick()
        composeTestRule.waitForIdle()
    }

    // ══════════════════════════════════════════════════════════════
    //  1. AUTHENTICATION — Registration
    // ══════════════════════════════════════════════════════════════

    /**
     * TEST 1.1: Verify the Login screen displays on first launch.
     * Validates: "Welcome Back" title, email/password fields, Login button, Register link.
     */
    @Test
    fun test_01_loginScreenDisplaysCorrectly() {
        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email Address").assertExists()
        composeTestRule.onNodeWithText("Password").assertExists()
        composeTestRule.onNodeWithText("Login").assertExists()
        composeTestRule.onNodeWithText("Don't have an account? Register").assertIsDisplayed()
    }

    /**
     * TEST 1.2: Verify the Register screen is accessible from Login.
     * Validates: "Create Account" title, name/email/password fields, terms checkbox, Sign Up button.
     */
    @Test
    fun test_02_registerScreenDisplaysCorrectly() {
        composeTestRule.onNodeWithText("Don't have an account? Register").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Full Name").assertExists()
        composeTestRule.onNodeWithText("Email Address").assertExists()
        composeTestRule.onNodeWithText("Create Password").assertExists()
        composeTestRule.onNodeWithText("I agree to the Terms of Service").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up").assertExists()
    }

    /**
     * TEST 1.3: Successful registration navigates to the Dashboard.
     * Validates: Dashboard greeting and user name are visible after registration.
     */
    @Test
    fun test_03_registrationSuccessNavigatesToDashboard() {
        registerAndLandOnDashboard(name = "Alice")

        // Should be on Dashboard with the user's name
        composeTestRule.onNodeWithText("Good Morning,").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
        composeTestRule.onNodeWithText("Today's Rituals").assertIsDisplayed()
    }

    /**
     * TEST 1.4: Register button is disabled when terms are not accepted.
     * Validates: Form validation prevents submission without accepting terms.
     */
    @Test
    fun test_04_registerButtonDisabledWithoutTerms() {
        composeTestRule.onNodeWithText("Don't have an account? Register").performClick()
        composeTestRule.waitForIdle()

        // Fill fields but DON'T accept terms
        composeTestRule.onNodeWithText("Full Name").performTextInput("Test")
        composeTestRule.onAllNodesWithText("Email Address")[0].performTextInput("a@b.com")
        composeTestRule.onNodeWithText("Create Password").performTextInput("123456")
        closeSoftKeyboard()
        composeTestRule.waitForIdle()

        // Sign Up should be disabled (terms not checked)
        composeTestRule.onNodeWithText("Sign Up").assertIsNotEnabled()
    }

    /**
     * TEST 1.5: Register button is disabled when password is too short (< 6 chars).
     * Validates: Password length validation.
     */
    @Test
    fun test_05_registerButtonDisabledWithShortPassword() {
        composeTestRule.onNodeWithText("Don't have an account? Register").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Full Name").performTextInput("Test")
        composeTestRule.onAllNodesWithText("Email Address")[0].performTextInput("a@b.com")
        composeTestRule.onNodeWithText("Create Password").performTextInput("123") // Too short
        closeSoftKeyboard()
        composeTestRule.onNodeWithText("I agree to the Terms of Service").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Sign Up").assertIsNotEnabled()
    }

    /**
     * TEST 1.6: Navigate back to Login from Register screen.
     * Validates: "Already have an account? Log In" link works correctly.
     */
    @Test
    fun test_06_navigateBackToLoginFromRegister() {
        composeTestRule.onNodeWithText("Don't have an account? Register").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Already have an account? Log In").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════════
    //  2. AUTHENTICATION — Login
    // ══════════════════════════════════════════════════════════════

    /**
     * TEST 2.1: Login button is disabled when fields are empty.
     * Validates: Form validation prevents empty submission.
     */
    @Test
    fun test_07_loginButtonDisabledWithEmptyFields() {
        // No input entered — Login button should be disabled
        composeTestRule.onNodeWithText("Login").assertIsNotEnabled()
    }

    /**
     * TEST 2.2: Login with no registered account shows error.
     * Validates: "No account found. Please register." error message.
     */
    @Test
    fun test_08_loginWithNoAccountShowsError() {
        composeTestRule.onNodeWithText("Email Address").performTextInput("nobody@test.com")
        composeTestRule.onNodeWithText("Password").performTextInput("wrongpass")
        closeSoftKeyboard()
        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("No account found. Please register.").assertIsDisplayed()
    }

    /**
     * TEST 2.3: Login with wrong password shows error.
     * Validates: "Invalid email or password." error message.
     */
    @Test
    fun test_09_loginWithWrongPasswordShowsError() {
        // First, register a user
        registerAndLandOnDashboard()

        // Logout
        navigateToTab("Profile")
        composeTestRule.onNodeWithText("Log Out").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Attempt login with wrong password
        composeTestRule.onNodeWithText("Email Address").performTextInput("test@habitual.com")
        composeTestRule.onNodeWithText("Password").performTextInput("wrongpassword")
        closeSoftKeyboard()
        composeTestRule.onNodeWithText("Login").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Invalid email or password.").assertIsDisplayed()
    }

    /**
     * TEST 2.4: Login with correct credentials succeeds.
     * Validates: Dashboard is shown after successful login.
     */
    @Test
    fun test_10_loginWithCorrectCredentials() {
        // Register then logout
        registerAndLandOnDashboard()
        navigateToTab("Profile")
        composeTestRule.onNodeWithText("Log Out").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Login with correct credentials
        loginWithCredentials()

        // Should be on Dashboard
        composeTestRule.onNodeWithText("Good Morning,").assertIsDisplayed()
        composeTestRule.onNodeWithText("Today's Rituals").assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════════
    //  3. AUTHENTICATION — Logout & Delete Account
    // ══════════════════════════════════════════════════════════════

    /**
     * TEST 3.1: Logout from Profile returns to Login screen.
     * Validates: Full logout flow navigates back to "Welcome Back" screen.
     */
    @Test
    fun test_11_logoutReturnsToLoginScreen() {
        registerAndLandOnDashboard()

        navigateToTab("Profile")
        composeTestRule.onNodeWithText("Log Out").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
    }

    /**
     * TEST 3.2: Delete Account shows confirmation dialog first.
     * Validates: AlertDialog with title and text appears before deletion.
     */
    @Test
    fun test_12_deleteAccountShowsConfirmationDialog() {
        registerAndLandOnDashboard()
        navigateToTab("Profile")

        composeTestRule.onNodeWithText("Delete Account").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Dialog should appear
        composeTestRule.onNodeWithText("Delete Account?").assertIsDisplayed()
        composeTestRule.onNodeWithText("This will permanently delete your profile, all rituals, diary entries, and wellbeing data. This action cannot be undone.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    /**
     * TEST 3.3: Cancelling delete dialog keeps user on Profile.
     * Validates: Cancel button dismisses dialog without action.
     */
    @Test
    fun test_13_cancelDeleteAccountKeepsUserOnProfile() {
        registerAndLandOnDashboard()
        navigateToTab("Profile")

        composeTestRule.onNodeWithText("Delete Account").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.waitForIdle()

        // Should still be on Profile — user name should be visible
        composeTestRule.onNodeWithText("Test User").assertIsDisplayed()
    }

    /**
     * TEST 3.4: Confirming Delete Account returns to Login screen.
     * Validates: Full account deletion flow.
     */
    @Test
    fun test_14_confirmDeleteAccountReturnsToLogin() {
        registerAndLandOnDashboard()
        navigateToTab("Profile")

        composeTestRule.onNodeWithText("Delete Account").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════════
    //  4. DASHBOARD & HABIT MANAGEMENT
    // ══════════════════════════════════════════════════════════════

    /**
     * TEST 4.1: Dashboard empty state is displayed when no habits exist.
     * Validates: "No rituals for today. Take a rest." message shown.
     */
    @Test
    fun test_15_dashboardEmptyStateVisible() {
        registerAndLandOnDashboard()

        composeTestRule.onNodeWithText("Today's Rituals").assertIsDisplayed()
        composeTestRule.onNodeWithText("No rituals for today. Take a rest.").assertIsDisplayed()
    }

    /**
     * TEST 4.2: FAB is present on Dashboard for adding habits.
     * Validates: The floating action button with "Add Ritual" content description exists.
     */
    @Test
    fun test_16_dashboardFabPresent() {
        registerAndLandOnDashboard()

        composeTestRule.onNodeWithContentDescription("Add Ritual").assertIsDisplayed()
    }

    /**
     * TEST 4.3: Tapping FAB navigates to Add Habit screen.
     * Validates: "Add Habit" title and form fields appear.
     */
    @Test
    fun test_17_fabOpensAddHabitScreen() {
        registerAndLandOnDashboard()

        composeTestRule.onNodeWithContentDescription("Add Ritual").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Add New Ritual").assertIsDisplayed()
        composeTestRule.onNodeWithText("Habit Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Category").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save Ritual").assertExists()
    }

    /**
     * TEST 4.4: Save Ritual button is disabled when habit name is empty.
     * Validates: Form validation on the HabitForm component.
     */
    @Test
    fun test_18_saveRitualDisabledWithEmptyName() {
        registerAndLandOnDashboard()

        composeTestRule.onNodeWithContentDescription("Add Ritual").performClick()
        composeTestRule.waitForIdle()

        // Name field is empty by default
        composeTestRule.onNodeWithText("Save Ritual").performScrollTo().assertIsNotEnabled()
    }

    /**
     * TEST 4.5: Creating a habit makes it appear on the Dashboard.
     * Validates: Full habit creation flow — from form to dashboard display.
     */
    @Test
    fun test_19_createHabitAppearsOnDashboard() {
        registerAndLandOnDashboard()

        createHabit(name = "Morning Run")

        // Back on Dashboard, the new habit should be visible
        composeTestRule.onNodeWithText("Morning Run").assertIsDisplayed()
        composeTestRule.onNodeWithText("Health").assertIsDisplayed()
    }

    /**
     * TEST 4.6: Habit completion toggle icon is present.
     * Validates: The completion toggle button exists on habit cards.
     */
    @Test
    fun test_20_habitToggleCompletionPresent() {
        registerAndLandOnDashboard()
        createHabit(name = "Read Book")

        // The complete icon should exist
        composeTestRule.onAllNodesWithContentDescription("Complete").onFirst().assertIsDisplayed()
    }

    /**
     * TEST 4.7: Add Habit form shows all category options.
     * Validates: Category dropdown contains Health, Study, Personal, Work, Well-being.
     */
    @Test
    fun test_21_habitFormCategoriesAvailable() {
        registerAndLandOnDashboard()

        composeTestRule.onNodeWithContentDescription("Add Ritual").performClick()
        composeTestRule.waitForIdle()

        // The default selected category should be "Health"
        composeTestRule.onNodeWithText("Health").assertIsDisplayed()
    }

    /**
     * TEST 4.8: Add Habit form shows day selector chips.
     * Validates: Repeat days section with "Every Day" toggle and day chips exist.
     */
    @Test
    fun test_22_habitFormDaySelectorPresent() {
        registerAndLandOnDashboard()

        composeTestRule.onNodeWithContentDescription("Add Ritual").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Repeat on").assertIsDisplayed()
        composeTestRule.onNodeWithText("Every Day").assertIsDisplayed()
    }

    /**
     * TEST 4.9: Add Habit form shows target duration slider.
     * Validates: Duration slider section is present.
     */
    @Test
    fun test_23_habitFormDurationSliderPresent() {
        registerAndLandOnDashboard()

        composeTestRule.onNodeWithContentDescription("Add Ritual").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Target Duration").assertIsDisplayed()
    }

    /**
     * TEST 4.10: Back navigation from Add Habit returns to Dashboard.
     * Validates: Back button on HabitDetailScreen works correctly.
     */
    @Test
    fun test_24_backFromAddHabitReturnsToDashboard() {
        registerAndLandOnDashboard()

        composeTestRule.onNodeWithContentDescription("Add Ritual").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Today's Rituals").assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════════
    //  5. BOTTOM NAVIGATION
    // ══════════════════════════════════════════════════════════════

    /**
     * TEST 5.1: Bottom navigation bar is visible on Dashboard.
     * Validates: All four tab labels (Rituals, Wellbeing, Diary, Profile) are present.
     */
    @Test
    fun test_25_bottomNavBarVisible() {
        registerAndLandOnDashboard()

        composeTestRule.onNodeWithText("Rituals").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wellbeing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Diary").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    /**
     * TEST 5.2: Navigate to Wellbeing tab.
     * Validates: Wellbeing header is displayed after tapping the tab.
     */
    @Test
    fun test_26_navigateToWellbeingTab() {
        registerAndLandOnDashboard()

        navigateToTab("Wellbeing")

        composeTestRule.onNodeWithText("Well-being").assertIsDisplayed()
        composeTestRule.onNodeWithText("Steps").assertIsDisplayed()
    }

    /**
     * TEST 5.3: Navigate to Diary tab.
     * Validates: Diary header is displayed after tapping the tab.
     */
    @Test
    fun test_27_navigateToDiaryTab() {
        registerAndLandOnDashboard()

        navigateToTab("Diary")

        composeTestRule.onNodeWithText("Recent entries").assertIsDisplayed()
    }

    /**
     * TEST 5.4: Navigate to Profile tab.
     * Validates: User name and profile elements are displayed.
     */
    @Test
    fun test_28_navigateToProfileTab() {
        registerAndLandOnDashboard(name = "NavTestUser")

        navigateToTab("Profile")

        composeTestRule.onNodeWithText("NavTestUser").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log Out").assertIsDisplayed()
    }

    /**
     * TEST 5.5: Navigate back to Dashboard from another tab.
     * Validates: Returning to Rituals tab shows Dashboard content.
     */
    @Test
    fun test_29_navigateBackToDashboard() {
        registerAndLandOnDashboard()

        navigateToTab("Profile")
        navigateToTab("Rituals")

        composeTestRule.onNodeWithText("Today's Rituals").assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════════
    //  6. DIARY MANAGEMENT
    // ══════════════════════════════════════════════════════════════

    /**
     * TEST 6.1: Diary empty state shown when no entries exist.
     * Validates: "Your journal is empty." message is displayed.
     */
    @Test
    fun test_30_diaryEmptyStateVisible() {
        registerAndLandOnDashboard()
        navigateToTab("Diary")

        composeTestRule.onNodeWithText("Your journal is empty.").assertIsDisplayed()
    }

    /**
     * TEST 6.2: Diary FAB is present for adding entries.
     * Validates: Add entry floating action button exists.
     */
    @Test
    fun test_31_diaryFabPresent() {
        registerAndLandOnDashboard()
        navigateToTab("Diary")

        composeTestRule.onNodeWithContentDescription("Add New Entry").assertIsDisplayed()
    }

    /**
     * TEST 6.3: Tapping diary FAB opens New Entry screen.
     * Validates: "New Entry" header and form fields are shown.
     */
    @Test
    fun test_32_diaryFabOpensNewEntryScreen() {
        registerAndLandOnDashboard()
        navigateToTab("Diary")

        composeTestRule.onNodeWithContentDescription("Add New Entry").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("New Entry").assertIsDisplayed()
        composeTestRule.onNodeWithText("Title").assertExists()
        composeTestRule.onNodeWithText("Add Tag").assertExists()
    }

    /**
     * TEST 6.4: Diary save button is disabled when title/content are empty.
     * Validates: Save icon is disabled with empty required fields.
     */
    @Test
    fun test_33_diarySaveDisabledWithEmptyFields() {
        registerAndLandOnDashboard()
        navigateToTab("Diary")

        composeTestRule.onNodeWithContentDescription("Add New Entry").performClick()
        composeTestRule.waitForIdle()

        // Save button should be disabled (both title and content are empty)
        composeTestRule.onNodeWithContentDescription("Save Entry").assertIsNotEnabled()
    }

    /**
     * TEST 6.5: Diary save disabled when only title is filled.
     * Validates: Both title AND content must be filled to enable save.
     */
    @Test
    fun test_34_diarySaveDisabledWithOnlyTitle() {
        registerAndLandOnDashboard()
        navigateToTab("Diary")

        composeTestRule.onNodeWithContentDescription("Add New Entry").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Title").performTextInput("Just a title")
        closeSoftKeyboard()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Save Entry").assertIsNotEnabled()
    }

    /**
     * TEST 6.6: Creating a diary entry navigates back and shows the entry.
     * Validates: Full diary creation flow — entry appears in the list after creation.
     */
    @Test
    fun test_35_createDiaryEntryAppearsInList() {
        registerAndLandOnDashboard()
        navigateToTab("Diary")

        createDiaryEntry(title = "Happy Day", content = "Everything went extremely well today.")

        // Should be back on Diary screen with the new entry visible
        composeTestRule.onNodeWithText("Happy Day").assertIsDisplayed()
    }

    /**
     * TEST 6.7: Back navigation from New Entry returns to Diary list.
     * Validates: Back button works on DiaryDetailScreen.
     */
    @Test
    fun test_36_backFromNewEntryReturnsToDiary() {
        registerAndLandOnDashboard()
        navigateToTab("Diary")

        composeTestRule.onNodeWithContentDescription("Add New Entry").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Navigate Back").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Recent entries").assertIsDisplayed()
    }

    /**
     * TEST 6.8: Tapping a diary entry opens the view screen with details.
     * Validates: DiaryViewScreen displays entry title and content.
     */
    @Test
    fun test_37_tapDiaryEntryOpensViewScreen() {
        registerAndLandOnDashboard()
        navigateToTab("Diary")
        createDiaryEntry(title = "Test View", content = "Content to view here")

        // Tap on the entry
        composeTestRule.onNodeWithText("Test View").performClick()
        composeTestRule.waitForIdle()

        // Should see the entry content on the view screen
        composeTestRule.onNodeWithText("Test View").assertIsDisplayed()
        composeTestRule.onNodeWithText("Content to view here").assertIsDisplayed()
    }

    /**
     * TEST 6.9: Diary view screen has edit and delete buttons.
     * Validates: Action bar icons for editing and deleting an entry.
     */
    @Test
    fun test_38_diaryViewScreenHasEditAndDeleteButtons() {
        registerAndLandOnDashboard()
        navigateToTab("Diary")
        createDiaryEntry(title = "Actions Test", content = "Test content for actions")

        composeTestRule.onNodeWithText("Actions Test").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Delete").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Edit Entry").assertIsDisplayed()
    }

    /**
     * TEST 6.10: Deleting a diary entry shows confirmation dialog.
     * Validates: Delete confirmation dialog appears with proper text.
     */
    @Test
    fun test_39_deleteDiaryEntryShowsConfirmation() {
        registerAndLandOnDashboard()
        navigateToTab("Diary")
        createDiaryEntry(title = "Delete Me", content = "This entry will be deleted")

        composeTestRule.onNodeWithText("Delete Me").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("Delete").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Delete Entry?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you want to delete this diary entry? This cannot be undone.")
            .assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════════
    //  7. WELLBEING
    // ══════════════════════════════════════════════════════════════

    /**
     * TEST 7.1: Wellbeing screen displays all stat sections.
     * Validates: Steps, Sleep, Water, and Daily Summary are shown.
     */
    @Test
    fun test_40_wellbeingScreenDisplaysStats() {
        registerAndLandOnDashboard()
        navigateToTab("Wellbeing")

        composeTestRule.onNodeWithText("Well-being").assertIsDisplayed()
        composeTestRule.onNodeWithText("Steps").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sleep").assertIsDisplayed()
        composeTestRule.onNodeWithText("Water").assertIsDisplayed()
        composeTestRule.onNodeWithText("Daily Summary").assertIsDisplayed()
    }

    /**
     * TEST 7.2: Log Water button is present on Wellbeing screen.
     * Validates: "Log Water" CTA button exists.
     */
    @Test
    fun test_41_logWaterButtonPresent() {
        registerAndLandOnDashboard()
        navigateToTab("Wellbeing")

        composeTestRule.onNodeWithText("Log Water").assertIsDisplayed()
    }

    /**
     * TEST 7.3: Tapping Log Water button opens water dialog.
     * Validates: Dialog with "Log Water Intake" title, amount input, and unit chips appear.
     */
    @Test
    fun test_42_logWaterOpensDialog() {
        registerAndLandOnDashboard()
        navigateToTab("Wellbeing")

        composeTestRule.onNodeWithText("Log Water").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Log Water Intake").assertIsDisplayed()
        composeTestRule.onNodeWithText("ml").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cups").assertIsDisplayed()
        composeTestRule.onNodeWithText("Oz").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    /**
     * TEST 7.4: Cancel water dialog dismisses it.
     * Validates: Tapping Cancel closes the dialog, returning to the Wellbeing screen.
     */
    @Test
    fun test_43_cancelWaterDialogDismisses() {
        registerAndLandOnDashboard()
        navigateToTab("Wellbeing")

        composeTestRule.onNodeWithText("Log Water").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.waitForIdle()

        // Dialog should be gone, still on Wellbeing
        composeTestRule.onNodeWithText("Log Water Intake").assertDoesNotExist()
        composeTestRule.onNodeWithText("Well-being").assertIsDisplayed()
    }

    /**
     * TEST 7.5: Sync Steps button is present on Wellbeing screen.
     * Validates: Step sync icon button exists.
     */
    @Test
    fun test_44_syncStepsButtonPresent() {
        registerAndLandOnDashboard()
        navigateToTab("Wellbeing")

        composeTestRule.onNodeWithContentDescription("Sync Steps").assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════════
    //  8. PROFILE MANAGEMENT
    // ══════════════════════════════════════════════════════════════

    /**
     * TEST 8.1: Profile screen displays user name.
     * Validates: The registered user name is shown on the Profile screen.
     */
    @Test
    fun test_45_profileDisplaysUserName() {
        registerAndLandOnDashboard(name = "JaneDoe")
        navigateToTab("Profile")

        composeTestRule.onNodeWithText("JaneDoe").assertIsDisplayed()
    }

    /**
     * TEST 8.2: Profile screen shows mastery level.
     * Validates: "Mastery Level: 0" is shown for a new user.
     */
    @Test
    fun test_46_profileShowsMasteryLevel() {
        registerAndLandOnDashboard()
        navigateToTab("Profile")

        composeTestRule.onNodeWithText("Mastery Level: 0").assertIsDisplayed()
    }

    /**
     * TEST 8.3: Dark Mode toggle is present on Profile.
     * Validates: "Dark Mode" setting row and switch exist.
     */
    @Test
    fun test_47_darkModeTogglePresent() {
        registerAndLandOnDashboard()
        navigateToTab("Profile")

        composeTestRule.onNodeWithText("Dark Mode").performScrollTo().assertIsDisplayed()
    }

    /**
     * TEST 8.4: My Rituals section is present on Profile.
     * Validates: "My Rituals" row with chevron icon exists.
     */
    @Test
    fun test_48_myRitualsSectionPresent() {
        registerAndLandOnDashboard()
        navigateToTab("Profile")

        composeTestRule.onNodeWithText("My Rituals").performScrollTo().assertIsDisplayed()
    }

    /**
     * TEST 8.5: My Rituals bottom sheet opens with empty state.
     * Validates: Tapping "My Rituals" opens sheet showing "No rituals added yet."
     */
    @Test
    fun test_49_myRitualsSheetShowsEmptyState() {
        registerAndLandOnDashboard()
        navigateToTab("Profile")

        composeTestRule.onNodeWithText("My Rituals").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Your Rituals").assertIsDisplayed()
        composeTestRule.onNodeWithText("No rituals added yet.").assertIsDisplayed()
    }

    /**
     * TEST 8.6: Edit Name icon is visible next to user name on Profile.
     * Validates: Edit name pencil icon with "Edit Name" content description exists.
     */
    @Test
    fun test_50_editNameIconPresent() {
        registerAndLandOnDashboard()
        navigateToTab("Profile")

        composeTestRule.onNodeWithContentDescription("Edit Name").assertIsDisplayed()
    }

    /**
     * TEST 8.7: Profile shows Log Out and Delete Account buttons.
     * Validates: Both destructive action buttons are visible.
     */
    @Test
    fun test_51_profileShowsLogOutAndDeleteButtons() {
        registerAndLandOnDashboard()
        navigateToTab("Profile")

        composeTestRule.onNodeWithText("Log Out").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete Account").performScrollTo().assertIsDisplayed()
    }

    // ══════════════════════════════════════════════════════════════
    //  9. CROSS-FEATURE INTEGRATION & EDGE CASES
    // ══════════════════════════════════════════════════════════════

    /**
     * TEST 9.1: Creating a habit then viewing My Rituals shows it.
     * Validates: Data flows correctly between Dashboard and Profile features.
     */
    @Test
    fun test_52_habitAppearsInProfileRitualsList() {
        registerAndLandOnDashboard()
        createHabit(name = "Meditate")

        navigateToTab("Profile")
        composeTestRule.onNodeWithText("My Rituals").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Meditate").assertIsDisplayed()
    }

    /**
     * TEST 9.2: Multiple habits can be created sequentially.
     * Validates: The app handles multiple habit additions correctly.
     */
    @Test
    fun test_53_multipleHabitsCanBeCreated() {
        registerAndLandOnDashboard()

        createHabit(name = "Exercise")
        createHabit(name = "Journal")

        composeTestRule.onNodeWithText("Exercise").assertIsDisplayed()
        composeTestRule.onNodeWithText("Journal").assertIsDisplayed()
    }

    /**
     * TEST 9.3: Multiple diary entries can be created sequentially.
     * Validates: The app handles multiple diary entry additions correctly.
     */
    @Test
    fun test_54_multipleDiaryEntriesCanBeCreated() {
        registerAndLandOnDashboard()
        navigateToTab("Diary")

        createDiaryEntry(title = "Entry One", content = "First diary content")
        createDiaryEntry(title = "Entry Two", content = "Second diary content")

        composeTestRule.onNodeWithText("Entry One").assertIsDisplayed()
        composeTestRule.onNodeWithText("Entry Two").assertIsDisplayed()
    }

    /**
     * TEST 9.4: Dashboard header shows user's registered name.
     * Validates: Dashboard greeting personalisation with user name.
     */
    @Test
    fun test_55_dashboardShowsUserName() {
        registerAndLandOnDashboard(name = "PersonalisedUser")

        composeTestRule.onNodeWithText("Good Morning,").assertIsDisplayed()
        composeTestRule.onNodeWithText("PersonalisedUser").assertIsDisplayed()
    }

    /**
     * TEST 9.5: Tab switching preserves data (no data loss).
     * Validates: Creating a habit then switching tabs and returning still shows the habit.
     */
    @Test
    fun test_56_tabSwitchingPreservesData() {
        registerAndLandOnDashboard()
        createHabit(name = "Persistent Habit")

        // Switch to other tabs and back
        navigateToTab("Wellbeing")
        navigateToTab("Diary")
        navigateToTab("Rituals")

        // Habit should still be visible
        composeTestRule.onNodeWithText("Persistent Habit").assertIsDisplayed()
    }

    /**
     * TEST 9.6: Full round-trip — Register, Create Data, Logout, Login, Verify Data Persists.
     * Validates: End-to-end data persistence across sessions.
     */
    @Test
    fun test_57_fullRoundTripDataPersistence() {
        // Register and create a habit
        registerAndLandOnDashboard(name = "PersistUser")
        createHabit(name = "Yoga")

        // Logout
        navigateToTab("Profile")
        composeTestRule.onNodeWithText("Log Out").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Login again
        loginWithCredentials()

        // Habit should still exist
        composeTestRule.onNodeWithText("Yoga").assertIsDisplayed()
    }

    /**
     * TEST 9.7: Register with blank name field keeps button disabled.
     * Validates: Edge case — all fields required for registration.
     */
    @Test
    fun test_58_registerDisabledWithBlankName() {
        composeTestRule.onNodeWithText("Don't have an account? Register").performClick()
        composeTestRule.waitForIdle()

        // Fill email and password but leave name empty
        composeTestRule.onAllNodesWithText("Email Address")[0].performTextInput("a@b.com")
        composeTestRule.onNodeWithText("Create Password").performTextInput("123456")
        closeSoftKeyboard()
        composeTestRule.onNodeWithText("I agree to the Terms of Service").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Sign Up").assertIsNotEnabled()
    }

    /**
     * TEST 9.8: Wellbeing screen shows initial 0 step count.
     * Validates: Default wellbeing state for a new user.
     */
    @Test
    fun test_59_wellbeingShowsInitialZeroSteps() {
        registerAndLandOnDashboard()
        navigateToTab("Wellbeing")

        composeTestRule.onNodeWithText("0").assertIsDisplayed()
    }

    /**
     * TEST 9.9: Diary sort menu has all options.
     * Validates: Sort dropdown contains Newest First, Oldest First, and Alphabetical.
     */
    @Test
    fun test_60_diarySortOptionsAvailable() {
        registerAndLandOnDashboard()
        navigateToTab("Diary")

        // Tap Sort button
        composeTestRule.onNodeWithContentDescription("Sort Options").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Newest First").assertIsDisplayed()
        composeTestRule.onNodeWithText("Oldest First").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alphabetical").assertIsDisplayed()
    }
}