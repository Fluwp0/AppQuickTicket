package com.quickticket.app.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quickticket.app.network.RetrofitClient
import com.quickticket.app.network.model.LoginRequest
import com.quickticket.app.network.model.RegisterRequest
import com.quickticket.app.network.model.TicketClaimRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false
)

data class RegisterFormState(
    val name: String = "",
    val rut: String = "",
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val nameError: String? = null,
    val rutError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val repeatPasswordError: String? = null,
    val isLoading: Boolean = false
)

sealed interface AuthEffect {
    data object LoginSuccess : AuthEffect
    data object RegisterSuccess : AuthEffect
    data class ShowMessage(val message: String) : AuthEffect
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LOGGED_EMAIL = "logged_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_RUT = "user_rut"
        private const val KEY_LAST_TICKET_DATE = "last_ticket_date"
    }



    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _login = MutableStateFlow(LoginFormState())
    val login: StateFlow<LoginFormState> = _login.asStateFlow()

    private val _register = MutableStateFlow(RegisterFormState())
    val register: StateFlow<RegisterFormState> = _register.asStateFlow()

    private val _effects = Channel<AuthEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userRut = MutableStateFlow("")
    val userRut: StateFlow<String> = _userRut.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _lastTicketDate = MutableStateFlow("")
    private val _ticketUsedToday = MutableStateFlow(false)
    val ticketUsedToday: StateFlow<Boolean> = _ticketUsedToday.asStateFlow()

    private val _avatarBitmap = MutableStateFlow<Bitmap?>(null)
    val avatarBitmap: StateFlow<Bitmap?> = _avatarBitmap.asStateFlow()

    private fun todayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    init {
        val email = prefs.getString(KEY_LOGGED_EMAIL, null)
        if (email != null) {
            val name = prefs.getString(KEY_USER_NAME, "") ?: ""
            val rut = prefs.getString(KEY_USER_RUT, "") ?: ""
            val lastTicket = prefs.getString(KEY_LAST_TICKET_DATE, "") ?: ""

            _login.value = _login.value.copy(email = email)
            _userName.value = name
            _userRut.value = rut
            _userEmail.value = email
            _lastTicketDate.value = lastTicket

            _isLoggedIn.value = true

            refreshTicketStatusFromBackend()
        }
    }

    fun ensureTicketStateForToday() {
        val today = todayString()
        if (_lastTicketDate.value != today) {
            _ticketUsedToday.value = false
        }
        refreshTicketStatusFromBackend()
    }

    fun markTicketGeneratedToday() {
        claimTicketOnBackend()
    }

    private fun refreshTicketStatusFromBackend() {
        val email = prefs.getString(KEY_LOGGED_EMAIL, null) ?: return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getTicketStatus(email)
                _ticketUsedToday.value = response.ticketUsedToday
                _lastTicketDate.value = response.lastTicketDate ?: ""

                prefs.edit()
                    .putString(KEY_LAST_TICKET_DATE, _lastTicketDate.value)
                    .apply()
            } catch (_: Exception) {

            }
        }
    }

    private fun claimTicketOnBackend() {
        val email = prefs.getString(KEY_LOGGED_EMAIL, null) ?: return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.claimTicket(
                    TicketClaimRequest(email)
                )

                val today = todayString()
                _ticketUsedToday.value = true
                _lastTicketDate.value = today

                prefs.edit()
                    .putString(KEY_LAST_TICKET_DATE, today)
                    .apply()

                _effects.send(AuthEffect.ShowMessage("Ticket generado con éxito"))
            } catch (e: Exception) {
                val msg = e.message ?: "No se pudo generar el ticket"
                _effects.send(AuthEffect.ShowMessage(msg))
            }
        }
    }



    fun onLoginEmailChange(value: String) {
        _login.value = _login.value.copy(
            email = value,
            emailError = null
        )
    }

    fun onLoginPasswordChange(value: String) {
        _login.value = _login.value.copy(
            password = value,
            passwordError = null
        )
    }

    fun onRegisterNameChange(value: String) {
        _register.value = _register.value.copy(
            name = value,
            nameError = null
        )
    }

    fun onRegisterRutChange(value: String) {
        _register.value = _register.value.copy(
            rut = value,
            rutError = null
        )
    }

    fun onRegisterEmailChange(value: String) {
        _register.value = _register.value.copy(
            email = value,
            emailError = null
        )
    }

    fun onRegisterPasswordChange(value: String) {
        _register.value = _register.value.copy(
            password = value,
            passwordError = null
        )
    }

    fun onRegisterRepeatPasswordChange(value: String) {
        _register.value = _register.value.copy(
            repeatPassword = value,
            repeatPasswordError = null
        )
    }

    fun submitLogin() {
        val s = _login.value
        val emailTrimmed = s.email.trim()
        val emailOk = Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()
        val passOk = s.password.isNotBlank()

        _login.value = s.copy(
            emailError = if (!emailOk) "Email inválido" else null,
            passwordError = if (!passOk) "Contraseña requerida" else null
        )
        if (!emailOk || !passOk) return

        viewModelScope.launch {
            _login.value = _login.value.copy(isLoading = true)

            try {
                val response = RetrofitClient.api.login(
                    LoginRequest(emailTrimmed, s.password)
                )

                _userName.value = response.name
                _userRut.value = response.rut
                _userEmail.value = response.email
                _isLoggedIn.value = true

                prefs.edit()
                    .putString(KEY_LOGGED_EMAIL, response.email)
                    .putString(KEY_USER_NAME, response.name)
                    .putString(KEY_USER_RUT, response.rut)
                    .putString(KEY_LAST_TICKET_DATE, "")
                    .apply()

                _ticketUsedToday.value = false
                _lastTicketDate.value = ""
                refreshTicketStatusFromBackend()

                _effects.send(AuthEffect.LoginSuccess)

            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException ->
                        if (e.code() == 400) "Credenciales inválidas" else "Error ${e.code()}"
                    else -> e.message ?: "Error de login"
                }
                _effects.send(AuthEffect.ShowMessage(msg))
            } finally {
                _login.value = _login.value.copy(isLoading = false)
            }
        }
    }

    fun submitRegister() {
        val s = _register.value
        val emailTrimmed = s.email.trim()
        val nameOk = s.name.isNotBlank()
        val rutOk = s.rut.isNotBlank()
        val emailOk = Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()
        val passOk = s.password.length >= 6
        val repeatOk = s.password == s.repeatPassword

        _register.value = s.copy(
            nameError = if (!nameOk) "Nombre requerido" else null,
            rutError = if (!rutOk) "RUT inválido" else null,
            emailError = if (!emailOk) "Email inválido" else null,
            passwordError = if (!passOk) "Mínimo 6 caracteres" else null,
            repeatPasswordError = if (!repeatOk) "No coinciden" else null
        )
        if (!nameOk || !rutOk || !emailOk || !passOk || !repeatOk) return

        viewModelScope.launch {
            _register.value = _register.value.copy(isLoading = true)

            try {
                val response = RetrofitClient.api.register(
                    RegisterRequest(s.name, s.rut, emailTrimmed, s.password)
                )

                _userName.value = response.name
                _userRut.value = response.rut
                _userEmail.value = response.email
                _isLoggedIn.value = true

                _ticketUsedToday.value = false
                _lastTicketDate.value = ""

                prefs.edit()
                    .putString(KEY_LOGGED_EMAIL, response.email)
                    .putString(KEY_USER_NAME, response.name)
                    .putString(KEY_USER_RUT, response.rut)
                    .putString(KEY_LAST_TICKET_DATE, "")
                    .apply()

                refreshTicketStatusFromBackend()

                _effects.send(AuthEffect.RegisterSuccess)

            } catch (e: Exception) {
                val msg = when (e) {
                    is HttpException ->
                        if (e.code() == 400) "Correo ya en uso" else "Error ${e.code()}"
                    else -> e.message ?: "Error de registro"
                }
                _effects.send(AuthEffect.ShowMessage(msg))
            } finally {
                _register.value = _register.value.copy(isLoading = false)
            }
        }
    }

    fun logout() {
        prefs.edit().clear().apply()

        _login.value = LoginFormState()
        _register.value = RegisterFormState()
        _userName.value = ""
        _userRut.value = ""
        _userEmail.value = ""
        _lastTicketDate.value = ""
        _ticketUsedToday.value = false
        _avatarBitmap.value = null
        _isLoggedIn.value = false
    }

    fun updateAvatar(bitmap: Bitmap) {
        _avatarBitmap.value = bitmap
    }
}
