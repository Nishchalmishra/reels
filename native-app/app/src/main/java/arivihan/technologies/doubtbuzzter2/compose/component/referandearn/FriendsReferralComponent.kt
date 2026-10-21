package arivihan.technologies.doubtbuzzter2.compose.component.referandearn

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.compose.component.AsyncImage
import arivihan.technologies.doubtbuzzter2.model.refer.InviteStatus
import arivihan.technologies.doubtbuzzter2.model.refer.InviteStep
import arivihan.technologies.doubtbuzzter2.model.refer.InvitedFriendReferralItem
import arivihan.technologies.doubtbuzzter2.model.refer.InvitedFriendUiModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds


@Composable
fun ReferralHistorySection(
    friendsReferrals: List<InvitedFriendReferralItem>,
    onViewMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.aapke_invited_dost),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onViewMoreClick
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.view_more),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colorResource(id = R.color.arivihan_secondary_color)
                        )

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            modifier = Modifier
                                .size(20.dp),
                            contentDescription = null,
                            tint = colorResource(id = R.color.arivihan_secondary_color)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                friendsReferrals.forEach { dto ->
                    InviteFriendCard(
                        referral = dto.toUiModel(context),
                        referralSteps = emptyList()
                    )
                }
            }
        }
    }
}




fun InvitedFriendReferralItem.toUiModel(context: Context): InvitedFriendUiModel {
    val inviteStatus = when (status.uppercase()) {
        "PENDING", "REGISTERED", "REFERRED" -> InviteStatus.PENDING
        "SUCCESSFUL", "SUCCESS", "COMPLETED", "SUBSCRIBED" -> InviteStatus.SUCCESSFUL
        "EXPIRED", "FAILED" -> InviteStatus.EXPIRED
        else -> InviteStatus.PENDING
    }

    return InvitedFriendUiModel(
        displayName = displayName,
        maskedNumber = maskedNumber,
        illustration = illustration,
        illustrationIndex = illustrationIndex,
        status = inviteStatus,
        statusText = statusText,
        windowExpiresAt = windowExpiresAt,
        steps = steps ?: emptyList(),
        states = states ?: emptyList(),
        showRewardBadge = inviteStatus == InviteStatus.SUCCESSFUL,
        description = statusText,
        avatarUrl = avatarUrl
    )
}






