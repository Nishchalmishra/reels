package arivihan.technologies.doubtbuzzter2.activity.refer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity
import arivihan.technologies.doubtbuzzter2.compose.ArivihanComposeApp
import arivihan.technologies.doubtbuzzter2.compose.ArivihanScreenState
import arivihan.technologies.doubtbuzzter2.compose.ArivihanWindowInsets
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanButton
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanButtonStyle
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanInfoBanner
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanScreenStateContainer
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanTopAppBar
import arivihan.technologies.doubtbuzzter2.compose.component.AsyncImage
import arivihan.technologies.doubtbuzzter2.compose.component.DashedDivider
import arivihan.technologies.doubtbuzzter2.compose.component.InfoBannerSize
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.BookCartItemData
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.BookCartItemWithQuantity
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.BookOrderShimmer
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.RecommendationCard
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.RecommendationCardData
import arivihan.technologies.doubtbuzzter2.compose.component.rememberNetworkGuard
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys
import arivihan.technologies.doubtbuzzter2.model.book_order.CartData
import arivihan.technologies.doubtbuzzter2.model.book_order.CartItemRequest
import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper
import arivihan.technologies.doubtbuzzter2.viewModel.BookOrderActionState
import arivihan.technologies.doubtbuzzter2.viewModel.BookOrderUiState
import arivihan.technologies.doubtbuzzter2.viewModel.BookOrderViewModel
import arivihan.technologies.doubtbuzzter2.viewModel.CatalogSheetState
import kotlinx.coroutines.launch
import org.json.JSONObject


class BookOrderActivity : BaseActivity(), EdgeToEdgeHelper.SelfManagedInsets {


    private lateinit var viewModel: BookOrderViewModel


    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.refresh()
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[BookOrderViewModel::class.java]
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_book_order_screen_view.actionName)
        setContent {


            ArivihanComposeApp(enabled = true) {
                val cartState by viewModel.cartState.observeAsState(BookOrderUiState(loading = true))
                val catalogSheetState by viewModel.catalogSheetState.observeAsState(CatalogSheetState())
                val actionState by viewModel.actionState.observeAsState(BookOrderActionState())
                val networkGuard = rememberNetworkGuard()

                LaunchedEffect(cartState) {
                    val shareText = cartState.data?.emptyShareText
                    if (!shareText.isNullOrEmpty()) {
                        MyApplication.saveData(PreferenceKeys.REFER_AND_EARN_SHARE_TEXT.key, shareText)
                    }
                }

                if (actionState.error != null) {
                    Toast.makeText(this, actionState.error, Toast.LENGTH_SHORT).show()
                    viewModel.clearActionError()
                }

                //screen
                BookOrderScreen(
                    cartState = cartState,
                    catalogSheetState = catalogSheetState,
                    actionState = actionState,
                    onBack = {
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_book_order_screen_back_btn_click.actionName)
                        finish()
                             },
                    onOrderHistory = { OrderHistoryActivity.start(this) },
                    onOpenCatalog = { networkGuard { viewModel.openCatalogSheet() } },
                    onDismissCatalogSheet = { viewModel.dismissCatalogSheet() },
                    onAddItem = { subjectCode ->
                        networkGuard {
                            viewModel.addItem(CartItemRequest(subjectCode, 1, free = false))
                            val analyticsData = HashMap<String, String>()
                            analyticsData["subject_code"] = subjectCode
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_order_book_add_btn_click.actionName, analyticsData)
                        }
                    },
                    onAddItems = { items ->
                        networkGuard {
                            viewModel.updateCart(items.map { (code, qty) ->
                                CartItemRequest(code, qty, free = false)
                            })
                            viewModel.dismissCatalogSheet()
                            val analyticsData = HashMap<String, String>()
                            analyticsData["subject_Code"] = items.keys.joinToString(",")
                            analyticsData["book_quatity"] = items.values.joinToString(",")
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_order_book_deal_add_btn_click.actionName, analyticsData)
                        }
                    },
                    onDeleteItem = { subjectCode ->
                        networkGuard {
                            val currentItem = cartState.data?.items?.firstOrNull { it.subjectCode == subjectCode && !it.free }

                            if (currentItem != null && currentItem.qty > 1) {
                                viewModel.updateCart(listOf(
                                    CartItemRequest(subjectCode, currentItem.qty - 1, currentItem.free)
                                ))
                            } else if (currentItem != null) {
                                viewModel.deleteItem(CartItemRequest(subjectCode, 1, free = false))
                            }
                            val analyticsData = HashMap<String, String>()
                            analyticsData["subject_code"] = subjectCode
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_order_book_delete_btn_click.actionName, analyticsData )
                        }
                    },
                    onShareReferral = { shareText ->
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_book_order_share_btn_click.actionName)
                        val finalShareText = shareText.ifEmpty { MyApplication.getStringData(PreferenceKeys.REFER_AND_EARN_SHARE_TEXT.key) }

                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, finalShareText)
                        }
                        startActivity(Intent.createChooser(shareIntent, null))
                    },
                    onProceed = {
                        networkGuard {
                            val subjectCodes = cartState.data?.items?.map { it.subjectCode } ?: emptyList()
                            UserAddressActivity.startForCheckout(
                                context = this,
                                subjectCodes = ArrayList(subjectCodes)
                            )
                            val analyticsData = HashMap<String, String>()
                            analyticsData["subject_codes"] = subjectCodes.joinToString(",")
                            analyticsData["books_quantity"] = cartState.data?.items?.map { it.qty }?.joinToString(",") ?: ""
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_book_order_proceed_btn_click.actionName,analyticsData)
                        }
                    },
                    onRefresh = { networkGuard { viewModel.refresh() } }
                )
            }
        }
    }
}







