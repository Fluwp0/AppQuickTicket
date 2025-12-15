package com.quickticket.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.collectAsState
import java.util.Date
import java.util.Locale
import com.quickticket.app.ui.viewmodel.AuthViewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier


@Composable
fun HomeScreen(
    vm: AuthViewModel,
    onGoProfile: () -> Unit,
    onGoCart: () -> Unit
) {
    val mercadoLibreYellow = Color(0xFFFFF159)
    val primaryText = Color(0xFF000000)
    val userEmail by vm.userEmail.collectAsState()
    var showConfirm by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf(25) }
    val nombreUsuario = vm.userName.collectAsState().value
    val fechaActual = remember {
        val formato = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        formato.format(Date())
    }

    LaunchedEffect(Unit) {
        vm.ensureTicketStateForToday()
    }

    val hasTicketToday by vm.ticketUsedToday.collectAsState()

    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            secondsLeft = 25
            while (secondsLeft > 0) {
                delay(1_000)
                secondsLeft--
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mercadoLibreYellow)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .padding(bottom = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "QT QuickTicket",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = primaryText,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(32.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Hola $nombreUsuario",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryText
                )
            }

            Spacer(Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "Ticket de almuerzo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = fechaActual,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(20.dp))

                    ElevatedButton(
                        onClick = { showConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !hasTicketToday,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3483FA),
                            disabledContainerColor = Color(0xFFB2C8F6),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        Text(
                            text = "Generar ticket",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Surface(
            tonalElevation = 8.dp,
            shadowElevation = 10.dp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
                .height(56.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onGoProfile) {
                    Icon(Icons.Default.Person, contentDescription = "Perfil")
                }
                IconButton(onClick = { /* ya estás en Home */ }) {
                    Icon(Icons.Default.Home, contentDescription = "Inicio")
                }
                IconButton(onClick = onGoCart) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                }
            }
        }

        if (showConfirm) {
            AlertDialog(
                onDismissRequest = { showConfirm = false },
                title = { Text("Generar Ticket") },
                text = { Text("¿Seguro que quieres generar el ticket?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirm = false
                            showSuccess = true
                            vm.markTicketGeneratedToday()
                        }
                    ) {
                        Text("Generar Ticket")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirm = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showSuccess) {
            TicketSuccessOverlay(
                secondsLeft = secondsLeft,
                onFinish = { showSuccess = false }
            )
        }
    }
}

@Composable
private fun TicketSuccessOverlay(
    secondsLeft: Int,
    onFinish: () -> Unit
) {
    val green = Color(0xFF00C853)
    val errorRed = Color(0xFFF23D4F)
    val isExpired = secondsLeft <= 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isExpired) errorRed else green),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(32.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .width(180.dp)
                    .height(110.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isExpired) Icons.Default.Close else Icons.Default.Check,
                        contentDescription = if (isExpired) "Ticket expirado" else "Ticket aprobado",
                        tint = if (isExpired) errorRed else green,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = if (isExpired) "Ticket expirado" else "Ticket generado\ncon éxito",
                color = Color.White,
                fontSize = 26.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = if (isExpired)
                    "El tiempo para usar tu ticket ha terminado"
                else
                    "Muestra esta pantalla\nen el comedor para recibir tu almuerzo",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(50),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = if (secondsLeft > 0)
                        "Disponible por $secondsLeft segundos"
                    else
                        "Ticket expirado",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    color = errorRed,
                    fontWeight = FontWeight.Medium
                )
            }

            if (secondsLeft <= 0) {
                Spacer(Modifier.height(32.dp))

                ElevatedButton(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 64.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(50),
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 10.dp
                    ),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Volver",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}
