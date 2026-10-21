package arivihan.technologies.doubtbuzzter2.toppernotes.presentation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.activity.MyDownloadActivity
import arivihan.technologies.doubtbuzzter2.compose.ArivihanComposeApp
import arivihan.technologies.doubtbuzzter2.model.ThemeEnum
import arivihan.technologies.doubtbuzzter2.common.CommonMethods
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource
import arivihan.technologies.doubtbuzzter2.enums.SubscriptionPopupType
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionType
import arivihan.technologies.doubtbuzzter2.listener.SubscriptionCallback
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum
import arivihan.technologies.doubtbuzzter2.model.SubscriptionCheckDto
import arivihan.technologies.doubtbuzzter2.model.SubscriptionLockPopupDto
import com.google.gson.Gson
import java.io.Serializable
import org.json.JSONObject
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity
import arivihan.technologies.doubtbuzzter2.viewModel.NotesViewModel
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.model.Topper
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.model.TopperNote
import arivihan.technologies.doubtbuzzter2.toppernotes.presentation.components.TopperNotesScreen
import arivihan.technologies.doubtbuzzter2.utils.PdfUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * "Topper Ke Notes", built with Compose.
 *
 * The bottom navigation is hidden while this screen is on top and restored when it
 * goes away, so the fragment behaves the same whether it is shown inside
 * [arivihan.technologies.doubtbuzzter2.activity.HomeScreen] or in its own activity.
 *
 * Supports dark theme for PRO toppers with a circular reveal animation matching
 * [arivihan.technologies.doubtbuzzter2.activity.competitive.subject.chapter.ChapterListSubscribedActivity].
 */
@AndroidEntryPoint
class TopperNotesFragment : Fragment() {

    private val viewModel: TopperNotesViewModel by viewModels()

    /**
     * The offline notes table is owned by the legacy [NotesViewModel], which is a plain
     * AndroidViewModel outside the Hilt graph. Downloads go through it and [PdfUtils] so a
     * note fetched here is the same record My Downloads and the legacy list already read.
     */
    private val notesViewModel: NotesViewModel by lazy {
        MyApplication.getInstance().notesViewModel
    }

    /** Keyed by note id as a String, matching [NoteEntity.getId]. */
    private val downloadStates = mutableStateMapOf<String, NoteDownloadState>()

    /** Extras the subscription-status call wants forwarded to the plans page. */
    private var lockActivityParams: Map<String, Any>? = null

    /**
     * Compose-observable theme state. Updated either instantly (initial load) or
     * after the circular reveal animation completes (topper switch).
     */
    private var composeTheme by mutableStateOf(ThemeEnum.LIGHT)

    /**
     * True while a circular reveal animation is in progress. Prevents the
     * [observeThemeChanges] collector from applying the theme instantly when
     * [handleTopperClick] has already started an animated transition.
     */
    private var isRevealAnimationRunning = false

