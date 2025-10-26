package com.example.kanjilearning.di

import android.content.Context
import android.util.Log
import com.example.kanjilearning.BuildConfig
import com.example.kanjilearning.R
import com.example.kanjilearning.oauth.GoogleOAuthConfig
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * VI: Cung cấp cấu hình Google Sign-In dùng chung toàn app.
 */
@Module
@InstallIn(SingletonComponent::class)
object GoogleAuthModule {

    @Provides
    @Singleton
    fun provideGoogleOAuthConfig(
        @ApplicationContext context: Context
    ): GoogleOAuthConfig {
        return GoogleOAuthConfig.fromRawResource(context, R.raw.google_oauth_client)
            ?: GoogleOAuthConfig(BuildConfig.GOOGLE_WEB_CLIENT_ID)
    }

    @Provides
    @Singleton
    fun provideGoogleSignInRequest(
        @ApplicationContext context: Context,
        config: GoogleOAuthConfig
    ): GetSignInIntentRequest {
        val builder = GetSignInIntentRequest.builder()
        val clientId = resolveWebClientId(context, config)
        if (clientId != null) {
            builder.setServerClientId(clientId)
        } else {
            Log.w(
                "GoogleAuth",
                "Missing Google OAuth client ID – continuing without ID token requests"
            )
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideIdentitySignInClient(
        @ApplicationContext context: Context
    ): SignInClient = Identity.getSignInClient(context)

    @Provides
    @Singleton
    fun provideGoogleSignInOptions(
        @ApplicationContext context: Context,
        config: GoogleOAuthConfig
    ): GetSignInIntentRequest {
        val builder = GetSignInIntentRequest.builder()
        val clientId = resolveWebClientId(context, config)
        if (clientId != null) {
            builder.setServerClientId(clientId)
        } else {
            Log.w(
                "GoogleAuth",
                "Missing Google OAuth client ID – continuing without ID token requests"
            )
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideGoogleSignInClient(
        @ApplicationContext context: Context
    ): SignInClient = Identity.getSignInClient(context)

    private fun resolveWebClientId(
        context: Context,
        config: GoogleOAuthConfig
    ): String? {
        val candidateIds = sequence {
            yield(config.clientId)
            yield(readStringResource(context, "default_web_client_id"))
            yield(BuildConfig.GOOGLE_WEB_CLIENT_ID)
        }
            .mapNotNull { it?.trim() }
            .filter { it.isNotEmpty() && !it.contains("placeholder", ignoreCase = true) }

        return candidateIds.firstOrNull()
    }

    private fun readStringResource(
        context: Context,
        name: String
    ): String? {
        val resId = context.resources.getIdentifier(name, "string", context.packageName)
        if (resId == 0) return null
        return runCatching { context.getString(resId).trim() }
            .getOrElse {
                Log.w("GoogleAuth", "Failed to read string resource '$name'", it)
                null
            }
    }
}
