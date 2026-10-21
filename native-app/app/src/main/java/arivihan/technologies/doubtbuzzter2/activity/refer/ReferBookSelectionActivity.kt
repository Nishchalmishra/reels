package arivihan.technologies.doubtbuzzter2.activity.refer

import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.lifecycle.ViewModelProvider
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity
import arivihan.technologies.doubtbuzzter2.compose.ArivihanComposeApp
import arivihan.technologies.doubtbuzzter2.compose.ArivihanWindowInsets
import arivihan.technologies.doubtbuzzter2.compose.component.rememberNetworkGuard
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanAutoResizeText
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanButton
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanButtonStyle
import arivihan.technologies.doubtbuzzter2.compose.component.AsyncImage
import androidx.compose.ui.zIndex
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys
import arivihan.technologies.doubtbuzzter2.model.book_order.CartItemRequest
import arivihan.technologies.doubtbuzzter2.model.refer.BookItem
import arivihan.technologies.doubtbuzzter2.model.refer.GiftRevealData
import arivihan.technologies.doubtbuzzter2.viewModel.BookOrderViewModel
import arivihan.technologies.doubtbuzzter2.viewModel.ReferAndEarnViewModel



class ReferBookSelectionActivity : BaseActivity(), EdgeToEdgeHelper.SelfManagedInsets {

    companion object {
        const val EXTRA_ANIMATION_TITLE = "show_animation_title"
        const val EXTRA_ANIMATION_SUB_TITLE = "show_animation_sub_title"
        const val EXTRA_SHOW_ANIMATION = "show_animation"
        const val EXTRA_MILESTONE_COINS = "milestone_coins"
        const val EXTRA_COINS_PER_REFERRAL = "coins_per_referral"
    }



    private lateinit var viewModel: ReferAndEarnViewModel
    private lateinit var bookOrderViewModel: BookOrderViewModel



    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.loadGiftReveal()
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[ReferAndEarnViewModel::class.java]
        bookOrderViewModel = ViewModelProvider(this)[BookOrderViewModel::class.java]
        viewModel.loadGiftReveal()

        val animationTitle = intent.getStringExtra(EXTRA_ANIMATION_TITLE) ?: ""
        val animationSubTitle = intent.getStringExtra(EXTRA_ANIMATION_SUB_TITLE) ?: ""
        val showAnimation = intent.getBooleanExtra(EXTRA_SHOW_ANIMATION, false)
        val milestoneCoins = intent.getIntExtra(EXTRA_MILESTONE_COINS, 0)
        val coinsPerReferral = intent.getIntExtra(EXTRA_COINS_PER_REFERRAL, 50)

