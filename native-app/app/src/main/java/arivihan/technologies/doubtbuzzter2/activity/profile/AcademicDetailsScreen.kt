package arivihan.technologies.doubtbuzzter2.activity.profile

import android.graphics.fonts.FontStyle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.model.NameIdModel
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel
import arivihan.technologies.doubtbuzzter2.model.batch.grade.GradeModel
import arivihan.technologies.doubtbuzzter2.model.profile.StreamOption
import arivihan.technologies.doubtbuzzter2.viewModel.profile.AcademicDetailsViewModel
import arivihan.technologies.doubtbuzzter2.compose.component.AsyncImage
import arivihan.technologies.doubtbuzzter2.compose.component.ShimmerEffect
import arivihan.technologies.doubtbuzzter2.compose.component.rememberNetworkConnected
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanSnackbar
import arivihan.technologies.doubtbuzzter2.compose.component.ArivihanSnackbarType
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents
import arivihan.technologies.doubtbuzzter2.helper.UserNameValidationConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicDetailsScreen(
    viewModel: AcademicDetailsViewModel,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isConnected = rememberNetworkConnected()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var wasDisconnected by remember { mutableStateOf(false) }
    var networkSnackbarMessage by remember { mutableStateOf<String?>(null) }
    var networkSnackbarType by remember { mutableStateOf(ArivihanSnackbarType.Positive) }


    LaunchedEffect(state.error) {
        state.error?.let {
            networkSnackbarType = if (state.errorColorRes == R.color.error_button_color) {
                ArivihanSnackbarType.Negative
            } else {
                ArivihanSnackbarType.Positive
            }
            networkSnackbarMessage = it
            viewModel.clearError()
        }
    }

    LaunchedEffect(isConnected) {
        if (!isConnected) {
            wasDisconnected = true
            networkSnackbarType = ArivihanSnackbarType.Negative
            networkSnackbarMessage = context.getString(R.string.NoInternetConnection)
        } else if (wasDisconnected) {
            networkSnackbarType = ArivihanSnackbarType.Positive
            networkSnackbarMessage = context.getString(R.string.InternetConnection)
        }
    }

    val isScrollable by remember {
        derivedStateOf {
            scrollState.canScrollForward || scrollState.canScrollBackward
        }
    }

    val isAtBottom by remember {
        derivedStateOf {
            // When we can't scroll forward anymore, we are at the bottom
            !scrollState.canScrollForward
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(text = state.titleText, fontSize = 20.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = {
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.academic_details_back_btn_click.actionName)
                        onBack()
                    }) {
                        Icon(
                            painter = painterResource(id= R.drawable.arrow_back_ios_black) ,
                            modifier = Modifier.size(20.dp),
                            contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        snackbarHost = { },
        floatingActionButton = {
            val isAnySnackbarVisible by remember {
                derivedStateOf {
                    snackbarHostState.currentSnackbarData != null || networkSnackbarMessage != null
                }
            }
            if (isScrollable && !isAnySnackbarVisible) {
                FloatingScrollButton(
                    isAtBottom = isAtBottom,
                    text = if (isAtBottom) state.scrollUpText else state.scrollDownText,
                    onClick = {
                        coroutineScope.launch {
                            if (isAtBottom) {
                                scrollState.animateScrollToItem(0)
                            } else {
                                scrollState.animateScrollToItem(scrollState.layoutInfo.totalItemsCount - 1)
                            }
                        }
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color.White)) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize().padding(horizontal = 15.dp).imePadding(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 0.dp)
            ) {
                item {
                    Text(
                        text = state.subtitleText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                }

                // Medium Section
                item {
                    Column {
                        Text(text = state.mediumSectionTitle, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        if (state.isLanguagesLoading) {
                            OptionsShimmer()
                        } else if (state.mediums.isNotEmpty()) {
                            SelectionGridSection(
                                options = state.mediums.map { it.name },
                                selectedOption = state.mediums.find { it.id == state.selectedMedium }?.name ?: "",
                                onOptionSelected = { name: String ->
                                    state.mediums.find { it.name == name }?.let { viewModel.onMediumSelected(it.id) }
                                }
                            )
                        }
                    }
                }

                // Goal Section
                if (state.selectedMedium.isNotEmpty()) {
                    item {
                        Column {
                            Text(text = state.goalSectionTitle, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (state.isGoalsLoading) {
                                OptionsShimmer()
                            } else {
                                GoalSelectionSection(
                                    goals = state.goals,
                                    selectedGoalId = state.selectedGoalId,
                                    onGoalSelected = { viewModel.onGoalSelected(it) }
                                )
                            }
                        }
                    }
                }

                // Class Section
                if (state.selectedGoalId.isNotEmpty() && !state.isNeetSelected) {
                    item {
                        Column {
                            Text(text = state.classSectionTitle, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (state.isClassesLoading) {
                                OptionsShimmer()
                            } else {
                                ClassSelectionSection(
                                    classes = state.classes,
                                    selectedClassId = state.selectedClassId,
                                    onClassSelected = { viewModel.onClassSelected(it) }
                                )
                            }
                        }
                    }
                }

                // Stream Section
                if (state.selectedClassId.isNotEmpty() && !state.isNeetSelected && !state.isClass10Selected) {
                    item {
                        Column {
                            Text(text = state.streamSectionTitle, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (state.isStreamsLoading) {
                                OptionsShimmer()
                            } else {
                                StreamSelectionGridSection(
                                    options = state.streams,
                                    selectedStreamId = state.selectedStreamId,
                                    onStreamSelected = { viewModel.onStreamSelected(it) }
                                )
                            }
                        }
                    }
                }

                // Name Section
                item {
                    NameSection(
                        title = state.nameSectionTitle,
                        name = state.username,
                        placeholder = state.namePlaceholder,
                        error = state.nameError,
                        onNameChanged = { 
                            viewModel.onNameChanged(it)
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.academic_details_name_entered.actionName)
                        }
                    )
                }

                // Bottom Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                MyApplication.logFirebaseBundleCreation(FirebaseEvents.academic_details_cancel_btn_click.actionName)
                                onBack()
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, colorResource(R.color.arivihan_secondary_color))
                        ) {
                            Text(text = state.cancelText, color = colorResource(R.color.arivihan_secondary_color), fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = {
                                MyApplication.logFirebaseBundleCreation(FirebaseEvents.academic_details_save_btn_click.actionName)
                                android.util.Log.d("AcademicDetailsScreen", "Save button clicked")
                                viewModel.onSaveClicked() 
                            },
                            enabled = isConnected,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.arivihan_secondary_color))
                        ) {
                            Text(text = state.saveText, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            
            ArivihanSnackbar(
                message = networkSnackbarMessage,
                type = networkSnackbarType,
                onDismiss = { networkSnackbarMessage = null },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
            )
        }
    }

    if (state.showSaveConfirmation) {
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.ad_save_changes_popup_view.actionName)
        SaveConfirmationPopup(
            title = state.confirmationTitle,
            body = state.confirmationBody,
            cancelText = state.cancelText,
            saveChangesText = state.saveChangesText,
            onDismiss = { viewModel.onDismissConfirmation() },
            onSave = {
                android.util.Log.d("AcademicDetailsScreen", "Confirm Save clicked in dialog")
                viewModel.onConfirmSave()
            }
        )
    }
    
    LaunchedEffect(state.isSaveSuccess) {
        if (state.isSaveSuccess) {
            onSaveSuccess()
        }
    }
}

@Composable
fun OptionsShimmer() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(2) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShimmerEffect(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                ShimmerEffect(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}

@Composable
fun GoalSelectionSection(
    goals: List<OnBoardingSelectionModel>,
    selectedGoalId: String,
    onGoalSelected: (OnBoardingSelectionModel) -> Unit
) {
    Column {
        goals.chunked(2).forEach { rowGoals ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowGoals.forEach { goal ->
                    SelectableCard(
                        text = goal.name,
                        isSelected = goal.id == selectedGoalId,
                        onClick = {
                            val analyticsData = HashMap<String, String>()
                            analyticsData ["goalSelected"] = goal.name
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.academic_details_goal_selected.actionName, analyticsData)
                            onGoalSelected(goal)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (rowGoals.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ClassSelectionSection(
    classes: List<GradeModel>,
    selectedClassId: String,
    onClassSelected: (GradeModel) -> Unit
) {
    Column {
        classes.chunked(2).forEach { rowClasses ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowClasses.forEach { grade ->
                    val gradeCode = grade.gradeCode ?: grade.code ?: grade.id?.toString() ?: ""
                    SelectableCard(
                        text = grade.name ?: grade.displayName ?: "",
                        isSelected = gradeCode == selectedClassId,
                        onClick = {
                            val analyticsData = HashMap<String, String>()
                            analyticsData ["classSelected"] = grade.name ?: grade.displayName ?: ""
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.academic_details_class_selected.actionName, analyticsData)
                            onClassSelected(grade)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowClasses.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun SelectionGridSection(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column {
        options.chunked(2).forEach { rowOptions ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowOptions.forEach { option ->
                    SelectableCard(
                        text = option,
                        isSelected = (option == selectedOption),
                        onClick = {
                            val analyticsData = HashMap<String, String>()
                            analyticsData ["mediumSelected"] = option
                            MyApplication.logFirebaseBundleCreation(FirebaseEvents.academic_details_medium_selected.actionName,analyticsData)
                            onOptionSelected(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowOptions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun SelectionIcon(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            // Light blue halo (Outer ring)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color(0x3326C6DA), shape = CircleShape)
            )
            // Solid blue circle (Inner)
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(color = colorResource(R.color.arivihan_secondary_color), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color.White
                )
            }
        } else {
            // Unselected border circle
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0x4D121212),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun SelectableCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(56.dp)
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = Color(0x4026C6DA),
                        ambientColor = Color(0x4026C6DA)
                    )
                } else Modifier
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (isSelected) colorResource(R.color.arivihan_secondary_color) else Color(0x4D121212)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF1FBFC) else Color.White
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectionIcon(
                isSelected = isSelected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text, 
                fontSize = 17.sp, 
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.Black else Color(0x4D121212)
            )
        }
    }
}

@Composable
fun StreamSelectionGridSection(
    options: List<StreamOption>,
    selectedStreamId: String,
    onStreamSelected: (String) -> Unit
) {
    Column {
        options.chunked(2).forEach { rowOptions ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowOptions.forEach { option ->
                    StreamCard(
                        option = option,
                        isSelected = (option.id == selectedStreamId),
                        onClick = { onStreamSelected(option.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowOptions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun StreamCard(
    option: StreamOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(56.dp)
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = Color(0xFFE8E8E8),
                        ambientColor = Color(0xFFE8E8E8)
                    )
                } else Modifier
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (isSelected) colorResource(R.color.arivihan_secondary_color) else Color(0x4D121212)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF1FBFC) else Color.White
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectionIcon(
                isSelected = isSelected,
                onClick = {
                    val analyticsData = HashMap<String, String>()
                    analyticsData ["streamSelected"] = option.name
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.academic_details_stream_selected.actionName, analyticsData)
                    onClick()
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = option.name, 
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                color = if (isSelected) Color.Black else Color(0x4D121212)
            )
            if (!option.iconUrl.isNullOrEmpty()) {
                AsyncImage(
                    url = option.iconUrl,
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            if (isSelected) colorResource(R.color.arivihan_secondary_color) else Color(0xFFF5F5F5),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(4.dp),
                    colorFilter = ColorFilter.tint(if (isSelected) Color.White else Color.Gray)
                )
            }
        }
    }
}

@Composable
fun NameSection(
    title: String,
    name: String,
    placeholder: String,
    error: String?,
    onNameChanged: (String) -> Unit
) {
    Column {
        Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { if (it.length <= 20) onNameChanged(it) },
            modifier = Modifier.fillMaxWidth(),
             singleLine = true,
            placeholder = {
                Text(
                    placeholder,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0x4D121212),
                    maxLines = 1
                )
            },
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(
                        text = error,
                        color = colorResource(R.color.error_button_color),
                        fontSize = 12.sp
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(R.color.arivihan_secondary_color),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                errorBorderColor = colorResource(R.color.error_button_color)
            )
        )
    }
}

@Composable
fun FloatingScrollButton(
    isAtBottom: Boolean,
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(Color.Black, RoundedCornerShape(24.dp))
            .clickable {
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.academic_details_scroll_btn_click.actionName)
                onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(id = R.drawable.pdf_viewer_ic_arrow_back),
                modifier = Modifier.rotate(if (isAtBottom) 90f else 270f),
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}
@Composable
fun SaveConfirmationPopup(
    title: String,
    body: String,
    cancelText: String,
    saveChangesText: String,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Close button at top right
                IconButton(
                    onClick = {
                        MyApplication.logFirebaseBundleCreation(FirebaseEvents.ad_save_changes_popup_cross_btn_click.actionName)
                        onDismiss()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 25.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Warning Image
                    Icon(
                        painter = painterResource(id = R.drawable.ic_yellow_warning_icon),
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = Color.Unspecified
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Subtitle
                    Text(
                        text = body,
                        textAlign = TextAlign.Center,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF696969),
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                MyApplication.logFirebaseBundleCreation(FirebaseEvents.ad_save_changes_popup_cancel_btn_click.actionName)
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF26C6DA)),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(
                                text = cancelText,
                                color = colorResource(id = R.color.arivihan_secondary_color),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                        Button(
                            onClick = {
                                MyApplication.logFirebaseBundleCreation(FirebaseEvents.ad_save_changes_popup_save_btn_click.actionName)
                                android.util.Log.d("AcademicDetailsScreen", "Confirm Save clicked in dialog")
                                onSave()
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.arivihan_secondary_color)),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(
                                text = saveChangesText,
                                fontSize = 15.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