    private var composeView: ComposeView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).also { composeView = it }.apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.topper_ke_notes_screen_view.actionName)
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            ArivihanComposeApp(theme = composeTheme) {
                TopperNotesScreen(
                    state = state,
                    onBackClick = { requireActivity().onBackPressedDispatcher.onBackPressed() },
                    onMyDownloadsClick = ::openMyDownloads,
                    onTopperClick = ::handleTopperClick,
                    onSubjectClick = { viewModel.onSubjectSelected(it.id) },
                    onNoteClick = ::openNote,
                    onDownloadClick = ::downloadNote,
                    onRetryClick = viewModel::retry,
                    downloads = downloadStates
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notesViewModel.getAllNotes(MyApplication.getStringData(PreferenceKeys.USER_ID.key))
            .observe(viewLifecycleOwner) { entities ->
                entities?.forEach { entity ->
                    downloadStates[entity.id] = NoteDownloadState(entity.status, entity.progress)
                }
            }

        fetchSubscriptionCopy()
        observeThemeChanges()
    }

    override fun onResume() {
        super.onResume()
        setBottomNavigationVisible(false)
    }

    override fun onDestroyView() {
        composeView = null
        setBottomNavigationVisible(true)
        super.onDestroyView()
    }

    // ── Theme management ────────────────────────────────────────────────────────

    /**
     * Observes [TopperNotesUiState.isDarkTheme] to apply the theme instantly when the
     * change is NOT user-initiated (e.g. initial topper auto-selection on load).
     *
     * User-initiated changes go through [handleTopperClick] which sets
     * [isRevealAnimationRunning] before calling [TopperNotesViewModel.onTopperSelected],
     * so the collector skips those and lets the reveal animation handle the switch.
     */
    private fun observeThemeChanges() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .map { it.isDarkTheme }
                    .distinctUntilChanged()
                    .collectLatest { isDark ->
                        // Skip if handleTopperClick already started a reveal for this change
                        if (isRevealAnimationRunning) return@collectLatest

                        val targetTheme = if (isDark) ThemeEnum.DARK else ThemeEnum.LIGHT
                        if (composeTheme != targetTheme) {
                            applyThemeInstant(isDark)
                        }
                    }
            }
        }
    }

    /**
     * Applies dark/light theme instantly without animation.
     * Used on initial load when the first topper might already be PRO.
     */
    private fun applyThemeInstant(isDark: Boolean) {
        composeTheme = if (isDark) ThemeEnum.DARK else ThemeEnum.LIGHT
        val host = activity
        if (host is TopperNotesActivity) {
            host.applyThemeInstant(isDark)
        }
    }

    /**
     * Handles topper card click — checks if a theme change is needed and either
     * runs a circular reveal animation or just selects the topper.
     */
    private fun handleTopperClick(topper: Topper) {
        // Block clicks while a reveal animation is in progress
        if (isRevealAnimationRunning) return

        val currentState = viewModel.uiState.value
        val currentIsDark = currentState.isDarkTheme
        val newIsDark = topper.isPro

        // If theme change needed, set the flag BEFORE updating the ViewModel so that
        // observeThemeChanges() skips the emission triggered by onTopperSelected().
        if (currentIsDark != newIsDark) {
            isRevealAnimationRunning = true
        }

        // Always update the ViewModel selection
        viewModel.onTopperSelected(topper.code)

        // If no theme change needed, nothing more to do
        if (currentIsDark == newIsDark) return

        val host = activity
        if (host is TopperNotesActivity) {
            val displayMetrics = resources.displayMetrics
            val cx = displayMetrics.widthPixels / 2
            val density = displayMetrics.density
            // Topper cards are roughly at: header(76dp) + chips(42dp) + card center(~60dp) = ~178dp
            val cy = (178 * density).toInt()

            host.runCircularReveal(newIsDark, cx, cy) {
                isRevealAnimationRunning = false
                if (!isAdded) return@runCircularReveal
                composeTheme = if (newIsDark) ThemeEnum.DARK else ThemeEnum.LIGHT
            }
        } else {
            // Not hosted in TopperNotesActivity (e.g., HomeScreen) — switch instantly
            isRevealAnimationRunning = false
            composeTheme = if (newIsDark) ThemeEnum.DARK else ThemeEnum.LIGHT
        }
    }

    // ── Note actions ────────────────────────────────────────────────────────────

    private fun openMyDownloads() {
        startActivity(Intent(requireContext(), MyDownloadActivity::class.java))
    }

    private fun openNote(note: TopperNote) {
        if (note.locked) {
            openLockedNote(note)
            return
        }
        PdfUtils.openPdfViewer(
            requireContext(),
            note.url,
            note.title,
            note.type,
            note.subjectName,
            false,
            note.url,
            TAG,
            note.id
        )
    }

    /**
     * Mirrors the legacy `ToppersNotesActivity.onNoteDownloadClick`: insert the row first so
     * the list has something to render progress against, then hand off to [PdfUtils], which
     * owns the actual fetch and writes status/progress back to the same table.
     */
    private fun downloadNote(note: TopperNote) {
        if (note.locked || note.url.isBlank()) return

        if (!CommonMethods.isOnline(requireContext())) {
            Toast.makeText(
                requireContext(),
                getString(R.string.NoInternetConnection),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val noteId = note.id.toString()
        if (downloadStates[noteId]?.let { it.isInProgress || it.isComplete } == true) return

        val entity = NoteEntity(
            noteId,
            viewModel.uiState.value.selectedSubjectId,
            note.subjectName,
            note.title,
            note.notesBy,
            note.url,
            note.locked,
            0, 0, "pdf", NoteEntity.STATUS_PENDING, 0, null,
            MyApplication.getStringData(PreferenceKeys.MY_LANG.key)
        )
        entity.setUserId(MyApplication.getStringData(PreferenceKeys.USER_ID.key))
        entity.setNoteType(note.type)

        notesViewModel.insert(entity)
        PdfUtils.startPdfDownload(requireContext(), entity)

        // Show progress on the next frame rather than waiting for Room to round-trip.
        downloadStates[noteId] = NoteDownloadState(NoteEntity.STATUS_PENDING, 0)
        val analytics_data = HashMap<String, String>()
        analytics_data["noteId"] = noteId
        analytics_data["subjectName"] = note.subjectName
        analytics_data["chapterTitle"] = note.title
        analytics_data["noteType"] = note.type
        analytics_data["notesBy"] = note.notesBy
        analytics_data["notesSelectedTopper"] = viewModel.uiState.value.selectedTopperCode.toString()
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.topper_ke_notes_download_btn_click.actionName,analytics_data)
    }

    // ── Subscription gate ───────────────────────────────────────────────────────

    /**
     * Locked notes go straight to the plans page — no interstitial dialog. The subscription
     * copy is still fetched up front, but only for the activityParams it carries, which are
     * forwarded to that page as extras.
     */
    private fun openLockedNote(note: TopperNote) {
        if (!CommonMethods.isOnline(requireContext())) {
            Toast.makeText(
                requireContext(),
                getString(R.string.NoInternetConnection),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        MyApplication.logFirebaseBundleCreation(
            FirebaseEvents.toppers_notes_lock_click.actionName,
            hashMapOf(
                "chapterTitle" to note.title,
                "subjectName" to note.subjectName,
                "notesType" to note.type
            )
        )
        openSubscriptionPage(note.accessType)
    }

    /**
     * Warms [lockActivityParams] so a locked tap can forward them without a round trip.
     */
    private fun fetchSubscriptionCopy() {
        if (!CommonMethods.isOnline(requireContext())) return

        MyApplication.getSubscriptionStatus(requireContext(), object : SubscriptionCallback {
            override fun onSuccess(dto: SubscriptionCheckDto?) {
                if (!isAdded) return
                lockActivityParams = dto?.activityParams
            }

            override fun onError(errorMessage: String?) = Unit
        })
    }

    private fun openSubscriptionPage(accessType: String) {
        val intent = SubscriptionNavigationHelper.getSubscriptionPageIntent(
            requireActivity(),
            SubscriptionPopupType.NOTES.name,
            RedirectionSource.TOPPERS_KE_NOTES_JOIN
        )
        lockActivityParams?.forEach { (key, value) ->
            if (value is Serializable) intent.putExtra(key, value)
        }
        buildTopperNotesLockPopupDto(accessType)?.let { dto ->
            intent.putExtra(CampaignSubscriptionActivity.BUNDLE_SUBSCRIPTION_DATA, dto)
        }
        startActivity(intent)
    }

    private fun buildTopperNotesLockPopupDto(accessType: String): SubscriptionLockPopupDto? {
        return try {
            val json = MyApplication.getFirebaseRemoteConfig()
                .getString("topper_notes_lock_popup_config")
            if (json.isNullOrBlank()) return null

            val lang = MyApplication.getStringData(PreferenceKeys.MY_LANG.key)
                ?.takeIf { it.isNotEmpty() } ?: ArivihanLanguageEnum.ENGLISH.code
            val root = JSONObject(json)
            val planKey = accessType.uppercase()
            val planObj = root.optJSONObject(planKey) ?: root.optJSONObject(SubscriptionType.PRO.name) ?: return null
            val langObj = planObj.optJSONObject(lang) ?: planObj.optJSONObject(ArivihanLanguageEnum.ENGLISH.code) ?: return null

            Gson().fromJson(langObj.toString(), SubscriptionLockPopupDto::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /** No-op when the host has no bottom navigation, e.g. [TopperNotesActivity]. */
    private fun setBottomNavigationVisible(visible: Boolean) {
        activity?.findViewById<View>(R.id.bottom_navigation)?.visibility =
            if (visible) View.VISIBLE else View.GONE
    }

    companion object {
        private const val TAG = "TopperNotesFragment"

        fun newInstance() = TopperNotesFragment()
    }
}
