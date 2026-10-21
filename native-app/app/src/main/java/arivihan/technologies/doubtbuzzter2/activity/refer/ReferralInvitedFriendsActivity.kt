package arivihan.technologies.doubtbuzzter2.activity.refer

import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity
import arivihan.technologies.doubtbuzzter2.compose.ArivihanComposeApp
import arivihan.technologies.doubtbuzzter2.compose.ArivihanWindowInsets
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanTopAppBar
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.InviteFriendCard
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.ReferralsListShimmer
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.toUiModel
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents
import arivihan.technologies.doubtbuzzter2.model.refer.uistate.FriendsReferralUiState
import arivihan.technologies.doubtbuzzter2.viewModel.ReferAndEarnViewModel

class ReferralInvitedFriendsActivity : BaseActivity(), EdgeToEdgeHelper.SelfManagedInsets {

    private lateinit var viewModel: ReferAndEarnViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[ReferAndEarnViewModel::class.java]

        MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_referral_invite_friend_screen_view.actionName)


        viewModel.loadReferralHistoryPaginated()

        setContent {
            ArivihanComposeApp(enabled = true) {
                val referralHistoryUiState by viewModel.friendsReferralUiState.observeAsState(
                    FriendsReferralUiState(loading = true)
                )

                ReferralInvitedFriendsScreen(
                    uiState = referralHistoryUiState,
                    onBack = {
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_referal_invite_screen_back_btn_click.actionName)
                        finish()
                     },
                    onLoadMore = {
                        viewModel.loadMoreReferralHistory()
                    }
                )
            }
        }
    }

}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReferralInvitedFriendsScreen(
    uiState: FriendsReferralUiState,
    onBack: () -> Unit,
    onLoadMore: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }

    LaunchedEffect(listState, uiState.hasMore) {
        val analyticsData = HashMap<String, String>()
        // analyticsData["flow_type"] = "books" // or "coins" dynamically
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_invited_dost_screen_view.actionName, analyticsData)
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }.collect { lastVisibleIndex ->
            if (
                lastVisibleIndex != null &&
                lastVisibleIndex >= uiState.friendsReferrals.size - 3 &&
                uiState.hasMore &&
                !uiState.loadingMore &&
                !uiState.loading
            ) {
                onLoadMore()
            }
        }
    }



    Scaffold(
        contentWindowInsets = ArivihanWindowInsets.Default,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ArivihanTopAppBar(
                title = stringResource(R.string.aapke_invited_dost),
                onBackClick = {
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_dost_back_btn_click.actionName)
                    onBack()
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.loading -> {
                    ReferralsListShimmer()
                }
                uiState.error != null -> {
                    ErrorMessage(
                        message = uiState.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.friendsReferrals.isEmpty() -> {
                    EmptyState(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            items = uiState.friendsReferrals,
                            key = { index, _ -> index }
                        ) { index, dto ->
                            val friend = dto.toUiModel(context)
                            val hasSteps = friend.steps.isNotEmpty()
                            val expanded = expandedStates[index] ?: false

                            InviteFriendCard(
                                referral = friend,
                                referralSteps = friend.steps,
                                expanded = hasSteps && expanded,
                                onExpandClick = {
                                    if (hasSteps) {
                                        val newState = !expanded
                                        expandedStates[index] = newState
                                        val analyticsData = HashMap<String, String>()
                                        analyticsData["action"] =
                                            if (newState) "expand"
                                            else "collapse"
                                        analyticsData["flow_type"] = "books"
//                                      analyticsData["referral_status"] =
//                                          when {
//                                              dto.isExpired -> "expired"
//                                              dto.isCompleted -> "successful"
//                                              else -> "pending"
//                                          }
                                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_friend_card_expand_click.actionName, analyticsData)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }

                        if (uiState.loadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}






@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_pro_premium_star),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.Gray.copy(alpha = 0.5f)
        )
        Text(
            text = stringResource(R.string.no_invites_yet),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            ),
            color = Color.Black
        )
        Text(
            text = stringResource(R.string.start_inviting_friends),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp
            ),
            color = Color.Gray
        )
    }
}



@Composable
private fun ErrorMessage(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Oops!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF666666)
        )
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color(0xFF999999)
        )
    }
}
