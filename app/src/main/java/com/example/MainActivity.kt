package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.example.data.model.Ride
import com.example.data.model.WalletTransaction
import com.example.ui.AppScreen
import com.example.ui.ChatMessage
import com.example.ui.DolphinViewModel
import com.example.ui.Translations
import com.example.ui.DolphinMapView
import com.example.ui.PastRidesListView
import com.example.ui.DriverStatusPanel
import com.example.ui.DolphinRideBookingForm
import com.example.ui.DolphinJumpAnimation
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private val viewModel: DolphinViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainContent(viewModel: DolphinViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentLang by viewModel.currentLang.collectAsState()

    fun t(key: String): String {
        return Translations.t(key, currentLang)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.Splash -> SplashScreenView(viewModel, ::t)
                AppScreen.Login -> LoginScreenView(viewModel, ::t)
                AppScreen.FaceVerify -> FaceVerifyScreenView(viewModel, ::t)
                AppScreen.Home -> HomeScreenView(viewModel, ::t)
                AppScreen.Rides -> RidesScreenView(viewModel, ::t)
                AppScreen.Wallet -> WalletScreenView(viewModel, ::t)
                AppScreen.Owner -> OwnerScreenView(viewModel, ::t)
                AppScreen.Settings -> SettingsScreenView(viewModel, ::t)
            }

            // Flag stripe at the top matching Nepali flag style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.TopCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(Color(0xFFDC143C))
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(Color(0xFF003893))
                )
            }
        }
    }
}