        setContent {
            ArivihanComposeApp(enabled = true) {
                val giftRevealState by viewModel.giftRevealState.observeAsState()
                var animationCompleted by remember { mutableStateOf(!showAnimation) }

                if (!animationCompleted) {
                    GiftRevealScreen(
                        title = animationTitle,
                        subTitle = animationSubTitle,
                        onCompleteAt = 0.5f,
                        onRevealComplete = {
                            animationCompleted = true
                            if (milestoneCoins > 0) {
                                val lastShownMilestone = MyApplication.getStringData(PreferenceKeys.MILESTONE_ANIMATION_LAST_SHOWN.key)?.toIntOrNull() ?: 0

                                if (milestoneCoins > lastShownMilestone) {
                                    MyApplication.saveData(PreferenceKeys.MILESTONE_ANIMATION_LAST_SHOWN.key, milestoneCoins.toString())
                                    MyApplication.saveData(PreferenceKeys.MILESTONE_ANIMATION_SHOWN_PREFIX.key + milestoneCoins, "true")
                                }
                            }
                        }
                    )
                } else {
                    val giftData = giftRevealState?.data
                    val networkGuard = rememberNetworkGuard()
                    val cartState by bookOrderViewModel.cartState.observeAsState()

                    BookSelectionScreen(
                        giftRevealData = giftData,
                        showAnimation = showAnimation,
                        coinsUsed = cartState?.data?.summary?.coinsUsed ?: 0,
                        coinsPerReferral = coinsPerReferral,
                        onBack = {
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_book_selection_screen_back_btn_click.actionName)
                            finish() },
                        onBookAdded = { subjectCode ->
                            networkGuard {
                                bookOrderViewModel.addItem(
                                    CartItemRequest(subjectCode, 1, free = true)
                                )
                            }
                        },
                        onBookRemoved = { subjectCode ->
                            networkGuard {
                                bookOrderViewModel.deleteItem(
                                    CartItemRequest(subjectCode, 1, free = true)
                                )
                            }
                        },
                        onBookSwapped = { removedSubjectCode, addedSubjectCode ->
                            networkGuard {
                                //single update call so the remove + add can't race each other
                                bookOrderViewModel.updateCart(
                                    listOf(
                                        CartItemRequest(removedSubjectCode, 0, free = true),
                                        CartItemRequest(addedSubjectCode, 1, free = true)
                                    )
                                )
                            }
                        },
                        onBuyNow = {
                            networkGuard {
                                startActivity(Intent(this@ReferBookSelectionActivity, BookOrderActivity::class.java))
                            }
                        },
                        onBuyLater = {
                            finish()
                        }
                    )
                }
            }
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSelectionScreen(
    giftRevealData: GiftRevealData?,
    showAnimation: Boolean = false,
    coinsUsed: Int = 0,
    coinsPerReferral: Int,
    onBack: () -> Unit,
    onBookAdded: (String) -> Unit,
    onBookRemoved: (String) -> Unit,
    onBookSwapped: (String, String) -> Unit,
    onBuyNow: () -> Unit,
    onBuyLater: () -> Unit
) {
    val scrollState = rememberScrollState()
    val maxSelectable = giftRevealData?.maxSelectable ?: 1

    val selectableBookIds = remember(giftRevealData) {
        (giftRevealData?.selectableBooks ?: emptyList()).map { it.id }.toSet()
    }

    val books = remember(giftRevealData, selectableBookIds) {
        (giftRevealData?.books ?: emptyList()).map { book ->
            book.copy(selectable = selectableBookIds.contains(book.id))
        }
    }

    var selectedBookIds by remember(books) {
        mutableStateOf(books.filter { it.inCart && it.selectable }.map { it.id })
    }


    Scaffold(
        containerColor = Color(0xFFF5F9FA),
        contentWindowInsets = ArivihanWindowInsets.Default,
        bottomBar = {
            if (selectedBookIds.isNotEmpty() && coinsUsed != 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    val referralCount = ReferAndEarnActivity.calculateReferralCount(coinsUsed, coinsPerReferral)

                    CoinInfoCard(
                        coinAmount = coinsUsed,
                        referralCount = referralCount,
                        title = giftRevealData?.cardInfoTitle ?: stringResource(R.string.book_selection_coin_warning_dynamic),
                        subtitle = giftRevealData?.cardInfoSubTitle ?: stringResource(R.string.book_selection_referral_info_dynamic_text),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ArivihanButton(
                            text = stringResource(R.string.book_selection_buy_later),
                            onClick = {
                                MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_book_selection_buy_later_btn_click.actionName)
                                onBuyLater()
                            },
                            modifier = Modifier
                                .weight(1f),
                            style = ArivihanButtonStyle.Primary
                        )

                        ArivihanButton(
                            text = stringResource(R.string.book_selection_buy_now),
                            onClick = {
                                val analyticsdata = HashMap<String, String>()
                                analyticsdata ["selected_books"] = selectedBookIds.toString()
                                MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_book_selection_buy_now_btn_click.actionName, analyticsdata)
                                onBuyNow()
                            },
                            modifier = Modifier
                                .weight(1f),
                            enabled = selectedBookIds.isNotEmpty(),
                            style = ArivihanButtonStyle.Secondary
                        )
                    }
                }
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            //animation
            if (showAnimation) {
                val giftComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.refer_book_reveal_2))

                Box(
                    modifier = Modifier
                        .size(400.dp)
                        .zIndex(1f),
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        composition = giftComposition,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                //back button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_new),
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                    }
                }


                //content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {

                    Spacer(modifier = Modifier.height(70.dp)) //back button size

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = giftRevealData?.title ?: stringResource(R.string.book_selection_title),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = giftRevealData?.subtitle ?: stringResource(R.string.book_selection_subtitle),
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }

