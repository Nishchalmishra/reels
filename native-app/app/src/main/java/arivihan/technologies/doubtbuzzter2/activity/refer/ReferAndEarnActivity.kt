package arivihan.technologies.doubtbuzzter2.activity.refer


import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity
import arivihan.technologies.doubtbuzzter2.activity.ProPlanSubscriptionActivity
import arivihan.technologies.doubtbuzzter2.compose.ArivihanComposeApp
import arivihan.technologies.doubtbuzzter2.compose.ArivihanScreenState
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanScreenStateContainer
import arivihan.technologies.doubtbuzzter2.compose.component.AsyncImage
import arivihan.technologies.doubtbuzzter2.compose.component.MiniVideoPlayer
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.BottomBarSkeleton
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.FreeRewardBannerSkeleton
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.MonthlyWinnersCard
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.ReferralHistorySection
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.ReferralHistorySkeleton
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.RewardProgressCard
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.RewardProgressCardSkeleton
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.TopHeaderImageSkeleton
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.Winner
import arivihan.technologies.doubtbuzzter2.compose.component.rememberNetworkGuard
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType
import arivihan.technologies.doubtbuzzter2.model.player.MiniVideoPlayerConfig
import arivihan.technologies.doubtbuzzter2.model.refer.ActionButton
import arivihan.technologies.doubtbuzzter2.model.refer.CoinMilestone
import arivihan.technologies.doubtbuzzter2.model.refer.InfoSection
import arivihan.technologies.doubtbuzzter2.model.refer.ReferAndEarnScreenData
import arivihan.technologies.doubtbuzzter2.viewModel.ReferAndEarnViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class MiniVideoPlayerStage{
    FULL_SCREEN,
    MINI_PLAYER
}

data class VideoPlayerState(
    val startingStage: MiniVideoPlayerStage = MiniVideoPlayerStage.MINI_PLAYER,
    val showMiniPlayer: Boolean = false,
    val videoUrl: String = "",
    val videoThumbnailUrl: String = ""
)



//refer and earn activity
class ReferAndEarnActivity : BaseActivity(), EdgeToEdgeHelper.SelfManagedInsets {

    companion object {
        val calculateReferralCount: (Int?, Int) -> Int = { milestoneCoins, coinsPerReferral ->
            milestoneCoins?.div(coinsPerReferral) ?: 0
        }
    }

    private var navigatedAway = false

    private val viewModel: ReferAndEarnViewModel by lazy {
        ViewModelProvider(this)[ReferAndEarnViewModel::class.java]
    }

    // Firebase RDB coin listener
    private var coinDbRef: DatabaseReference? = null
    private var coinListener: ValueEventListener? = null
    private var lastRdbCoinValue: Int? = null

    override fun onResume() {
        super.onResume()
        if (navigatedAway) {
            navigatedAway = false
            viewModel.loadReferAndEarnScreen()
        }
        attachCoinListener()
    }

    override fun onPause() {
        super.onPause()
        detachCoinListener()
    }

    override fun onDestroy() {
        detachCoinListener()
        super.onDestroy()
    }

    private fun attachCoinListener() {
        if (coinListener != null) return // already attached

        val userId = MyApplication.getStringData(PreferenceKeys.USER_ID.key)
        if (userId.isNullOrEmpty()) return

        coinDbRef = FirebaseDatabase.getInstance()
            .getReference("users/refer-and-earn-coins/$userId")

        coinListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (isFinishing || isDestroyed) return
                try {
                    if (!snapshot.exists()) return
                    val coins = snapshot.getValue(Int::class.java) ?: return
                    if (coins == lastRdbCoinValue) return // same value, skip
                    lastRdbCoinValue = coins
                    viewModel.onRdbCoinUpdate(coins)
                } catch (e: Exception) {
                    Log.e("ReferAndEarnActivity", "Error reading coin from RDB", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReferAndEarnActivity", "Coin listener cancelled: ${error.message}")
            }
        }

        coinDbRef?.addValueEventListener(coinListener!!)
    }