@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookOrderScreen(
    cartState: BookOrderUiState,
    catalogSheetState: CatalogSheetState,
    actionState: BookOrderActionState,
    onBack: () -> Unit,
    onOrderHistory: () -> Unit,
    onOpenCatalog: () -> Unit,
    onDismissCatalogSheet: () -> Unit,
    onAddItem: (String) -> Unit,
    onAddItems: (Map<String, Int>) -> Unit,
    onDeleteItem: (String) -> Unit,
    onShareReferral: (String) -> Unit,
    onProceed: () -> Unit,
    onRefresh: () -> Unit
) {

    val screenState: ArivihanScreenState<CartData> = when {
        cartState.loading -> ArivihanScreenState.Loading
        cartState.error == BookOrderViewModel.NETWORK_ERROR -> ArivihanScreenState.NetworkError
        cartState.error != null && cartState.data == null -> ArivihanScreenState.Error(cartState.error)
        cartState.data != null -> ArivihanScreenState.Content(cartState.data)
        else -> ArivihanScreenState.Loading
    }



    Scaffold(
        containerColor = colorResource(R.color.grey_100),
        contentWindowInsets = ArivihanWindowInsets.Default,
        topBar = {
            ArivihanTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.book_order_screen_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(R.color.dark_text_color)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onBack()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_new),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = {
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_book_order_history_click.actionName)
                            onOrderHistory()
                        },
                        modifier = Modifier
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colorResource(R.color.input_name_stroke_default)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colorResource(R.color.grey_100)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_refer_history_icon),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = colorResource(R.color.arivihan_secondary_color)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = stringResource(R.string.order_history_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = colorResource(R.color.arivihan_secondary_color),
                            )
                        )
                    }
                }
            )
        },
        bottomBar = {
            val data = cartState.data

            if (data != null && data.items.isNotEmpty()) {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    BookOrderBottomBar(
                        data = data,
                        isLoading = actionState.loading,
                        onProceed = onProceed
                    )
                }
            }
        }
    ) { paddingValues ->


        ArivihanScreenStateContainer(
            state = screenState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            onRetry = onRefresh,
            loadingContent = { BookOrderShimmer() }
        ) { data ->

            //empty
            if (data.items.isEmpty()) {
                EmptyCartView(
                    imageUrl = data.emptyImageUrl,
                    title = data.emptyTitle.ifEmpty { stringResource(R.string.book_order_empty_title) },
                    buttonText = data.emptyButtonText.ifEmpty { stringResource(R.string.book_order_refer_now_cta) },
                    onShareClick = { onShareReferral(data.emptyShareText) }
                )
            }

            //content
            else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SelectedBooksCard(
                        data = data,
                        actionLoading = actionState.loading,
                        onAddItem = onAddItem,
                        onDeleteItem = onDeleteItem,
                        onOpenCatalog = onOpenCatalog
                    )

                    PaymentSummaryCard(data = data)

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }


    //bottom sheet
    if (catalogSheetState.isVisible) {
        CatalogBottomSheet(
            sheetState = catalogSheetState,
            onConfirmSelection = onAddItems,
            onDismiss = onDismissCatalogSheet
        )
    }


}







