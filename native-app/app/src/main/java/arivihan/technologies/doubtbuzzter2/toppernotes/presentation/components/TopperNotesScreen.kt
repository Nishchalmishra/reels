package arivihan.technologies.doubtbuzzter2.toppernotes.presentation.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.common.CommonMethods
import arivihan.technologies.doubtbuzzter2.compose.ArivihanScreenState
import arivihan.technologies.doubtbuzzter2.compose.ArivihanWindowInsets
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanScreenStateContainer
import arivihan.technologies.doubtbuzzter2.compose.component.rememberNetworkGuard
import arivihan.technologies.doubtbuzzter2.compose.component.AsyncImage
import arivihan.technologies.doubtbuzzter2.compose.component.ShimmerEffect
import arivihan.technologies.doubtbuzzter2.toppernotes.presentation.NoteDownloadState
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.model.Topper
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.model.TopperNote
import arivihan.technologies.doubtbuzzter2.toppernotes.domain.model.TopperSubject
import arivihan.technologies.doubtbuzzter2.toppernotes.presentation.TopperNotesUiState
import com.google.firebase.Firebase

// ── Light-mode palette (default) ─────────────────────────────────────────────
private val LightAccent = Color(0xFF26C6DA)
private val LightScreenBg = Color.White
private val LightCardBg = Color.White
private val LightCardBorder = Color(0xFFE2E8EC)
private val LightTitleColor = Color(0xFF1B2733)
private val LightMutedText = Color(0xFF8A99A8)
private val LightHeaderTitle = Color.Black
private val LightTabUnselectedBoundary = Color(0x2605040E)
private val LightTabUnselectedText = Color(0x9905040E)
private val LightMyDownloadsBorder = Color.Transparent
private val LightMyDownloadsCardBg = Color.White

// ── Dark-mode palette (PRO topper) ──────────────────────────────────────────
private val TealAccent = Color(0xFF26C6DA)
private val DarkScreenBg = Color(0xFF141414)
private val DarkCardBg = Color(0xFF1E1E1E)
private val DarkCardBorder = Color(0x14FFFFFF)
private val DarkTitleColor = Color.White
private val DarkMutedText = Color(0xFFAAAAAA)
private val DarkHeaderTitle = Color.White
private val DarkTabUnselectedBoundary = Color(0x29FFFFFF)
private val DarkTabUnselectedText = Color(0x99FFFFFF)
private val DarkMyDownloadsBorder = Color(0x29FFFFFF)
private val DarkMyDownloadsCardBg = Color.Transparent

private val NoteCardShadowBlur = 8.dp
private val NoteCardShadowSpread = 0.dp
private val NoteCardShadowColor = Color(0xFF000000)
private val LightNoteCardShadowAlpha = 0.08f
private val DarkNoteCardShadowAlpha = 0f

private val LockedTitleColor = Color(0xFF8A8A8A)

/** Holds resolved theme colors for this screen, created once at the top level. */
private data class TopperNotesTheme(
    val isDark: Boolean,
    val accent: Color,
    val screenBg: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val titleColor: Color,
    val mutedText: Color,
    val headerTitle: Color,
    val tabUnselectedBoundary: Color,
    val tabUnselectedText: Color,
    val myDownloadsBorder: Color,
    val myDownloadsCardBg: Color,
    val noteCardShadowAlpha: Float,
    val progressTrackColor: Color
)