@Composable
fun SplashScreenView(viewModel: DolphinViewModel, t: (String) -> String) {
    // Hold 2 seconds then navigate to login
    LaunchedEffect(Unit) {
        delay(2200)
        viewModel.navigateTo(AppScreen.Login)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF212121),
                        Color(0xFF1A1A2E),
                        Color(0xFFA07000)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            DolphinJumpAnimation(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = t("appName"),
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFFC107),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = t("tagline"),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = t("appSubtitle"),
                fontSize = 13.sp,
                color = Color(0xFFFFF8E1).copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(60.dp))
            CircularProgressIndicator(
                color = Color(0xFFFFC107),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = t("splashLoading"),
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "🏍️  🛺  🚗  ⚡",
                fontSize = 18.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = t("madeInNepal"),
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun LoginScreenView(viewModel: DolphinViewModel, t: (String) -> String) {
    val context = LocalContext.current
    var phoneInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8E1))
            .verticalScroll(rememberScrollState())
    ) {
        // Creative dark header block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF212121), Color(0xFF161625))
                    )
                )
                .padding(top = 40.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                DolphinJumpAnimation(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = t("appName"),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFC107)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = t("loginSubtitle"),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = t("phoneLabel"),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF757575),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            OutlinedTextField(
                value = phoneInput,
                onValueChange = { if (it.length <= 10) phoneInput = it },
                placeholder = { Text(text = t("phonePlaceholder")) },
                leadingIcon = {
                    Text(
                        text = "+977",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                maxLines = 1,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFC107),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_phone_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = t("passwordLabel"),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF757575),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                placeholder = { Text(text = t("passwordPlaceholder")) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                maxLines = 1,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFC107),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input")
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFF44336),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (phoneInput.length < 10) {
                        errorMessage = t("errorPhone")
                    } else if (passwordInput.length < 6) {
                        errorMessage = t("errorPassword")
                    } else {
                        viewModel.login(phoneInput)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_login_button")
            ) {
                Text(
                    text = t("loginButton"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = t("noAccount") + " ",
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
                Text(
                    text = t("signUp"),
                    fontSize = 14.sp,
                    color = Color(0xFFFF6F00),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        Toast.makeText(context, "Sign Up feature is coming soon!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = t("demoHint"),
                fontSize = 11.sp,
                color = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Or continue with divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
                Text(
                    text = "  " + t("orContinue") + "  ",
                    fontSize = 13.sp,
                    color = Color(0xFF9E9E9E),
                    fontWeight = FontWeight.Medium
                )
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                SocialLoginPill("🇬", onClick = { Toast.makeText(context, "Google Login", Toast.LENGTH_SHORT).show() })
                Spacer(modifier = Modifier.width(16.dp))
                SocialLoginPill("🇫", onClick = { Toast.makeText(context, "Facebook Login", Toast.LENGTH_SHORT).show() })
                Spacer(modifier = Modifier.width(16.dp))
                SocialLoginPill("🍎", onClick = { Toast.makeText(context, "Apple Login", Toast.LENGTH_SHORT).show() })
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = t("madeInNepal"),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center,
            color = Color(0xFF9E9E9E),
            fontSize = 11.sp
        )
    }
}

@Composable
fun FaceVerifyScreenView(viewModel: DolphinViewModel, t: (String) -> String) {
    var isProcessing by remember { mutableStateOf(false) }
    var verifyResultText by remember { mutableStateOf("") }
    var verifySuccess by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "face_scan")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 280f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF212121))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(top = 40.dp, bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = t("faceVerifyTitle"),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFC107)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = t("faceVerifySubtitle"),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Custom Camera scan frame
            Box(
                modifier = Modifier
                    .size(260.dp, 300.dp)
                    .border(3.dp, Color(0xFFFFC107), RoundedCornerShape(32.dp))
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF101010)),
                contentAlignment = Alignment.Center
            ) {
                // Represent camera mockup using dynamic custom vector shapes (Canvas)
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val w = size.width
                    val h = size.height

                    // Drawing beautiful mock face contours (asymmetry & restraint layout)
                    drawArc(
                        color = Color.White.copy(alpha = 0.15f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = true,
                        size = androidx.compose.ui.geometry.Size(w * 0.5f, h * 0.45f),
                        topLeft = Offset(w * 0.25f, h * 0.2f)
                    )

                    // Draw shoulders
                    drawArc(
                        color = Color.White.copy(alpha = 0.15f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        size = androidx.compose.ui.geometry.Size(w * 0.8f, h * 0.4f),
                        topLeft = Offset(w * 0.1f, h * 0.65f)
                    )
                }

                if (!verifySuccess) {
                    // Scanning line moving continuously
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .offset(y = (-150 + scanLineY).dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFFFFC107),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                if (!isProcessing && verifyResultText.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📷",
                            fontSize = 58.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = t("faceVerifyInstruction"),
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.62f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFFFFC107))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = t("faceVerifyProcessing"),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (verifyResultText.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = verifyResultText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (verifySuccess) Color(0xFF4CAF50) else Color(0xFFF44336),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Instructions list in beautiful minimal layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
            ) {
                FaceInstructionRow("•  " + t("faceInstruction1"))
                FaceInstructionRow("•  " + t("faceInstruction2"))
                FaceInstructionRow("•  " + t("faceInstruction3"))
                FaceInstructionRow("•  " + t("faceInstruction4"))
            }
        }

        Button(
            onClick = {
                isProcessing = true
                verifyResultText = ""
                scope.launch {
                    delay(2000)
                    isProcessing = false
                    val pass = Math.random() > 0.15
                    if (pass) {
                        verifySuccess = true
                        verifyResultText = t("faceVerifySuccess")
                        delay(1200)
                        viewModel.navigateTo(AppScreen.Home)
                    } else {
                        verifySuccess = false
                        verifyResultText = t("faceVerifyFail")
                    }
                }
            },
            enabled = !isProcessing && !verifySuccess,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFC107),
                disabledContainerColor = Color(0xFFFFC107).copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .height(54.dp)
                .testTag("face_verify_trigger_button")
        ) {
            Text(
                text = t("faceVerifyButton"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
        }
    }
}

