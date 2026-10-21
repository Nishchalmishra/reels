package arivihan.technologies.doubtbuzzter2.compose.component.referandearn

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanInfoBanner
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanQuantityCounter
import arivihan.technologies.doubtbuzzter2.compose.component.AsyncImage



enum class BookCartItemMode {
    Cart,
    OrderSuccess,
    OrderHistory
}

data class BookCartItemData(
    val name: String,
    val imageUrl: String = "",
    val finalPrice: Int = 0,
    val actualPrice: Int = 0,
    val discountPercent: Int = 0,
    val coinUsage: Int = 0,
    val qty: Int = 0,
    val isFree: Boolean = false,
    val showPrice: Boolean = false,
)




@Composable
fun BookCartItem(
    modifier: Modifier = Modifier,
    data: BookCartItemData,
    mode: BookCartItemMode,
    trailingAction: @Composable (() -> Unit)? = null,
    subjectFontWeight: FontWeight = FontWeight.Medium
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {

        Card(
            modifier = Modifier
                .size(width = 68.dp, height = 84.dp)
                .padding(2.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = colorResource(R.color.book_order_placeholder_bg)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            if (data.imageUrl.isNotEmpty()) {
                AsyncImage(
                    url = data.imageUrl,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .height(72.dp)
                .weight(1f)
        ) {
            Row {
                Text(
                    text = data.name,
                    modifier = Modifier
                        .weight(1f, fill = true),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = subjectFontWeight,
                        color = colorResource(R.color.dark_text_color),
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (data.isFree) {
                    Spacer(modifier = Modifier.width(8.dp))

                    FreeBadge()
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(1f))

            when (mode) {
                BookCartItemMode.Cart -> {
                    if (data.showPrice) CartPriceRow(data)
                }
                BookCartItemMode.OrderSuccess -> OrderSuccessContent(data)
                BookCartItemMode.OrderHistory -> HistoryPriceRow(data)
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        if (trailingAction != null) {
            trailingAction()
        }
    }
}

@Composable
fun BookCartItemWithQuantity(
    modifier: Modifier = Modifier,
    data: BookCartItemData,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    subjectFontWeight: FontWeight = FontWeight.Medium
) {
    BookCartItem(
        modifier = modifier,
        data = data,
        mode = BookCartItemMode.Cart,
        trailingAction =
            if (data.isFree) null
            else {
            {
                ArivihanQuantityCounter(
                    count = quantity,
                    onIncrement = onIncrement,
                    onDecrement = onDecrement,
                    accentColor = colorResource(R.color.book_order_accent_color)
                )
            }
        },
        subjectFontWeight = subjectFontWeight
    )
}



@Composable
fun CartPriceRow(
    data: BookCartItemData
) {
    Row(
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "₹${data.finalPrice}",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorResource(R.color.dark_text_color)
        )

        if (data.actualPrice > data.finalPrice) {
            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "₹${data.actualPrice}",
                fontSize = 14.sp,
                color = colorResource(R.color.book_order_text_secondary),
                textDecoration = TextDecoration.LineThrough
            )

//            if (data.discountPercent >= 75 && !data.isFree) {
//                Spacer(modifier = Modifier.width(6.dp))
//
//                Box(
//                    modifier = Modifier
//                        .clip(RoundedCornerShape(12.dp))
//                        .background(Color(0xFFD0E9EF))
//                        .padding(horizontal = 6.dp, vertical = 4.dp)
//                ) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text(
//                            text = "🔥",
//                            fontSize = 9.sp
//                        )
//                        Spacer(modifier = Modifier.width(2.dp))
//                        Text(
//                            text = stringResource(R.string.book_order_best_deal_),
//                            fontSize = 11.sp,
//                            fontWeight = FontWeight.ExtraBold,
//                            color = colorResource(R.color.refer_and_earn_accent)
//                        )
//                    }
//                }
//            } else
            if (data.discountPercent > 0) {
                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE6F6EE))
                        .padding(horizontal = 5.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${data.discountPercent}% off",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F8F5F)
                    )
                }
            }
        }
    }
}





@Composable
private fun OrderSuccessContent(
    data: BookCartItemData
) {
//    if (data.qty > 1) {
        Text(
            text = stringResource(R.string.qty) + " " + data.qty,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 14.sp,
                color = Color(0x80121212),
            ),
        )
//    }
}





@Composable
private fun HistoryPriceRow(data: BookCartItemData) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (data.coinUsage > 0) {
            Text(
                text = "₹0",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "(",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            )

            Icon(
                painter = painterResource(id = R.drawable.refer_coin),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp)
                    .padding(horizontal = 2.dp)
            )

            Spacer(modifier = Modifier.width(2.dp))

            Text(
                text = "${data.coinUsage}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            )

            Spacer(modifier = Modifier.width(2.dp))

            Text(
                text = ")",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            )
        } else {
            Text(
                text = "₹${data.finalPrice}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.dark_text_color)
            )
        }

//        if (data.qty > 1) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(4.dp)
                    .background(Color(0x66666699), shape = CircleShape)
            )
            Text(
                text = stringResource(R.string.qty) + " " + data.qty,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.sp,
                    color = Color(0x80121212),
                ),
            )
