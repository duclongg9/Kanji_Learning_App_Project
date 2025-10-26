package com.example.kanjilearning.di

import android.content.Context
import android.util.Log
import com.example.kanjilearning.BuildConfig
import com.example.kanjilearning.R
import com.example.kanjilearning.oauth.GoogleOAuthConfig
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
    fun provideGoogleSignInOptions(
        config: GoogleOAuthConfig
    ): GoogleSignInOptions {
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestId()
        val clientId = config.clientId.ifBlank { BuildConfig.GOOGLE_WEB_CLIENT_ID }.trim()
        if (clientId.isNotEmpty()) {
            builder.requestIdToken(clientId)
        } else {
            Log.w("GoogleAuth", "Missing Google OAuth client ID – continuing without ID token requests")
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideGoogleSignInClient(
        @ApplicationContext context: Context,
        options: GoogleSignInOptions
    ): GoogleSignInClient = GoogleSignIn.getClient(context, options)
}