@Composable
private fun SelectedBooksCard(
    data: CartData,
    actionLoading: Boolean,
    onAddItem: (String) -> Unit,
    onDeleteItem: (String) -> Unit,
    onOpenCatalog: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.white)
        ),
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
                    text = "${stringResource(R.string.book_order_selected_books_title)} (${
                        data.itemCount.toString().padStart(2, '0')
                    })",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.dark_text_color)
                    )
                )
            }

            DashedDivider()

            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                data.items.forEachIndexed { index, item ->

                    BookCartItemWithQuantity(
                        data = BookCartItemData(
                            name = item.name,
                            imageUrl = item.imageUrl,
                            finalPrice = item.lineTotal,
                            actualPrice = item.mrp * item.qty, //temp for now
                            discountPercent = item.discountPercent,
                            isFree = item.free,
                            showPrice = true
                        ),
                        quantity = item.qty,
                        onIncrement = { onAddItem(item.subjectCode) },
                        onDecrement = { onDeleteItem(item.subjectCode) }
                    )


                    if (index < data.items.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = colorResource(R.color.book_order_divider)
                        )
                    }
                }

                if (data.recommendations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    CouponCard(
                        backgroundColor = colorResource(R.color.book_order_card_bg),
                        borderColor = colorResource(R.color.book_order_card_border),
                        notchOffsetY = 72f
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                        ) {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .padding(horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.book_order_bundle_title),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorResource(R.color.book_order_title_teal),
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = stringResource(R.string.book_order_bundle_subtitle),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            DashedDivider(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp),
                                color = colorResource(R.color.book_order_card_border)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (data.recommendations.size <= 3) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    data.recommendations.forEach { item ->
                                        RecommendationCard(
                                            modifier = Modifier.weight(1f),
                                            data = RecommendationCardData(
                                                name = item.title,
                                                imageUrl = item.imageUrl,
                                                price = item.price,
                                                isSemiBestDeal = false,
                                                isBestDeal = item.bestDeal
                                            ),
                                            onAdd = { onOpenCatalog() }
                                        )
                                    }
                                }
                            } else {
                                val lazyRowState = rememberLazyListState(
                                    initialFirstVisibleItemIndex = maxOf(0, data.recommendations.lastIndex)
                                )
                                val coroutineScope = rememberCoroutineScope()
                                val showLeftArrow by remember { derivedStateOf { lazyRowState.firstVisibleItemIndex > 0 } }
                                val showRightArrow by remember { derivedStateOf { lazyRowState.firstVisibleItemIndex < data.recommendations.size - 3 } }

                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    LazyRow(
                                        state = lazyRowState,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(
                                            start = 10.dp,
                                            end = 14.dp
                                        )
                                    ) {
                                        itemsIndexed(data.recommendations) { index, item ->
                                            val isSemiBestDeal = data.recommendations.size == 4 &&
                                                index == 2 &&
                                                data.recommendations.last().bestDeal

                                            RecommendationCard(
                                                modifier = Modifier
                                                    .width(110.dp),
                                                data = RecommendationCardData(
                                                    name = item.title,
                                                    imageUrl = item.imageUrl,
                                                    price = item.price,
                                                    isSemiBestDeal = isSemiBestDeal,
                                                    isBestDeal = item.bestDeal
                                                ),
                                                onAdd = { onOpenCatalog() }
                                            )
                                        }
                                    }

                                    if (showLeftArrow) {
                                        Surface(
                                            onClick = {
                                                coroutineScope.launch {
                                                    lazyRowState.animateScrollToItem(
                                                        (lazyRowState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                                                    )
                                                }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.CenterStart)
                                                .padding(start = 5.dp)
                                                .size(32.dp),
                                            shape = CircleShape,
                                            color = Color.White,
                                            shadowElevation = 2.dp
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_back_new),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                    tint = colorResource(R.color.arivihan_secondary_color)
                                                )
                                            }
                                        }
                                    }

                                    if (showRightArrow) {
                                        Surface(
                                            onClick = {
                                                coroutineScope.launch {
                                                    lazyRowState.animateScrollToItem(
                                                        (lazyRowState.firstVisibleItemIndex + 1).coerceAtMost(data.recommendations.size - 1)
                                                    )
                                                }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.CenterEnd)
                                                .padding(end = 5.dp)
                                                .size(32.dp),
                                            shape = CircleShape,
                                            color = Color.White,
                                            shadowElevation = 2.dp
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_baseline_arrow_forward_ios_24),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                    tint = colorResource(R.color.arivihan_secondary_color)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))


                val fallback = stringResource(R.string.book_order_delivery_text)
                val deliveryTextJson = MyApplication.getFirebaseRemoteConfig().getString("book_order_delivery_text")
                val langKey = if (MyApplication.getLocale().language == "hi") "hi" else "en"
                val deliveryText = if (deliveryTextJson.isNotEmpty()) JSONObject(deliveryTextJson).optString(langKey, fallback) else fallback

                ArivihanInfoBanner(
                    modifier = Modifier.padding(8.dp),
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
private fun CouponCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    borderColor: Color,
    notchRadius: Float = 10f,
    notchOffsetY: Float = 72f,
    cornerRadius: Float = 10f,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val notchRadiusPx = with(density) { notchRadius.dp.toPx() }
    val cornerRadiusPx = with(density) { cornerRadius.dp.toPx() }
    val notchYPx = with(density) { notchOffsetY.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val roundRect = Path().apply {
                    addRoundRect(
                        RoundRect(
                            left = 0f,
                            top = 0f,
                            right = size.width,
                            bottom = size.height,
                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                        )
                    )
                }

                val notches = Path().apply {
                    addOval(
                        Rect(
                            center = Offset(0f, notchYPx),
                            radius = notchRadiusPx
                        )
                    )
                    addOval(
                        Rect(
                            center = Offset(size.width, notchYPx),
                            radius = notchRadiusPx
                        )
                    )
                }

                val couponPath = Path()
                couponPath.op(roundRect, notches, androidx.compose.ui.graphics.PathOperation.Difference)

                drawPath(
                    path = couponPath,
                    color = backgroundColor
                )

                drawPath(
                    path = couponPath,
                    color = borderColor,
                    style = Stroke(width = with(density) { 1.dp.toPx() })
                )
            }
    ) {
        content()
    }
}









