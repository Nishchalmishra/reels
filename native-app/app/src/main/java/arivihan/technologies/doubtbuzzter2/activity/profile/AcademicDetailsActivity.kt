package arivihan.technologies.doubtbuzzter2.activity.profile

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.activity.BaseActivity
import arivihan.technologies.doubtbuzzter2.activity.HomeScreen
import arivihan.technologies.doubtbuzzter2.compose.ArivihanComposeApp
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents
import arivihan.technologies.doubtbuzzter2.viewModel.profile.AcademicDetailsViewModel

class AcademicDetailsActivity : BaseActivity() {

    private lateinit var viewModel: AcademicDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyApplication.logFirebaseBundleCreation(FirebaseEvents.academic_details_screen_view.actionName)
        viewModel = ViewModelProvider(this)[AcademicDetailsViewModel::class.java]

        val examIdFromIntent = intent.getIntExtra("examId", -1)
        if (examIdFromIntent != -1) {
            viewModel.setExamIdFromHome(examIdFromIntent)
        }

        setContent {
            ArivihanComposeApp {
                AcademicDetailsScreen(
                    viewModel = viewModel,
                    onBack = { finish() },
                    onSaveSuccess = {
                        val intent = Intent(this, HomeScreen::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}
