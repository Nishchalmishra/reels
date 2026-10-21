package arivihan.technologies.doubtbuzzter2.compose.component

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.compose.ArivihanScreenState
import arivihan.technologies.doubtbuzzter2.compose.ui.appFontFamily
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import kotlin.time.Duration.Companion.milliseconds


enum class ArivihanButtonStyle {
    Primary,
    Secondary,
    ErrorPrimary,
    ErrorSecondary
}




// ArivihanScreenStateContainer
// ArivihanButton
// RewardButton
// AsyncImage
// ArivihanText
// ArivihanTopAppBar
// FormInputField
// ArivihanAutoResizeText
// ShimmerEffect
// OrderHistoryShimmer
// BookOrderShimmer




@Composable
fun rememberNetworkConnected(): Boolean {
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(true) }

    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isConnected = true
            }
            override fun onLost(network: Network) {
                isConnected = false
            }
        }

        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    return isConnected
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun NetworkSnackbar(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    var wasDisconnected by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var snackbarType by remember { mutableStateOf(ArivihanSnackbarType.Positive) }
    val context = LocalContext.current

    LaunchedEffect(isConnected) {
        if (!isConnected) {
            wasDisconnected = true
            snackbarType = ArivihanSnackbarType.Negative
            snackbarMessage = context.getString(R.string.NoInternetConnection)
        } else if (wasDisconnected) {
            snackbarType = ArivihanSnackbarType.Positive
            snackbarMessage = context.getString(R.string.InternetConnection)
        }
    }

    ArivihanSnackbar(
        message = snackbarMessage,
        type = snackbarType,
        modifier = modifier,
        onDismiss = { snackbarMessage = null }
    )
}



@Composable
fun <T> ArivihanScreenStateContainer(
    state: ArivihanScreenState<T>,
    modifier: Modifier = Modifier,
    snackbarModifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
    loadingContent: (@Composable () -> Unit)? = null,
    content: @Composable (T) -> Unit
) {
    val isNetworkConnected = rememberNetworkConnected()

    //reload only when network reconnects and state is NetworkError
    LaunchedEffect(isNetworkConnected) {
        if (isNetworkConnected && state is ArivihanScreenState.NetworkError) {
            onRetry()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        when (state) {
            is ArivihanScreenState.Loading -> {
                if (loadingContent != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        loadingContent()
                    }
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            is ArivihanScreenState.Content -> content(state.data)
            is ArivihanScreenState.NetworkError -> ArivihanNetworkError(onRetry = onRetry)
            is ArivihanScreenState.Error -> ArivihanErrorScreen(
                message = state.message,
                onRetry = onRetry
            )
        }

        NetworkSnackbar(
            isConnected = isNetworkConnected,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .then(snackbarModifier)
        )
    }
}


@Composable
fun rememberNetworkGuard(): (() -> Unit) -> Unit {
    val context = LocalContext.current
    val isConnected = rememberNetworkConnected()

    return remember(isConnected, context) {
        { action: () -> Unit ->
            if (isConnected) {
                action()
            } else {
                android.widget.Toast.makeText(
                    context,
                    R.string.NoInternetConnection,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}


/////// Button ////

@Composable
fun ArivihanButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    style: ArivihanButtonStyle = ArivihanButtonStyle.Primary,
    leftIcon: (@Composable (() -> Unit))? = null,
    rightIcon: (@Composable (() -> Unit))? = null,
) {
    val (containerColor, contentColor, borderColor) = when (style) {
        ArivihanButtonStyle.Primary -> Triple(
            if (enabled) colorResource(R.color.btn_enabled_bg) else colorResource(R.color.btn_disabled_bg),
            if (enabled) colorResource(R.color.btn_enabled_text) else colorResource(R.color.btn_disabled_text),
            Color.Transparent
        )

        ArivihanButtonStyle.Secondary -> Triple(
            Color.Transparent,
            if (enabled) colorResource(R.color.arivihan_secondary_color) else colorResource(R.color.btn_disabled_text),
            if (enabled) colorResource(R.color.arivihan_secondary_color) else colorResource(R.color.btn_disabled_text)
        )

        ArivihanButtonStyle.ErrorPrimary -> Triple(
            if (enabled) colorResource(R.color.error_btn_primary_bg) else colorResource(R.color.btn_disabled_bg),
            if (enabled) colorResource(R.color.error_btn_primary_tx) else colorResource(R.color.btn_disabled_text),
            Color.Transparent
        )

        ArivihanButtonStyle.ErrorSecondary -> Triple(
            if (enabled) colorResource(R.color.error_btn_secondary_bg) else colorResource(R.color.btn_disabled_bg),
            if (enabled) colorResource(R.color.error_btn_secondary_tx) else colorResource(R.color.btn_disabled_text),
            if (enabled) colorResource(R.color.error_btn_secondary_tx) else colorResource(R.color.btn_disabled_text)
        )
    }

    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp),
        border = if (borderColor != Color.Transparent) BorderStroke(1.dp, borderColor) else null,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = if (loading) containerColor else containerColor,
            disabledContentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            hoveredElevation = 0.dp,
            focusedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
            } else {
                leftIcon?.invoke()

                if (leftIcon != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                )

                if (rightIcon != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                rightIcon?.invoke()
            }
        }
    }
}





@Composable
fun RewardButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(48.dp)
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            val goldBrush = Brush.linearGradient(
                0.1484f to Color(0xFFEF9F0B),
                0.3647f to Color(0xFFFBD950),
                0.5228f to Color(0xFFFEEC77),
                0.6656f to Color(0xFFFBD950),
                0.8516f to Color(0xFFEA9C0B)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(50.dp)
                    .offset(x = (-20).dp, y = (-10).dp)
                    .background(color = Color(0xFFEF9F0B))
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(50.dp)
                    .offset(x = 20.dp, y = 10.dp)
                    .background(color = Color(0xFFEF9F0B))
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = colorResource(R.color.new_lecture_premium_btn),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                Icon(
                    painter = painterResource(R.drawable.ic_pro_premium_star),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))


                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        brush = goldBrush
                    )
                )
            }
        }
    }
}