//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    BookCoinExchangeInfo(
//                        coinAmount = (giftRevealData?.books?.firstOrNull()?.mrp ?: 400).toString(),
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 24.dp)
//                    )
//
                    Spacer(modifier = Modifier.height(24.dp))

                    BookGridNew(
                        books = books,
                        selectedBookIds = selectedBookIds,
                        onBookSelected = { book ->
                            if (!book.selectable) return@BookGridNew

                            val alreadySelected = selectedBookIds.contains(book.id)

                            if (alreadySelected) {
                                selectedBookIds = selectedBookIds.filter { it != book.id }
                                onBookRemoved(book.subjectCode)
                            } else if (selectedBookIds.size < maxSelectable) {
                                selectedBookIds = selectedBookIds + book.id
                                onBookAdded(book.subjectCode)
                            } else if (selectedBookIds.isNotEmpty()) {
                                val removedBookId = selectedBookIds.first()
                                val removedBook = books.find { it.id == removedBookId }
                                if (removedBook != null) {
                                    selectedBookIds = selectedBookIds.drop(1) + book.id
                                    onBookSwapped(removedBook.subjectCode, book.subjectCode)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}




@Composable
fun BookGridNew(
    books: List<BookItem>,
    selectedBookIds: List<String>,
    onBookSelected: (BookItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemsInRow = if (books.size >= 5) 3 else 2
    val paddingMoreVertical = if (books.size >= 5) 20 else 0
    val paddingMoreHorizontal = if (books.size >= 5) 0 else 20
    val rows = books.chunked(itemsInRow)

    Column(
        modifier = modifier
            .padding(horizontal = paddingMoreHorizontal.dp, vertical = paddingMoreVertical.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { rowBooks ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp,  Alignment.CenterHorizontally)
            ) {
                repeat(itemsInRow - rowBooks.size) {
                    Spacer(modifier = Modifier.weight(0.5f))
                }

                rowBooks.forEach { book ->
                    BookCard(
                        book = book,
                        isSelected = selectedBookIds.contains(book.id),
                        onClick = { onBookSelected(book) },
                        itemsInRow = itemsInRow,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                    )
                }

                repeat(itemsInRow - rowBooks.size) {
                    Spacer(modifier = Modifier.weight(0.5f))
                }
            }
        }
    }
}




@Composable
fun BookCard(
    book: BookItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    itemsInRow: Int,
    modifier: Modifier = Modifier
) {
    val isInCart = book.inCart
    val isDelivered = book.delivered
    val isSelectable = book.selectable

    val borderColor = if (isSelected) Color(0xFF70D9E6) else Color(0xFFBEEEF4)
    val containerColor = if (isSelected) Color(0xFFDEF6F9) else Color.Transparent


    val baseFontSize = if (itemsInRow >= 3) 14.sp else 16.sp

    Box(
        modifier = modifier
            .aspectRatio(3f / 4f)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .then(
                    if (isSelectable) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    }
                )
                .alpha(if (isDelivered || !isSelectable) 0.7f else 1f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                AsyncImage(
                    url = book.imageUrl,
                    contentDescription = book.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .padding(all = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    ArivihanAutoResizeText(
                        text = book.name,
                        maxLines = 1,
                        baseFontSize = baseFontSize,
                        minFontSize = 10.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        when {
            book.delivered -> {
                StyledBookBox(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.delivered)
                )
            }
            book.redeemed -> {
                StyledBookBox(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.ordered)
                )
            }
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(20.dp)
                    .background(colorResource(R.color.arivihan_secondary_color), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}




@Composable
fun StyledBookBox(
    modifier: Modifier = Modifier,
    text: String
) {
    val themeColor = Color(0xFFFF7A37)
    val borderColor = Color(0xFFFFD4BE)
    val backgroundColor = Color(0xFFFFEEE5)
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .background(backgroundColor, shape)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = text,
                tint = themeColor,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(2.dp))

            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = themeColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}




@Composable
fun CoinInfoCard(
    coinAmount: Int,
    referralCount: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEBEBEB)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(120.dp)
                    .offset(x = 40.dp, y = (-2).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF26C6DA).copy(alpha = 0.1f), Color.Transparent)
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(120.dp)
                    .offset(x = 40.dp, y = (-52).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF26C6DA).copy(alpha = 0.1f), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    Icon(
                        painter = painterResource(R.drawable.refer_coin),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    val parts = title.ifEmpty { stringResource(R.string.book_selection_coin_warning_dynamic) }.split("{coin}")

                    Text(
                        text = buildAnnotatedString {
                            append(parts.getOrNull(0) ?: "")
                            withStyle(SpanStyle(color = colorResource(R.color.arivihan_secondary_color))) {
                                append("$coinAmount ${stringResource(R.string.book_selection_coin_amount)}")
                            }
                            append(parts.getOrNull(1) ?: "")
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                    )
                }

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    thickness = 1.dp,
                    color = Color(0xFFEBEBEB)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {

                    val subParts = subtitle.ifEmpty { stringResource(R.string.book_selection_referral_info_dynamic_text) }.split("{count}")

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color.Gray)) {
                                append(subParts.getOrNull(0) ?: "")
                            }
                            withStyle(SpanStyle(color = colorResource(R.color.arivihan_secondary_color))) {
                                append(" $referralCount ${stringResource(R.string.referral_label)} ")
                            }
                            withStyle(SpanStyle(color = Color.Gray)) {
                                append(subParts.getOrNull(1) ?: "")
                            }
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                        ),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}


@Composable
fun BookCoinExchangeInfo(
    coinAmount: String,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(0xFF007988)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .rotate(45f)
                    .background(accentColor.copy(alpha = 0.8f))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.book_selection_book_equals),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    ),
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    painter = painterResource(R.drawable.refer_coin),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = coinAmount,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    ),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .rotate(45f)
                    .background(accentColor.copy(alpha = 0.8f))
            )
        }
    }
}