//        }
    }
}




@Composable
private fun FreeBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val ribbonColor = Color(0xFF4CAF50)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(52.dp)
            .height(22.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val notchDepth = w * 0.12f

            val ribbonPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w - notchDepth, h / 2f)
                lineTo(w, h)
                lineTo(0f, h)
                lineTo(notchDepth, h / 2f)
                close()
            }
            drawPath(ribbonPath, color = ribbonColor)

            val shimmerWidth = w * 0.4f
            val shimmerStart = (w + shimmerWidth) * animatedProgress - shimmerWidth

            val shimmerPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w - notchDepth, h / 2f)
                lineTo(w, h)
                lineTo(0f, h)
                lineTo(notchDepth, h / 2f)
                close()
            }

            clipPath(shimmerPath) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        start = Offset(shimmerStart, 0f),
                        end = Offset(shimmerStart + shimmerWidth, h)
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.book_order_free_tag),
                fontSize = 14.sp,
                modifier = Modifier
                    .scale(0.9f),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
            )
        }
    }
}


@Composable
fun AddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = colorResource(R.color.book_order_accent_color),
    filled: Boolean = false,
    paddingValues: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
) {
    val contentColor = if (filled) Color.White else accentColor

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, accentColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (filled) accentColor else Color.Transparent
        ),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add_24),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = contentColor
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = stringResource(R.string.add),
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}


data class RecommendationCardData(
    val name: String,
    val imageUrl: String = "",
    val price: Int = 0,
    val isSemiBestDeal: Boolean,
    val isBestDeal: Boolean
)

@Composable
fun RecommendationCard(
    modifier: Modifier = Modifier,
    data: RecommendationCardData,
    onAdd: () -> Unit,
) {
    val borderColor = if (data.isBestDeal || data.isSemiBestDeal) colorResource(R.color.refer_and_earn_accent) else Color(0xFFDCEFEC)

    Column(
        modifier = modifier
            .offset(
                y = (-10).dp
            )
    ) {

        if (!data.isBestDeal) {
            Spacer(modifier = Modifier
                .padding(bottom = 10.dp)
                .height(26.dp))
        }else{
            Box(
                modifier = Modifier
                    .offset(y = 10.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(colorResource(R.color.refer_and_earn_accent))
                    .padding(top = 6.dp)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.book_order_best_deal),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        url = data.imageUrl,
                        modifier = Modifier
                            .height(70.dp)
                            .padding(5.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = data.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorResource(R.color.dark_text_color),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "₹${data.price}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.dark_text_color)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (data.isBestDeal || data.isSemiBestDeal) {
                        AddButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            onClick = onAdd,
                            accentColor = colorResource(R.color.refer_and_earn_accent),
                            paddingValues = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                        filled = true
                        )
                    } else {
                        AddButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            onClick = onAdd
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}




















@Preview(showBackground = true)
@Composable
private fun RecommendationCardPreview() {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RecommendationCard(
            modifier = Modifier
                .width(100.dp),
            data = RecommendationCardData(
                name = "Mathematics Part 1",
                price = 400,
                isSemiBestDeal = true,
                isBestDeal = true
            ),
            onAdd = {}
        )

        RecommendationCard(
            data = RecommendationCardData(
                name = "Science Part 2",
                price = 350,
                isSemiBestDeal = false,
                isBestDeal = false
            ),
            onAdd = {}
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun BookCartItemCartPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // With quantity counter
        BookCartItemWithQuantity(
            data = BookCartItemData(
                name = "Mathematics Part 1",
                finalPrice = 299,
                actualPrice = 499,
                discountPercent = 40,
                isFree = false,
                showPrice = false
            ),
            quantity = 2,
            onIncrement = {},
            onDecrement = {}
        )

        // Free badge, no trailing action
        BookCartItem(
            data = BookCartItemData(
                name = "Science Part 2",
                finalPrice = 0,
                actualPrice = 399,
                isFree = true
            ),
            mode = BookCartItemMode.Cart
        )

        // With quantity at 0 (shows Add button)
        BookCartItemWithQuantity(
            data = BookCartItemData(
                name = "Hindi Vyakaran",
                finalPrice = 199,
                actualPrice = 299,
                discountPercent = 33,
                isFree = false
            ),
            quantity = 0,
            onIncrement = {},
            onDecrement = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BookCartItemOrderSuccessPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BookCartItem(
            data = BookCartItemData(
                name = "Mathematics Part 1",
                isFree = true
            ),
            mode = BookCartItemMode.OrderSuccess
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BookCartItemOrderHistoryPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BookCartItem(
            data = BookCartItemData(
                name = "English Grammar",
                coinUsage = 400,
                isFree = true
            ),
            mode = BookCartItemMode.OrderHistory
        )

        BookCartItem(
            data = BookCartItemData(
                name = "Hindi Vyakaran",
                finalPrice = 350,
                coinUsage = 0,
                isFree = false
            ),
            mode = BookCartItemMode.OrderHistory
        )
    }
}