@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogBottomSheet(
    sheetState: CatalogSheetState,
    onConfirmSelection: (Map<String, Int>) -> Unit,
    onDismiss: () -> Unit
) {
    val networkGuard = rememberNetworkGuard()
    val modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val quantities = remember { mutableStateMapOf<String, Int>() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalSheetState,
        containerColor = colorResource(R.color.white),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.book_order_bottom_sheet_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.dark_text_color)
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = colorResource(R.color.book_order_text_secondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            DashedDivider()

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                when {
                    sheetState.loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colorResource(R.color.arivihan_secondary_color))
                        }
                    }

                    sheetState.error != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.book_order_sheet_error),
                                fontSize = 14.sp,
                                color = colorResource(R.color.book_order_text_secondary)
                            )
                        }
                    }

                    sheetState.data != null && sheetState.data.books.isNotEmpty() -> {
                        val books = sheetState.data.books
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            items(books.size) { index ->
                                val book = books[index]

                                BookCartItemWithQuantity(
                                    data = BookCartItemData(
                                        name = book.name,
                                        imageUrl = book.imageUrl,
                                        finalPrice = book.mrp,
                                        actualPrice = book.mrp,
                                        isFree = book.mrp == 0,
                                        showPrice = false
                                    ),
                                    quantity = quantities[book.subjectCode] ?: 0,
                                    onIncrement = {
                                        quantities[book.subjectCode] = (quantities[book.subjectCode] ?: 0) + 1
                                    },
                                    onDecrement = {
                                        val current = quantities[book.subjectCode] ?: 0
                                        if (current > 0) {
                                            quantities[book.subjectCode] = current - 1
                                        }
                                    },
                                    subjectFontWeight = FontWeight.SemiBold
                                )

                                if (index < books.lastIndex) {
                                    Spacer(Modifier.height(8.dp))
                                    HorizontalDivider(color = colorResource(R.color.book_order_divider))
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    networkGuard {
                        val selected = quantities.filter { it.value > 0 }
                        onConfirmSelection(selected)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.book_order_accent_color))
            ) {
                Text(
                    text = stringResource(R.string.book_order_continue_cta),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    painter = painterResource(id = R.drawable.arrow_forward_white_24),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
        }
    }
}







