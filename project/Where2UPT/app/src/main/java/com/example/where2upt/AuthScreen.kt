import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.where2upt.AuthRepository
import kotlinx.coroutines.launch
import com.example.where2upt.R

@Composable
fun AuthScreen(
    backgroundRes: Int = R.drawable.bg_upt,
    onAuthenticated: () -> Unit
) {
    val repo = remember { AuthRepository() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var pwdVisible by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current

    fun isUptEmail(raw: String): Boolean {
        val e = raw.trim().lowercase()
        return e.endsWith("@student.upt.ro") || e.endsWith("@upt.ro")
    }

    fun mapError(t: Throwable): String {
        val msg = t.message?.lowercase().orEmpty()
        return when {
            "no user record" in msg -> "Account not found."
            "badly formatted" in msg -> "Invalid email format."
            "password is invalid" in msg -> "Wrong password."
            "blocked all requests" in msg -> "Too many attempts. Try again later."
            "network error" in msg -> "Network error. Check your connection."
            else -> t.message ?: "Authentication error."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF3F51B5), Color(0xFF673AB7))))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Where2UPT", fontSize = 30.sp, color = Color.White)
                Spacer(Modifier.height(10.dp))
                Text(
                    "Sign in with your institutional account",
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    isError = email.isNotBlank() && !isUptEmail(email),
                    supportingText = {
                        if (email.isNotBlank() && !isUptEmail(email)) {
                            Text("Use an @student.upt.ro or @upt.ro email", color = Color(0xFFFFCDD2))
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White
                    )
                )

                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (pwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { pwdVisible = !pwdVisible }) {
                            Icon(
                                if (pwdVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (pwdVisible) "Hide password" else "Show password",
                                tint = Color.White
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White
                    )
                )

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        focus.clearFocus()
                        scope.launch {
                            loading = true; error = null; message = null
                            val mail = email.trim().lowercase()
                            try {
                                if (!isUptEmail(mail)) throw IllegalArgumentException(
                                    "Please use an @student.upt.ro or @upt.ro email."
                                )
                                if (password.isBlank()) throw IllegalArgumentException("Enter your password.")
                                repo.login(mail, password)
                                onAuthenticated()
                            } catch (e: Exception) {
                                error = mapError(e)
                            } finally { loading = false }
                        }
                    },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Login", color = Color.Black)
                }

                if (error != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(error!!, color = Color.Red, fontSize = 13.sp)
                }

                if (message != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(message!!, color = Color(0xFFB3E5FC), fontSize = 13.sp)
                }

                Spacer(Modifier.height(12.dp))
                TextButton(onClick = { showResetDialog = true }) {
                    Text("I lost my password", color = Color.White)
                }

                if (loading) {
                    Spacer(Modifier.height(8.dp))
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }

        if (showResetDialog) {
            ResetPasswordDialog(
                email = email.trim().lowercase(),
                validateDomain = ::isUptEmail,
                onDismiss = { showResetDialog = false },
                onReset = { enteredEmail ->
                    scope.launch {
                        loading = true; error = null
                        val mail = enteredEmail.trim().lowercase()
                        try {
                            if (!isUptEmail(mail)) throw IllegalArgumentException(
                                "Please use an @student.upt.ro or @upt.ro email."
                            )
                            repo.resetPassword(mail)
                            message = "Reset link sent to $mail"
                        } catch (e: Exception) {
                            error = mapError(e)
                        } finally {
                            loading = false; showResetDialog = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun ResetPasswordDialog(
    email: String,
    validateDomain: (String) -> Boolean,
    onDismiss: () -> Unit,
    onReset: (String) -> Unit
) {
    var tempEmail by remember { mutableStateOf(email) }
    val isValid = tempEmail.isNotBlank() && validateDomain(tempEmail)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset password") },
        text = {
            Column {
                Text("Enter your institutional email to receive a reset link:")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = tempEmail,
                    onValueChange = { tempEmail = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = tempEmail.isNotBlank() && !validateDomain(tempEmail),
                    supportingText = {
                        if (tempEmail.isNotBlank() && !validateDomain(tempEmail)) {
                            Text("Use an @student.upt.ro or @upt.ro email")
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onReset(tempEmail) }, enabled = isValid) {
                Text("Send link")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
