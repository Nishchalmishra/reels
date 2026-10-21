package arivihan.technologies.doubtbuzzter2.activity.refer

import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen
import arivihan.technologies.doubtbuzzter2.compose.ArivihanComposeApp
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanInfoBanner
import arivihan.technologies.doubtbuzzter2.compose.component.DashedDivider
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.BookCartItem
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.BookCartItemData
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.BookCartItemMode
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys
import arivihan.technologies.doubtbuzzter2.model.order_success.OrderItemView
import arivihan.technologies.doubtbuzzter2.model.order_success.OrderView
import arivihan.technologies.doubtbuzzter2.repository.OrderConfirmRepository
import arivihan.technologies.doubtbuzzter2.viewModel.OrderConfirmUiState
import arivihan.technologies.doubtbuzzter2.viewModel.OrderConfirmViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject


class OrderSuccessActivity : BaseActivity(), EdgeToEdgeHelper.SelfManagedInsets, PaymentResultListener {


    private lateinit var viewModel: OrderConfirmViewModel
    private var currentOrderId: String? = null
    private val showSuccess = mutableStateOf(false)


    companion object {
        const val EXTRA_SUBJECT_CODES = "extra_subject_codes"
        const val EXTRA_ADDRESS_ID = "extra_address_id"

        fun startForPayment(
            context: Context,
            subjectCodes: ArrayList<String>,
            addressId: String?
        ) {
            val intent = Intent(context, OrderSuccessActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_SUBJECT_CODES, subjectCodes)
                putExtra(EXTRA_ADDRESS_ID, addressId)
            }
            context.startActivity(intent)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Checkout.preload(applicationContext)
        viewModel = ViewModelProvider(this)[OrderConfirmViewModel::class.java]

        val subjectCodes = intent.getStringArrayListExtra(EXTRA_SUBJECT_CODES) ?: arrayListOf()
        val addressId = intent.getStringExtra(EXTRA_ADDRESS_ID)

        showSuccess.value = false
        viewModel.initiateCheckout(addressId, subjectCodes)

        viewModel.checkoutState.observe(this) { state ->
            if (isFinishing || isDestroyed) return@observe
            if (showSuccess.value) return@observe

            if (state.data != null) {
                currentOrderId = state.data.orderId

                if (state.data.payable == 0 || !state.data.paymentRequired) {
                    showSuccess.value = true
                    viewModel.getOrderById(state.data.orderId)
                } else {
                    startRazorpay(state.data)
                }
            } else if (state.error != null) {
                Toast.makeText(this, getString(R.string.payment_failed), Toast.LENGTH_SHORT).show()
                finish()
            }
        }


        setContent {
            ArivihanComposeApp(
                enabled = true
            ) {
                val uiState by viewModel.uiState.observeAsState(OrderConfirmUiState(loading = true))
                val isSuccess by showSuccess

                BackHandler {
                    val homeIntent = Intent(this@OrderSuccessActivity, HomeScreen::class.java)
                    homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(homeIntent)
                    finish()
                }

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
//                    contentWindowInsets = ArivihanWindowInsets.Default,
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        if (isSuccess) {
                            OrderSuccessScreen(
                                uiState = uiState,
                                onDone = {
                                    val homeIntent = Intent(this@OrderSuccessActivity, HomeScreen::class.java)
                                    homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(homeIntent)
                                    finish()
                                }
                            )
                        } else {
                            PaymentLoadingScreen()
                        }
                    }
                }
            }
        }
    }

    // ── Razorpay ────────────────────────────────────────────────────────────────

    private fun startRazorpay(data: OrderConfirmRepository.CheckoutData) {
        try {
            val checkout = Checkout()
            checkout.setKeyID(data.providerKey ?: "")

            val userPhone = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey())
            val userEmail = MyApplication.getStringData(PreferenceKeys.USER_EMAIL.getKey())

            val options = JSONObject()
            options.put("name", "Arivihan")
            options.put("description", "Book Order")
            options.put("image", "https://reels-video.s3.ap-south-1.amazonaws.com/images/logo.png")
            options.put("order_id", data.gatewayOrderId)
            options.put("currency", data.currency ?: "INR")
            options.put("amount", (data.payable * 100).toString()) // amount in paise
            options.put("theme.color", "#1493B0")

            if (!userPhone.isNullOrEmpty()) options.put("prefill.contact", userPhone)
            if (!userEmail.isNullOrEmpty()) options.put("prefill.email", userEmail)

            val method = JSONObject()
            method.put("netbanking", 1)
            method.put("card", 1)
            method.put("upi", 1)
            method.put("wallet", 1)
            options.put("method", method)

            val retry = JSONObject()
            retry.put("enabled", true)
            retry.put("max_count", 4)
            options.put("retry", retry)

            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening payment gateway", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onPaymentSuccess(paymentId: String?) {
        showSuccess.value = true
        viewModel.confirmOrder(currentOrderId ?: "", paymentId ?: "", "")
    }

    override fun onPaymentError(code: Int, description: String?) {
        Toast.makeText(this, getString(R.string.payment_failed), Toast.LENGTH_SHORT).show()
        finish()
    }




}



@Composable
private fun PaymentLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            CircularProgressIndicator(
                color = colorResource(R.color.book_order_accent_color)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.payment_loading_msg),
                fontSize = 14.sp,
                color = colorResource(R.color.book_order_text_secondary)
            )

        }
    }
}