@Composable
fun AppHeaderView(viewModel: DolphinViewModel, t: (String) -> String, title: String) {
    val currentLang by viewModel.currentLang.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .drawBehind {
                drawLine(
                    color = Color(0xFFE0E0E0),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "🐬",
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF212121)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { viewModel.toggleAIChat() },
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFFFF8E1), CircleShape)
            ) {
                Text(text = "🤖")
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { /* Notifications placeholder */ },
                modifier = Modifier.size(36.dp)
            ) {
                Text(text = "🔔", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.toggleLang() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                border = BorderStroke(1.5.dp, Color(0xFFFFC107)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text(
                    text = if (currentLang == "ne") "EN" else "ने",
                    color = Color(0xFFFF6F00),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(viewModel: DolphinViewModel, t: (String) -> String, activeScreen: AppScreen) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.White)
            .drawBehind {
                drawLine(
                    color = Color(0xFFFFC107).copy(alpha = 0.15f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        BottomNavItem(
            icon = "🏠",
            label = t("navHome"),
            isActive = activeScreen == AppScreen.Home,
            onClick = { viewModel.navigateTo(AppScreen.Home) }
        )
        BottomNavItem(
            icon = "🛵",
            label = t("navRides"),
            isActive = activeScreen == AppScreen.Rides,
            onClick = { viewModel.navigateTo(AppScreen.Rides) }
        )
        BottomNavItem(
            icon = "💰",
            label = t("navWallet"),
            isActive = activeScreen == AppScreen.Wallet,
            onClick = { viewModel.navigateTo(AppScreen.Wallet) }
        )
        BottomNavItem(
            icon = "👑",
            label = t("navOwner"),
            isActive = activeScreen == AppScreen.Owner,
            onClick = { viewModel.navigateTo(AppScreen.Owner) }
        )
        BottomNavItem(
            icon = "⚙️",
            label = t("navSettings"),
            isActive = activeScreen == AppScreen.Settings,
            onClick = { viewModel.navigateTo(AppScreen.Settings) }
        )
    }
}

@Composable
fun BottomNavItem(icon: String, label: String, isActive: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = icon,
            fontSize = if (isActive) 22.sp else 19.sp,
            color = if (isActive) Color(0xFFFF6F00) else Color(0xFF9E9E9E)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Color(0xFFFF6F00) else Color(0xFF9E9E9E)
        )
    }
}

@Composable
fun ChatAssistantPanel(viewModel: DolphinViewModel, t: (String) -> String) {
    val isOpen by viewModel.aiChatOpen.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isGenerating by viewModel.isAiGenerating.collectAsState()

    var textInput by remember { mutableStateOf("") }

    if (isOpen) {
        Dialog(
            onDismissRequest = { viewModel.closeAIChat() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.72f)
                    .border(2.dp, Color(0xFFFFC107), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF212121), Color(0xFF161625))
                                )
                            )
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🐬", fontSize = 34.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = t("aiTitle"),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFC107)
                                )
                                Text(
                                    text = t("aiSubtitle"),
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.closeAIChat() },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Text(text = "✕", color = Color.White, fontSize = 13.sp)
                        }
                    }

                    // Chat messages viewport
                    val listState = rememberScrollState()
                    LaunchedEffect(chatMessages.size) {
                        listState.animateScrollTo(listState.maxValue)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFFAFAFA))
                            .verticalScroll(listState)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (message in chatMessages) {
                            ChatBubble(message)
                        }

                        if (isGenerating) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .clip(RoundedCornerShape(bottomStart = 2.dp, topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp))
                                    .background(Color(0xFFF0F0F0))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = t("aiThinking"),
                                    fontSize = 13.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // Easy Quick suggestions actions row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .background(Color(0xFFFAFAFA))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        QuickSuggestionPill(t("aiQuick1"), onClick = { viewModel.sendChatMessage(t("aiQuick1")) })
                        QuickSuggestionPill(t("aiQuick2"), onClick = { viewModel.sendChatMessage(t("aiQuick2")) })
                        QuickSuggestionPill(t("aiQuick3"), onClick = { viewModel.sendChatMessage(t("aiQuick3")) })
                        QuickSuggestionPill(t("aiQuick4"), onClick = { viewModel.sendChatMessage(t("aiQuick4")) })
                    }

                    Divider(color = Color(0xFFE0E0E0))

                    // Input box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text(text = t("aiPlaceholder"), fontSize = 13.sp) },
                            maxLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFC107),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(max = 80.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (textInput.trim().isNotEmpty()) {
                                    viewModel.sendChatMessage(textInput)
                                    textInput = ""
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFFFC107), CircleShape)
                        ) {
                            Text(text = "➤", fontSize = 16.sp, color = Color(0xFF212121))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreenView(viewModel: DolphinViewModel, t: (String) -> String) {
    val userName by viewModel.userName.collectAsState()
    val fromLoc by viewModel.fromLocation.collectAsState()
    val toLoc by viewModel.toLocation.collectAsState()
    val isSearching by viewModel.isSearchingRides.collectAsState()

    val showCategorySheetFor by viewModel.showSubtypeDialogFor.collectAsState()
    val activeTrackRide by viewModel.activeRideTracking.collectAsState()

    var showFromListDropdown by remember { mutableStateOf(false) }
    var showToListDropdown by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Set standard current position
    LaunchedEffect(Unit) {
        if (fromLoc.isEmpty()) {
            viewModel.updateFromLocation("Kathmandu, Nepal")
        }
    }

    val scaffoldState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp)
                .background(Color(0xFFFFF8E1))
                .verticalScroll(scaffoldState)
        ) {
            AppHeaderView(viewModel, t, "Dolphin Ride")

            // Greeting card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${t("homeGreeting")}, $userName!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = t("homeSubtitle"),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                // Commission badge
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3))
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🐬 5%",
                        color = Color(0xFFFF6F00),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Interactive Premium Dolphin Map Component
            DolphinMapView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                fromLoc = fromLoc,
                toLoc = toLoc,
                activeTracking = activeTrackRide != null,
                selectedCategory = showCategorySheetFor ?: "bike"
            )

            // Quick Category Shortcuts Dashboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionShortPill(icon = "🏍️", label = t("dolphinBike"), onClick = { viewModel.selectCategory("bike") })
                QuickActionShortPill(icon = "🛺", label = t("dolphinAuto"), onClick = { viewModel.selectCategory("auto") })
                QuickActionShortPill(icon = "🚗", label = t("dolphinCar"), onClick = { viewModel.selectCategory("car") })
                QuickActionShortPill(icon = "🆘", label = t("sos"), onClick = { triggerSosAlertLocal(context) })
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search destination with Gemini API pricing
            DolphinRideBookingForm(
                viewModel = viewModel,
                t = t,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Available categories list
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = t("availableRides"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Category Card Bike
                HomeScreenCategoryRow(
                    icon = "🏍️",
                    title = t("dolphinBike"),
                    desc = "Standard • Premium • Electric Scooter",
                    rate = "Rs. 15",
                    onBookClick = { viewModel.selectCategory("bike") }
                )

                // Category Card Auto
                HomeScreenCategoryRow(
                    icon = "🛺",
                    title = t("dolphinAuto"),
                    desc = "Standard • Share 3-Seater",
                    rate = "Rs. 25",
                    onBookClick = { viewModel.selectCategory("auto") }
                )

                // Category Card Car
                HomeScreenCategoryRow(
                    icon = "🚗",
                    title = t("dolphinCar"),
                    desc = "Economy • Comfort • Premium Custom • XL",
                    rate = "Rs. 40",
                    onBookClick = { viewModel.selectCategory("car") }
                )

                Spacer(modifier = Modifier.height(16.dp))
                // Platform Owner description commission badge
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🐬 " + t("commissionDescription"),
                        modifier = Modifier.padding(14.dp),
                        color = Color(0xFFFF6F00),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Floating Action buttons (SOS / AI help)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloatingActionButton(
                onClick = { viewModel.toggleAIChat() },
                containerColor = Color(0xFF1A1A2E),
                contentColor = Color(0xFFFFC107),
                shape = CircleShape,
                modifier = Modifier
                    .size(54.dp)
                    .border(2.dp, Color(0xFFFFC107), CircleShape)
            ) {
                Text(text = "🤖", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            FloatingActionButton(
                onClick = { triggerSosAlertLocal(context) },
                containerColor = Color(0xFFF44336),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(54.dp)
                    .border(2.dp, Color.White, CircleShape)
            ) {
                Text(text = "🆘", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }

        BottomNavBar(viewModel, t, AppScreen.Home)

        // Subtypes sheet popup overlays
        if (showCategorySheetFor != null) {
            SubtypeDialogView(viewModel, showCategorySheetFor!!, t)
        }

        // Active ride progress tracker screen overlays
        if (activeTrackRide != null) {
            ActiveRideTrackerView(viewModel, activeTrackRide!!, t)
        }

        ChatAssistantPanel(viewModel, t)
    }
}

@Composable
fun QuickActionShortPill(icon: String, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(Color(0xFFFFF8E1), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF757575)
        )
    }
}

@Composable
fun HomeScreenCategoryRow(icon: String, title: String, desc: String, rate: String, onBookClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onBookClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color(0xFFFFF8E1), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = desc, fontStyle = FontStyle.Normal, color = Color.Gray, fontSize = 11.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = rate, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFFF6F00))
                Text(text = "per km", fontSize = 10.sp, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun SubtypeDialogView(viewModel: DolphinViewModel, type: String, t: (String) -> String) {
    val title = when (type) {
        "bike" -> t("dolphinBike")
        "auto" -> t("dolphinAuto")
        else -> t("dolphinCar")
    }

    val subtypes = when (type) {
        "bike" -> listOf(
            Triple("standard", t("bikeStandard"), t("bikeStandardDesc")),
            Triple("premium", t("bikePremium"), t("bikePremiumDesc")),
            Triple("electric", t("bikeElectric"), t("bikeElectricDesc"))
        )
        "auto" -> listOf(
            Triple("standard", t("autoStandard"), t("autoStandardDesc")),
            Triple("share", t("autoShare"), t("autoShareDesc"))
        )
        else -> listOf(
            Triple("economy", t("carEconomy"), t("carEconomyDesc")),
            Triple("comfort", t("carComfort"), t("carComfortDesc")),
            Triple("premium", t("carPremium"), t("carPremiumDesc")),
            Triple("xl", t("carXL"), t("carXLDesc"))
        )
    }

    Dialog(
        onDismissRequest = { viewModel.selectCategory(null) }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6F00)
                )

                Spacer(modifier = Modifier.height(16.dp))

                subtypes.forEach { item ->
                    val subtypeId = item.first
                    val name = item.second
                    val desc = item.third

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                viewModel.initiateBooking(type, subtypeId)
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(Color(0xFFFFF8E1), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (type) {
                                        "bike" -> "🏍️"
                                        "auto" -> "🛺"
                                        else -> "🚗"
                                    },
                                    fontSize = 22.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = desc, fontSize = 11.sp, color = Color.Gray)
                            }

                            Text(
                                text = "👉",
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { viewModel.selectCategory(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Cancel", color = Color(0xFFFF6F00))
                }
            }
        }
    }

    // Active bookings Dialog details
    val activeSubSelected by viewModel.activeSubtypeSelected.collectAsState()
    if (activeSubSelected != null) {
        BookingConfirmationDialog(viewModel, type, activeSubSelected!!, t)
    }
}

