package com.quickticket.app.navigation

import android.Manifest
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.quickticket.app.ui.screen.CartScreen
import com.quickticket.app.ui.screen.HomeScreen
import com.quickticket.app.ui.screen.LoginScreen

import com.quickticket.app.ui.screen.ProfileScreen
import com.quickticket.app.ui.screen.RegisterScreen
import com.quickticket.app.ui.viewmodel.AuthViewModel

@Composable
fun AppNavGraph() {

    val navController = rememberNavController()
    val vm: AuthViewModel = viewModel()
    val isLoggedIn by vm.isLoggedIn.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) NavRoute.Home.route else NavRoute.Login.route
    ) {

        composable(route = NavRoute.Login.route) {
            LoginScreen(
                vm = vm,
                onLogin = {
                    navController.navigate(NavRoute.Home.route) {
                        popUpTo(NavRoute.Login.route) { inclusive = true }
                    }
                },
                onGoRegister = {
                    navController.navigate(NavRoute.Register.route)
                }
            )
        }

        composable(route = NavRoute.Register.route) {
            RegisterScreen(
                vm = vm,
                onRegisterOk = {
                    navController.navigate(NavRoute.Home.route) {
                        popUpTo(NavRoute.Login.route) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(route = NavRoute.Home.route) {
            HomeScreen(
                vm = vm,
                onGoProfile = { navController.navigate(NavRoute.Profile.route) },
                onGoCart = { navController.navigate(NavRoute.Cart.route) }
            )
        }

        composable(route = NavRoute.Profile.route) {

            val context = LocalContext.current
            var showSourceDialog by remember { mutableStateOf(false) }
            val galleryLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    if (uri != null) {
                        try {
                            val bitmap = MediaStore.Images.Media.getBitmap(
                                context.contentResolver,
                                uri
                            )
                            vm.updateAvatar(bitmap)
                        } catch (_: Exception) {
                        }
                    }
                }

            val cameraLauncher =
                rememberLauncherForActivityResult(
                    ActivityResultContracts.TakePicturePreview()
                ) { bitmap ->
                    if (bitmap != null) {
                        vm.updateAvatar(bitmap)
                    }
                }


            val cameraPermissionLauncher =
                rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (granted) {

                        try {
                            cameraLauncher.launch(null)
                        } catch (_: Exception) {
                            galleryLauncher.launch("image/*")
                        }
                    } else {
                        galleryLauncher.launch("image/*")
                    }
                }

            ProfileScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onLogout = {
                    vm.logout()
                    navController.navigate(NavRoute.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onPickImage = {
                    showSourceDialog = true
                }
            )

            if (showSourceDialog) {
                AlertDialog(
                    onDismissRequest = { showSourceDialog = false },
                    title = { Text("Seleccionar foto de perfil") },
                    text = { Text("¿De dónde quieres obtener tu foto?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showSourceDialog = false
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        ) {
                            Text("Cámara")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showSourceDialog = false
                                galleryLauncher.launch("image/*")
                            }
                        ) {
                            Text("Galería")
                        }
                    }
                )
            }
        }

        composable(route = NavRoute.Cart.route) {

            val userEmail by vm.userEmail.collectAsState()

            CartScreen(
                userEmail = userEmail,
                onBackHome = { navController.popBackStack() }
            )
        }


    }
}
