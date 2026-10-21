package arivihan.technologies.doubtbuzzter2.activity.profile

import android.content.Intent
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import arivihan.technologies.doubtbuzzter2.compose.ArivihanComposeApp
import arivihan.technologies.doubtbuzzter2.compose.component.profile.ProfileHeaderComponent

object ProfileComposeHelper {
    @JvmStatic
    fun setProfileHeader(
        composeView: ComposeView,
        fragment: Fragment,
        username: String?,
        isSubscribed: Boolean,
        isNeet: Boolean,
        logoUrl: String?,
        tags: List<String>
    ) {
        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ArivihanComposeApp {
                    ProfileHeaderComponent(
                        username = username,
                        isSubscribed = isSubscribed,
                        isNeet = isNeet,
                        logoUrl = logoUrl,
                        tags = tags,
                        onHeaderClick = {
                            val intent = Intent(fragment.requireContext(), AcademicDetailsActivity::class.java)
                            fragment.startActivity(intent)
                        },
                        onEditNameClick = {
                            val intent = Intent(fragment.requireContext(), arivihan.technologies.doubtbuzzter2.common.NameInputActivity::class.java)
                            fragment.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