@Composable
fun BookingConfirmationDialog(viewModel: DolphinViewModel, type: String, subtype: String, t: (String) -> String) {
    val activePrice by viewModel.activePrice.collectAsState()
    val activeDist by viewModel.activeDistance.collectAsState()
    val activeDriver by viewModel.activeDriver.collectAsState()

    val context = LocalContext.current

    if (activeDriver != null) {
        Dialog(
            onDismissRequest = { viewModel.initiateBooking(type, "") }
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Booking Confirmation",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Estimated Fare",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Rs. ${activePrice.toInt()}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF6F00)
                    )
                    Text(
                        text = "(Distance: ${String.format(Locale.US, "%.1f", activeDist)} km)",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Driver info block
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(Color(0xFFFFC107), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "👤", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = activeDriver!!.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "${activeDriver!!.vehicle} • ⭐ ${activeDriver!!.rating}", fontSize = 11.sp, color = Color.Gray)
                            }
                            Text(
                                text = "📞 " + activeDriver!!.phone.takeLast(4),
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "🐬 5% platforms owner commission is added automatically",
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFFF6F00),
                        fontStyle = FontStyle.Italic
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.confirmRideBooking(type, subtype)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Confirm Booking", fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.initiateBooking(type, "") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Go Back", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveRideTrackerView(viewModel: DolphinViewModel, ride: Ride, t: (String) -> String) {
    val step by viewModel.activeTrackingStep.collectAsState()
    val driverState by viewModel.activeDriver.collectAsState()
    val otp by viewModel.activeOtp.collectAsState()
    val price by viewModel.activePrice.collectAsState()

    val showRatingDialog by viewModel.showRatingDialog.collectAsState()

    val context = LocalContext.current

    val driver = driverState
    if (driver != null) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = t("rideConfirmed"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6F00)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Animated linear trace stepper progress bar indicators
                    LinearProgressIndicator(
                        progress = (step + 1) / 4f,
                        color = Color(0xFFFFC107),
                        trackColor = Color(0xFFEEEEEE),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ActiveStepText("Confirmed", isActive = step >= 0)
                        ActiveStepText("Arriving", isActive = step >= 1)
                        ActiveStepText("Started", isActive = step >= 2)
                        ActiveStepText("Completed", isActive = step >= 3)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "🔐 ${t("otpToStart")}: $otp",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF6F00)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DriverStatusPanel(
                        step = step,
                        driverName = driver.name,
                        vehiclePlate = driver.vehicle,
                        phoneNumber = driver.phone
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { Toast.makeText(context, "Calling Driver...", Toast.LENGTH_SHORT).show() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF8E1)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = t("contactDriver"), color = Color(0xFFFF6F00), fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                viewModel.dismissRatingDialog()
                                Toast.makeText(context, "Ride Cancelled", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Cancel", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    if (showRatingDialog) {
        RatingSubmissionDialog(viewModel, ride, price, t)
    }
}

@Composable
fun RatingSubmissionDialog(viewModel: DolphinViewModel, ride: Ride, price: Double, t: (String) -> String) {
    var starsGiven by remember { mutableStateOf(5) }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = t("rideCompleteTitle"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6F00)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = t("rideCompleteMsg"),
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Breakdown cost
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Base Fare", fontSize = 12.sp, color = Color.Gray)
                            Text(text = "Rs. ${(price * 0.4).toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Distance Fare", fontSize = 12.sp, color = Color.Gray)
                            Text(text = "Rs. ${(price * 0.4).toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Time Fare", fontSize = 12.sp, color = Color.Gray)
                            Text(text = "Rs. ${(price * 0.2).toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Total Paid", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Rs. ${price.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF6F00))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = t("rateDriver"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..5).forEach { starIndex ->
                        val active = starIndex <= starsGiven
                        IconButton(
                            onClick = { starsGiven = starIndex },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Text(
                                text = "⭐",
                                fontSize = 30.sp,
                                modifier = Modifier.drawBehind {
                                    drawCircle(Color.Transparent)
                                },
                                color = if (active) Color(0xFFFFC107) else Color.LightGray.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.submitRideRating(starsGiven)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = t("submitRating"), fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                }
            }
        }
    }
}

@Composable
fun RidesScreenView(viewModel: DolphinViewModel, t: (String) -> String) {
    val ridesHistorical by viewModel.ridesList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8E1))
            .padding(bottom = 64.dp)
    ) {
        AppHeaderView(viewModel, t, t("navRides"))

        if (ridesHistorical.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🛵", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "No rides completed yet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Book your first ride on Home screen!", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                PastRidesListView(
                    rides = ridesHistorical,
                    onRideClick = {}
                )
            }
        }

        BottomNavBar(viewModel, t, AppScreen.Rides)
    }
}

