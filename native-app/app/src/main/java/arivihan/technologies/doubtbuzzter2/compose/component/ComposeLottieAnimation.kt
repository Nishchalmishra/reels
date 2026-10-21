package arivihan.technologies.doubtbuzzter2.compose.component

import androidx.annotation.RawRes
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import com.airbnb.lottie.compose.*

@Composable
fun CommonLottieAnimation(
    @RawRes animationRes: Int,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    iterations: Int = LottieConstants.IterateForever,
    alignment: Alignment = Alignment.Center,
    speed: Float = 1f
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(animationRes)
    )

    Box(
        modifier = modifier,
        contentAlignment = alignment
    ) {
        LottieAnimation(
            composition = composition,
            isPlaying = isPlaying,
            iterations = iterations,
            speed = speed
        )
    }
}