    private fun detachCoinListener() {
        if (coinDbRef != null && coinListener != null) {
            coinDbRef?.removeEventListener(coinListener!!)
            coinListener = null
        }
        lastRdbCoinValue = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //load data
        viewModel.loadReferAndEarnScreen()
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_screen_view.actionName)

        setContent {
            ArivihanComposeApp(
                enabled = true,
            ) {
                val screenState by viewModel.screenState.observeAsState(ArivihanScreenState.Loading)
                val isRefreshing by viewModel.isRefreshing.observeAsState(false)
                val networkGuard = rememberNetworkGuard()

                LaunchedEffect(screenState) {
                    val data = (screenState as? ArivihanScreenState.Content<ReferAndEarnScreenData>)?.data
                    val shareData = data?.share
                    if (shareData != null) {
                        val inviteText = buildString {
                            append(shareData.text)
                            append(shareData.link)
                        }
                        MyApplication.saveData(PreferenceKeys.REFER_AND_EARN_SHARE_TEXT.key, inviteText)
                    }
                }

                ReferAndEarnScreen(
                    screenState = screenState,
                    isRefreshing = isRefreshing,
                    onBack = { finish() },
                    onRefresh = {
                        networkGuard { viewModel.refreshReferAndEarnScreen() }
                    },
                    onOpenReferralHistory = {
                        networkGuard {
                            navigatedAway = true
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_screen_referral_history_btn_click.actionName)
                            startActivity(Intent(this@ReferAndEarnActivity, ReferralInvitedFriendsActivity::class.java))
                        }
                    },
                    onMonthWinnerViewMoreClick = {
                        networkGuard {
                            navigatedAway = true
                            startActivity(Intent(this@ReferAndEarnActivity, MonthlyWinnersActivity::class.java))
                        }
                    },
                    onCartOpen = {
                        networkGuard {
                            navigatedAway = true
                            startActivity(Intent(this@ReferAndEarnActivity, BookOrderActivity::class.java))
                        }
                    },
                    onButtonClick = { button ->
                        networkGuard {
                            val data = (screenState as? ArivihanScreenState.Content<ReferAndEarnScreenData>)?.data
                            when (button.action?.screenId) {
                                "GIFT_REVEAL" -> openGiftReveal(data)
                                "PRO_PLAN_BUY" -> openProPlan()
                                else -> openScreenFromButton(button)
                            }
                        }
                    },
                    onShare = {
                        shareReferral(screenState) },
                )
            }
        }
    }




    //gift reveal
    private fun openGiftReveal(data: ReferAndEarnScreenData?) {
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_screen_redeem_btn_click.actionName)
        val isBookFlow = data?.flowType == "BOOK"

                            val achievedMilestone = data?.progress?.milestones
                                ?.filter { it.achieved }
                                ?.filter { it.rewardType == "BOOK" }
                                ?.maxByOrNull { it.coins }

                            val referralCount = calculateReferralCount(achievedMilestone?.coins, data?.coinsCard?.coinsPerReferral ?: 1)

        // Backend else string.
        val giftRevealText = data?.giftRevealText
        val animationTitle = giftRevealText?.title?.takeIf { it.isNotBlank() }
            ?.let { runCatching { String.format(it, referralCount) }.getOrDefault(it) }
            ?: getString(R.string.gift_reveal_title_dynamic, referralCount.toString())
        val animationSubTitle = giftRevealText?.subtitle?.takeIf { it.isNotBlank() } ?: getString(R.string.gift_reveal_tap_hint)

                            val showAnimation = if (isBookFlow && achievedMilestone != null && referralCount > 0) {
                                val currentCoins = achievedMilestone.coins
                                val lastShownMilestone = MyApplication.getStringData(PreferenceKeys.MILESTONE_ANIMATION_LAST_SHOWN.key)?.toIntOrNull() ?: 0
                                currentCoins > lastShownMilestone
                            } else {
                                false
                            }

                            val currentMilestoneCoins = achievedMilestone?.coins ?: 0
                            val coinsPerReferral = data?.coinsCard?.coinsPerReferral ?: 50

        navigatedAway = true
        startActivity(
            Intent(this@ReferAndEarnActivity, ReferBookSelectionActivity::class.java).apply {
                putExtra(ReferBookSelectionActivity.EXTRA_ANIMATION_TITLE, animationTitle)
                putExtra(ReferBookSelectionActivity.EXTRA_ANIMATION_SUB_TITLE, animationSubTitle)
                putExtra(ReferBookSelectionActivity.EXTRA_SHOW_ANIMATION, showAnimation)
                putExtra(ReferBookSelectionActivity.EXTRA_MILESTONE_COINS, currentMilestoneCoins)

            }
        )
    }


    // OPEN_PRO_PLAN
    private fun openProPlan() {
        navigatedAway = true
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_screen_pro_plan_btn_click.actionName)
        startActivity(Intent(this@ReferAndEarnActivity, ProPlanSubscriptionActivity::class.java).apply {
            putExtra(ProPlanSubscriptionActivity.BUNDLE_COIN_FLOW, true)
        })
    }


    private fun openScreenFromButton(button: ActionButton) {
        val action = button.action ?: return
        val className = action.activity?.takeIf { it.isNotBlank() } ?: return
        try {
            val intent = Intent(this, Class.forName(className))
            val params = action.params?.takeIf { it.isNotBlank() }?.let { JSONObject(it) }
            params?.keys()?.forEach { key ->
                when (val value = params.get(key)) {
                    is Boolean -> intent.putExtra(key, value)
                    is Int -> intent.putExtra(key, value)
                    is Long -> intent.putExtra(key, value)
                    is Double -> intent.putExtra(key, value)
                    else -> intent.putExtra(key, value.toString())
                }
            }
            navigatedAway = true
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("ReferAndEarnActivity", "Cannot open '$className'", e)
        }
    }


    //share link
    private fun shareReferral(state: ArivihanScreenState<ReferAndEarnScreenData>) {
        val data = (state as? ArivihanScreenState.Content)?.data ?: return
        val shareData = data.share ?: return
        val inviteText = buildString {
            append(shareData.text)
            append(shareData.link)
        }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, inviteText)
        }
        startActivity(Intent.createChooser(sendIntent, getString(R.string.referYourFriend)))
    }

}










