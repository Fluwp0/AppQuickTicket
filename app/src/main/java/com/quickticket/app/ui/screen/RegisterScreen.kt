package com.quickticket.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.TextFieldDefaults
import com.quickticket.app.ui.viewmodel.AuthViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegisterScreen(
    vm: AuthViewModel,
    onRegisterOk: () -> Unit,
    onBackToLogin: () -> Unit
) {

    val state = vm.register.collectAsState().value


    var name by rememberSaveable { mutableStateOf("") }
    var lastP by rememberSaveable { mutableStateOf("") }
    var lastM by rememberSaveable { mutableStateOf("") }
    var rut by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var repeat by rememberSaveable { mutableStateOf("") }
    var passVisible by rememberSaveable { mutableStateOf(false) }
    var repeatVisible by rememberSaveable { mutableStateOf(false) }


    LaunchedEffect(name, lastP, lastM) {
        val fullName = listOf(name, lastP, lastM)
            .filter { it.isNotBlank() }
            .joinToString(" ")
        vm.onRegisterNameChange(fullName)
    }
    LaunchedEffect(email) { vm.onRegisterEmailChange(email) }
    LaunchedEffect(password) { vm.onRegisterPasswordChange(password) }
    LaunchedEffect(repeat) { vm.onRegisterRepeatPasswordChange(repeat) }

    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.effects.collect { e ->
            when (e) {
                is com.quickticket.app.ui.viewmodel.AuthEffect.RegisterSuccess -> onRegisterOk()
                is com.quickticket.app.ui.viewmodel.AuthEffect.ShowMessage ->
                    snackbarHost.showSnackbar(e.message)
                else -> Unit
            }
        }
    }

    val yellow = Color(0xFFFFF159)
    val blueButton = Color(0xFF2968C8)
    val darkText = Color(0xFF111111)
    val textFieldColors = TextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        focusedLabelColor = Color.Black,
        unfocusedLabelColor = Color.Black,
        cursorColor = Color.Black
    )



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(yellow)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .blur(if (state.isLoading) 6.dp else 0.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {


            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {


                    Text(
                        text = "Crear usuario",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkText
                    )

                    Spacer(Modifier.height(16.dp))


                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nombre") },
                        singleLine = true,
                        colors = textFieldColors,

                        )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = lastP,
                        onValueChange = { lastP = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Apellido paterno") },
                        singleLine = true,
                        colors = textFieldColors,

                        )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = lastM,
                        onValueChange = { lastM = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Apellido materno") },
                        singleLine = true,
                        colors = textFieldColors,

                        )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = rut,
                        onValueChange = {
                            rut = it
                            vm.onRegisterRutChange(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("RUT") },
                        isError = state.rutError != null,
                        supportingText = { state.rutError?.let { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        colors = textFieldColors
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = textFieldColors
                        )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Contraseña") },
                        singleLine = true,
                        colors = textFieldColors,
                        visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(onClick = { passVisible = !passVisible }) {
                                Text(if (passVisible) "Ocultar" else "Mostrar")
                            }
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = repeat,
                        onValueChange = { repeat = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Repetir contraseña") },
                        singleLine = true,
                        colors = textFieldColors,
                        visualTransformation = if (repeatVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(onClick = { repeatVisible = !repeatVisible }) {
                                Text(if (repeatVisible) "Ocultar" else "Mostrar")
                            }
                        }
                    )

                    Spacer(Modifier.height(20.dp))


                    Button(
                        onClick = {
                            // Mandamos todos los datos al ViewModel
                            vm.onRegisterRutChange(rut)
                            vm.onRegisterEmailChange(email)
                            vm.onRegisterPasswordChange(password)
                            vm.onRegisterRepeatPasswordChange(repeat)

                            vm.submitRegister()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = blueButton,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Registrarme")
                    }

                    Spacer(Modifier.height(12.dp))


                    TextButton(
                        onClick = onBackToLogin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("¿Ya tienes una cuenta? Inicia sesión aquí")
                    }
                }
            }


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "QT QuickTicket",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = darkText
                )
            }
        }


        if (state.isLoading) {
            Box(
                Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        SnackbarHost(
            hostState = snackbarHost,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}