private fun topperNotesTheme(isDark: Boolean) = if (isDark) TopperNotesTheme(
    isDark = true,
    accent = TealAccent,
    screenBg = DarkScreenBg,
    cardBg = DarkCardBg,
    cardBorder = DarkCardBorder,
    titleColor = DarkTitleColor,
    mutedText = DarkMutedText,
    headerTitle = DarkHeaderTitle,
    tabUnselectedBoundary = DarkTabUnselectedBoundary,
    tabUnselectedText = DarkTabUnselectedText,
    myDownloadsBorder = DarkMyDownloadsBorder,
    myDownloadsCardBg = DarkMyDownloadsCardBg,
    noteCardShadowAlpha = DarkNoteCardShadowAlpha,
    progressTrackColor = Color(0x33FFFFFF)
) else TopperNotesTheme(
    isDark = false,
    accent = LightAccent,
    screenBg = LightScreenBg,
    cardBg = LightCardBg,
    cardBorder = LightCardBorder,
    titleColor = LightTitleColor,
    mutedText = LightMutedText,
    headerTitle = LightHeaderTitle,
    tabUnselectedBoundary = LightTabUnselectedBoundary,
    tabUnselectedText = LightTabUnselectedText,
    myDownloadsBorder = LightMyDownloadsBorder,
    myDownloadsCardBg = LightMyDownloadsCardBg,
    noteCardShadowAlpha = LightNoteCardShadowAlpha,
    progressTrackColor = LightCardBorder
)

private const val StateChangeMillis = 220

private const val ShimmerRowCount = 8

private enum class NoteTrailing { Locked, Complete, InProgress, Ready }

private val TrailingSlotWidth = 34.dp
private val TrailingSlotHeight = 38.dp

private val LockIconSize = 34.dp

private val EmptyDownloadState = NoteDownloadState()

private val NunitoUnselected = FontFamily(Font(R.font.nunito_regular))
private val NunitoSelected = FontFamily(Font(R.font.nunito_medium))

private val NunitoBold = FontFamily(Font(R.font.nunito_bold))
private val UrbanistMedium = FontFamily(Font(R.font.urbanist_medium))


private val ScreenInset = 16.dp

private val TopperCardGap = 2.dp
private const val SelectedCardShare = 0.56f
private const val TopperCardAspect = 0.69f

