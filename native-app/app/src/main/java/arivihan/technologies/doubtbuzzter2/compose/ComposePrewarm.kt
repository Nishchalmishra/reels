package arivihan.technologies.doubtbuzzter2.compose

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.compose.component.ShimmerEffect
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.BottomBarSkeleton
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.FreeRewardBannerSkeleton
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.ReferralHistorySkeleton
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.RewardProgressCardSkeleton
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.TopHeaderImageSkeleton

object ComposePrewarm {

    @JvmStatic
    fun prewarm(activity: MyApplication) {


        try {
            val view = ComposeView(activity)
            view.setContent {
                ArivihanComposeApp(enabled = false) {
                    PrewarmHeavyComponents()

//                    ReferAndEarnScreen(
//                        screenState = ReferAndEarnScreenState(loading = true),
//                        onBack = {  },
//                        onRefresh = { },
//                        onOpenReferralHistory = { },
//                        onMonthWinnerViewMoreClick = { },
//                        redeemBtnClick = { },
//                        proPlanBtnClick = { },
//                        onShare = {  },
//                    )
                }


            }

            Handler(Looper.getMainLooper()).postDelayed({
                view.disposeComposition()
                Log.d("ComposePrewarm", "Disposed prewarm composition")
            }, 50)

            Log.d("ComposePrewarm", "Successfully prewarmed Compose runtime")
        } catch (e: Exception) {
            Log.e("ComposePrewarm", "Error during prewarm", e)
        }
    }

    @Composable
    private fun PrewarmHeavyComponents() {
        Column(modifier = Modifier.size(0.dp)) {
            // Heavy PNG images - forces decode and cache
            Image(
                painter = painterResource(R.drawable.referpng),
                contentDescription = null,
                modifier = Modifier.size(1.dp),
                contentScale = ContentScale.Crop
            )
            Image(
                painter = painterResource(R.drawable.refer_coin),
                contentDescription = null,
                modifier = Modifier.size(1.dp),
                contentScale = ContentScale.Crop
            )
            Image(
                painter = painterResource(R.drawable.gift),
                contentDescription = null,
                modifier = Modifier.size(1.dp),
                contentScale = ContentScale.Crop
            )
            Image(
                painter = painterResource(R.drawable.trophy),
                contentDescription = null,
                modifier = Modifier.size(1.dp),
                contentScale = ContentScale.Crop
            )
            Image(
                painter = painterResource(R.drawable.img_golden_trophy),
                contentDescription = null,
                modifier = Modifier.size(1.dp),
                contentScale = ContentScale.Crop
            )
            Image(
                painter = painterResource(R.drawable.img_super_coin_golden),
                contentDescription = null,
                modifier = Modifier.size(1.dp),
                contentScale = ContentScale.Crop
            )
            Image(
                painter = painterResource(R.drawable.img_social_share),
                contentDescription = null,
                modifier = Modifier.size(1.dp),
                contentScale = ContentScale.Crop
            )

            // Heavy vector icons - forces XML parsing and rendering
            Icon(
                painter = painterResource(R.drawable.ic_pro_premium_star),
                contentDescription = null,
                modifier = Modifier.size(1.dp)
            )
            Icon(
                painter = painterResource(R.drawable.ic_edit_profile_star),
                contentDescription = null,
                modifier = Modifier.size(1.dp)
            )
            Icon(
                painter = painterResource(R.drawable.ic_edit_profile_crown),
                contentDescription = null,
                modifier = Modifier.size(1.dp)
            )
            Icon(
                painter = painterResource(R.drawable.ic_trophy_svgrepo_com),
                contentDescription = null,
                modifier = Modifier.size(1.dp)
            )
            Icon(
                painter = painterResource(R.drawable.ic_trophy_icon_final),
                contentDescription = null,
                modifier = Modifier.size(1.dp)
            )
            Icon(
                painter = painterResource(R.drawable.share_05),
                contentDescription = null,
                modifier = Modifier.size(1.dp)
            )

            // Shimmer animation - triggers infinite transition setup
            ShimmerEffect(modifier = Modifier.size(1.dp))

            // Card + shapes + elevation - prewarms material layer
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.size(1.dp)
            ) {}

            // Button colors - prewarms ButtonDefaults internals
            Button(
                onClick = {},
                modifier = Modifier.size(1.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E98AC))
            ) {}

            // CircleShape background - prewarms shape cache
            Box(
                modifier = Modifier
                    .size(1.dp)
                    .background(Color.Gray, CircleShape)
            )
        }
    }
}


object ComposeHomePrewarm {

    fun setupPrewarm(composeView: ComposeView) {
        composeView.setContent {
            ArivihanComposeApp(enabled = true) {
                MaterialTheme {
                    val scrollState = rememberScrollState()
                    val isLoading = remember { mutableStateOf(true) }

                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            BottomBarSkeleton()
                        }
                    ) { paddingValues ->
                        Column(
                            modifier = Modifier
                                .padding(paddingValues)
                                .verticalScroll(scrollState)
                        ) {
                            TopHeaderImageSkeleton()

                            Spacer(modifier = Modifier.height(16.dp))

                            RewardProgressCardSkeleton()

                            Spacer(modifier = Modifier.height(16.dp))

                            FreeRewardBannerSkeleton()

                            Spacer(modifier = Modifier.height(16.dp))

                            ReferralHistorySkeleton()

                            repeat(3) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(Color.Gray, CircleShape)
                                        )
                                        Column {
                                            Text("Title", fontSize = 16.sp)
                                            Text("Subtitle", fontSize = 14.sp)
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E98AC)
                                )
                            ) {
                                Text("Button Text")
                            }
                        }
                    }
                }
            }
        }
    }
}
