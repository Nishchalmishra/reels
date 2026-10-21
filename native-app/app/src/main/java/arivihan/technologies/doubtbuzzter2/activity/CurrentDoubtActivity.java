package arivihan.technologies.doubtbuzzter2.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.DoubtAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.helper.network.NetworkStatusLiveData;
import arivihan.technologies.doubtbuzzter2.model.DoubtModel;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class CurrentDoubtActivity extends BaseActivity implements DoubtAdapter.OnDoubtClick {

    private RelativeLayout ParentLayout;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String userEmail;
    private int currentDoubtLoaded = 0;
    ProgressBar progressBar;
    private RecyclerView recyclerView;
    private LottieAnimationView NoData;
    private LinearLayout back;
    private TextView title;
    private List<DoubtModel> doubt = new ArrayList<>();
    NetworkStatusLiveData networkStatusLiveData;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_doubt);

        ParentLayout = findViewById(R.id.ParentRelativeLayout);
        progressBar = findViewById(R.id.progress_dataLoading);
        back = findViewById(R.id.back_arrow);
        title=findViewById(R.id.activity_title);
        title.setText(getResources().getString(R.string.Current_Doubt));
        recyclerView = findViewById(R.id.RecyclerViewCurrentDoubt);
        NoData = findViewById(R.id.lottieNoData);


        userEmail = MyApplication.getStringData(PreferenceKeys.USER_ID.getKey());


        recyclerView.setLayoutManager(new LinearLayoutManager(CurrentDoubtActivity.this, LinearLayoutManager.VERTICAL ,false));

        DoubtAdapter Adapter = new DoubtAdapter(doubt,this,this,"CurrentDoubt");
        recyclerView.setAdapter(Adapter);



        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        networkStatusLiveData = new NetworkStatusLiveData(this);
        networkStatusLiveData.observe(this, isOnline -> {
            if(isOnline){
                if(currentDoubtLoaded == 0){
                    currentDoubtLoaded = 1;
                    loadDoubt();
                }
                if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                    CommonMethods.showPositiveSnackbar(ParentLayout,getString(R.string.InternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
                }
            }
            else{
                if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())){
                    CommonMethods.showNegativeSnackbar(ParentLayout,getString(R.string.NoInternetConnection));
                    MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),false);
                    MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),true);
                }
            }
        });

        if(CommonMethods.isOnline(this)){
            if(currentDoubtLoaded == 0){
                currentDoubtLoaded = 1;
                loadDoubt();
            }
            if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_CONNECTED.getKey())) {
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
            }
        }
        else{
            if(!MyApplication.getBoolData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey())){
                MyApplication.saveData(PreferenceKeys.CONNECTION_CONNECTED.getKey(),true);
                MyApplication.saveData(PreferenceKeys.CONNECTION_DISCONNECTED.getKey(),false);
            }
        }




    }


    private void loadDoubt() {

        db.collection("Doubt")
                .whereEqualTo("Solved", false)
                .whereEqualTo("AskedBy", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    Log.d("Rey", " ReyMysterio=> onComplete ");
                    if (task.isSuccessful()) {
                        doubt.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            DoubtModel dm = new DoubtModel();
                            if(document.getData().get("Taken")!=null) {
                                if(document.getData().get("Taken").equals(true)) {
                                    //  statusList.add("Taken");
                                    Log.i("Status "," Taken");
                                    dm.setSolved(false);
                                }
                                else {
                                    // statusList.add("Published");
                                    Log.i("Status "," Published");
                                    dm.setSolved(true);
                                }
                            }
                            else {
                               // statusList.add("");
                                dm.setSolved(false);
                            }

                            if(document.getData().get("Text")!=null) {
                               // doubtList.add(document.getData().get("Text").toString());
                              //  chapterList.add(document.getData().get("Chapter").toString());
                             //   pointsList.add(document.getData().get("DoubtPoints").toString());

                                dm.setText(document.getData().get("Text").toString());
                                dm.setChapter(document.getData().get("Chapter").toString());
                                dm.setSubject(document.getData().get("Subject").toString());

                                if(document.getData().get("SolvedBy") != null) {
                                  //  solvedByList.add(document.getData().get("SolvedBy").toString());
                                    Log.i("Solved By ",document.getData().get("SolvedBy").toString());
                                }

                                if(document.getData().get("AskedAt") != null) {
                                    dm.setAskedAt(document.getData().get("AskedAt").toString());
                                }

                                if(document.getData().get("PreferredFormat")!=null) {
                                  //  preferredFormatList.add(document.getData().get("PreferredFormat").toString());
                                    dm.setPreferredFormat(document.getData().get("PreferredFormat").toString());
                                }
                                else {
                                  //  preferredFormatList.add("");
                                    dm.setPreferredFormat("Not Selected");
                                }

                                if(document.getData().get("Text").toString().isEmpty()) {
                                 //   imageReferenceList.add(document.getData().get("ImageID").toString());
                                    dm.setImageUrl(document.getData().get("ImageID").toString());
                                    dm.setDoubtType("IMAGE");
                                    }
                                else {
                                    dm.setDoubtType("TEXT");
                                }
                                if(!document.getData().get("Description").toString().isEmpty()) {
                                    //   imageReferenceList.add(document.getData().get("ImageID").toString());
                                    dm.setDescription(document.getData().get("Description").toString());
                                }
                                else{
                                    dm.setDescription("");
                                }
                            }
                            else {
                                // Nothing to show------
                            }


                            doubt.add(dm);


                        }

                        progressBar.setVisibility(View.GONE);
                        Collections.sort(doubt, (doubtModel1, doubtModel2) -> Long.compare(doubtModel2.getAskedOn(), doubtModel1.getAskedOn()));

                        DoubtAdapter adapter = new DoubtAdapter(doubt,CurrentDoubtActivity.this::ondoubtclick,CurrentDoubtActivity.this,"CurrentDoubt");
                        recyclerView.setAdapter(adapter);

                        if(doubt.size() == 0){
                            NoData.setVisibility(View.VISIBLE);
                        }
                        else{
                            NoData.setVisibility(View.GONE);
                        }

                    } else {
                        Log.d("RRey", "Error getting documents: ", task.getException());
                    }
                });



    }

    @Override
    public void ondoubtclick(int position) {
    }

}