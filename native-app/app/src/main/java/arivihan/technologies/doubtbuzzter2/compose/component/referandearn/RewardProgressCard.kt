package arivihan.technologies.doubtbuzzter2.compose.component.referandearn

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanButton
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanButtonStyle
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanText
import arivihan.technologies.doubtbuzzter2.compose.component.AsyncImage
import arivihan.technologies.doubtbuzzter2.compose.component.RewardButton
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType
import arivihan.technologies.doubtbuzzter2.model.refer.ActionButton
import arivihan.technologies.doubtbuzzter2.model.refer.CoinMilestone
import org.json.JSONObject


@Composable
fun RewardProgressCard(
    modifier: Modifier = Modifier,
    title: String,
    coinIcon: Painter,
    totalCoins: Int,
    onInfoClick: () -> Unit = {},
    milestones: List<CoinMilestone>,
    buttons: List<ActionButton?>?,
    onButtonClick: (ActionButton) -> Unit
) {

    val visibleButtons = buttons.orEmpty().filterNotNull().filter { button ->
        !button.text.isNullOrBlank() && when (button.action?.screenId) {
            "PRO_PLAN_BUY" -> !SubscriptionNavigationHelper.hideProPlanSubscription() && !SubscriptionType.isProSubscription()
            else -> true
        }
    }

    val milestonePoints = milestones.map { it.point }
    val progress = if (milestonePoints.isNotEmpty()) calculateProgress(totalCoins, milestonePoints) else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.white)
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ){
            //style
            Icon(
                painter = painterResource(R.drawable.ic_refer_progress_style),
                contentDescription = null,
                tint = colorResource(R.color.arivihan_secondary_color),
                modifier = Modifier
                    .width(246.dp)
                    .requiredHeightIn()
                    .align(Alignment.TopEnd)
            )

            //content
            Column(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 14.dp)
                    .padding(horizontal = 16.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(vertical = 5.dp),
                ) {
                    ArivihanText(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.icon_sav_info),
                        tint = colorResource(R.color.arivihan_secondary_color),
                        contentDescription = null,
                        modifier = Modifier
                            .size(26.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onInfoClick
                            )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = coinIcon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = totalCoins.toString(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                        ),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(45.dp))

                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp),
                ) {
                    RewardProgressBar(
                        modifier = Modifier
                            .fillMaxWidth(),
                        progress = progress,
                        milestones = milestones,
                    )
                }

                if (visibleButtons.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                }

                val arrowTransition = rememberInfiniteTransition(label = "arrowSlide")
                val arrowOffsetFraction by arrowTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = -1f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 1200
                            1f at 0 using LinearEasing
                            0f at 300 using LinearEasing
                            0f at 500 using LinearEasing
                            (-1f) at 800 using LinearEasing
                            (-1f) at 1200
                        },
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "arrowOffset"
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    visibleButtons.forEach { button ->
                        val label = button.text.orEmpty()
                        val buttonModifier = Modifier
                            .height(44.dp)
                            .weight(1f)

                        if (button.style == "PRO_BTN") {
                            RewardButton(
                                modifier = buttonModifier,
                                text = label,
                                enabled = button.enabled,
                                onClick = { onButtonClick(button) }
                            )
                        } else {
                            val style = if (button.style == "SECONDARY") {
                                ArivihanButtonStyle.Secondary
                            } else {
                                ArivihanButtonStyle.Primary
                            }

                            ArivihanButton(
                                text = label,
                                style = style,
                                enabled = button.enabled,
                                modifier = buttonModifier,
                                onClick = { onButtonClick(button) },
                                rightIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clipToBounds(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.arrow_left),
                                            contentDescription = null,
                                            tint = LocalContentColor.current,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .rotate(180f)
                                                .graphicsLayer {
                                                    translationX = arrowOffsetFraction * size.width
                                                }
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

        }

    }
}


