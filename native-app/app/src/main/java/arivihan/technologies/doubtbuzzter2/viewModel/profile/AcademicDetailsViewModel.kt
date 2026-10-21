package arivihan.technologies.doubtbuzzter2.viewModel.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import arivihan.technologies.doubtbuzzter2.MyApplication
import android.content.res.Configuration
import arivihan.technologies.doubtbuzzter2.R
import arivihan.technologies.doubtbuzzter2.common.CommonMethods
import arivihan.technologies.doubtbuzzter2.constants.UrlRoutes
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceMethods
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel
import arivihan.technologies.doubtbuzzter2.model.UserOnboardingState
import arivihan.technologies.doubtbuzzter2.model.batch.grade.GradeModel
import arivihan.technologies.doubtbuzzter2.model.batch.stream.StreamModel
import arivihan.technologies.doubtbuzzter2.model.batch.user.UserDetailsResponse
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse
import arivihan.technologies.doubtbuzzter2.model.profile.AcademicDetailsState
import arivihan.technologies.doubtbuzzter2.model.profile.MediumOption
import arivihan.technologies.doubtbuzzter2.model.profile.StreamOption
import arivihan.technologies.doubtbuzzter2.model.profile.UpdateAcademicDetailsRequest
import arivihan.technologies.doubtbuzzter2.helper.UserNameValidationConfig
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject
import java.util.Locale

class AcademicDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AcademicDetailsState())

    val uiState: StateFlow<AcademicDetailsState> = _uiState.asStateFlow()
    private val gson = Gson()
    private var originalOnboardingState: UserOnboardingState? = null
    private var examIdFromHome: Int? = null

    // -------------------------------------------------------------------------
    // CASCADE INVALIDATION
    //
    // medium -> goal -> grade -> stream is a strict chain. Whenever the user
    // changes a level, every request already in flight for the levels below it
    // is stale and must NOT be allowed to write into the state when it lands.
    // Volley delivers all callbacks on the main thread, so plain counters are
    // enough here - no synchronisation required.
    // -------------------------------------------------------------------------
    private var goalsRequestId = 0
    private var gradesRequestId = 0
    private var streamsRequestId = 0

    // An explicit tap by the user must always win over the "selected" flag the
    // API sends back for the previously saved profile. Before the first tap the
    // server stays authoritative, which is the original first-load behaviour.
    private var userPickedMedium = false
    private var userHasSelected = false

    init {
        loadGoalOptions()
        fetchInitialData()
        fetchLanguages()
    }

    fun setExamIdFromHome(examId: Int) {
        this.examIdFromHome = examId
    }

    // -------------------------------------------------------------------------
    // INITIAL DATA
    // -------------------------------------------------------------------------

    private fun fetchInitialData() {
        Log.d("AcademicDetailsVM", "fetchInitialData started")

        // 1. Immediate Discovery from Local Preferences (Home Screen source)
        val currentSelection = PreferenceMethods.getSelectedOnboardingModel()
        val prefExamId = MyApplication.getStringData(PreferenceKeys.EXAM_ID.key) ?: ""
        val prefGradeCode = MyApplication.getStringData(PreferenceKeys.GRADE_CODE.getKey()) ?: ""
        val prefStreamCode = MyApplication.getStringData(PreferenceKeys.STREAM_CODE.getKey()) ?: ""
        val prefExamCode = MyApplication.getStringData(PreferenceKeys.EXAM_CODE.key) ?: ""
        Log.d("AcademicDetailsVM", "GRADE_CODE: value='$prefGradeCode', missing=${prefGradeCode.isBlank()}")

        Log.d("AcademicDetailsVM", "STREAM_CODE: value='$prefStreamCode', missing=${prefStreamCode.isBlank()}")

        Log.d("AcademicDetailsVM", "EXAM_CODE: value='$prefExamCode', missing=${prefExamCode.isBlank()}")

        val internalMedium = MyApplication.getStringData(PreferenceKeys.MY_LANG.key) ?: "en"
        val currentName = MyApplication.getStringData(PreferenceKeys.NAME.key) ?: ""
        val currentMedium = ArivihanLanguageEnum.fromString(internalMedium)?.name ?: LANGUAGE_ENGLISH

        // Initial IDs for highlighting - prioritized by local selection then active batch
        val initialGoalId = currentSelection?.id ?: prefExamId
        val initialClassId = currentSelection?.gradeCode ?: prefGradeCode
        val initialStreamId = currentSelection?.streamCode ?: prefStreamCode

        val isNeet = currentSelection?.examCode?.equals(EXAM_CODE_NEET, ignoreCase = true) == true ||
                     initialGoalId.contains(EXAM_CODE_NEET, ignoreCase = true)


        updateUIStrings(currentMedium)

        // 2. Initialize State Immediately so cards are highlighted during API loads
        _uiState.update {
            it.copy(
                username = currentName,
                selectedMedium = currentMedium,
                selectedGoalId = initialGoalId,
                selectedOnboardingModel = currentSelection,
                selectedClassId = initialClassId,
                selectedStreamId = initialStreamId,
                isNeetSelected = isNeet,
                isClass10Selected = initialClassId.contains("10", ignoreCase = true),
                isClassesLoading = initialGoalId.isNotEmpty() && !isNeet,
                isStreamsLoading = initialClassId.isNotEmpty() && !isNeet &&
                        !initialClassId.contains("10", ignoreCase = true),
                isLoading = false
            )
        }

        // 3. Trigger the cascade from the top. fetchGoals -> fetchGrades ->
        //    fetchStreams chains itself, so the grade and stream lists are always
        //    fetched for the medium/goal/grade that is actually resolved here
        //    instead of for whatever was cached from a previous session.
        fetchGoals(currentMedium)

        // 4. Refine/Sync with Server-side Onboarding State
        val phoneNumber = MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.key)
        val params = HashMap<String, Any>()
        params["phoneNumber"] = phoneNumber ?: ""
        val url = CommonMethods.getUrlParam(UrlRoutes.GET_ONBOARDING_STATE, params)

        val request = MyJsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val state: UserOnboardingState = gson.fromJson(
                        response.toString(),
                        UserOnboardingState::class.java
                    )
                    originalOnboardingState = state

                    // If local discovery was empty but server has data, update and re-fetch
                    if (initialGoalId.isEmpty() && (state.selectedBoard != null || state.courseId != null)) {
                        val serverGoalId = state.selectedBoard ?: state.courseId ?: ""
                        if (serverGoalId.isNotEmpty()) {
                            _uiState.update { it.copy(selectedGoalId = serverGoalId) }
                            // Always re-fetch with the medium currently on screen, not the
                            // one captured when this request was created.
                            fetchGoals(_uiState.value.selectedMedium)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AcademicDetailsVM", "Error parsing onboarding state refinement", e)
                }
            },
            { error ->
                Log.e("AcademicDetailsVM", "Error refining state from server", error)
            }
        )

        MyVolleySingleton.getInstance(getApplication()).addToRequestQueue(request)
    }

    private fun fetchLanguages() {
        _uiState.update { it.copy(isLanguagesLoading = true) }
        val url = CommonMethods.getUrlParam(UrlRoutes.SUPPORTED_LANGUAGES, HashMap())
        val request = MyJsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val data = response.optJSONArray("data")
                    if (data != null) {
                        val list = mutableListOf<MediumOption>()
                        var apiSelectedMedium: String? = null

                        for (i in 0 until data.length()) {
                            val obj = data.getJSONObject(i)
                            val code = obj.optString("code") // e.g., "ENGLISH", "HINDI"
                            val mediumdisplayName = obj.optString("mediumDisplayName")
                            val isSelected = obj.optBoolean("selected") || obj.optBoolean("isSelected")

                            if (isSelected) {
                                apiSelectedMedium = code
                                Log.d("AcademicDetailsVM", "API Selected Medium: $code")
                            }

                            list.add(MediumOption(code, mediumdisplayName))
                        }

                        val previousMedium = _uiState.value.selectedMedium
                        // Never let a late language response overwrite a medium the
                        // user has already tapped.
                        val resolvedMedium = when {
                            userPickedMedium -> previousMedium
                            apiSelectedMedium != null -> apiSelectedMedium
                            else -> previousMedium
                        }

                        _uiState.update { currentState ->
                            currentState.copy(
                                mediums = list,
                                selectedMedium = resolvedMedium,
                                isLanguagesLoading = false,
                                isLoading = false
                            )
                        }

                        // The cascade was started with `previousMedium`. If the server
                        // says the profile is actually on another medium, re-run it so
                        // goals/grades/streams match what the medium card shows.
                        if (!resolvedMedium.equals(previousMedium, ignoreCase = true)) {
                            Log.d("AcademicDetailsVM", "Medium corrected by API: $previousMedium -> $resolvedMedium, reloading cascade")
                            updateUIStrings(resolvedMedium)
                            reloadCascadeForMedium(resolvedMedium)
                        }
                    } else {
                        _uiState.update { it.copy(isLanguagesLoading = false, isLoading = false) }
                    }
                } catch (e: Exception) {
                    Log.e("AcademicDetailsVM", "Error parsing languages", e)
                    _uiState.update { it.copy(isLanguagesLoading = false, isLoading = false) }
                }
            },
            { error ->
                Log.e("AcademicDetailsVM", "Error fetching languages", error)
                val isOffline = !CommonMethods.isOnline(getApplication())
                _uiState.update { currentState ->
                    currentState.copy(
                        isLanguagesLoading = isOffline && currentState.mediums.isEmpty(),
                        isLoading = false
                    )
                }
            }
        )
        MyVolleySingleton.getInstance(getApplication()).addToRequestQueue(request)
    }

    // -------------------------------------------------------------------------
    // GOALS
    // -------------------------------------------------------------------------

    private fun fetchGoals(language: String) {
        val requestId = ++goalsRequestId
        // A new exam list makes every grade/stream call below it obsolete.
        gradesRequestId++
        streamsRequestId++

        _uiState.update { it.copy(isGoalsLoading = true) }
        val url = CommonMethods.getUrlParam(UrlRoutes.ACADEMIC_EXAM_LIST, HashMap())
        val request = languageAwareGet(url, language,
            { response ->
                if (requestId != goalsRequestId) {
                    Log.d("AcademicDetailsVM", "Dropping stale goals response ($requestId)")
                    return@languageAwareGet
                }
                try {
                    val data = response.optJSONArray("data")
                    if (data == null) {
                        _uiState.update { it.copy(isGoalsLoading = false) }
                        clearGradesAndStreams()
                        return@languageAwareGet
                    }

                    val freshList = mutableListOf<OnBoardingSelectionModel>()
                    var apiSelectedGoalId: String? = null

                    for (i in 0 until data.length()) {
                        val obj = data.getJSONObject(i)
                        val id = obj.optString("id")
                        val shortName = obj.optString("shortName")
                        val isSelected = obj.optBoolean("selected") || obj.optBoolean("isSelected")

                        if (id.isNotEmpty() && shortName.isNotEmpty()) {
                            if (isSelected) {
                                apiSelectedGoalId = id
                                Log.d("AcademicDetailsVM", "API Selected Goal: $id")
                            }

                            val model = OnBoardingSelectionModel()
                            model.name = shortName
                            model.id = id
                            model.batchCode = obj.optString("batchCode")
                            model.examCode = obj.optString("code").ifEmpty { id }
                            model.streamCode = obj.optString("streamCode")
                            model.gradeCode = obj.optString("gradeCode")
                            model.yearCode = obj.optString("yearCode")
                            model.boardCode = obj.optString("boardCode")
                            model.courseId = obj.optString("courseId")
                            model.languageCode = language
                            freshList.add(model)
                        }
                    }

                    val current = _uiState.value
                    // Exam ids are language independent, so a selection the user already
                    // made survives a medium switch: it is re-resolved against the new
                    // list first and only falls back to the API's own flag if it is gone.
                    val byCurrent = freshList.find { it.id == current.selectedGoalId }
                    val byApi = freshList.find { it.id == apiSelectedGoalId }
                    val resolved = if (userHasSelected) byCurrent ?: byApi else byApi ?: byCurrent
                    val resolvedGoalId = resolved?.id ?: current.selectedGoalId
                    val isNeet = resolved?.let { isNeetGoal(it) } ?: current.isNeetSelected

                    _uiState.update {
                        it.copy(
                            goals = freshList,
                            selectedGoalId = resolvedGoalId,
                            selectedOnboardingModel = resolved ?: it.selectedOnboardingModel,
                            isNeetSelected = isNeet,
                            isGoalsLoading = false,
                            isClassesLoading = resolved != null && !isNeet
                        )
                    }

                    // Cascade OUTSIDE of update {} - the lambda passed to
                    // MutableStateFlow.update may be re-executed on contention, which
                    // would fire the request twice.
                    if (resolved != null && !isNeet) {
                        fetchGrades(resolved.examCode ?: resolved.id, language)
                    } else {
                        clearGradesAndStreams()
                    }
                } catch (e: Exception) {
                    Log.e("AcademicDetailsVM", "Error parsing exams from API", e)
                    val cached = PreferenceMethods.getOnboardingCourseList()
                    _uiState.update { it.copy(goals = cached ?: emptyList(), isGoalsLoading = false) }
                    cascadeFromCachedGoal(cached, language)
                }
            },
            { error ->
                if (requestId != goalsRequestId) return@languageAwareGet
                Log.e("AcademicDetailsVM", "Error fetching exams from API", error)
                val cached = PreferenceMethods.getOnboardingCourseList()
                val isOffline = !CommonMethods.isOnline(getApplication())
                _uiState.update { currentState ->
                    currentState.copy(
                        goals = cached ?: emptyList(),
                        isGoalsLoading = isOffline && (cached == null || cached.isEmpty())
                    )
                }
                cascadeFromCachedGoal(cached, language)
            }
        )
        MyVolleySingleton.getInstance(getApplication()).addToRequestQueue(request)
    }

    // -------------------------------------------------------------------------
    // GRADES
    // -------------------------------------------------------------------------

    private fun fetchGrades(examCode: String, language: String) {
        val requestId = ++gradesRequestId
        // A new grade list makes the in-flight stream call obsolete.
        streamsRequestId++

        if (examCode.isEmpty()) {
            _uiState.update { it.copy(classes = emptyList(), isClassesLoading = false) }
            clearStreams()
            return
        }
        _uiState.update { it.copy(isClassesLoading = true, classes = emptyList()) }
        val map = HashMap<String, Any>()
        map["examCode"] = examCode
        val url = CommonMethods.getUrlParam(UrlRoutes.AV_GRADE_LIST, map)

        val request = languageAwareGet(url, language,
            { response ->
                if (requestId != gradesRequestId) {
                    Log.d("AcademicDetailsVM", "Dropping stale grades response ($requestId)")
                    return@languageAwareGet
                }
                try {
                    val token = object : TypeToken<ApiWrappedResponse<List<GradeModel>>>() {}.type
                    val apiResponse: ApiWrappedResponse<List<GradeModel>> = gson.fromJson(response.toString(), token)

                    if (apiResponse.isSuccess() && apiResponse.data != null) {
                        val gradeModels = apiResponse.data
                        val current = _uiState.value

                        // Grade codes are language independent - keep what the user
                        // already picked, only fall back to the API's flag.
                        val byCurrent = gradeModels.find { gradeCodeOf(it) == current.selectedClassId }
                        val byApi = gradeModels.find { it.selected }
                        val preSelected = if (userHasSelected) byCurrent ?: byApi else byApi ?: byCurrent

                        if (preSelected != null) {
                            Log.d("AcademicDetailsVM", "Resolved Class: ${gradeCodeOf(preSelected)}")
                        }

                        val resolvedClassId = preSelected?.let { gradeCodeOf(it) } ?: current.selectedClassId
                        val isClass10 = preSelected?.let { isClass10Grade(it) } ?: current.isClass10Selected
                        val shouldLoadStreams = preSelected != null && !current.isNeetSelected && !isClass10

                        _uiState.update {
                            it.copy(
                                classes = gradeModels,
                                selectedClassId = resolvedClassId,
                                selectedGrade = preSelected ?: it.selectedGrade,
                                isClass10Selected = isClass10,
                                isClassesLoading = false,
                                isStreamsLoading = shouldLoadStreams
                            )
                        }

                        if (shouldLoadStreams) {
                            val effectiveExamCode = current.selectedOnboardingModel?.examCode
                                ?: preSelected.examCode
                                ?: examCode
                            fetchStreams(effectiveExamCode, resolvedClassId, language)
                        } else {
                            clearStreams()
                        }
                    } else {
                        _uiState.update { it.copy(classes = emptyList(), isClassesLoading = false) }
                        clearStreams()
                    }
                } catch (e: Exception) {
                    Log.e("AcademicDetailsVM", "Error parsing grades", e)
                    _uiState.update { it.copy(isClassesLoading = false) }
                    clearStreams()
                }
            },
            { error ->
                if (requestId != gradesRequestId) return@languageAwareGet
                Log.e("AcademicDetailsVM", "Error fetching grades", error)
                val isOffline = !CommonMethods.isOnline(getApplication())
                _uiState.update { currentState ->
                    currentState.copy(isClassesLoading = isOffline && currentState.classes.isEmpty())
                }
                clearStreams()
            }
        )
        MyVolleySingleton.getInstance(getApplication()).addToRequestQueue(request)
    }

    // -------------------------------------------------------------------------
    // STREAMS
    // -------------------------------------------------------------------------

    private fun fetchStreams(examCode: String, gradeCode: String, language: String) {
        val requestId = ++streamsRequestId

        if (examCode.isBlank() || gradeCode.isBlank()) {
            _uiState.update { it.copy(streams = emptyList(), streamModels = emptyList(), isStreamsLoading = false) }
            return
        }
        _uiState.update { it.copy(isStreamsLoading = true, streams = emptyList()) }
        val map = HashMap<String, Any>()
        map["examCode"] = examCode
        map["gradeCode"] = gradeCode
        val url = CommonMethods.getUrlParam(UrlRoutes.AV_STREAM_LIST, map)

        val request = languageAwareGet(url, language,
            { response ->
                if (requestId != streamsRequestId) {
                    Log.d("AcademicDetailsVM", "Dropping stale streams response ($requestId)")
                    return@languageAwareGet
                }
                try {
                    val token = object : TypeToken<ApiWrappedResponse<List<StreamModel>>>() {}.type
                    val apiResponse: ApiWrappedResponse<List<StreamModel>> = gson.fromJson(response.toString(), token)

                    if (apiResponse.isSuccess() && apiResponse.data != null) {
                        val streamModels = apiResponse.data
                        _uiState.update { currentState ->
                            // Stream codes are language independent - the user's own
                            // pick wins over the API's saved selection.
                            val byCurrent = streamModels.find { it.code == currentState.selectedStreamId }
                            val byApi = streamModels.find { it.selected }
                            val preSelected = if (userHasSelected) byCurrent ?: byApi else byApi ?: byCurrent

                            currentState.copy(
                                streamModels = streamModels,
                                streams = mapStreams(streamModels),
                                selectedStream = preSelected ?: currentState.selectedStream,
                                selectedStreamId = preSelected?.code ?: currentState.selectedStreamId,
                                isStreamsLoading = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(streams = emptyList(), streamModels = emptyList(), isStreamsLoading = false) }
                    }
                } catch (e: Exception) {
                    Log.e("AcademicDetailsVM", "Error parsing streams", e)
                    _uiState.update { it.copy(isStreamsLoading = false) }
                }
            },
            { error ->
                if (requestId != streamsRequestId) return@languageAwareGet
                Log.e("AcademicDetailsVM", "Error fetching streams", error)
                val isOffline = !CommonMethods.isOnline(getApplication())
                _uiState.update { currentState ->
                    currentState.copy(isStreamsLoading = isOffline && currentState.streams.isEmpty())
                }
            }
        )
        MyVolleySingleton.getInstance(getApplication()).addToRequestQueue(request)
    }

    // -------------------------------------------------------------------------
    // SELECTION HANDLERS
    // -------------------------------------------------------------------------

    fun onMediumSelected(mediumId: String) {
        if (mediumId.equals(_uiState.value.selectedMedium, ignoreCase = true)) return

        userPickedMedium = true
        userHasSelected = true
        updateUIStrings(mediumId)
        reloadCascadeForMedium(mediumId)
    }

    fun onGoalSelected(selected: OnBoardingSelectionModel) {
        userHasSelected = true
        val isNeet = isNeetGoal(selected)
        val mediumId = _uiState.value.selectedMedium
        this.examIdFromHome = null

        // Drop grade/stream responses that belong to the previous goal.
        gradesRequestId++
        streamsRequestId++

        _uiState.update {
            it.copy(
                selectedGoalId = selected.id,
                selectedOnboardingModel = selected,
                isNeetSelected = isNeet,
                selectedClassId = "",
                selectedGrade = null,
                selectedStreamId = "",
                selectedStream = null,
                classes = emptyList(),
                streams = emptyList(),
                streamModels = emptyList(),
                isClass10Selected = false,
                isClassesLoading = !isNeet,
                isStreamsLoading = false,
                isLoading = false
            )
        }
        if (!isNeet) {
            fetchGrades(selected.examCode ?: "", mediumId)
        }
    }

    fun onClassSelected(gradeModel: GradeModel) {
        userHasSelected = true
        val mediumId = _uiState.value.selectedMedium
        val gradeCode = gradeCodeOf(gradeModel)
        val gradeName = gradeModel.displayName ?: gradeModel.name ?: ""
        val isClass10 = isClass10Grade(gradeModel)

        Log.d("AcademicDetailsVM", "onClassSelected: code=$gradeCode, name=$gradeName, isClass10=$isClass10")

        // Drop stream responses that belong to the previous class.
        streamsRequestId++

        _uiState.update {
            it.copy(
                selectedClassId = gradeCode,
                selectedGrade = gradeModel,
                isClass10Selected = isClass10,
                selectedStreamId = "",
                selectedStream = null,
                streams = emptyList(),
                streamModels = emptyList(),
                isStreamsLoading = !isClass10,
                isLoading = false
            )
        }

        if (!isClass10) {
            val examCode = _uiState.value.selectedOnboardingModel?.examCode
                          ?: gradeModel.examCode
                          ?: MyApplication.getStringData(PreferenceKeys.EXAM_CODE.key)
                          ?: ""
            Log.d("AcademicDetailsVM", "Triggering fetchStreams: exam=$examCode, grade=$gradeCode, medium=$mediumId")
            fetchStreams(examCode, gradeCode, mediumId)
        }
    }

    fun onStreamSelected(streamId: String) {
        userHasSelected = true
        val selectedStream = _uiState.value.streamModels.firstOrNull { it.code == streamId }
        _uiState.update {
            it.copy(
                selectedStreamId = streamId,
                selectedStream = selectedStream
            )
        }
    }

    // -------------------------------------------------------------------------
    // CASCADE HELPERS
    // -------------------------------------------------------------------------

    /**
     * Re-runs the whole medium -> goal -> grade -> stream chain for [medium].
     *
     * The selected codes are deliberately kept: exam / grade / stream codes are
     * language independent, so switching medium only has to re-fetch the three
     * lists so that every section shows the new medium's content. Each list is
     * then re-resolved against the code the user already had selected.
     */
    private fun reloadCascadeForMedium(medium: String) {
        val current = _uiState.value
        val willLoadClasses = current.selectedGoalId.isNotEmpty() && !current.isNeetSelected
        val willLoadStreams = willLoadClasses &&
                current.selectedClassId.isNotEmpty() && !current.isClass10Selected

        _uiState.update {
            it.copy(
                selectedMedium = medium,
                goals = emptyList(),
                classes = emptyList(),
                streams = emptyList(),
                streamModels = emptyList(),
                isGoalsLoading = true,
                isClassesLoading = willLoadClasses,
                isStreamsLoading = willLoadStreams,
                isLoading = false
            )
        }
        fetchGoals(medium)
    }

    /**
     * The exam list call failed. Fall back to the cached goal / persisted exam
     * code so the class and stream sections still load instead of shimmering
     * forever.
     */
    private fun cascadeFromCachedGoal(cached: List<OnBoardingSelectionModel>?, language: String) {
        val current = _uiState.value
        val resolved = cached?.find { it.id == current.selectedGoalId }
        val examCode = resolved?.examCode
            ?: MyApplication.getStringData(PreferenceKeys.EXAM_CODE.key)
            ?: ""

        if (!current.isNeetSelected && current.selectedGoalId.isNotEmpty() && examCode.isNotEmpty()) {
            fetchGrades(examCode, language)
        } else {
            clearGradesAndStreams()
        }
    }

    private fun clearGradesAndStreams() {
        gradesRequestId++
        streamsRequestId++
        _uiState.update {
            it.copy(
                classes = emptyList(),
                streams = emptyList(),
                streamModels = emptyList(),
                isClassesLoading = false,
                isStreamsLoading = false
            )
        }
    }

    private fun clearStreams() {
        streamsRequestId++
        _uiState.update {
            it.copy(
                streams = emptyList(),
                streamModels = emptyList(),
                isStreamsLoading = false
            )
        }
    }

    /**
     * Builds a GET whose `Accept-Language` is the medium selected **on this
     * screen** instead of the one persisted in SharedPreferences.
     *
     * [VolleyRequestHelper.getHeaders] derives `Accept-Language` from
     * [PreferenceKeys.MY_LANG], which is only written when the profile is saved.
     * Without this override every exam / grade / stream list came back in the
     * previously saved medium, no matter which medium card was tapped.
     */
    private fun languageAwareGet(
        url: String,
        language: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (VolleyError) -> Unit
    ): MyJsonObjectRequest {
        val headerLanguage = languageHeaderValue(language)
        Log.d("AcademicDetailsVM", "GET $url [Accept-Language: $headerLanguage]")
        return object : MyJsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            Response.Listener<JSONObject> { response -> onSuccess(response) },
            Response.ErrorListener { error -> onError(error) }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = super.getHeaders()
                headers[HEADER_ACCEPT_LANGUAGE] = headerLanguage
                return headers
            }
        }
    }

    /** Maps "en" / "hi" / "ENGLISH" / "HINDI" to the value the backend expects. */
    private fun languageHeaderValue(medium: String?): String =
        if (ArivihanLanguageEnum.fromString(medium ?: "") == ArivihanLanguageEnum.HINDI) {
            LANGUAGE_HINDI
        } else {
            LANGUAGE_ENGLISH
        }

    private fun isNeetGoal(goal: OnBoardingSelectionModel?): Boolean {
        if (goal == null) return false
        return goal.examCode?.equals(EXAM_CODE_NEET, ignoreCase = true) == true
    }

    private fun gradeCodeOf(grade: GradeModel): String =
        grade.gradeCode ?: grade.code ?: grade.id?.toString() ?: ""

    /**
     * `neumericValue` is language independent, so it is preferred over the
     * display name - a Hindi grade label does not necessarily contain "10".
     */
    private fun isClass10Grade(grade: GradeModel): Boolean {
        if (grade.neumericValue > 0) return grade.neumericValue == 10
        return grade.displayName?.contains("10", ignoreCase = true) == true ||
                grade.name?.contains("10", ignoreCase = true) == true
    }

    private companion object {
        const val HEADER_ACCEPT_LANGUAGE = "Accept-Language"
        const val LANGUAGE_ENGLISH = "ENGLISH"
        const val LANGUAGE_HINDI = "HINDI"
        const val EXAM_CODE_NEET = "NEET"
        //const val EXAM_ID_NEET = "3"
    }

    fun onNameChanged(newName: String) {
        if (newName.length > 20) return
        val config = UserNameValidationConfig.fromRemoteConfig()
        val error = config.getValidationError(newName, getApplication<Application>().getString(R.string.name_required_error))
        
        _uiState.update { 
            it.copy(
                username = newName,
                nameError = error
            )
        }
    }

    // -------------------------------------------------------------------------
    // SAVE LOGIC
    // -------------------------------------------------------------------------

    fun onSaveClicked() {
        Log.d("AcademicDetailsVM", "onSaveClicked called")
        val state = _uiState.value
        val context = getApplication<Application>().applicationContext

        val config = UserNameValidationConfig.fromRemoteConfig()
        val nameValidationError = config.getValidationError(state.username, context.getString(R.string.name_required_error))

        // Validation logic
        val errorMessage = when {
            state.selectedMedium.isEmpty() -> context.getString(R.string.please_select_language)
            state.selectedGoalId.isEmpty() -> context.getString(R.string.please_select_exam)
            !state.isNeetSelected && state.selectedClassId.isEmpty() -> context.getString(R.string.fill_your_class)
            !state.isNeetSelected && !state.isClass10Selected && state.selectedStreamId.isEmpty() -> context.getString(R.string.please_select_subject)
            nameValidationError != null -> nameValidationError
            else -> null
        }

        if (errorMessage != null) {
            _uiState.update { it.copy(error = errorMessage, nameError = nameValidationError, errorColorRes = R.color.error_button_color) }
            return
        }

        _uiState.update { it.copy(showSaveConfirmation = true) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, errorColorRes = null) }
    }

    fun onDismissConfirmation() {
        Log.d("AcademicDetailsVM", "onDismissConfirmation called")
        _uiState.update { it.copy(showSaveConfirmation = false) }
    }

    fun onConfirmSave() {
        Log.d("AcademicDetailsVM", "onConfirmSave called")
        _uiState.update { it.copy(isLoading = true, showSaveConfirmation = false) }
        saveData()
    }

    private fun saveData() {
        Log.d("AcademicDetailsVM", "saveData called")
        val state = _uiState.value
        val selectedGoal = state.selectedOnboardingModel ?: state.goals.find { it.id == state.selectedGoalId }
        
        if (selectedGoal == null) {
            Log.e("AcademicDetailsVM", "saveData: selectedGoal is null!")
            _uiState.update { it.copy(isLoading = false, error = "Please select a goal", errorColorRes = R.color.error_button_color) }
            return
        }

        val request = UpdateAcademicDetailsRequest()
        request.setUserName(state.username.trim())
        request.setLanguage(state.selectedMedium)
        
        // Use the numeric ID from the model
        val examId = examIdFromHome ?: selectedGoal.id?.toIntOrNull()
        request.setExamId(examId)

        if (!state.isNeetSelected) {
            val gradeId = state.selectedGrade?.gradeId ?: state.selectedGrade?.id ?: state.selectedClassId.toIntOrNull()
            request.setGradeId(gradeId)

            if (gradeId == null) {
                Log.e("AcademicDetailsVM", "saveData: gradeId is null!")
                _uiState.update { it.copy(isLoading = false, error = "Please select a class", errorColorRes = R.color.error_button_color) }
                return
            }

            if (!state.isClass10Selected) {
                val streamId = state.selectedStream?.id ?: state.selectedStreamId.toIntOrNull()
                request.setStreamId(streamId)
                
                if (streamId == null) {
                    Log.e("AcademicDetailsVM", "saveData: streamId is null!")
                    _uiState.update { it.copy(isLoading = false, error = "Please select a stream", errorColorRes = R.color.error_button_color) }
                    return
                }
            }
            request.setAcademicYearId(state.selectedGrade?.academicYearId)
        }

        Log.d("AcademicDetailsVM", "--- Saving Academic Details ---")
        Log.d("AcademicDetailsVM", "Name: ${request.userName}")
        Log.d("AcademicDetailsVM", "Exam ID: ${request.examId}")
        Log.d("AcademicDetailsVM", "Grade ID: ${request.gradeId}")
        Log.d("AcademicDetailsVM", "Stream ID: ${request.streamId}")
        Log.d("AcademicDetailsVM", "Academic Year ID: ${request.academicYearId}")
        Log.d("AcademicDetailsVM", "Language: ${request.language}")
        Log.d("AcademicDetailsVM", "Is NEET: ${state.isNeetSelected}")
        Log.d("AcademicDetailsVM", "Is Class 10: ${state.isClass10Selected}")
        Log.d("AcademicDetailsVM", "-------------------------------")

        updateAcademicDetails(request)
    }

    private fun updateAcademicDetails(request: UpdateAcademicDetailsRequest) {
        val userId = MyApplication.getStringData(PreferenceKeys.USER_ID.key)
        val url = CommonMethods.getUrlParam(UrlRoutes.GET_ACADEMIC_DETAILS, HashMap())

        val requestBody = JSONObject(gson.toJson(request))

        Log.d("AcademicDetailsVM", "PUT Request to: $url")
        Log.d("AcademicDetailsVM", "Payload: $requestBody")
        Log.d("AcademicDetailsVM", "User ID: $userId")
        Log.d("AcademicDetailsVM", "Language: ${request.language}")

        val volleyRequest = object : MyJsonObjectRequest(
            Method.PUT,
            url,
            requestBody,
            { response -> handleAcademicDetailsSuccess(response) },
            { error -> handleAcademicDetailsError(error) }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = super.getHeaders()
                headers["userId"] = userId ?: ""
                headers["Accept-Language"] = request.language ?: "en"
                return headers
            }
        }
        MyVolleySingleton.getInstance(getApplication()).addToRequestQueue(volleyRequest)
    }

    private fun handleAcademicDetailsSuccess(response: JSONObject) {
        Log.d("AcademicDetailsVM", "Response from backend: $response")
        try {
            if (response.optBoolean("success")) {
                val data = response.optJSONObject("data")
                if (data != null) {
                    val userDetails = gson.fromJson(data.toString(), UserDetailsResponse::class.java)
                    
                    // 1. Clear home screen caches
                    MyApplication.getInstance()?.homeScreenViewModel?.removeDataModel()

                    // 2. Clear onboarding selection model
                    MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.DO_NOT_DELETE_SELECTED_COURSE_MODEL.getKey())

                    // --- Capture old batch-keyed values BEFORE saveUserProfileLocally overwrites them ---
                    val oldCourseId = MyApplication.getStringData(PreferenceKeys.COURSE_ID.getKey())

                    // 3. Firebase nodes whose paths use old BATCH_CODE / CLASS_ID / COURSE_ID — clear before profile save
                    MyApplication.getToppersNotesStatusDatabaseReference().setValue(null)
                    MyApplication.deleteLiveSessionStatus()

                    // 4. Save fresh profile (overwrites BATCH_CODE, CLASS_ID, COURSE_ID, etc.)
                    MyApplication.saveUserProfileLocally(userDetails)

                    // 5. Ensure login state stays true
                    MyApplication.saveData(PreferenceKeys.USER_LOGGED_IN.key, true)

                    // 6. Re-evaluate competitive profile flag
                    MyApplication.isCompetitiveProfile()

                    // 7. Firebase nodes keyed only by userId — safe to clear after profile save
                    MyApplication.getTestGenerationParentDatabaseReference().setValue(null)
                    MyApplication.getNeetWeakTopicDatabaseReference().setValue(null)

                    // 8. Stale QR code cache tied to old batch pricing
                    MyApplication.removeString(PreferenceKeys.QR_CODE_IMAGE.key)
                    MyApplication.removeString(PreferenceKeys.QR_CODE_IMAGE_TIMESTAMP.key)
                    MyApplication.removeString(PreferenceKeys.QR_CODE_ID.key)
                    MyApplication.removeString(PreferenceKeys.QR_CODE_ORDER_ID.key)
                    MyApplication.removeString(PreferenceKeys.QR_CODE_SUBSCRIPTION_DTO.key)

                    // 9. PINJRATOD diagnostic state (keyed by old courseId)
                    if (!oldCourseId.isNullOrEmpty()) {
                        MyApplication.removeString(PreferenceKeys.PINJRATOD_CARD_TAPPED_PREFIX.getKey() + oldCourseId)
                        MyApplication.removeString(PreferenceKeys.PINJRATOD_TEST_ATTEMPTED_PREFIX.getKey() + oldCourseId)
                    }

                    // 10. ASAT state — availability is batch-specific
                    MyApplication.removeString(PreferenceKeys.ASAT_LAST_CALL_BACK_TIME.getKey())
                    MyApplication.removeString(PreferenceKeys.ASAT_TITLE.getKey())
                    MyApplication.saveData(PreferenceKeys.SHOW_ASAT.getKey(), false)
                    MyApplication.removeString(PreferenceKeys.HIDE_ASAT_NEW_BADGE.getKey())

                    // 11. Syllabus must be regenerated for the new batch
                    MyApplication.saveData(PreferenceKeys.SYLLABUS_GENERATED.getKey(), false)

                    // 12. NEET adaptive path state — irrelevant for Board users
                    MyApplication.removeString(PreferenceKeys.ADAPTIVE_SELECTED_CHAPTER_ID.getKey())
                    MyApplication.removeString(PreferenceKeys.ADAPTIVE_SELECTED_TOPIC_ID.getKey())
                    MyApplication.saveData(PreferenceKeys.ADAPTIVE_CHAPTER_COMPLETION_SCREEN_VISIBLE.getKey(), false)
                    MyApplication.removeString(PreferenceKeys.NEET_WEAK_TOPIC_SELECTION_VIDEO_COUNT.getKey())

                    // 13. Completed lectures list is course-specific
                    MyApplication.removeString(PreferenceKeys.COMPLETED_LECTURES.getKey())

                    // 14. Stale course ID in do-not-delete prefs
                    MyApplication.removeFromDoNotDeleteSharedPreference(PreferenceKeys.SELECTED_COURSE_ID.getKey())

                    // 15. Room DB: clear media rows tied to old batch courses
                    val userId = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())
                    MyApplication.getInstance()?.getMediaViewModel()?.clearUserData(userId)

                    Log.d("AcademicDetailsVM", "Batch change: all old-batch caches cleared. Redirecting to Home.")
                    _uiState.update { it.copy(isLoading = false, isSaveSuccess = true, error = null) }
                }
            } else {
                val message = response.optString("message", "Failed to update academic details")
                Log.e("AcademicDetailsVM", "Backend returned failure: $message")
                _uiState.update { it.copy(isLoading = false, error = message, errorColorRes = R.color.error_button_color) }
            }
        } catch (e: Exception) {
            Log.e("AcademicDetailsVM", "Error parsing response", e)
            _uiState.update { it.copy(isLoading = false, error = "Failed to process server response", errorColorRes = R.color.error_button_color) }
        }
    }

    private fun handleAcademicDetailsError(error: VolleyError) {
        var message = "Failed to update academic details"
        try {
            val responseData = error.networkResponse?.data
            if (responseData != null) {
                val responseStr = String(responseData)
                Log.e("AcademicDetailsVM", "Error response from backend: $responseStr")
                val json = JSONObject(responseStr)
                message = json.optString("message", message)
            }
        } catch (e: Exception) {
            Log.e("AcademicDetailsVM", "Error parsing error response", e)
        }
        _uiState.update { it.copy(isLoading = false, error = message, errorColorRes = R.color.error_button_color) }
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private fun updateUIStrings(mediumId: String) {
        val context = getApplication<Application>().applicationContext
        val configuration = Configuration(context.resources.configuration)
        
        // Map API code "ENGLISH"/"HINDI" back to locale "en"/"hi"
        val localeCode = ArivihanLanguageEnum.fromStringToCode(mediumId) ?: "en"
        configuration.setLocale(Locale.forLanguageTag(localeCode))
        val localizedContext = context.createConfigurationContext(configuration)

        _uiState.update { 
            it.copy(
                titleText = localizedContext.getString(R.string.academic_details_title),
                subtitleText = localizedContext.getString(R.string.academic_details_subtitle),
                mediumSectionTitle = localizedContext.getString(R.string.medium_section_title),
                goalSectionTitle = localizedContext.getString(R.string.goal_section_title),
                classSectionTitle = localizedContext.getString(R.string.class_section_title),
                streamSectionTitle = localizedContext.getString(R.string.stream_section_title),
                nameSectionTitle = localizedContext.getString(R.string.name_section_title),
                cancelText = localizedContext.getString(R.string.edit_cancel),
                saveText = localizedContext.getString(R.string.edit_save),
                namePlaceholder = localizedContext.getString(R.string.enter_your_name_placeholder),
                confirmationTitle = localizedContext.getString(R.string.confirmation_title),
                confirmationBody = localizedContext.getString(R.string.confirmation_body),
                saveChangesText = localizedContext.getString(R.string.save_changes),
                scrollDownText = localizedContext.getString(R.string.scroll_down),
                scrollUpText = localizedContext.getString(R.string.scroll_up)
            )
        }
    }

    private fun mapStreams(streamList: List<StreamModel>?): List<StreamOption> {
        return streamList?.map {
            StreamOption(
                id = it.code ?: "",
                name = it.displayName ?: "",
                iconUrl = it.iconUrl
            )
        } ?: emptyList()
    }

    private fun loadGoalOptions() {
        val courses = PreferenceMethods.getOnboardingCourseList()
        if (!courses.isNullOrEmpty()) {
            _uiState.update { it.copy(goals = courses) }
        }
    }
}
