package arivihan.technologies.doubtbuzzter2.activity.refer

import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.MyApplication.logFirebaseBundleCreation
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity
import arivihan.technologies.doubtbuzzter2.compose.ArivihanComposeApp
import arivihan.technologies.doubtbuzzter2.compose.ArivihanWindowInsets
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanTopAppBar
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.Winner
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.WinnerPodiumView
import arivihan.technologies.doubtbuzzter2.compose.component.referandearn.WinnersListShimmer
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents
import arivihan.technologies.doubtbuzzter2.model.refer.MonthlyWinner
import arivihan.technologies.doubtbuzzter2.model.refer.uistate.LeaderboardState
import arivihan.technologies.doubtbuzzter2.model.refer.uistate.MonthlyWinnersUiState
import arivihan.technologies.doubtbuzzter2.viewModel.ReferAndEarnViewModel


class MonthlyWinnersActivity : BaseActivity(), EdgeToEdgeHelper.SelfManagedInsets {

    private lateinit var viewModel: ReferAndEarnViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(R.color.background)
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_monthly_winners_screen_view.actionName)
        viewModel = ViewModelProvider(this)[ReferAndEarnViewModel::class.java]


        viewModel.loadLeaderboard("global", refresh = true)
        setContent {
            val leaderboardState by viewModel.leaderboardState.observeAsState(LeaderboardState())
            val uiState by viewModel.monthlyWinnersState.observeAsState(MonthlyWinnersUiState())

            ArivihanComposeApp(enabled = true) {
                MonthlyWinnersScreen(
                    leaderboardState = leaderboardState,
                    uiState = uiState,
                    onBack = {
                        logFirebaseBundleCreation(FirebaseEvents.raw_monthly_winner_screen_back_btn_click.actionName)
                        finish() },
                    onLoadMore = {
                        viewModel.loadMoreLeaderboard("global")
                    }
                )
            }
        }
    }



}







@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthlyWinnersScreen(
    leaderboardState: LeaderboardState,
    uiState: MonthlyWinnersUiState,
    onBack: () -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()

    val leaderboardData = leaderboardState.data
    val self = leaderboardData?.self

    val selfData = self?.let {
        MonthlyWinner(
            id = it.rank.toString(),
            rank = it.rank,
            name = it.displayName,
            coins = it.coins,
            referralCount = it.referrals,
            isCurrentUser = it.you,
            illustration = it.illustration,
            avatarUrl = it.avatarUrl
        )
    }

    val useNewApi = leaderboardData != null && leaderboardData.winners.isNotEmpty()

    val winners = if (useNewApi) {
        leaderboardData.winners.map { winner ->
            MonthlyWinner(
                id = winner.rank.toString(),
                rank = winner.rank,
                name = winner.displayName,
                coins = winner.coins,
                referralCount = winner.referrals,
                isCurrentUser = winner.you,
                illustration = winner.illustration,
                avatarUrl = winner.avatarUrl
            )
        }
    } else {
        uiState.winners
    }


    val hasMore = if (useNewApi) leaderboardData.hasNext else uiState.hasMore
    val loading = if (useNewApi) leaderboardState.loading else uiState.loading
    val loadingMore = if (useNewApi) leaderboardState.loadingMore else uiState.loadingMore
    val error = if (useNewApi) leaderboardState.error else uiState.error


    val topThreeWinners = winners.take(3).mapIndexed { index, winner ->
        Winner(
            name = winner.name,
            avatar = R.drawable.avatar,
            ringColor = when (index) {
                0 -> colorResource(R.color.ring_gold)
                1 -> colorResource(R.color.ring_silver)
                2 -> colorResource(R.color.ring_bronze)
                else -> Color.Gray
            },
            illustration = winner.illustration ?: "boy",
            coins = winner.coins,
            avatarUrl = winner.avatarUrl
        )
    }


    val reachedEnd by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= totalItems - 3
        }
    }

    LaunchedEffect(reachedEnd, hasMore, loadingMore, loading) {
        if (reachedEnd && hasMore && !loadingMore && !loading) {
            onLoadMore()
        }
    }

    val isUserVisible by remember {
        derivedStateOf {
            val visibleIndices = listState.layoutInfo.visibleItemsInfo.map { it.index }
            val userIndex = winners.indexOfFirst { it.isCurrentUser }
            userIndex in visibleIndices
        }
    }

    Scaffold(
        contentWindowInsets = ArivihanWindowInsets.Default,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ArivihanTopAppBar(
                title = stringResource(R.string.monthly_winners),
                onBackClick =  onBack
            )
        },
        bottomBar = {
            if (selfData != null && !isUserVisible) {
                TopRoundedCard(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth(),
                    content = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(all = 16.dp)
                        ) {
                            WinnerListItem(
                                winner = selfData
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                loading -> {
                    WinnersListShimmer()
                }

                error != null -> {
                    ErrorMessage(
                        message = error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                winners.isNotEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .offset(
                                        y = ((30).dp)
                                    ),
                            ) {
                                WinnerPodiumView(
                                    winners = topThreeWinners
                                )
                            }

                            TopRoundedCard(
                                modifier = Modifier
                                    ,
                                content = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ) {
                                        //list header
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(all = 16.dp),
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.rank),
                                                    modifier = Modifier.width(60.dp),
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        color = Color(0xFF666666),
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    textAlign = TextAlign.Center
                                                )

                                                Text(
                                                    text = stringResource(R.string.name),
                                                    modifier = Modifier.weight(1f),
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        color = Color(0xFF666666),
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                )

                                                Text(
                                                    text = stringResource(R.string.referrals),
                                                    modifier = Modifier.width(90.dp),
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        color = Color(0xFF666666),
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }


                                        //list
                                        LazyColumn(
                                            state = listState,
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(
                                                horizontal = 16.dp
                                            ),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(winners) { winner ->
                                                WinnerListItem(
                                                    winner = winner,
                                                    isCurrentUser = winner.isCurrentUser
                                                )
                                            }

                                            if (loadingMore) {
                                                item {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(16.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(
                                                                24.dp
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
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
private fun WinnerListItem(
    winner: MonthlyWinner,
    isCurrentUser: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${winner.rank}",
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (winner.isCurrentUser) Color(0xFfFF6D24) else Color.Black
                )
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${winner.name}${if (winner.isCurrentUser) " (${stringResource(R.string.you_)})" else ""}",
                    fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = if (winner.isCurrentUser) Color(0xFfFF6D24) else Color.Black
                )
            }

            Row(
                modifier = Modifier.width(90.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.refer_coin),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = winner.coins.toString(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
        }
    }
}






@Composable
private fun ErrorMessage(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Oops!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            fontSize = 14.sp,
            color = Color(0xFF999999)
        )
    }
}




@Composable
fun TopRoundedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp
                    ),
                    clip = false
                )
                .align(Alignment.TopCenter)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp
            ),
            color = Color.White,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}