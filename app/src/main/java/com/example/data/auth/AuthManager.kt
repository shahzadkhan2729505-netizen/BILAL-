package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val isLoggedIn: Boolean = false,
    val email: String = "",
    val name: String = "",
    val centerName: String = "Bilal Ahmad Milk Collection",
    val role: String = "Dairy Collection Center Admin",
    val isGoogleAccount: Boolean = true,
    val avatarInitial: String = "B",
    val isProTrialActive: Boolean = true,
    val trialDaysRemaining: Int = 14
)

class AuthManager private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("bamc_auth_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadSavedProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: AuthManager? = null

        fun getInstance(context: Context): AuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthManager(context).also { INSTANCE = it }
            }
        }
    }

    private fun loadSavedProfile(): UserProfile {
        val isLoggedIn = prefs.getBoolean("is_logged_in", true) // Default logged in as admin for smooth workflow
        val email = prefs.getString("user_email", "bilal.dairy@gmail.com") ?: "bilal.dairy@gmail.com"
        val name = prefs.getString("user_name", "Bilal Ahmad") ?: "Bilal Ahmad"
        val center = prefs.getString("center_name", "Bilal Ahmad Milk Collection") ?: "Bilal Ahmad Milk Collection"
        val role = prefs.getString("user_role", "Dairy Collection Center Admin") ?: "Dairy Collection Center Admin"
        val isPro = prefs.getBoolean("is_pro_trial", true)
        val initial = name.firstOrNull()?.uppercase() ?: "B"

        return UserProfile(
            isLoggedIn = isLoggedIn,
            email = email,
            name = name,
            centerName = center,
            role = role,
            isGoogleAccount = true,
            avatarInitial = initial,
            isProTrialActive = isPro,
            trialDaysRemaining = 14
        )
    }

    fun signInWithGoogle(email: String, name: String) {
        val initial = name.firstOrNull()?.uppercase() ?: "G"
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_email", email)
            .putString("user_name", name)
            .putBoolean("is_pro_trial", true)
            .apply()

        _userProfile.value = UserProfile(
            isLoggedIn = true,
            email = email,
            name = name,
            centerName = "Bilal Ahmad Milk Collection",
            role = "Dairy Collection Center Admin",
            isGoogleAccount = true,
            avatarInitial = initial,
            isProTrialActive = true,
            trialDaysRemaining = 14
        )
    }

    fun signOut() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .apply()

        _userProfile.value = _userProfile.value.copy(
            isLoggedIn = false
        )
    }

    fun activateProTrial() {
        prefs.edit()
            .putBoolean("is_pro_trial", true)
            .apply()

        _userProfile.value = _userProfile.value.copy(
            isProTrialActive = true,
            trialDaysRemaining = 14
        )
    }
}
