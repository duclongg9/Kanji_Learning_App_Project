package com.example.kanjilearning.presentation.login

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.kanjilearning.databinding.ActivityLoginBinding
import com.example.kanjilearning.presentation.router.RouterActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * VI: Màn đăng nhập bằng Google.
 */
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    /**
     * VI: Đăng ký nhận kết quả từ Google Sign-In.
     */
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            handleGoogleAccount(account)
        } catch (ex: ApiException) {
            binding.textStatus.text = "Đăng nhập thất bại: ${ex.statusCode}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleSignIn()
        setupObservers()
        setupListeners()
        animateLoginButton()
    }

    /**
     * VI: Thiết lập GoogleSignInClient với email scope cơ bản.
     */
    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    /**
     * VI: Quan sát state từ ViewModel để cập nhật UI.
     */
    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.progress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    binding.buttonGoogle.isEnabled = !state.isLoading
                    state.errorMessage?.let { message ->
                        binding.textStatus.text = message
                    }
                    if (state.isSuccess) {
                        startActivity(Intent(this@LoginActivity, RouterActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }

    /**
     * VI: Gán sự kiện click cho nút Google.
     */
    private fun setupListeners() {
        binding.buttonGoogle.setOnClickListener {
            val intent = googleSignInClient.signInIntent
            signInLauncher.launch(intent)
        }
    }

    /**
     * VI: Tạo hiệu ứng fade-in cho nút đăng nhập sau khi màn hình load.
     */
    private fun animateLoginButton() {
        binding.buttonGoogle.alpha = 0f
        binding.buttonGoogle.post {
            val animation = AlphaAnimation(0f, 1f).apply {
                duration = 800
                fillAfter = true
            }
            binding.buttonGoogle.startAnimation(animation)
        }
    }

    /**
     * VI: Nhận GoogleSignInAccount và chuyển sang ViewModel xử lý.
     */
    private fun handleGoogleAccount(account: GoogleSignInAccount?) {
        if (account == null) {
            binding.textStatus.text = "Không lấy được tài khoản Google"
            return
        }
        val isOnline = isOnline()
        viewModel.onGoogleSignInSuccess(
            googleId = account.id.orEmpty(),
            displayName = account.displayName.orEmpty(),
            email = account.email.orEmpty(),
            online = isOnline
        )
    }

    /**
     * VI: Kiểm tra kết nối mạng để quyết định sync role hay fallback FREE.
     */
    private fun isOnline(): Boolean {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
