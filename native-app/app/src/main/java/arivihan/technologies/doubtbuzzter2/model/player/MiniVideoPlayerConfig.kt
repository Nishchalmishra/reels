package arivihan.technologies.doubtbuzzter2.model.player

import android.os.Parcel
import android.os.Parcelable
import arivihan.technologies.doubtbuzzter2.activity.refer.MiniVideoPlayerStage
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class MiniVideoPlayerConfig(
    val startingStage: MiniVideoPlayerStage = MiniVideoPlayerStage.MINI_PLAYER,
    val widthDp: Int = 0,
    val heightDp: Int = 0,
    val videoUrl: String = "",
    val thumbnailUrl: String? = null,
    val showCloseButton: Boolean = true,
    val showVolumeButton: Boolean = true,
    val showResizeButton: Boolean = true,
    val marginStartDp: Int = 0,
    val marginTopDp: Int = 0,
    val marginEndDp: Int = 0,
    val marginBottomDp: Int = 0,
    val gravity: MiniPlayerPosition? = null
) : Parcelable, Serializable {

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }

    class Builder {
        private var widthDp: Int = 0
        private var heightDp: Int = 0
        private var videoUrl: String = ""
        private var thumbnailUrl: String? = null
        private var showCloseButton: Boolean = true
        private var showVolumeButton: Boolean = true
        private var showResizeButton: Boolean = true
        private var marginStartDp: Int = 0
        private var marginTopDp: Int = 0
        private var marginEndDp: Int = 0
        private var marginBottomDp: Int = 0
        private var gravity: MiniPlayerPosition? = null

        fun widthDp(widthDp: Int) = apply { this.widthDp = widthDp }
        fun heightDp(heightDp: Int) = apply { this.heightDp = heightDp }
        fun videoUrl(videoUrl: String) = apply { this.videoUrl = videoUrl }
        fun thumbnailUrl(thumbnailUrl: String?) = apply { this.thumbnailUrl = thumbnailUrl }
        fun showCloseButton(showCloseButton: Boolean) = apply { this.showCloseButton = showCloseButton }
        fun showVolumeButton(showVolumeButton: Boolean) = apply { this.showVolumeButton = showVolumeButton }
        fun showResizeButton(showResizeButton: Boolean) = apply { this.showResizeButton = showResizeButton }
        fun marginStartDp(marginStartDp: Int) = apply { this.marginStartDp = marginStartDp }
        fun marginTopDp(marginTopDp: Int) = apply { this.marginTopDp = marginTopDp }
        fun marginEndDp(marginEndDp: Int) = apply { this.marginEndDp = marginEndDp }
        fun marginBottomDp(marginBottomDp: Int) = apply { this.marginBottomDp = marginBottomDp }
        fun gravity(gravity: MiniPlayerPosition?) = apply { this.gravity = gravity }

        fun build() = MiniVideoPlayerConfig(
            widthDp = widthDp,
            heightDp = heightDp,
            videoUrl = videoUrl,
            thumbnailUrl = thumbnailUrl,
            showCloseButton = showCloseButton,
            showVolumeButton = showVolumeButton,
            showResizeButton = showResizeButton,
            marginStartDp = marginStartDp,
            marginTopDp = marginTopDp,
            marginEndDp = marginEndDp,
            marginBottomDp = marginBottomDp,
            gravity = gravity
        )
    }

}