@Composable
private fun OrderSuccessScreen(
    uiState: OrderConfirmUiState,
    onDone: () -> Unit
) {
    var animationCompleted by remember { mutableStateOf(false) }

    val revealComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.refer_book_reveal_2))
    val tickComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.right_tick))

    val revealProgress by animateLottieCompositionAsState(
        composition = revealComposition,
        isPlaying = true,
        iterations = 1,
        speed = 1f
    )

    val tickProgress by animateLottieCompositionAsState(
        composition = tickComposition,
        isPlaying = true,
        iterations = 1,
        speed = 1f
    )

    LaunchedEffect(Unit) {
        snapshotFlow { revealProgress }
            .collect { progress ->
                if (progress >= 0.95f && !animationCompleted) {
                    animationCompleted = true
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
            Image(
                painter = painterResource(id = R.drawable.bg_order_sucess_screen),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            val verticalBias by animateFloatAsState(
                targetValue = if (animationCompleted) -1f else -0.2f,
                animationSpec = tween(durationMillis = 600),
                label = "verticalBias"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentAlignment = BiasAlignment(
                    horizontalBias = 0f,
                    verticalBias = verticalBias
                )
            ) {
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LottieAnimation(
                            composition = revealComposition,
                            progress = { revealProgress },
                            modifier = Modifier.fillMaxSize()
                        )

                        LottieAnimation(
                            composition = tickComposition,
                            progress = { tickProgress },
                            modifier = Modifier.size(90.dp)
                        )
                    }

                    Text(
                        text = stringResource(R.string.order_success_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .offset(y = (-0).dp)
                            .align(Alignment.BottomCenter),
                        color = colorResource(R.color.order_success_green_dark)
                    )
                }
            }


            AnimatedVisibility(
                visible = animationCompleted,
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth(),
                enter = slideInVertically(
                    animationSpec = tween(600),
                    initialOffsetY = { it }
                ) + fadeIn(animationSpec = tween(400))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 220.dp)
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    when {
                        uiState.loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = colorResource(R.color.book_order_accent_color)
                                )
                            }
                        }

                        uiState.error != null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.error,
                                    fontSize = 14.sp,
                                    color = colorResource(R.color.book_order_text_secondary),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        uiState.data != null -> {

                            //order cart
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = colorResource(R.color.white)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                    ) {
                                        Text(
                                            text = stringResource(R.string.order_success_booked),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorResource(R.color.dark_text_color)
                                        )
                                    }

                                    DashedDivider()

                                    Column(
                                        modifier = Modifier
                                            .verticalScroll(rememberScrollState())
                                            .padding(16.dp),
                                    ) {
                                        uiState.data.items.forEachIndexed { index, item ->
                                            BookCartItem(
                                                data = BookCartItemData(
                                                    name = item.name,
                                                    imageUrl = item.imageUrl,
                                                    finalPrice = 0,
                                                    actualPrice = item.unitPrice,
                                                    coinUsage = item.coinUsage,
                                                    isFree = item.free,
                                                    qty = item.qty
                                                ),
                                                mode = BookCartItemMode.OrderSuccess
                                            )

                                            if (index < uiState.data.items.lastIndex) {
                                                HorizontalDivider(
                                                    modifier = Modifier.padding(vertical = 12.dp),
                                                    color = colorResource(R.color.book_order_divider)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        //info section
                                        val fallback = stringResource(R.string.book_order_delivery_text)
                                        val deliveryTextJson = MyApplication.getFirebaseRemoteConfig().getString("book_order_delivery_text")
                                        val langKey = if (MyApplication.getLocale().language == "hi") "hi" else "en"
                                        val deliveryText = if (deliveryTextJson.isNotEmpty()) JSONObject(deliveryTextJson).optString(langKey, fallback) else fallback

                                        ArivihanInfoBanner(
                                            text = deliveryText,
                                            iconRes = R.drawable.ic_delivery_truck,
                                            color = colorResource(R.color.book_order_delivery_orange)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            //whatsapp cart
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = colorResource(R.color.white)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_iwhatsapp),
                                        contentDescription = null,
                                        modifier = Modifier.size(28.dp)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = stringResource(R.string.order_success_whatsapp_msg),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colorResource(R.color.dark_text_color)
                                    )
                                }
                            }

                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }


        // Bottom bar
        AnimatedVisibility(
            visible = animationCompleted,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .align(Alignment.BottomCenter),
            enter = slideInVertically(
                animationSpec = tween(600),
                initialOffsetY = { it }
            ) + fadeIn(animationSpec = tween(400))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedButton(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, colorResource(R.color.arivihan_secondary_color)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                    )
                ) {
                    Text(
                        text = stringResource(R.string.go_to_home),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(R.color.arivihan_secondary_color)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OrderSuccessScreenPreview() {
    val sampleUiState = OrderConfirmUiState(
        loading = false,
        data = OrderView(
            id = "ORD001",
            status = "CONFIRMED",
            paymentStatus = "PAID",
            totalAmount = 400,
            bookCount = 2,
            placedAt = "2026-07-15",
            items = listOf(
                OrderItemView(
                    subjectCode = "BIO",
                    name = "जीव विज्ञान",
                    qty = 1,
                    unitPrice = 0,
                    coinUsage = 400,
                    imageUrl = "https://assets.arivihan.com/refer-win/books/bio.png"
                ),
                OrderItemView(
                    subjectCode = "MAT",
                    name = "गणित",
                    qty = 1,
                    unitPrice = 400,
                    coinUsage = 0,
                    imageUrl = "https://assets.arivihan.com/refer-win/books/mat.png"
                )
            )
        )
    )

    OrderSuccessScreen(
        uiState = sampleUiState,
        onDone = {}
    )
}