@Composable
fun TopperNotesScreen(
    state: TopperNotesUiState,
    onBackClick: () -> Unit,
    onMyDownloadsClick: () -> Unit,
    onTopperClick: (Topper) -> Unit,
    onSubjectClick: (TopperSubject) -> Unit,
    onNoteClick: (TopperNote) -> Unit,
    onDownloadClick: (TopperNote) -> Unit,
    onRetryClick: () -> Unit,
    downloads: Map<String, NoteDownloadState> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val theme = remember(state.isDarkTheme) { topperNotesTheme(state.isDarkTheme) }

    val animatedScreenBg by animateColorAsState(
        targetValue = theme.screenBg,
        animationSpec = tween(StateChangeMillis),
        label = "screenBg"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(animatedScreenBg)
    ) {
        // Teal ellipse gradient at top-center for dark theme
        // Radius is based on screen width so the glow scales across devices
        if (state.isDarkTheme) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val density = LocalDensity.current.density
                val widthPx = constraints.maxWidth.toFloat()
                // Ellipse radius = 0.75× screen width (diameter = 1.5× screen width)
                val ellipseRadius = widthPx * 0.75f
                // Center the ellipse so roughly half sits above the top edge
                val ellipseCenterY = -ellipseRadius * 0.11f
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    TealAccent.copy(alpha = 0.45f),
                                    TealAccent.copy(alpha = 0.20f),
                                    Color.Transparent
                                ),
                                center = Offset(widthPx / 2f, ellipseCenterY),
                                radius = ellipseRadius
                            )
                        )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(ArivihanWindowInsets.Default)
        ) {
        TopperNotesHeader(
            theme = theme,
            onBackClick = onBackClick,
            onMyDownloadsClick = onMyDownloadsClick
        )

        val screenState: ArivihanScreenState<TopperNotesUiState> = when {
            state.isNetworkError -> ArivihanScreenState.NetworkError
            state.errorMessage != null -> ArivihanScreenState.Error(state.errorMessage)
            state.isLoadingToppers -> ArivihanScreenState.Loading
            else -> ArivihanScreenState.Content(state)
        }

        val networkGuard = rememberNetworkGuard()

        ArivihanScreenStateContainer(
            state = screenState,
            onRetry = onRetryClick,
            loadingContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TopperCardsShimmer(isDark = theme.isDark)
                    NotesLoadingShimmer(isDark = theme.isDark)
                }
            }
        ) { loaded ->
            Column(modifier = Modifier.fillMaxSize()) {
                if (loaded.subjects.isNotEmpty()) {
                    SubjectChipsRow(
                        theme = theme,
                        selectedTopperCode = loaded.selectedTopperCode,
                        subjects = loaded.subjects,
                        selectedSubjectId = loaded.selectedSubjectId,
                        onSubjectClick = { subject -> networkGuard { onSubjectClick(subject) } }
                    )
                }

                if (loaded.toppers.isNotEmpty()) {
                    TopperCardsRow(
                        isDarkTheme = theme.isDark,
                        toppers = loaded.toppers,
                        selectedTopperCode = loaded.selectedTopperCode,
                        onTopperClick = { topper -> networkGuard { onTopperClick(topper) } }
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        loaded.isLoadingNotes -> NotesLoadingShimmer(isDark = theme.isDark)
                        loaded.notes.isEmpty() -> CenteredMessage(
                            "No notes available yet",
                            theme = theme
                        )
                        else -> NotesList(
                            theme = theme,
                            selectedTopperCode = loaded.selectedTopperCode,
                            notes = loaded.notes,
                            downloads = downloads,
                            onNoteClick = onNoteClick,
                            onDownloadClick = onDownloadClick
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = this.clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    onClick = onClick
)

@Composable
private fun TopperNotesHeader(
    theme: TopperNotesTheme,
    onBackClick: () -> Unit,
    onMyDownloadsClick: () -> Unit
) {
    val animatedHeaderTitle by animateColorAsState(
        targetValue = theme.headerTitle,
        animationSpec = tween(StateChangeMillis),
        label = "headerTitle"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .noRippleClickable(onClick = {
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.topper_ke_notes_back_btn_click.actionName)
                    onBackClick()
                })
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_icon),
                contentDescription = "Back",
                tint = animatedHeaderTitle,
                modifier = Modifier.size(width = 13.dp, height = 15.dp)
            )
        }

        Text(
            text = stringResource(R.string.Toppers_ke_Notes),
            color = animatedHeaderTitle,
            fontSize = 18.sp,
            fontFamily = NunitoBold,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
        )

        val myDownloadsContentColor = if (theme.isDark) Color.White else Color.Black

        Card(
            onClick = {
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.topper_ke_notes_my_downloads_btn_click.actionName)
                onMyDownloadsClick()
            },
            shape = RoundedCornerShape(9.dp),
            colors = CardDefaults.cardColors(containerColor = theme.myDownloadsCardBg),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (theme.isDark) 0.dp else 3.dp
            ),
            border = if (theme.isDark) BorderStroke(0.5.dp, theme.myDownloadsBorder) else null,
            modifier = Modifier
                .padding(vertical = 2.dp, horizontal = 5.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.my_download),
                    color = myDownloadsContentColor,
                    fontSize = 13.sp,
                    fontFamily = UrbanistMedium,
                    fontWeight = FontWeight.Normal
                )
                Spacer(Modifier.width(6.dp))
                Image(
                    painter = painterResource(R.drawable.download_dark),
                    contentDescription = null,
                    colorFilter = if (theme.isDark) {
                        ColorFilter.tint(Color.White)
                    } else null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SubjectChipsRow(
    theme: TopperNotesTheme,
    selectedTopperCode: String?,
    subjects: List<TopperSubject>,
    selectedSubjectId: String?,
    onSubjectClick: (TopperSubject) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = ScreenInset, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        subjects.forEach { subject ->
            val selected = subject.id == selectedSubjectId
            val chipTitle = remember(subject.name) { CommonMethods.getShortName(subject.name) }

            val selectedFill = theme.accent
            val unselectedFill = if (theme.isDark) Color(0x1AFFFFFF) else Color.White

            val chipFill by animateColorAsState(
                targetValue = if (selected) selectedFill else unselectedFill,
                animationSpec = tween(StateChangeMillis),
                label = "chipFill"
            )
            val chipStroke by animateColorAsState(
                targetValue = when {
                    selected -> Color.Transparent
                    else -> theme.tabUnselectedBoundary
                },
                animationSpec = tween(StateChangeMillis),
                label = "chipStroke"
            )
            val chipLabel by animateColorAsState(
                targetValue = when {
                    selected -> Color.White
                    else -> theme.tabUnselectedText
                },
                animationSpec = tween(StateChangeMillis),
                label = "chipLabel"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(34.dp)
                    .defaultMinSize(minWidth = 61.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(chipFill)
                    .border(
                        width = if (selected) 0.dp else 0.5.dp,
                        color = chipStroke,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .noRippleClickable {
                        val analyticsData = HashMap<String, String>()
                        analyticsData["selected_topper_code"] = selectedTopperCode.toString()
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.topper_ke_notes_subject_click.actionName,analyticsData)
                        onSubjectClick(subject) }
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    text = chipTitle,
                    color = chipLabel,
                    fontSize = 13.sp,
                    fontFamily = if (selected) NunitoSelected else NunitoUnselected,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TopperCardsRow(
    isDarkTheme: Boolean,
    toppers: List<Topper>,
    selectedTopperCode: String?,
    onTopperClick: (Topper) -> Unit
) {
    val single = toppers.size == 1
    val scrollState = rememberScrollState()

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val available = maxWidth - ScreenInset * 2 - TopperCardGap
        val selectedWidth = available * SelectedCardShare
        val unselectedWidth = available * (1f - SelectedCardShare)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (single) Modifier else Modifier.horizontalScroll(scrollState))
                .padding(horizontal = ScreenInset, vertical = 8.dp),
            horizontalArrangement = if (single) {
                Arrangement.Center
            } else {
                Arrangement.spacedBy(TopperCardGap)
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            toppers.forEach { topper ->
                val selected = topper.code == selectedTopperCode
                TopperCard(
                    isDarkTheme = isDarkTheme,
                    topper = topper,
                    selected = selected,
                    width = if (selected) selectedWidth else unselectedWidth,
                    onClick = { onTopperClick(topper) }
                )
            }
        }
    }
}

@Composable
private fun TopperCard(
    isDarkTheme: Boolean,
    topper: Topper,
    selected: Boolean,
    width: Dp,
    onClick: () -> Unit
) {
    val animatedWidth by animateDpAsState(
        targetValue = width,
        animationSpec = tween(StateChangeMillis),
        label = "topperCardWidth"
    )
    var aspect by remember(topper.code) { mutableFloatStateOf(TopperCardAspect) }
    val height by animateDpAsState(
        targetValue = animatedWidth * aspect,
        animationSpec = tween(StateChangeMillis),
        label = "topperCardHeight"
    )

    val imageUrl = resolveTopperImageUrl(topper, selected, isDarkTheme)

    Box {
        AsyncImage(
            url = imageUrl,
            contentDescription = "${topper.name}, ${topper.examYear}",
            contentScale = ContentScale.Fit,
            onAspectRatio = { aspect = it },
            modifier = Modifier
                .width(animatedWidth)
                .height(height)
                .clip(RoundedCornerShape(1.dp))
                .noRippleClickable(onClick = {
                    val analyticsData = HashMap<String, String>()
                    analyticsData["selected_topper_code"] = topper.code
                    analyticsData["selected_topper_name"] = topper.name
                    analyticsData["selected_topper_rank"] = topper.rank.toString()
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.topper_ke_notes_topper_card_click.actionName)

                    onClick()
                })
        )
    }
}

private fun resolveTopperImageUrl(topper: Topper, selected: Boolean, isDark: Boolean): String {
    return if (selected) topper.selectedImageUrl else topper.unselectedImageUrl
}

@Composable
private fun NotesList(
    theme: TopperNotesTheme,
    selectedTopperCode: String?,
    notes: List<TopperNote>,
    downloads: Map<String, NoteDownloadState>,
    onNoteClick: (TopperNote) -> Unit,
    onDownloadClick: (TopperNote) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ScreenInset,
            end = ScreenInset,
            top = 8.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = notes, key = { it.id }) { note ->
            NoteRow(
                theme = theme,
                note = note,
                download = downloads[note.id.toString()] ?: EmptyDownloadState,
                onClick = {
                    val analyticsData = HashMap<String, String>()
                    analyticsData["note_Title"] = note.title
                    analyticsData["note_Id"] = note.id.toString()
                    analyticsData["note_isLocked"] = note.locked.toString()
                    analyticsData["note_Subject"] = note.subjectName
                    analyticsData["selected_topper_code"] = selectedTopperCode.toString()
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.topper_ke_notes_note_click.actionName,analyticsData)

                    onNoteClick(note) },
                onDownloadClick = { onDownloadClick(note) },
                modifier = Modifier.animateItem(
                    fadeInSpec = tween(StateChangeMillis),
                    placementSpec = tween(StateChangeMillis),
                    fadeOutSpec = tween(StateChangeMillis)
                )
            )
        }
    }
}

