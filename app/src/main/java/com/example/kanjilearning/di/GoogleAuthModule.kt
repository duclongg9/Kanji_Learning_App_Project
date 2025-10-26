package com.example.kanjilearning.di

import android.content.Context
import com.example.kanjilearning.BuildConfig
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
    fun provideGoogleSignInOptions(): GoogleSignInOptions {
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestId()
        val clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        if (clientId.isNotBlank()) {
            builder.requestIdToken(clientId)
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
