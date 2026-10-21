package arivihan.technologies.doubtbuzzter2.activity.refer

import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanInfoBanner
import arivihan.technologies.doubtbuzzter2.compose.component.InfoBannerSize
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
import arivihan.technologies.doubtbuzzter2.compose.ArivihanComposeApp
import arivihan.technologies.doubtbuzzter2.compose.ArivihanWindowInsets
import arivihan.technologies.doubtbuzzter2.compose.ArivihanScreenState
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanButton
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanButtonStyle
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanScreenStateContainer
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanTopAppBar
import arivihan.technologies.doubtbuzzter2.compose.component.DashedDivider
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.BookCartItem
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.BookCartItemData
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.BookCartItemMode
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.OrderHistoryShimmer
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents
import arivihan.technologies.doubtbuzzter2.model.order_history.OrderHistoryEntry
import arivihan.technologies.doubtbuzzter2.viewModel.OrderHistoryUiState
import arivihan.technologies.doubtbuzzter2.viewModel.OrderHistoryViewModel
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale


class OrderHistoryActivity : BaseActivity(), EdgeToEdgeHelper.SelfManagedInsets {



    private lateinit var viewModel: OrderHistoryViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[OrderHistoryViewModel::class.java]
        viewModel.loadOrders()
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_order_history_screen_view.actionName)
        val showReferEarnBtn = intent.getBooleanExtra(EXTRA_SHOW_REFER_EARN_BTN, false)

        setContent {
            ArivihanComposeApp(
                enabled = true
            ) {
                val uiState by viewModel.uiState.observeAsState(OrderHistoryUiState(loading = true))

                OrderHistoryScreen(
                    uiState = uiState,
                    showReferEarnBtn = showReferEarnBtn,
                    onBack = {
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_book_order_history_back_btn_click.actionName)
                        finish() },
                    onRetry = { viewModel.loadOrders() },
                    onReferEarnClick = {
                        startActivity(Intent(this@OrderHistoryActivity, ReferAndEarnActivity::class.java))
                    }
                )
            }
        }
    }

    companion object {
        private const val EXTRA_SHOW_REFER_EARN_BTN = "extra_show_refer_earn_btn"

        fun start(context: Context, showReferEarnBtn: Boolean = false) {
            context.startActivity(Intent(context, OrderHistoryActivity::class.java).apply {
                putExtra(EXTRA_SHOW_REFER_EARN_BTN, showReferEarnBtn)
            })
        }
    }
}







@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderHistoryScreen(
    uiState: OrderHistoryUiState,
    showReferEarnBtn: Boolean,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onReferEarnClick: () -> Unit
) {


    val screenState: ArivihanScreenState<List<OrderHistoryEntry>> = when {
        uiState.loading -> ArivihanScreenState.Loading
        uiState.error == OrderHistoryViewModel.NETWORK_ERROR -> ArivihanScreenState.NetworkError
        uiState.error != null -> ArivihanScreenState.Error(uiState.error)
        else -> ArivihanScreenState.Content(uiState.orders)
    }


    Scaffold(
        containerColor = colorResource(R.color.grey_100),
        contentWindowInsets = ArivihanWindowInsets.Default,
        topBar = {
            ArivihanTopAppBar(
                title = stringResource(R.string.order_history_title),
                onBackClick = onBack
            )
        }
    ) { paddingValues ->

        ArivihanScreenStateContainer(
            state = screenState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            onRetry = onRetry,
            loadingContent = {
                OrderHistoryShimmer() //shimmer
            }
        ) { orders ->

            //empty
            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_arivihan_girl_action_hand),
                            contentDescription = null,
                            modifier = Modifier.size(220.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        //text remote config
                        val emptyMsgTxt = stringResource(R.string.order_history_empty_msg)
                        val referBtnTxt = stringResource(R.string.refer_karo)
                        val json = MyApplication.getFirebaseRemoteConfig().getString("order_history_empty_texts")
                        val lang = if (MyApplication.getLocale().language == "hi") "hi" else "en"
                        val texts = if (json.isNotEmpty()) JSONObject(json).optJSONObject(lang) else null

                        Text(
                            text = texts?.optString("msg", emptyMsgTxt) ?: emptyMsgTxt,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.dark_text_color),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )

                        if (showReferEarnBtn) {
                            Spacer(modifier = Modifier.height(24.dp))

                            ArivihanButton(
                                text = texts?.optString("btn", referBtnTxt) ?: referBtnTxt,
                                onClick = onReferEarnClick,
                                style = ArivihanButtonStyle.Primary,
                                modifier = Modifier
                                    .width(170.dp)
                                    .wrapContentWidth()
                            )
                        }
                    }
                }
            }
            //list
            else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        OrderCard(order = order)
                    }
                }
            }
        }
    }
}






