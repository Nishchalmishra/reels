package arivihan.technologies.doubtbuzzter2.model.profile

import arivihan.technologies.doubtbuzzter2.model.NameIdModel
import arivihan.technologies.doubtbuzzter2.model.OnBoardingSelectionModel
import arivihan.technologies.doubtbuzzter2.model.batch.grade.GradeModel
import arivihan.technologies.doubtbuzzter2.model.batch.stream.StreamModel

data class AcademicDetailsState(
    val isLoading: Boolean = false,
    val isLanguagesLoading: Boolean = false,
    val isGoalsLoading: Boolean = false,
    val isClassesLoading: Boolean = false,
    val isStreamsLoading: Boolean = false,
    val username: String = "",
    val selectedMedium: String = "", // "en" or "hi"
    val selectedGoalId: String = "",
    val selectedClassId: String = "",
    val selectedStreamId: String = "",

    val mediums: List<MediumOption> = emptyList(),

    val goals: List<OnBoardingSelectionModel> = emptyList(),
    val selectedOnboardingModel: OnBoardingSelectionModel? = null,

    val classes: List<GradeModel> = emptyList(),
    val selectedGrade: GradeModel? = null,

    val streams: List<StreamOption> = emptyList(),
    val selectedStream: StreamModel? = null,
    val streamModels: List<StreamModel> = emptyList(),

    val isNeetSelected: Boolean = false,
    val isClass10Selected: Boolean = false,
    val showSaveConfirmation: Boolean = false,
    val isSaveSuccess: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val errorColorRes: Int? = null,
    
    // Localized Strings
    val titleText: String = "Apni Details Select Karein",
    val subtitleText: String = "Apni board, class, subject aur medium ki sahi details select karein.",
    val mediumSectionTitle: String = "Apna medium select karein!",
    val goalSectionTitle: String = "Apka lakshya kya hai?",
    val classSectionTitle: String = "Apni class select karein!",
    val streamSectionTitle: String = "Apna subject select karein!",
    val nameSectionTitle: String = "Apna naam batayein!",
    val cancelText: String = "Cancel",
    val saveText: String = "Save",
    val namePlaceholder: String = "Enter Your Name",
    val confirmationTitle: String = "Changes save karein?",
    val confirmationBody: String = "Save karte hi aapki details naye selections ke saath update ho jayengi.",
    val saveChangesText: String = "Save Changes",
    val scrollDownText: String = "Neeche jaayein",
    val scrollUpText: String = "Upar jaayein"
)

data class MediumOption(
    val id: String,
    val name: String
)

data class StreamOption(
    val id: String,
    val name: String,
    val iconUrl: String? = null
)