@Composable
fun WalletScreenView(viewModel: DolphinViewModel, t: (String) -> String) {
    val balance by viewModel.walletBalance.collectAsState()
    val transactions by viewModel.transactionsList.collectAsState()

    var showLoadDialog by remember { mutableStateOf(false) }
    var loadAmount by remember { mutableStateOf("") }

    var showWithdrawDialog by remember { mutableStateOf(false) }
    var withdrawAmount by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8E1))
            .padding(bottom = 64.dp)
    ) {
        AppHeaderView(viewModel, t, t("wallet"))

        // Wallet Balance visual card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF212121))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Dolphin Wallet",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Rs. ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFC107)
                    )
                    Text(
                        text = String.format(Locale.US, "%,.1f", balance),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFC107)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showLoadDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "➕ Add Money", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showWithdrawDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "💸 Withdraw", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Text(
            text = t("transactionHistory"),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            items(transactions) { tx ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (tx.type == "commission" || tx.type == "earning") Color(0xFFFFF8E1) else Color(0xFFFAFAFA),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (tx.type) {
                                    "earning" -> "🛵"
                                    "commission" -> "🐬"
                                    "load" -> "➕"
                                    else -> "💸"
                                },
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = tx.desc, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "${tx.dateStr} • ${tx.timeStr}", fontSize = 11.sp, color = Color.Gray)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val sign = if (tx.type == "load" || tx.type == "earning") "+" else "-"
                            val color = if (tx.type == "load" || tx.type == "earning") Color(0xFF2E7D32) else Color(0xFFC62828)
                            Text(
                                text = "${sign}Rs. ${tx.amount.toInt()}",
                                fontWeight = FontWeight.Bold,
                                color = color,
                                fontSize = 14.sp
                            )
                            if (tx.commission > 0) {
                                Text(
                                    text = "🐬 5%: Rs. ${tx.commission.toInt()}",
                                    fontSize = 9.sp,
                                    color = Color(0xFFFF6F00)
                                )
                            }
                        }
                    }
                }
            }
        }

        BottomNavBar(viewModel, t, AppScreen.Wallet)
    }

    if (showLoadDialog) {
        Dialog(onDismissRequest = { showLoadDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Load Wallet Cash", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = loadAmount,
                        onValueChange = { loadAmount = it },
                        placeholder = { Text(text = "Amount in NPR") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFFC107))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val amt = loadAmount.toDoubleOrNull()
                            if (amt != null && amt > 0) {
                                viewModel.addWalletMoney(amt)
                                showLoadDialog = false
                                loadAmount = ""
                                Toast.makeText(context, "Wallet Loaded Successfully!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
                    ) {
                        Text(text = "Confirm Load")
                    }
                }
            }
        }
    }

    if (showWithdrawDialog) {
        Dialog(onDismissRequest = { showWithdrawDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Withdraw Cash", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = withdrawAmount,
                        onValueChange = { withdrawAmount = it },
                        placeholder = { Text(text = "Amount in NPR") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFFC107))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val amt = withdrawAmount.toDoubleOrNull()
                            if (amt != null && amt > 0 && amt <= balance) {
                                viewModel.withdrawWalletMoney(amt)
                                showWithdrawDialog = false
                                withdrawAmount = ""
                                Toast.makeText(context, "Withdrawal Initiated!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Insufficient Funds!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
                    ) {
                        Text(text = "Confirm Withdraw")
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerScreenView(viewModel: DolphinViewModel, t: (String) -> String) {
    val isAuthed by viewModel.isOwnerAuthenticated.collectAsState()
    val transactions by viewModel.transactionsList.collectAsState()

    val ownerTodayEarnings by viewModel.ownerTodayEarnings.collectAsState()
    val ownerTotalEarnings by viewModel.ownerTotalEarnings.collectAsState()
    val ownerTodayRides by viewModel.ownerTodayRides.collectAsState()
    val ownerTotalRides by viewModel.ownerTotalRides.collectAsState()

    var passwordInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF212121))
            .padding(bottom = 64.dp)
    ) {
        if (!isAuthed) {
            // Lock screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFC107).copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(text = "🔒 " + t("ownerAccessOnly"), color = Color(0xFFFFC107), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Dolphin Owner Portal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "👑", fontSize = 72.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = t("ownerLoginTitle"), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                Text(text = t("ownerLoginSub"), fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    placeholder = { Text(text = "Password Code / PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFC107),
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (passwordInput.isNotEmpty()) {
                            viewModel.authenticateOwner(passwordInput)
                        } else {
                            Toast.makeText(context, "Please enter Password", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = t("loginButton"), fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        } else {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "👑", fontSize = 34.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = t("ownerGreeting"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Text(text = t("commissionDescription"), fontSize = 10.sp, color = Color(0xFFFFC107))
                    }
                }
            }

            // Stats grid
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFFFF8E1))
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OwnerStatsCard(
                        icon = "💰",
                        value = "Rs. ${ownerTodayEarnings.toInt()}",
                        label = t("todayEarnings"),
                        modifier = Modifier.weight(1f)
                    )
                    OwnerStatsCard(
                        icon = "📊",
                        value = "Rs. ${ownerTotalEarnings.toInt()}",
                        label = t("totalEarnings"),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OwnerStatsCard(
                        icon = "🛵",
                        value = "$ownerTodayRides",
                        label = t("todayRides"),
                        modifier = Modifier.weight(1f)
                    )
                    OwnerStatsCard(
                        icon = "👥",
                        value = "42",
                        label = t("activeDrivers"),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "📋 Commission Audits", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                Spacer(modifier = Modifier.height(8.dp))

                // Commission tx list filter out earnings
                val commissions = transactions.filter { it.type == "commission" || it.commission > 0 }
                commissions.take(5).forEach { tx ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFFFF8E1), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🐬", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (tx.type == "commission") "5% Platform Credit" else "Audit Log: " + tx.desc.take(20) + "...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = "${tx.dateStr} • ${tx.timeStr}", fontSize = 10.sp, color = Color.Gray)
                            }
                            Text(
                                text = "+Rs. ${if (tx.type == "commission") tx.amount.toInt() else tx.commission.toInt()}",
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        BottomNavBar(viewModel, t, AppScreen.Owner)
    }
}

@Composable
fun OwnerStatsCard(icon: String, value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF6F00))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun SettingsScreenView(viewModel: DolphinViewModel, t: (String) -> String) {
    val currentLang by viewModel.currentLang.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userPhone by viewModel.userPhone.collectAsState()

    var isNotificationEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8E1))
            .padding(bottom = 64.dp)
    ) {
        AppHeaderView(viewModel, t, t("settings"))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Language Selection Block
            Column {
                Text(
                    text = t("language"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LanguageCheckCard(
                        flag = "🇳🇵",
                        name = "Nepali",
                        native = "नेपाली",
                        isSelected = currentLang == "ne",
                        onClick = { viewModel.setLang("ne") },
                        modifier = Modifier.weight(1f)
                    )

                    LanguageCheckCard(
                        flag = "🇬🇧",
                        name = "English",
                        native = "English",
                        isSelected = currentLang == "en",
                        onClick = { viewModel.setLang("en") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // User Info setting block
            Column {
                Text(
                    text = t("profile"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { Toast.makeText(context, "Editing logic is in progress!", Toast.LENGTH_SHORT).show() }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFFFF8E1), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👤", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = userPhone.ifEmpty { "9841234567" }, fontSize = 11.sp, color = Color.Gray)
                        }
                        Text(text = "→", color = Color.Gray)
                    }
                }
            }

            // Other configs block
            Column {
                Text(
                    text = "Controls",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔔", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = t("notifications"), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Switch(
                            checked = isNotificationEnabled,
                            onCheckedChange = { isNotificationEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFC107), checkedTrackColor = Color(0xFFFFE082))
                        )
                    }

                    Divider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "Privacy: Data is secured locally using Room persistence", Toast.LENGTH_LONG).show()
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔒", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = t("privacy"), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(text = "→", color = Color.Gray)
                    }

                    Divider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "Dolphin Ride Nepal v2.0 - Developed with Jetpack Compose", Toast.LENGTH_LONG).show()
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🐬", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = t("about"), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(text = t("version"), fontSize = 11.sp, color = Color.LightGray)
                    }
                }
            }

            // Quick App Install mockup
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        Toast.makeText(context, t("downloadReady"), Toast.LENGTH_SHORT).show()
                    },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                border = BorderStroke(1.dp, Color(0xFFC8E6C9))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📲", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = t("installApp"), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                        Text(text = t("installDesc"), fontSize = 11.sp, color = Color.Gray)
                    }
                    Text(text = "↓", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E7D32))
                }
            }

            // Sign out
            Button(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = t("logout"), fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer credits
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = t("appName") + " " + t("version"), fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(text = t("madeInNepal"), fontSize = 11.sp, color = Color.Gray)
                Text(text = t("copyright"), fontSize = 11.sp, color = Color.LightGray)
            }
        }

        BottomNavBar(viewModel, t, AppScreen.Settings)
    }
}