@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferAndEarnScreen(
    screenState: ArivihanScreenState<ReferAndEarnScreenData>,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onOpenReferralHistory: () -> Unit,
    onMonthWinnerViewMoreClick: () -> Unit,
    onCartOpen: () -> Unit,
    onButtonClick: (ActionButton) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var videoPlayerState by remember {
        mutableStateOf(VideoPlayerState())
    }


    val scope = rememberCoroutineScope()



    Box(modifier = Modifier.fillMaxSize()) {

        ArivihanScreenStateContainer(
            state = screenState,
            onRetry = onRefresh,
            // Keep connectivity feedback above the fixed action row and navigation bar.
            snackbarModifier = Modifier
                .padding(bottom = 84.dp),
            loadingContent = {
                ReferAndEarnLoadingContent(onBack = onBack)
            }
        ) { data ->
            //is cart shown previously
            val isShowPrevious = MyApplication.getBoolData(PreferenceKeys.REFER_AND_EARN_CART_SHOW_PREVIOUSLY.key)

            //save after count increase
            if ((data.cartBadge ?: 0) > 0) MyApplication.getBoolData(PreferenceKeys.REFER_AND_EARN_CART_SHOW_PREVIOUSLY.key)


            //show mini video player
            LaunchedEffect(Unit) {
                val isVideoPlayerShown = MyApplication.getBoolData(PreferenceKeys.REFER_AND_EARN_MINI_VIDEO_SHOW.key)
                if (isVideoPlayerShown) return@LaunchedEffect //already show

                //save
                MyApplication.saveData(PreferenceKeys.REFER_AND_EARN_MINI_VIDEO_SHOW.key, true)

                if (data.video == null) return@LaunchedEffect //null
                if (data.video.url.isEmpty()) return@LaunchedEffect //empty

                //show mini player
                videoPlayerState = videoPlayerState.copy(
                    startingStage = MiniVideoPlayerStage.MINI_PLAYER,
                    showMiniPlayer = true,
                    videoUrl = data.video.url,
                    videoThumbnailUrl = data.video.thumbnailUrl
                )
            }


            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                // The hero artwork is edge-to-edge. Insets are applied only to the controls that
                // must stay clear of the status/navigation bars below.
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .navigationBarsPadding()
                            .heightIn(min = 84.dp)
                            .zIndex(10f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                                if (data.flowType != "COIN_ONLY") {
                                    Box(
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable(onClick = {
                                                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_book_order_btn_click.actionName)
                                                    onCartOpen()
                                                }),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.5.dp, Color(0xFFE5F2F4)),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color.White
                                            ),
                                            elevation = CardDefaults.cardElevation(0.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_cart),
                                                    contentDescription = null,
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        if ((data.cartBadge ?: 0) > 0 || isShowPrevious) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = (-8).dp, y = (8).dp)
                                                    .background(Color.Red, CircleShape)
                                                    .border(1.5.dp, Color.Red, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = data.cartBadge.toString(),
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(18.dp))
                                }

                                Button(
                                    onClick = {
                                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_screen_share_btn_click.actionName)
                                        onShare()},
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2E98AC)
                                    )
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.share),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )

                                    Spacer(Modifier.width(16.dp))

                                    Text(
                                        text = stringResource(R.string.refer_share_karo_aur_jeeto),
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                        }
                    }
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState),
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF90E1EE),
                                        Color(0xFFFFFFFF)
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp),
                        ) {


                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_screen_back_btn_click.actionName)
                                        onBack()},
                                    modifier = Modifier.offset(x = (-10).dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_back_new),
                                        contentDescription = "Back",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Unspecified
                                    )
                                }
                            }


                            data.poster?.let { poster ->
                                AsyncImage(
                                    url = poster.imageUrl,
                                    modifier = Modifier.fillMaxWidth(),
                                    contentScale = ContentScale.FillWidth
                                )
                            }

                            data.let { screenData ->
                                screenData.coinsCard?.let { coinsCard ->
                                    screenData.progress?.let { progress ->

                                        if (progress.milestones.isNotEmpty()) {
                                            val milestones = progress.milestones.map { milestone ->

                                                val milestoneIcon = if (milestone.imageUrl?.isEmpty() ?: true) {
                                                    null
                                                } else {
                                                    if (milestone.achieved) {
                                                        R.drawable.refer_coin
                                                    } else {
                                                        R.drawable.refer_coin_grey
                                                    }
                                                }

                                                CoinMilestone(
                                                    point = milestone.coins,
                                                    rewardType = milestone.rewardType,
                                                    milestoneIcon = milestoneIcon,
                                                    milestoneBanner = milestone.imageUrl,
                                                    isUnlocked = milestone.achieved
                                                )
                                            }

                                            RewardProgressCard(
                                                modifier = Modifier.fillMaxWidth(),
                                                title = coinsCard.title,
                                                totalCoins = coinsCard.totalCoins,
                                                coinIcon = painterResource(R.drawable.refer_coin),
                                                milestones = milestones,
                                                onInfoClick = {
                                                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_card_info_btn_click.actionName)
                                                    scope.launch {
                                                        scrollState.animateScrollTo(scrollState.maxValue)
                                                    }
                                                },
                                                buttons = screenData.buttons,
                                                onButtonClick = onButtonClick
                                            )
                                        } else {
                                            Log.d("ReferAndEarnActivity", "No milestones available, skipping progress card")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        data.video?.takeIf { it.url.isNotEmpty() }?.let { video ->
                            FreeRewardsBanner(
                                modifier = Modifier,
                                title = video.title,
                                description = video.subtitle,
                                buttonText = stringResource(R.string.watch),
                                haveProPlan = SubscriptionType.isProSubscription(),
                                onWatchClick = {
                                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_free_reward_watch_video_btn_click.actionName)
                                    videoPlayerState = videoPlayerState.copy(
                                        startingStage = MiniVideoPlayerStage.FULL_SCREEN,
                                        showMiniPlayer = true,
                                        videoUrl = video.url,
                                        videoThumbnailUrl = video.thumbnailUrl
                                    )
                                }
                            )
                        }

                        val invitedFriends = data.invitedFriends?.friends.orEmpty()

                        //if have invited
                        if (invitedFriends.isNotEmpty()) {
                            ReferralHistorySection(
                                friendsReferrals = invitedFriends,
                                onViewMoreClick = {
                                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_referral_history_view_more_click.actionName)
                                    onOpenReferralHistory()}
                            )
                        }

                        //monthly winners
                        data.monthlyWinners?.let { monthlyWinnersData ->
                            val winners =
                                monthlyWinnersData.winners.take(3).mapIndexed { index, winner ->
                                    Winner(
                                        name = winner.displayName,
                                        avatar = R.drawable.avatar,
                                        ringColor = when (index) {
                                            0 -> colorResource(R.color.ring_gold)
                                            1 -> colorResource(R.color.ring_silver)
                                            2 -> colorResource(R.color.ring_bronze)
                                            else -> Color.Gray
                                        },
                                        illustration = winner.illustration,
                                        coins = winner.coins,
                                        avatarUrl = winner.avatarUrl
                                    )
                                }

                            MonthlyWinnersCard(
                                winnersImage = painterResource(R.drawable.monthly_winners_group),
                                subTitle = monthlyWinnersData.tagline,
                                winners = winners,
                                onViewMoreClick = {
                                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_monthly_winners_view_more_click.actionName)
                                    onMonthWinnerViewMoreClick()
                                }
                            )
                        }

                        data.infoSection?.let { infoSection ->
                            FreeBooksInfoCard(
                                modifier = Modifier.fillMaxWidth(),
                                infoSection = infoSection
                            )
                        }

                        data.callbackCard?.let { callbackCard ->
                            TalkToUsCard(
                                title = callbackCard.title,
                                subTitle = callbackCard.subTitle,
                                phoneNumber = callbackCard.phone,
                                onPhoneClick = {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, "tel:${callbackCard.phone}".toUri()))
                                }
                            )
                        }

                    }

                    Spacer(Modifier.height(16.dp))

                }
            }

        }


        //mini video player
        if (videoPlayerState.showMiniPlayer && videoPlayerState.videoUrl.isNotEmpty()) {
            MiniVideoPlayer(
                config = MiniVideoPlayerConfig(
                    startingStage = videoPlayerState.startingStage,
                    widthDp = 150,
                    heightDp = 0,
                    videoUrl = videoPlayerState.videoUrl,
                    thumbnailUrl = videoPlayerState.videoThumbnailUrl,
                    showCloseButton = true,
                    showVolumeButton = true,
                    showResizeButton = true
                ),
                onClose = {
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_video_swipe_back_btn_click.actionName)
                    videoPlayerState = VideoPlayerState()
                },
                onComplete = {
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_video_watch_successfully.actionName)
                    videoPlayerState = VideoPlayerState()
                }
            )
        }





    }
}