@Composable
fun InviteFriendCard(
    modifier: Modifier = Modifier,
    referral: InvitedFriendUiModel,
    referralSteps: List<InviteStep>,
    expanded: Boolean = false,
    onExpandClick: () -> Unit = {}
) {
    val hasSteps = referralSteps.isNotEmpty()

    val remainingTime = if (referral.status == InviteStatus.PENDING && referral.windowExpiresAt != null) {
        rememberRemainingTime(referral.windowExpiresAt)
    } else {
        null
    }

    val isTimerExpired = remainingTime == "Expired"

    val effectiveStatus = if (isTimerExpired) InviteStatus.EXPIRED else referral.status

    val (backgroundColor, textColor) = when (effectiveStatus) {
        InviteStatus.PENDING -> Color(0xFFFFF4CC) to Color(0xFFB78103)
        InviteStatus.SUCCESSFUL -> Color(0xFFE4F8EC) to Color(0xFF1A9C5A)
        InviteStatus.EXPIRED -> Color(0xFFFFEBEB) to Color(0xFFE53935)
    }


    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandClick() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                if (!referral.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        url = referral.avatarUrl,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentDescription = null,
                        errorContent = {
                            Image(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                                painter = painterResource(
                                    when (referral.illustration) {
                                        "boy" -> R.drawable.img_illustration_boy
                                        "girl" -> R.drawable.img_illustration_girl
                                        else -> R.drawable.img_illustration_netural
                                    }
                                ),
                                contentDescription = null
                            )
                        }
                    )
                } else {
                    Image(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        painter = painterResource(
                            when (referral.illustration) {
                                "boy" -> R.drawable.img_illustration_boy
                                "girl" -> R.drawable.img_illustration_girl
                                else -> R.drawable.img_illustration_netural
                            }
                        ),
                        contentDescription = null
                    )
                }

                if (referral.showRewardBadge) {
                    Image(
                        painter = painterResource(R.drawable.img_medel),
                        contentDescription = stringResource(R.string.reward_badge),
                        modifier = Modifier
                            .size(26.dp)
                            .offset(x = (7).dp, y = (7).dp)
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = referral.displayName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            color = backgroundColor,
                            shape = RoundedCornerShape(50),
                        ) {
                            Text(
                                text = when (effectiveStatus) {
                                    InviteStatus.PENDING -> stringResource(R.string.invite_status_pending)
                                    InviteStatus.SUCCESSFUL -> stringResource(R.string.invite_status_successful)
                                    InviteStatus.EXPIRED -> stringResource(R.string.invite_status_expired)
                                },
                                color = textColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (hasSteps) {
                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            painter = painterResource(R.drawable.arrow_down_white),
                            contentDescription = null,
                            tint = Color(0xFF4A4A4A),
                            modifier = Modifier
                                .size(14.dp)
                                .rotate(if (expanded) 180f else 0f)
                        )

                        Spacer(modifier = Modifier.width(4.dp))
                    }

                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {

                    Text(
                        text = referral.description,
                        color = if (effectiveStatus == InviteStatus.PENDING) Color.Black else Color.Gray,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )

                    if (effectiveStatus == InviteStatus.PENDING && remainingTime != null) {
                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = remainingTime,
                            modifier = Modifier.wrapContentWidth(),
                            color = textColor,
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

            }
        }

        AnimatedVisibility(
            visible = hasSteps && expanded
        ) {
            Column {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFFEAEAEA)
                )

                //steps
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    referralSteps.forEachIndexed { index, step ->
                        val completed = step.done
                        val failed = step.failed
                        val isLastStep = index == referralSteps.lastIndex

                        Row(
                            verticalAlignment = Alignment.Top
                        ) {

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                failed -> Color(0xFFE52E44)
                                                isLastStep -> if (completed) Color(0xFF1F8F5F) else Color(0xFF949494)
                                                else -> colorResource(R.color.arivihan_secondary_color)
                                            }
                                        )
                                        .zIndex(2f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = if (failed) painterResource(R.drawable.__icon__close_) else painterResource(R.drawable.ic_check),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = if (failed) Modifier.size(12.dp) else  Modifier.size(8.dp)
                                    )
                                }

                                if (!isLastStep) {
                                    Canvas(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(18.dp)
                                            .zIndex(1f),
                                    ) {
                                        drawLine(
                                            color = Color(0xFFD8D8D8),
                                            start = Offset(size.width / 2, 0f),
                                            end = Offset(size.width / 2, size.height),
                                            strokeWidth = 2.dp.toPx(),
                                            cap = StrokeCap.Round,
                                            pathEffect = PathEffect.dashPathEffect(
                                                floatArrayOf(6f, 16f)
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(16.dp))

                            Text(
                                text = step.label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    failed && (index != 0) -> Color(0xFF949494)
                                    isLastStep && !completed -> Color(0xFF949494)
                                    completed -> Color.Black
                                    else -> Color(0xFF949494)
                                },
                            )
                        }
                    }
                }
            }
        }


    }
}


@Composable
fun rememberRemainingTime(windowExpiresAt: String?): String {

    return produceState(
        initialValue = "",
        key1 = windowExpiresAt
    ) {
        if (windowExpiresAt == null) {
            value = ""
            return@produceState
        }

        while (true) {
            value = calculateRemainingTime(windowExpiresAt)
            if (value == "Expired") break
            delay(1000.milliseconds)
        }
    }.value
}



private fun calculateRemainingTime(expiryTimeString: String): String {
    return try {
        val expiryTime = java.time.Instant.parse(expiryTimeString)
        val currentTime = java.time.Instant.now()
        val durationMillis = java.time.Duration.between(currentTime, expiryTime).toMillis()
        if (durationMillis <= 0) "Expired" else formatDuration(durationMillis)
    } catch (e: Exception) {
        Log.e("ReferralHistory", "Error parsing expiry time: $expiryTimeString", e)
        "N/A"
    }
}


@SuppressLint("DefaultLocale")
private fun formatDuration(millis: Long): String {
    val days = millis / 86_400_000
    val hours = (millis / 3_600_000) % 24
    val minutes = (millis / 60_000) % 60

    return String.format("%02dd %02dh %02dm", days, hours, minutes)
}