/**
 * @param onAspectRatio reports the decoded artwork's height / width once it lands. Callers whose
 *   image carries baked-in content — text, badges, framing that must not be shaved — can size
 *   their box off this instead of hardcoding a ratio and cropping the mismatch away.
 */
@Composable
fun AsyncImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    errorContent: (@Composable (() -> Unit))? = null,
    colorFilter: androidx.compose.ui.graphics.ColorFilter? = null,
    onAspectRatio: ((Float) -> Unit)? = null,
) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    // The effect is keyed on url alone, so a plain capture would pin the first lambda and
    // report the ratio to a stale caller after a recomposition.
    val currentOnAspectRatio by rememberUpdatedState(onAspectRatio)

    androidx.compose.runtime.LaunchedEffect(url) {
        if (url.isNullOrBlank()) {
            isLoading = false
            hasError = true
            return@LaunchedEffect
        }

        try {
            // Only the decode belongs on IO; the state writes stay on the composition's
            // dispatcher rather than being published from a background thread.
            val bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get()
            }

            imageBitmap = bitmap
            isLoading = false
            hasError = false
            if (bitmap.width > 0 && bitmap.height > 0) {
                currentOnAspectRatio?.invoke(bitmap.height.toFloat() / bitmap.width)
            }
        } catch (e: Exception) {
            Log.e("AsyncImage", "Error loading image: $e")
            isLoading = false
            hasError = true
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                ShimmerEffect(modifier = Modifier.fillMaxSize())
            }

            hasError -> {
                errorContent?.invoke()
            }

            imageBitmap != null -> {
                Image(
                    bitmap = imageBitmap!!.asImageBitmap(),
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize(),
                    colorFilter = colorFilter
                )
            }
        }
    }
}




@Composable
fun ShimmerEffect(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.5f),
        Color.LightGray.copy(alpha = 0.3f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim.value - 500f, translateAnim.value - 500f),
        end = Offset(translateAnim.value, translateAnim.value)
    )

    Box(
        modifier = modifier.background(brush)
    )
}





/////// TEXT ////


@Composable
fun ArivihanText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight? = null
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        fontWeight = fontWeight,
        fontFamily = appFontFamily()
    )
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArivihanTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ArivihanTopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.dark_text_color)
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_new),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Unspecified
                )
            }
        },
        modifier = modifier
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArivihanTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.White
    )
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        TopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = colors
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.LightGray)
        )
    }
}