@Composable
private fun ReferAndEarnLoadingContent(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF90E1EE),
                            Color(0xFFFFFFFF)
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_back_btn_click.actionName)
                            onBack()},
                        modifier = Modifier.offset(x = (-10).dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_new),
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
                TopHeaderImageSkeleton()
                Spacer(Modifier.height(8.dp))
                RewardProgressCardSkeleton()
            }
        }
        Spacer(Modifier.height(16.dp))
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            FreeRewardBannerSkeleton()
            Spacer(Modifier.height(16.dp))
            ReferralHistorySkeleton()
            Spacer(Modifier.height(16.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        BottomBarSkeleton()
    }
}


@Composable
fun FreeRewardsBanner(
    title: String,
    description: String,
    onWatchClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonText: String,
    haveProPlan: Boolean,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onWatchClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
        ) {

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(80.dp)
                    .offset(x = 20.dp, y = (-20).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF26C6DA).copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(width = 72.dp, height = 56.dp)
                        .offset(y = (-4).dp),
                    contentAlignment = Alignment.TopCenter
                ) {

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .size(width = 72.dp, height = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE0F7FA))
                            .border(
                                1.dp,
                                colorResource(R.color.arivihan_secondary_color),
                                RoundedCornerShape(12.dp)
                            )
                    )

                    Image(
                        painter = painterResource(R.drawable.ritesh_sir_refer),
                        contentDescription = null,
                        modifier = Modifier
                            .size(74.dp)
                            .zIndex(1f)
                    )

                    Icon(
                        painter = painterResource(R.drawable.ic_play_video),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 8.dp)
                            .zIndex(1f)
                            .alpha(0.8f)
                            .size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                Button(
                    onClick = {
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_watch_video_btn_click.actionName)
                        onWatchClick()},
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF138D97)), // Teal color
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = buttonText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}