@Composable
private fun NoteRow(
    theme: TopperNotesTheme,
    note: TopperNote,
    download: NoteDownloadState,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = theme.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .then(
                if (theme.isDark) {
                    Modifier.border(
                        width = 1.dp,
                        color = theme.cardBorder,
                        shape = RoundedCornerShape(10.dp)
                    )
                } else {
                    Modifier.dropShadow(
                        shape = RoundedCornerShape(10.dp),
                        shadow = Shadow(
                            radius = NoteCardShadowBlur,
                            color = NoteCardShadowColor,
                            spread = NoteCardShadowSpread,
                            offset = DpOffset.Zero,
                            alpha = theme.noteCardShadowAlpha
                        )
                    )
                }
            )
            .noRippleClickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = note.title,
                color = if (note.locked) LockedTitleColor else theme.titleColor,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(12.dp))

            val phase = when {
                note.locked -> NoteTrailing.Locked
                download.isComplete -> NoteTrailing.Complete
                download.isInProgress -> NoteTrailing.InProgress
                else -> NoteTrailing.Ready
            }

            Crossfade(
                targetState = phase,
                animationSpec = tween(StateChangeMillis),
                label = "noteTrailing",
                modifier = Modifier.size(TrailingSlotWidth, TrailingSlotHeight)
            ) { current ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (current) {
                        NoteTrailing.Locked -> Image(
                            painter = painterResource(R.drawable.lock_image_red),
                            contentDescription = "Locked",
                            colorFilter = if (theme.isDark) {
                                ColorFilter.tint(Color(0xB2FFFFFF))
                            } else null,
                            modifier = Modifier.size(LockIconSize)
                        )

                        NoteTrailing.Complete -> Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Downloaded",
                            tint = theme.accent,
                            modifier = Modifier.size(24.dp)
                        )

                        NoteTrailing.InProgress -> Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(30.dp)
                        ) {
                            val sweep by animateFloatAsState(
                                targetValue = download.progress / 100f,
                                animationSpec = tween(StateChangeMillis),
                                label = "downloadSweep"
                            )
                            CircularProgressIndicator(
                                progress = { sweep },
                                color = theme.accent,
                                trackColor = theme.progressTrackColor,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "${download.progress}",
                                color = theme.mutedText,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }

                        NoteTrailing.Ready -> Image(
                            painter = painterResource(R.drawable.ic_download_pdf),
                            contentDescription = "Download PDF",
                            colorFilter = if (theme.isDark) {
                                ColorFilter.tint(Color(0xFFAEAEAE))
                            } else null,
                            modifier = Modifier
                                .size(width = 23.dp, height = 38.dp)
                                .noRippleClickable(onClick = onDownloadClick)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopperCardsShimmer(isDark: Boolean = false) {
    val shimmerBase = if (isDark) Color(0xFF2A2A2A) else Color.Transparent
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val available = maxWidth - ScreenInset * 2 - TopperCardGap
        val selectedWidth = available * SelectedCardShare
        val unselectedWidth = available * (1f - SelectedCardShare)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenInset, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(TopperCardGap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(selectedWidth, unselectedWidth).forEach { cardWidth ->
                Box(
                    modifier = Modifier
                        .width(cardWidth)
                        .height(cardWidth * TopperCardAspect)
                        .clip(RoundedCornerShape(14.dp))
                        .background(shimmerBase)
                ) {
                    ShimmerEffect(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

/**
 * Placeholder rows in the shape of [NoteRow], swept by a moving highlight.
 *
 * Same idea as the ShimmerFrameLayout the legacy screen wraps its list in, but drawn in
 * Compose so it needs no AndroidView bridge — and it shows the list's real geometry rather
 * than a spinner, so the swap into loaded content doesn't jump.
 */
@Composable
private fun NotesLoadingShimmer(isDark: Boolean = false) {
    val shimmerBase = if (isDark) Color(0xFF2A2A2A) else Color.Transparent
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenInset)
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(ShimmerRowCount) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(shimmerBase)
            ) {
                ShimmerEffect(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun CenteredMessage(message: String, theme: TopperNotesTheme = topperNotesTheme(false)) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = theme.mutedText, fontSize = 15.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun TopperNotesScreenPreview() {
    val sampleSubjects = listOf(
        TopperSubject("1", "Physics"),
        TopperSubject("2", "Chemistry"),
        TopperSubject("3", "Biology"),
        TopperSubject("4", "Mathematics")
    )
    val sampleToppers = listOf(
        Topper(
            code = "T1",
            name = "Aditya Jain",
            examYear = 2023,
            rank = 1,
            selectedImageUrl = "https://example.com/selected.png",
            unselectedImageUrl = "https://example.com/unselected.png",
            subjects = sampleSubjects,
            accessType = "PLUS"
        ),
        Topper(
            code = "T2",
            name = "Ishita",
            examYear = 2022,
            rank = 5,
            selectedImageUrl = "https://example.com/selected2.png",
            unselectedImageUrl = "https://example.com/unselected2.png",
            subjects = sampleSubjects.take(2),
            accessType = "PRO"
        )
    )
    val sampleNotes = listOf(
        TopperNote(
            id = 1,
            code = "N1",
            subjectName = "Physics",
            title = "Mechanics Part 1 - Laws of Motion",
            notesBy = "Aditya",
            url = "",
            type = "pdf",
            accessType = "PLUS",
            locked = false
        ),
        TopperNote(
            id = 2,
            code = "N2",
            subjectName = "Physics",
            title = "Optics Detailed Notes for NEET",
            notesBy = "Aditya",
            url = "",
            type = "pdf",
            accessType = "PRO",
            locked = true
        ),
        TopperNote(
            id = 3,
            code = "N3",
            subjectName = "Physics",
            title = "Thermodynamics Revision",
            notesBy = "Aditya",
            url = "",
            type = "pdf",
            accessType = "PRO",
            locked = false
        )
    )

    val state = TopperNotesUiState(
        isLoadingToppers = false,
        isLoadingNotes = false,
        toppers = sampleToppers,
        selectedTopperCode = "T1",
        selectedSubjectId = "1",
        notes = sampleNotes
    )

    MaterialTheme {
        TopperNotesScreen(
            state = state,
            onBackClick = {},
            onMyDownloadsClick = {},
            onTopperClick = {},
            onSubjectClick = {},
            onNoteClick = {},
            onDownloadClick = {},
            onRetryClick = {}
        )
    }
}