enum class InputType{
    TEXT,
    TEXTAREA
}




///////// Form Data //////

@Composable
fun FormInputField(
    modifier: Modifier = Modifier,
    value: String,
    isMandatory: Boolean = false,
    title: String,
    hint: String,
    error: String = "",
    imeAction: ImeAction,
    inputType: InputType = InputType.TEXT,
    onValueChange: (String) -> Unit,
    isFocused: (Boolean) -> Unit = {},
    isDone: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    onNext: () -> Unit = {},
    startIcon: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {

    val focusManager = LocalFocusManager.current
    var focused by remember { mutableStateOf(false) }



    LaunchedEffect(focused) {
        isFocused(focused)
    }


    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {

        if (title.isNotEmpty()) {
            Text(
                buildAnnotatedString {
                    append(title)

                    if (isMandatory) {
                        append(" ")
                        withStyle(
                            style = SpanStyle(
                                color = Color.Red,
                                fontSize = 12.sp,
                                baselineShift = BaselineShift.Superscript
                            )
                        ) {
                            append("*")
                        }
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 5.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            singleLine = inputType != InputType.TEXTAREA,
            minLines = if (inputType == InputType.TEXTAREA) 4 else 1,
            maxLines = if (inputType == InputType.TEXTAREA) 10 else 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onNext() },
                onDone = {
                    isDone()
                    focusManager.clearFocus()
                }
            ),
            modifier = modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused }
                .background(
                    color = Color.Gray.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 4.dp),
            textStyle = LocalTextStyle.current.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (startIcon != null) {
                        Spacer(Modifier.width(2.dp))
                        startIcon()
                        Spacer(Modifier.width(12.dp))
                    }

                    Box {
                        if (value.isEmpty()) {
                            Text(
                                text = hint,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                maxLines = 1
                            )
                        }

                        innerTextField()
                    }

                    if (trailingContent != null) {
                        Spacer(Modifier.weight(1f))
                        trailingContent()
                    }
                }
            }
        )


        if (error.isNotEmpty()) {
            Text(
                text = error,
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                style = MaterialTheme.typography.titleSmall,
                color = Color.Red
            )
        }
    }
}































@Composable
fun ArivihanOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(text = label) },
        placeholder = { Text(text = placeholder) },
        singleLine = false,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}





@Composable
fun ArivihanAlertDialog(
    visible: Boolean,
    title: String,
    text: String,
    confirmText: String = "OK",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title, fontWeight = FontWeight.Bold) },
            text = { Text(text = text) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(text = confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = "Close")
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArivihanBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (visible) {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (!subtitle.isNullOrEmpty()) {
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
                HorizontalDivider()
                content()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}



@Composable
private fun StatChip(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.6f
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}



@Composable
fun ArivihanAutoResizeText(
    modifier: Modifier = Modifier,
    text: String,
    maxLines: Int = 2,
    baseFontSize: TextUnit,
    minFontSize: TextUnit,
    textAlign: TextAlign = TextAlign.Center,
) {
    var fontSize by remember(text, baseFontSize) { mutableStateOf(baseFontSize) }
    var readyToDraw by remember(text, baseFontSize) { mutableStateOf(false) }

    Text(
        text = text,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            fontSize = fontSize,
        ),
        onTextLayout = { textLayoutResult ->
            if (!readyToDraw) {
                val hasOverflow = textLayoutResult.didOverflowWidth ||
                                  textLayoutResult.didOverflowHeight ||
                                  textLayoutResult.lineCount > maxLines

                if (hasOverflow && fontSize.value > minFontSize.value) {
                    fontSize = (fontSize.value * 0.9f).sp
                } else {
                    readyToDraw = true
                }
            }
        },
        modifier = modifier
    )
}

private fun resolveTopBannerIcon(iconName: String): Int {
    return when (iconName.lowercase()) {
        "star", "reward" -> R.drawable.ic_edit_profile_star
        "crown", "pro" -> R.drawable.ic_edit_profile_crown
        else -> R.drawable.referpng
    }
}








@Composable
fun ArivihanNetworkError(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_compose_network_error),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.no_internet_connection),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 21.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.check_internet_and_retry),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(R.color.text_gray),
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.btn_enabled_bg),
                contentColor = Color.White
            ),
            modifier = Modifier.height(44.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.retry_image),
                contentDescription = null,
                modifier = Modifier.size(21.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.retry_again),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