@Composable
fun RewardProgressBar(
    modifier: Modifier = Modifier,
    progress: Float,
    milestones: List<CoinMilestone>,
) {

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        val isAnyMileStoneUnlocked = milestones.any { it.isUnlocked }

        val infiniteTransitionMileStone = rememberInfiniteTransition(label = "milestoneScale")

        val scale by infiniteTransitionMileStone.animateFloat(
            initialValue = 1.1f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 700,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )


        val positions = List(milestones.size) { it.toFloat() / (milestones.size - 1) }

        val infiniteTransitionLine = rememberInfiniteTransition(label = "lineAnimation")

        val animatedProgress by infiniteTransitionLine.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1500,
                    easing = LinearEasing
                )
            ),
            label = "lineAnimation"
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .align(Alignment.Center)
        ) {
            val stroke = 12.dp.toPx()
            val startX = 6.dp.toPx()
            val endX = size.width - 6.dp.toPx()
            val centerY = size.height / 2

            drawLine(
                color = Color(0xFFEAEAEA),
                start = Offset(startX, centerY),
                end = Offset(endX, centerY),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )

            drawLine(
                color = Color(0xFF20C5E8),
                start = Offset(startX, centerY),
                end = Offset(
                    startX + (endX - startX) * progress,
                    centerY
                ),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )

            if (isAnyMileStoneUnlocked) {
                val progressWidth = (endX - startX) * progress
                val shimmerWidth = 200.dp.toPx()

                val shimmerStart = startX + (progressWidth + shimmerWidth) * animatedProgress - shimmerWidth

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.7f), Color.Transparent),
                        start = Offset(shimmerStart, centerY),
                        end = Offset(shimmerStart + shimmerWidth, centerY)
                    ),
                    start = Offset(
                        shimmerStart.coerceAtLeast(startX),
                        centerY
                    ),
                    end = Offset(
                        (shimmerStart + shimmerWidth).coerceAtMost(startX + progressWidth),
                        centerY
                    ),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }


            positions.forEachIndexed { index, percent ->
                val x = startX + (endX - startX) * percent
                val milestone = milestones[index]

                if (milestone.milestoneIcon == null ||  milestone.point == 0) {
                    drawCircle(
                        color = Color(0xFF20C5E8),
                        radius = 10.dp.toPx(),
                        center = Offset(x, centerY)
                    )
                }
            }
        }

        positions.forEachIndexed { index, percent ->
            val milestone = milestones[index]
            val itemWidth = 60.dp
            val centerOffset = itemWidth / 2
            val xOffset = maxWidth * percent - centerOffset

            if (milestone.point != 0) {
                milestone.milestoneBanner?.let { banner ->

                    //padding dynamic
                    val verticalPadding = remember(milestone.rewardType) {
                        try {
                            val json = MyApplication.getFirebaseRemoteConfig().getString("milestone_icon_reward_type_padding")
                            if (json.isNotEmpty()) {
                                val root = JSONObject(json)
                                root.optInt(milestone.rewardType, root.optInt("default", 12)).dp
                            } else 12.dp
                        } catch (_: Exception) { 12.dp }
                    }

                    AsyncImage(
                        url = banner,
                        modifier = Modifier
                            .height(62.dp)
                            .padding(vertical = verticalPadding)
                            .offset(
                                x = maxWidth * percent + when (index) {
                                    0 -> 14.dp
                                    milestones.lastIndex -> (-14).dp
                                    else -> 0.dp
                                },
                                y = (-34).dp
                            )
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)
                                layout(placeable.width, placeable.height) {
                                    placeable.placeRelative(-placeable.width / 2, 0)
                                }
                            },
                        contentScale = ContentScale.FillHeight
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(itemWidth)
                    .offset(
                        x = xOffset,
                        y = (33).dp
                    )
                    .wrapContentWidth(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                if (milestone.point != 0) {
                    milestone.milestoneIcon?.let { icon ->
                        Image(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                },
                        )
                    }
                }
            }

            //for 0 coin not show
            if (milestone.point != 0) {
                Text(
                    text = milestone.point.toString(),
                    color = if (milestone.isUnlocked) Color.Unspecified else Color(0xFF717171),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier
                        .width(itemWidth)
                        .offset(
                            x = xOffset,
                            y = 64.dp
                        )
                        .wrapContentWidth(Alignment.CenterHorizontally),
                )
            }
        }
    }
}


fun calculateProgress(
    coins: Int,
    milestones: List<Int>
): Float {
    if (milestones.isEmpty()) return 0f
    if (milestones.size == 1) return if (coins >= milestones[0]) 1f else 0f

    if (coins <= milestones.first()) return 0f
    if (coins >= milestones.last()) return 1f

    val segmentCount = milestones.size - 1

    for (i in 0 until segmentCount) {
        val start = milestones[i]
        val end = milestones[i + 1]
        if (coins in start..end) {
            val localProgress = (coins - start).toFloat() / (end - start)
            return (i + localProgress) / segmentCount
        }
    }

    return 0f
}