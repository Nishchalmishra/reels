package arivihan.technologies.doubtbuzzter2.compose.component.referandearn

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxSize
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.compose.component.AsyncImage




@Composable
fun MonthlyWinnersCard(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.monthly_winners),
    subTitle: String,
    winners: List<Winner>,
    winnersImage: Painter,
    onViewMoreClick: () -> Unit = {}
) {

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    ),
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

            Spacer(modifier = Modifier.height(8.dp))

            //2
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = winnersImage,
                    contentDescription = null,
                    modifier = Modifier.height(26.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF7A8686)
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            WinnerPodiumView(
                winners = winners
            )
        }
    }
}


@Composable
fun WinnerPodiumView(
    winners: List<Winner>
){
    if (winners.isEmpty()) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(310.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.podium),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.FillWidth
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-84).dp)
        ) {
            WinnerItem(
                winner = winners[0],
                avatarSize = 96.dp,
            )
        }

        if (winners.size > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.33f)
                    .offset(y = (-46).dp),
                contentAlignment = Alignment.Center
            ) {
                WinnerItem(
                    winner = winners[1],
                    avatarSize = 78.dp,
                )
            }
        }

        if (winners.size > 2) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth(0.33f)
                    .offset(y = (-32).dp),
                contentAlignment = Alignment.Center
            ) {
                WinnerItem(
                    winner = winners[2],
                    avatarSize = 78.dp,
                )
            }
        }
    }
}


data class Winner(
    val name: String,
    val coins: Int,
    @DrawableRes val avatar: Int,
    val ringColor: Color,
    val illustration: String,
    val avatarUrl: String? = null
)



@Composable
private fun WinnerItem(
    winner: Winner,
    avatarSize: Dp,
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = winner.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF121212)
            )
        )

        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .width(avatarSize + 42.dp)
                .height(avatarSize + 90.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (!winner.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    url = winner.avatarUrl,
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .border(3.dp, winner.ringColor, CircleShape),
                    contentDescription = null,
                    errorContent = {
                        Image(
                            painter = painterResource(
                                when (winner.illustration) {
                                    "boy" -> R.drawable.img_illustration_boy
                                    "girl" -> R.drawable.img_illustration_girl
                                    else -> R.drawable.img_illustration_netural
                                }
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(3.dp, winner.ringColor, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                )
            } else {
                Image(
                    painter = painterResource(
                        when (winner.illustration) {
                            "boy" -> R.drawable.img_illustration_boy
                            "girl" -> R.drawable.img_illustration_girl
                            else -> R.drawable.img_illustration_netural
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .border(3.dp, winner.ringColor, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Surface(
                modifier = Modifier
                    .offset(y = (-85).dp)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(50),
                color = Color.White,
                border = BorderStroke(1.5.dp, winner.ringColor),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.refer_coin),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(4.dp))

                    Text(
                        text = winner.coins.toString(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}