@Composable
private fun OrderCard(order: OrderHistoryEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.white)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            //header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colorResource(R.color.arivihan_secondary_color)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_package),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = stringResource(R.string.order_history_books_purchased),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.dark_text_color)
                        ),
                    )

                    if (!order.trackingId.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(R.string.order_id_label, order.trackingId),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorResource(R.color.book_order_text_secondary)
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "₹${order.totalAmount}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF121212)
                            ),
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .size(4.dp)
                                .background(Color(0x66666699), shape = CircleShape)
                        )
                        Text(
                            text = stringResource(R.string.book_order_placed_on, formatDate(order.placedAt)),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorResource(R.color.book_order_text_secondary)
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Badge
                OrderStatusBadge(status = order.status)
            }

            DashedDivider()

            //item list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                order.items.forEachIndexed { index, item ->

                    BookCartItem(
                        data = BookCartItemData(
                            name = item.name ?: "",
                            imageUrl = item.imageUrl ?: "",
                            finalPrice = if (item.free) 0 else item.lineTotal,
                            actualPrice = item.unitPrice,
                            coinUsage = item.coinUsage,
                            qty = item.qty,
                            isFree = item.free
                        ),
                        mode = BookCartItemMode.OrderHistory
                    )

                    if (index < order.items.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = colorResource(R.color.book_order_placeholder_bg)
                        )
                    }
                }
            }

            if (order.status.uppercase() !in listOf("DELIVERED", "CANCELLED", "FAILED")) {

                val fallback = stringResource(R.string.book_order_delivery_text)
                val json = MyApplication.getFirebaseRemoteConfig().getString("book_order_delivery_text")
                val langKey = if (MyApplication.getLocale().language == "hi") "hi" else "en"
                val deliveryText = if (json.isNotEmpty()) JSONObject(json).optString(langKey, fallback) else fallback

                ArivihanInfoBanner(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                    text = deliveryText,
                    iconRes = R.drawable.ic_delivery_truck,
                    color = colorResource(R.color.book_order_delivery_orange),
                    size = InfoBannerSize.Medium
                )
            }
        }
    }
}


@Composable
private fun OrderStatusBadge(status: String) {
    val (textColor, bgColor, displayText) = getOrderStatusStyle(status)

    Box(
        modifier = Modifier
            .background(
                color = bgColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = displayText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}


@Composable
private fun getOrderStatusStyle(status: String): Triple<Color, Color, String> {
    return when (status.uppercase()) {
        "DELIVERED" -> Triple(Color(0xFF4A9541), Color(0xFFDBEAD9), stringResource(R.string.order_status_delivered))
        "PACKED" -> Triple(Color(0xFFE89B05), Color(0xFFFAEBCD), stringResource(R.string.order_status_packed))
        "SHIPPED" -> Triple(Color(0xFF0E7D94), Color(0xFFE9F9FB), stringResource(R.string.order_status_shipped))
        "PLACED" -> Triple(Color(0xFF60BB05), Color(0xFFEFF8E6), stringResource(R.string.order_status_placed))
        "CANCELLED", "FAILED" -> Triple(Color(0xFFE10000), Color(0xFFFCE5E5), stringResource(R.string.order_status_failed))
        else -> Triple(Color(0xFF60BB05), Color(0xFFEFF8E6), status )
    }
}


private fun formatDate(isoString: String): String {
    if (isoString.isBlank()) return ""
    return try {
        val datePart = isoString.substringBefore('T')
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val date = input.parse(datePart) ?: return isoString

        val locale = Locale.getDefault()
        val pattern = if (locale.language == "hi") { "dd MMMM yyyy" } else { "dd MMM yyyy" }

        val output = SimpleDateFormat(pattern, locale)
        output.format(date)
    } catch (e: Exception) {
        isoString
    }
}