@Composable
fun LanguageCheckCard(flag: String, name: String, native: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable { onClick() },
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFFFFC107) else Color(0xFFE0E0E0)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFF8E1) else Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = flag, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = native, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun FaceInstructionRow(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.7f),
        fontSize = 13.sp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun QuickSuggestionPill(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(Color(0xFFFFF8E1), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFFFC107).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF6F00))
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isBot = !message.isUser
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = if (isBot) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isBot) 2.dp else 16.dp,
                        bottomEnd = if (isBot) 16.dp else 2.dp
                    )
                )
                .background(if (isBot) Color(0xFFE0E0E0) else Color(0xFFFFC107))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 13.sp,
                color = Color(0xFF212121),
                fontWeight = if (isBot) FontWeight.Normal else FontWeight.Medium
            )
        }
    }
}

@Composable
fun ActiveStepText(text: String, isActive: Boolean) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = if (isActive) Color(0xFFFF6F00) else Color.LightGray
    )
}

fun triggerSosAlertLocal(context: android.content.Context) {
    Toast.makeText(
        context,
        "🚨 Emergency SOS Triggered! Sending GPS location and contacting police 100 + alerting nearby riders in real-time!",
        Toast.LENGTH_LONG
    ).show()
}

@Composable
fun SocialLoginPill(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE0E0E0), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = symbol, fontSize = 24.sp)
    }
}