@Composable
private fun PaymentSummaryCard(data: CartData) {
    val summary = data.summary ?: return

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = stringResource(R.string.payment_summary_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.dark_text_color)
                )
            }

            DashedDivider()


            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                PaymentRow(
                    iconRes = R.drawable.ic_cart_total,
                    label = stringResource(R.string.ps_total_price_label),
                    value = "₹${summary.totalPrice}",
                    valueColor = colorResource(R.color.dark_text_color)
                )

                if (summary.offerDiscount > 0) {
                    PaymentRow(
                        iconRes = R.drawable.ic_cart_offer_discount,
                        label = stringResource(R.string.payment_offer_discount_label),
                        value = "-₹${summary.offerDiscount}",
                        valueColor = colorResource(R.color.book_order_green_badge)
                    )
                }

                if (summary.coinDiscount > 0) {
                    PaymentRow(
                        iconRes = R.drawable.refer_coin,
                        label = stringResource(R.string.ps_coin_discount_label),
                        value = "-₹${summary.coinDiscount}",
                        valueColor = colorResource(R.color.book_order_green_badge),
                        isImage = true
                    )
                }

                PaymentRow(
                    iconRes = R.drawable.ic_cart_delivery_truck,
                    label = stringResource(R.string.payment_delivery_charges_label),
                    value = if (summary.deliveryFree) stringResource(R.string.payment_free_label) else "₹${summary.deliveryCharges}",
                    valueColor = if (summary.deliveryFree) colorResource(R.color.book_order_green_badge) else colorResource(R.color.dark_text_color)
                )
            }

            val density = LocalDensity.current

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            ) {
                val r = with(density) { 8.dp.toPx() }
                var x = r

                while (x < size.width + r) {
                    drawCircle(
                        color = Color(0xFFDCF7F9),
                        radius = r,
                        center = Offset(x, 0f)
                    )
                    x += r * 2
                }

                drawRect(
                    color = Color(0xFFDCF7F9),
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xFFDCF7F9))
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.total),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(R.color.dark_text_color)
                )

                Text(
                    text = "₹${summary.finalTotal}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.dark_text_color)
                )
            }
        }
    }
}






@Composable
private fun PaymentRow(
    iconRes: Int,
    label: String,
    value: String,
    valueColor: Color,
    isImage: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isImage) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = colorResource(R.color.book_order_text_secondary),
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorResource(R.color.book_order_text_secondary)
                )
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        )
    }
}





@Composable
private fun BookOrderBottomBar(
    data: CartData,
    isLoading: Boolean,
    onProceed: () -> Unit
) {
    val finalTotal = data.summary?.finalTotal ?: data.payable
    val totalPrice = data.summary?.totalPrice ?: data.mrpTotal
    val discountPercent = if (totalPrice > 0 && totalPrice > finalTotal) {
        ((totalPrice - finalTotal) * 100) / totalPrice
    } else 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.white))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(0.6f)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "₹${finalTotal}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.dark_text_color)
                )

                if (totalPrice > finalTotal) {
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "₹${totalPrice}",
                        fontSize = 14.sp,
                        color = colorResource(R.color.book_order_text_secondary),
                        textDecoration = TextDecoration.LineThrough
                    )

                    if (discountPercent > 0) {
                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE6F6EE))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${discountPercent}% off",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F8F5F)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        ArivihanButton(
            text = stringResource(R.string.payment_proceed_cta),
            onClick = onProceed,
            loading = isLoading,
            modifier = Modifier
                .weight(0.4f),
            style = ArivihanButtonStyle.Primary
        )
    }
}


@Composable
private fun EmptyCartView(
    imageUrl: String = "",
    title: String,
    buttonText: String,
    onShareClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    url = imageUrl,
                    modifier = Modifier.size(220.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_empty_orders_cart),
                    contentDescription = null,
                    modifier = Modifier.size(220.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.dark_text_color),
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            ArivihanButton(
                text = buttonText,
                onClick = onShareClick,
                style = ArivihanButtonStyle.Primary,
                modifier = Modifier
                    .width(170.dp)
                    .wrapContentWidth(),
                leftIcon = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                }
            )
        }
    }
}