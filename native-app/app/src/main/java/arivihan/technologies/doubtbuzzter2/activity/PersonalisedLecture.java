package arivihan.technologies.doubtbuzzter2.activity;

import static arivihan.technologies.doubtbuzzter2.activity.HomeScreen.BroadcastStringForAction;
import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.GET_PERSONALISED_CHAPTER_LIST;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;
import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.PersonalisedLectureByDateAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.QuestionsAdapter;
import arivihan.technologies.doubtbuzzter2.adapter.TopicDetailAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.listener.ChapterDetailActivityListener;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureContentDto;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureInfoDto;
import arivihan.technologies.doubtbuzzter2.model.TopicModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;


public class PersonalisedLecture extends BaseActivity implements QuestionsAdapter.OnQuestionClick, TopicDetailAdapter.OnTopicClick, ChapterDetailActivityListener {


    private RecyclerView lecturesRecyclerView;
    private ImageView openInfoBtn;
    private LinearLayout back;
    Toolbar toolbar;

    TextView title;
    private Dialog infoDialog;
    private DatePickerDialog datePickerDialog;
    private CardView infoDialogTooltip;
    private RecyclerView lectureByDateRecyclerView;
    private ExtendedFloatingActionButton floatingActionButton;
    private ShimmerFrameLayout shimmer;
    private final Gson gson = new Gson();
    private ArrayList<TopicModel> topics = new ArrayList<>();
    private LinearLayout noDataView;
    private int selectedMonth, selectedYear;
    boolean dataLoaded;
    NetworkStatusLiveData networkStatusLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personalised_lecture);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        back = findViewById(R.id.back_arrow);
        title = findViewById(R.id.activity_title);
        title.setText(getResources().getString(R.string.personalised_lecture));

        openInfoBtn = findViewById(R.id.open_info_btn);
        lectureByDateRecyclerView = findViewById(R.id.lectures_by_date);
        floatingActionButton = findViewById(R.id.personalised_date_selector);
        shimmer = findViewById(R.id.personalised_shimmer);
        noDataView = findViewById(R.id.nodata);
        Calendar calendar = Calendar.getInstance();

        selectedMonth = calendar.get(Calendar.MONTH);
        selectedYear = calendar.get(Calendar.YEAR);

        getPersonalisedChapterList(selectedMonth, selectedYear);

        lectureByDateRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) || !recyclerView.isScrollContainer()) {
                    floatingActionButton.setVisibility(View.VISIBLE);
                } else {
                    floatingActionButton.setVisibility(View.GONE);
                }
            }
        });

        lectureByDateRecyclerView.setVisibility(View.GONE);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMonthYearPickerDialog(PersonalisedLecture.this);
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        networkStatusLiveData = new NetworkStatusLiveData(this, false);
        networkStatusLiveData.observe(this, isOnline -> {
            if(isOnline){
                if (!dataLoaded) {
                    getPersonalisedChapterList(selectedMonth, selectedYear);
                }
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(title, MyApplication.getInstance().getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), false);
                }
            }
            else{
                if (!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())) {
                    CommonMethods.showNegativeSnackbar(title, MyApplication.getInstance().getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(), false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(), true);
                }
            }
        });

    }

    private void showInfoDialog() {
        infoDialog = new Dialog(PersonalisedLecture.this);
        infoDialog.setCancelable(true);
        infoDialog.setContentView(R.layout.dialog_personalised_lecture_info);

        infoDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.bachground_custom_dialogu_typeactivity));
        infoDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        EdgeToEdgeHelper.applyToDialog(infoDialog);

        ImageView infoDialogCloseBtn = infoDialog.findViewById(R.id.close_dialog_btn);
        infoDialogCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoDialog.dismiss();
            }
        });
    }


    public void showMonthYearPickerDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.month_and_year_picker, null);
        DatePicker datePicker = dialogView.findViewById(R.id.datePicker);

        ViewGroup dayDatePicker = (ViewGroup) datePicker.getChildAt(0);
        ((ViewGroup) dayDatePicker.getChildAt(0)).getChildAt(1).setVisibility(View.GONE);

        datePicker.init(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), 1, null);
        datePicker.setMaxDate(Calendar.getInstance().getTimeInMillis());

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView)
                .setTitle(getResources().getString(R.string.Select_Month_and_Year))

                .setPositiveButton(getResources().getString(R.string.okay), (dialog, which) -> {

                    selectedMonth = datePicker.getMonth();
                    selectedYear = datePicker.getYear();

                    dataLoaded = false;
                    getPersonalisedChapterList(selectedMonth, selectedYear);

                })
                .setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> {
                });

        builder.create().show();
    }


    private void getPersonalisedChapterList(int month, int year) {
        shimmer.startShimmer();
        shimmer.setVisibility(View.VISIBLE);
        noDataView.setVisibility(View.GONE);
        lectureByDateRecyclerView.setVisibility(View.GONE);

        Map<String, Object> map = new HashMap<>();
        map.put("month", month);
        map.put("year", year);

        MyJsonObjectRequest lectureRequest = new MyJsonObjectRequest(Request.Method.GET,
                CommonMethods.getUrlParam(GET_PERSONALISED_CHAPTER_LIST, map),
                null,
                response -> {
                    dataLoaded = true;
                    TypeToken<Map<String, ArrayList<MicroLectureInfoDto>>> token = new TypeToken<Map<String, ArrayList<MicroLectureInfoDto>>>() {
                    };


                    Map<String, ArrayList<MicroLectureInfoDto>> personalisedMicrolectureList = null;
                    try {
                        personalisedMicrolectureList = gson.fromJson(response.getJSONObject("microlectureContentList").toString(), token.getType());
                        Log.d("PERSONALISED_LECTURES", personalisedMicrolectureList.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    updateRecyclerView(personalisedMicrolectureList);
                    if (personalisedMicrolectureList.size() == 0) {
                        noDataView.setVisibility(View.VISIBLE);
                        floatingActionButton.setVisibility(View.VISIBLE);
                    } else {
                        noDataView.setVisibility(View.GONE);
                    }


                }, new VolleyGlobalErrorListener(getApplicationContext()) {
            @Override
            public void postError(VolleyError error) {
                Log.i("personalisedM", error.toString());
                Toast.makeText(PersonalisedLecture.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        MyVolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(lectureRequest);

    }

    private void updateRecyclerView(Map<String, ArrayList<MicroLectureInfoDto>> personalisedMicrolectureList) {
        shimmer.stopShimmer();
        shimmer.setVisibility(View.GONE);
        lectureByDateRecyclerView.setVisibility(View.VISIBLE);


        PersonalisedLectureByDateAdapter personalisedLectureByDateAdapter = new PersonalisedLectureByDateAdapter(personalisedMicrolectureList);
        lectureByDateRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        lectureByDateRecyclerView.setAdapter(personalisedLectureByDateAdapter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        dataLoaded = false;
        getPersonalisedChapterList(selectedMonth, selectedYear);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
//        setIntent(intent);
    }


    @Override
    public void ontopicClick(int position) {

    }

    @Override
    public void onFinish() {

    }

    @Override
    public void onquestionclick(int position, String id) {

    }

    @Override
    public void goToActivity(MicroLectureContentDto microLectureContentDto, int position) {

    }
}