@Composable
fun FreeBooksInfoCard(
    modifier: Modifier = Modifier,
    infoSection: InfoSection
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {

            Text(
                text = infoSection.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                ),
            )

            Spacer(modifier = Modifier.height(26.dp))

            val steps = infoSection.tips

            steps.forEachIndexed { index, text ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier,
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .zIndex(1f)
                                    .background(Color(0xFFFFF3EB), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (index + 1).toString(),
                                    color = Color(0xFFFF6B1A),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            if (index != steps.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .offset(y = 14.dp)
                                        .width(7.dp)
                                        .height(22.dp)
                                        .background(Color(0xFFFFF3EB))
                                )
                            }
                            if (index != 0) {
                                Box(
                                    modifier = Modifier
                                        .offset(y = -14.dp)
                                        .width(7.dp)
                                        .height(22.dp)
                                        .background(Color(0xFFFFF3EB))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = text,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            ),
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF666666)
                        )
                    }

                    if (index != steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .padding(start = 10.5.dp)
                                .width(7.dp)
                                .height(22.dp)
                                .background(Color(0xFFFFF3EB))
                        )
                    }
                }
            }
        }
    }
}




@Composable
fun TalkToUsCard(
    title: String,
    subTitle: String,
    phoneNumber: String,
    modifier: Modifier = Modifier,
    onPhoneClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(R.drawable.talk_to_us_image),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(130.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subTitle,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .border(
                            1.dp,
                            colorResource(R.color.arivihan_secondary_color),
                            RoundedCornerShape(24)
                        )
                        .padding(10.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_talk_to_us_btn_click.actionName)
                                onPhoneClick()}
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        painter = painterResource(R.drawable.call_back),
                        contentDescription = null,
                        tint = Color(0xFF26C6DA),
                        modifier = Modifier.size(12.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = if (phoneNumber.startsWith("91") && phoneNumber.length > 10) {
                            "+91-${phoneNumber.substring(phoneNumber.length - 10)}"
                        } else if (phoneNumber.length == 10) {
                            "+91-$phoneNumber"
                        } else {
                            phoneNumber
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF26C6DA)
                        )
                    )
                }
            }
        }
    }
}