@Composable
fun GiftRevealScreen(
    title: String,
    subTitle: String,
    onCompleteAt: Float,
    onRevealComplete: () -> Unit,
) {
    var isLottiePlaying by remember { mutableStateOf(false) }
    val hasCompleted = remember { mutableStateOf(false) }

    val giftComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.refer_book_reveal))


    val giftProgress by animateLottieCompositionAsState(
        composition = giftComposition,
        isPlaying = isLottiePlaying,
        iterations = 1,
        speed = 1f
    )

    LaunchedEffect(Unit) {
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_gift_reveal_screen_view.actionName)
        snapshotFlow { giftProgress }
            .collect { progress ->
                if (
                    isLottiePlaying &&
                    !hasCompleted.value &&
                    progress >= onCompleteAt
                ) {
                    hasCompleted.value = true
                    onRevealComplete()
                }
            }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hover")
    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hover"
    )


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.ic_gift_reverl_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 34.dp)
                    .alpha(if (isLottiePlaying) 0f else 1f)
            )

            Spacer(modifier = Modifier.height(48.dp))


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .offset(
                        y = (-120).dp + if (!isLottiePlaying) hoverOffset.dp else 0.dp
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isLottiePlaying = true
                    },
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = giftComposition,
                    progress = { if (isLottiePlaying) giftProgress else 0f },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = subTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = colorResource(R.color.arivihan_secondary_color),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 34.dp)
                    .alpha(if (isLottiePlaying) 0f else 1f)
            )
        }
    }
}

//
//@Preview(
//    showBackground = true,
//    device = Devices.PIXEL_7
//)
//@Composable
//private fun BookGridNewPreview() {
//    val previewBooks = listOf(
//        BookItem(
//            id = "phy",
//            subjectCode = "PHY",
//            name = "Physics",
//            imageUrl = "https://assets.arivihan.com/refer-win/books/phy.png",
//            mrp = 400,
//            optional = false,
//            selectable = true,
//            inCart = false,
//            redeemed = true,
//            delivered = false
//        ),
//        BookItem(
//            id = "che",
//            subjectCode = "CHE",
//            name = "Chemistry",
//            imageUrl = "https://assets.arivihan.com/refer-win/books/che.png",
//            mrp = 400,
//            optional = false,
//            selectable = true,
//            inCart = false,
//            redeemed = false,
//            delivered = false
//        ),
//        BookItem(
//            id = "mat",
//            subjectCode = "MAT",
//            name = "Mathematics",
//            imageUrl = "https://assets.arivihan.com/refer-win/books/mat.png",
//            mrp = 400,
//            optional = false,
//            selectable = true,
//            inCart = false,
//            redeemed = false,
//            delivered = false
//        ),
//        BookItem(
//            id = "che",
//            subjectCode = "CHE",
//            name = "Chemistry",
//            imageUrl = "https://assets.arivihan.com/refer-win/books/che.png",
//            mrp = 400,
//            optional = false,
//            selectable = true,
//            inCart = false,
//            redeemed = false,
//            delivered = false
//        ),
//        BookItem(
//            id = "mat",
//            subjectCode = "MAT",
//            name = "Mathematics",
//            imageUrl = "https://assets.arivihan.com/refer-win/books/mat.png",
//            mrp = 400,
//            optional = false,
//            selectable = true,
//            inCart = false,
//            redeemed = false,
//            delivered = false
//        )
//    )
//
//    BookGridNew(
//        books = previewBooks,
//        selectedBookIds = listOf("mat"),
//        onBookSelected = {},
//        modifier = Modifier.padding(24.dp)
//    )
//}