@Composable
fun ArivihanErrorScreen(
    modifier: Modifier = Modifier,
    message: String? = null,
    onRetry: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_error),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource( R.string.something_went_wrong),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 21.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.please_try_again_later),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(R.color.text_gray),
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.btn_enabled_bg),
                contentColor = Color.White
            ),
            modifier = Modifier.height(44.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.retry_image),
                contentDescription = null,
                modifier = Modifier.size(21.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.retry_again),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}





enum class ArivihanSnackbarType {
    Positive,
    Negative
}

@Composable
fun ArivihanSnackbar(
    message: String?,
    type: ArivihanSnackbarType,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    val backgroundColor = when (type) {
        ArivihanSnackbarType.Positive -> colorResource(R.color.navy_blue)
        ArivihanSnackbarType.Negative -> colorResource(R.color.error_button_color)
    }

    val duration = when (type) {
        ArivihanSnackbarType.Positive -> 1000.milliseconds
        ArivihanSnackbarType.Negative -> 5000.milliseconds
    }

    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(duration)
            onDismiss()
        }
    }

    androidx.compose.animation.AnimatedVisibility(
        visible = message != null,
        enter = androidx.compose.animation.slideInVertically { it } + androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.slideOutVertically { it } + androidx.compose.animation.fadeOut(),
        // Every Compose snackbar must clear the navigation bar. Callers can add an extra
        // modifier when a screen also has a fixed bottom action row.
        modifier = Modifier
            .navigationBarsPadding()
            .then(modifier)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(backgroundColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = message ?: "",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}




@Composable
fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outline,
    dashWidth: Float = 25f,
    gapWidth: Float = 16f
) {

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .alpha(0.8f)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, center.y),
            end = Offset(size.width, center.y),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashWidth, gapWidth),
                0f
            )
        )
    }
}




enum class InfoBannerSize { Low, Medium, Large }


@Composable
fun ArivihanInfoBanner(
    text: String,
    @DrawableRes iconRes: Int,
    color: Color,
    modifier: Modifier = Modifier,
    size: InfoBannerSize = InfoBannerSize.Low
) {
    val textSize = when (size) {
        InfoBannerSize.Low -> 12.sp
        InfoBannerSize.Medium -> 13.sp
        InfoBannerSize.Large -> 15.sp
    }
    val iconSize = when (size) {
        InfoBannerSize.Low -> 16.dp
        InfoBannerSize.Medium -> 18.dp
        InfoBannerSize.Large -> 20.dp
    }
    val horizontalPadding = when (size) {
        InfoBannerSize.Low -> 12.dp
        InfoBannerSize.Medium -> 13.dp
        InfoBannerSize.Large -> 14.dp
    }
    val verticalPadding = when (size) {
        InfoBannerSize.Low -> 4.dp
        InfoBannerSize.Medium -> 5.dp
        InfoBannerSize.Large -> 6.dp
    }

    val gradient = Brush.horizontalGradient(
        colorStops = arrayOf(
            0.00f to color.copy(alpha = 0.08f),
            0.49f to color.copy(alpha = 0.00f),
            1.00f to color.copy(alpha = 0.08f)
        )
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(100.dp))
            .background(brush = gradient)
            .border(1.dp, color.copy(alpha = 0.60f), RoundedCornerShape(100.dp))
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(iconSize)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = textSize,
            color = color,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}








@Composable
fun ArivihanQuantityCounter(
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = colorResource(R.color.arivihan_secondary_color)
) {
    if (count == 0) {
        OutlinedButton(
            onClick = onIncrement,
            modifier = modifier.height(34.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, accentColor),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add_24),
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = accentColor
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = stringResource(R.string.add),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
        }
    }
    else {
        androidx.compose.material3.Surface(
            modifier = modifier,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 1.dp,
            border = BorderStroke(1.dp, Color(0xFFF0F0F0)),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.height(34.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .width(30.dp)
                        .clickable { onDecrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "−",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Text(
                    text = count.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(24.dp),
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .width(30.dp)
                        .clickable { onIncrement() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
