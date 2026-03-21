package com.yetzira.ContractorCashFlowAndroid.ui.settings

import android.content.Context
import androidx.annotation.StringRes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/** Minimal auth surface used by SettingsViewModel, wrapped for testability. */
interface AuthGateway {
    val currentUser: AuthUser?
    fun addAuthStateListener(listener: (AuthUser?) -> Unit)
    fun removeAuthStateListener(listener: (AuthUser?) -> Unit)
    suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthUser?>
    fun signOut()
}

data class AuthUser(
    val uid: String,
    val email: String?
)

class FirebaseAuthGateway(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthGateway {
    private val listeners = mutableMapOf<(AuthUser?) -> Unit, FirebaseAuth.AuthStateListener>()

    override val currentUser: AuthUser?
        get() = firebaseAuth.currentUser?.let { AuthUser(uid = it.uid, email = it.email) }

    override fun addAuthStateListener(listener: (AuthUser?) -> Unit) {
        val authListener = FirebaseAuth.AuthStateListener { auth ->
            listener(auth.currentUser?.let { AuthUser(uid = it.uid, email = it.email) })
        }
        listeners[listener] = authListener
        firebaseAuth.addAuthStateListener(authListener)
    }

    override fun removeAuthStateListener(listener: (AuthUser?) -> Unit) {
        val authListener = listeners.remove(listener) ?: return
        firebaseAuth.removeAuthStateListener(authListener)
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthUser?> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        result.user?.let { AuthUser(uid = it.uid, email = it.email) }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}

interface SettingsStringResolver {
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String
}

class AndroidSettingsStringResolver(
    private val context: Context
) : SettingsStringResolver {
    override fun getString(resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
}

