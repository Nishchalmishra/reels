package arivihan.technologies.doubtbuzzter2.compose.component

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.ui.PlayerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.activity.refer.MiniVideoPlayerStage
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents
import arivihan.technologies.doubtbuzzter2.model.player.MiniVideoPlayerConfig
import kotlin.math.roundToInt




@OptIn(UnstableApi::class)
@Composable
fun MiniVideoPlayer(
    modifier: Modifier = Modifier,
    config: MiniVideoPlayerConfig,
    onClose: () -> Unit,
    onComplete: () -> Unit = {},
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    var isVisible by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var isFullScreen by remember { mutableStateOf(config.startingStage == MiniVideoPlayerStage.FULL_SCREEN) }
    var isLoading by remember { mutableStateOf(true) }

    var playerWidth by remember { mutableStateOf(0) }
    var playerHeight by remember { mutableStateOf(0) }
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

    val initialWidth = with(density) { if (config.widthDp > 0) config.widthDp.dp.toPx() else screenWidth / 3f }
    val initialHeight = with(density) { if (config.heightDp > 0) config.heightDp.dp.toPx() else initialWidth * (16f / 9f) }

    val miniPadding = with(density) { 16.dp.toPx() }
    val fullScreenPadding = with(density) { 0.dp.toPx() }


    val fullScreenWidth = screenWidth - (fullScreenPadding * 2)
    val fullScreenHeight = screenHeight - (fullScreenPadding * 2)

    var offsetX by remember { mutableStateOf(screenWidth - initialWidth - miniPadding) }
    var offsetY by remember { mutableStateOf(screenHeight - initialHeight - miniPadding) }

    val exoPlayer = remember { createExoPlayer(context) }


    val animatedWidth by animateFloatAsState(
        targetValue = if (isFullScreen) fullScreenWidth else initialWidth,
        animationSpec = tween(300),
        label = "width"
    )

    val animatedHeight by animateFloatAsState(
        targetValue = if (isFullScreen) fullScreenHeight else initialHeight,
        animationSpec = tween(300),
        label = "height"
    )

    // Animate position as well
    val animatedOffsetX by animateFloatAsState(
        targetValue = if (isFullScreen) fullScreenPadding else offsetX,
        animationSpec = tween(300),
        label = "offsetX"
    )

    val animatedOffsetY by animateFloatAsState(
        targetValue = if (isFullScreen) fullScreenPadding else offsetY,
        animationSpec = tween(300),
        label = "offsetY"
    )



    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(config.videoUrl) {
        setupPlayer(
            exoPlayer = exoPlayer,
            videoUrl = config.videoUrl,
            context = context,
            onReady = { isLoading = false },
            onComplete = onComplete
        )
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.playWhenReady = false
                Lifecycle.Event.ON_RESUME -> exoPlayer.playWhenReady = true
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }



    if (!isVisible) return

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    animatedOffsetX.roundToInt(),
                    animatedOffsetY.roundToInt()
                )
            }
            .width(with(density) { animatedWidth.toDp() })
            .height(with(density) { animatedHeight.toDp() })
            .zIndex(10f)
            .pointerInput(isFullScreen) {
                if (!isFullScreen) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newX = (offsetX + dragAmount.x).coerceIn(
                            fullScreenPadding,
                            screenWidth - initialWidth - fullScreenPadding
                        )
                        val newY = (offsetY + dragAmount.y).coerceIn(
                            fullScreenPadding,
                            screenHeight - initialHeight - fullScreenPadding
                        )
                        offsetX = newX
                        offsetY = newY
                    }
                }
            }
            .onSizeChanged {
                playerWidth = it.width
                playerHeight = it.height
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!isFullScreen) Modifier.border(2.dp, colorResource(R.color.arivihan_secondary_color), RoundedCornerShape(12.dp))
                    else Modifier
                ),
            shape = if (!isFullScreen) RoundedCornerShape(12.dp) else RoundedCornerShape(0.dp),
            elevation = if (!isFullScreen) CardDefaults.cardElevation(6.dp) else CardDefaults.cardElevation(0.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center),
                        color = Color(0xFF26C6DA)
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (config.showVolumeButton) {
                        ControlButton(
                            iconRes = if (isMuted) R.drawable.media3_icon_volume_down else R.drawable.media3_icon_volume_off,
                            onClick = {
                                val activityParams = HashMap<String, String>()
                                activityParams["is_muted"] = isMuted.toString()
                                MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_video_volume_btn_click.actionName,activityParams)
                                isMuted = !isMuted
                                exoPlayer.volume = if (isMuted) 0f else 1f
                            }
                        )
                    }

                    if (config.showResizeButton) {
                        ControlButton(
                            iconRes = if (isFullScreen) R.drawable.minimize_video else R.drawable.maximize_video,
                            onClick = {
                                val activityParams = HashMap<String, String>()
                                activityParams["is_fullscreen"] = isFullScreen.toString()
                                MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_video_resize_btn_click.actionName,activityParams)
                                isFullScreen = !isFullScreen

                                if (!isFullScreen) {
                                    offsetX = screenWidth - initialWidth - miniPadding
                                    offsetY = screenHeight - initialHeight - miniPadding
                                }
                            }
                        )
                    }

                    if (config.showCloseButton) {
                        ControlButton(
                            iconRes = R.drawable.__icon__close_,
                            onClick = {
                                MyApplication.logFirebaseBundleCreation(FirebaseEvents.raw_video_close_btn_click.actionName)
                                isVisible = false
                                onClose()
                            }
                        )
                    }
                }
            }
        }
    }
}




@Composable
private fun ControlButton(
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            .size(32.dp)
            .clip(CircleShape)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}




@OptIn(UnstableApi::class)
private fun createExoPlayer(context: Context): ExoPlayer {
    val renderersFactory = DefaultRenderersFactory(context)
        .setEnableDecoderFallback(true)
        .setMediaCodecSelector(MediaCodecSelector.DEFAULT)

    return ExoPlayer.Builder(context, renderersFactory).build()
}



@OptIn(UnstableApi::class)
private fun setupPlayer(
    exoPlayer: ExoPlayer,
    videoUrl: String,
    context: Context,
    onReady: () -> Unit,
    onComplete: () -> Unit
) {
    val dataSourceFactory = DefaultDataSource.Factory(context)
    val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
        .createMediaSource(MediaItem.fromUri(videoUrl))

    exoPlayer.setMediaSource(mediaSource)
    exoPlayer.prepare()
    exoPlayer.playWhenReady = true

    exoPlayer.addListener(object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> onReady()
                Player.STATE_ENDED -> onComplete()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            onComplete()
        }
    })
}
