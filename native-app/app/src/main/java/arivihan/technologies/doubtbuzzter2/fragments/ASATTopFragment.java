package arivihan.technologies.doubtbuzzter2.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.activity.MockTestAnalysisActivity;
import arivihan.technologies.doubtbuzzter2.activity.asat.ASATActivity;
import arivihan.technologies.doubtbuzzter2.adapter.AsatAdapter;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.ResultInfoStatus;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.model.ArivihanLanguageEnum;
import arivihan.technologies.doubtbuzzter2.model.asat.ASATScreenProcessInfo;
import arivihan.technologies.doubtbuzzter2.model.asat.ASATStepsInfo;
import arivihan.technologies.doubtbuzzter2.model.MarksheetInfo;
import arivihan.technologies.doubtbuzzter2.model.ResultInfo;
import arivihan.technologies.doubtbuzzter2.model.TestInfo;
import arivihan.technologies.doubtbuzzter2.model.UserAsatInfo;
import lombok.Setter;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;


public class ASATTopFragment extends Fragment {

    private static final String TAG = ASATTopFragment.class.getSimpleName();
    AsatAdapter asatAdapter;
    RecyclerView rv_step;
    ArrayList<ASATStepsInfo> list = new ArrayList<>();
    LinearLayout ll_result_screen, ll_main_screen;
    TextView titles, tv_user_score, tv_total_score, tv_user_percentage, tv_coupon_text, tv_text_marks;
    CardView cv_view_result;
    LinearLayout ll_view_result;
    ImageView iv_main_screen_scholarship_poster, iv_coupon;
    ASATScreenProcessInfo asatScreenProcessInfo;
    RelativeLayout rl_coupon_layout;
    boolean isResultScreen = false;
    UserAsatInfo userAsatInfo;
    @Setter
    AsatAdapter.AsatListener asatListener;

    public ASATTopFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_a_s_a_t_top, container, false);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                asatScreenProcessInfo = getArguments().getSerializable(ASATActivity.ASAT_SCREEN_PROCESS_INFO, ASATScreenProcessInfo.class);
            } else {
                asatScreenProcessInfo = (ASATScreenProcessInfo) getArguments().getSerializable("asat_screen_process_info");
            }
            isResultScreen = getArguments().getBoolean(ASATActivity.ASAT_SCREEN_TYPE_RESULT, false);

            iv_main_screen_scholarship_poster = view.findViewById(R.id.iv_main_screen_scholarship_poster);
            tv_user_score = view.findViewById(R.id.tv_user_score);
            tv_total_score = view.findViewById(R.id.tv_total_score);
            tv_user_percentage = view.findViewById(R.id.tv_user_percentage);
            rl_coupon_layout = view.findViewById(R.id.rl_coupon_layout);
            iv_coupon = view.findViewById(R.id.iv_coupon);
            tv_coupon_text = view.findViewById(R.id.tv_coupon_text);
            tv_text_marks = view.findViewById(R.id.tv_text_marks);
            ll_result_screen = view.findViewById(R.id.ll_result_screen);
            cv_view_result = view.findViewById(R.id.cv_view_result);
            ll_view_result = view.findViewById(R.id.ll_view_result);
            ll_main_screen = view.findViewById(R.id.ll_main_screen);
            titles = view.findViewById(R.id.titles);
            rv_step = view.findViewById(R.id.rv_step);
            asatAdapter = new AsatAdapter(requireActivity(), list, asatListener);
            rv_step.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false));
            rv_step.setAdapter(asatAdapter);

            ll_view_result.setVisibility(MyApplication.getFirebaseRemoteConfig().getBoolean("hideAsatViewFullResult")?View.GONE:View.VISIBLE);
            tv_text_marks.setText(getString(R.string.marks).toLowerCase(Locale.getDefault()));
            titles.setLineSpacing(8, 1);
            rl_coupon_layout.setVisibility(View.GONE);

            cv_view_result.setOnClickListener(cardView -> {
                if (asatScreenProcessInfo != null
                        && asatScreenProcessInfo.getUserAsatInfo() != null
                        && asatScreenProcessInfo.getUserAsatInfo().getTestInfo() != null) {

                    TestInfo testInfo = asatScreenProcessInfo.getUserAsatInfo().getTestInfo();

                    Intent intent = new Intent(requireContext(), MockTestAnalysisActivity.class);
                    intent.putExtra("testSeriesId", testInfo.getTestId());
                    intent.putExtra("testSeriesListId", testInfo.getTestGroupId());
                    intent.putExtra("afterAttempt", false);
                    intent.putExtra("testSeriesName", testInfo.getTestName());
                    intent.putExtra(MockTestAnalysisActivity.ANALYSIS_TYPE, MockTestAnalysisActivity.AnalysisType.ASAT.name());
                    startActivity(intent);

                    Map<String, String> param = new HashMap<>();
                    param.put("testSeriesId", testInfo.getTestId());
                    param.put("testSeriesListId", testInfo.getTestGroupId());
                    param.put("testSeriesName", testInfo.getTestName());
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.asat_test_attempted_result.getActionName(), param);
                }
            });


            ArivihanLanguageEnum languageEnum = MyApplication.getCurrentAppLanguageEnum();
            Glide.with(requireContext())
                    .load(languageEnum==ArivihanLanguageEnum.HINDI?R.drawable.img_asat_scholarship_hi:R.drawable.img_asat_scholarship)
                    .into(iv_main_screen_scholarship_poster);

            try{
                int screenWidth = requireContext().getResources().getDisplayMetrics().widthPixels;

                Glide.with(requireContext())
                        .load(R.drawable.img_asat_coupon_bg)
                        .override(screenWidth, Target.SIZE_ORIGINAL) // resize only width, keep original height ratio
                        .centerInside()
                        .into(iv_coupon);
            }
            catch (Exception ignored){}
            setData();
            Log.d(TAG,"Result : "+isResultScreen);
        }catch (Exception e){
            e.printStackTrace();
        }

        return view;
    }
    private void setData(){
        if(asatScreenProcessInfo !=null) {
            int oldStepSize = list.size();
            list.addAll(asatScreenProcessInfo.getAsatStepsInfo());
            asatAdapter.notifyItemRangeInserted(oldStepSize, asatScreenProcessInfo.getAsatStepsInfo().size());
            updateUserData(asatScreenProcessInfo);
        }
        else {
            Toast.makeText(requireContext(),getString(R.string.something_went_wrong),Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserData(ASATScreenProcessInfo processInfo) {
        titles.setText(StyledTextBuilder.fromJson(requireContext(), processInfo.getText1()));
        userAsatInfo = processInfo.getUserAsatInfo();
        ResultInfo resultinfo = userAsatInfo.getResultInfo();
        TestInfo testinfo = userAsatInfo.getTestInfo();
//        MarksheetInfo marksheetinfo = userAsatInfo.getMarksheetInfo();

        if (resultinfo!=null && testinfo!=null && testinfo.isAttempted() && isResultScreen) {

            String asatCouponStr =  MyApplication.getFirebaseRemoteConfig()
                    .getString("asat_result_coupon_text_" +
                            MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
            String resultStatus = resultinfo.getInfoStatus();
            titles.setText(getString(R.string.ASAT));
            if(resultStatus!=null && resultStatus.equals(ResultInfoStatus.PENDING.name())){
                titles.setText(StyledTextBuilder.fromJson(requireContext(), processInfo.getText2()));
            } else if(resultStatus!=null && (resultStatus.equals(ResultInfoStatus.VERIFIED.name()) || resultStatus.equals(ResultInfoStatus.REJECTED.name()))){
                titles.setText(StyledTextBuilder.fromJson(requireContext(), resultStatus.equals(ResultInfoStatus.VERIFIED.name())?processInfo.getText3():processInfo.getText4()));
                asatCouponStr = asatCouponStr.replace("{0}", String.valueOf((int)resultinfo.getPercentDiscount()));
                tv_coupon_text.setText(StyledTextBuilder.fromJson(requireContext(), asatCouponStr));
                if((int)resultinfo.getPercentDiscount()>0) {
                    rl_coupon_layout.setVisibility(View.VISIBLE);
                }
            }
            ll_result_screen.setVisibility(View.VISIBLE);
            rv_step.setVisibility(View.GONE);
            ll_main_screen.setVisibility(View.GONE);

            tv_user_score.setText(floatToString(testinfo.getMarks()));
            tv_total_score.setText(floatToString(testinfo.getTotalMarks()));
            tv_user_percentage.setText(floatToString(testinfo.getPercentage()) + "%");

        } else{
            ll_result_screen.setVisibility(View.GONE);
            rv_step.setVisibility(View.VISIBLE);
            ll_main_screen.setVisibility(View.VISIBLE);
        }

    }

    private String floatToString(float value) {
        String str;
        if (Math.abs(value - (int) value) < 0.0001f) {
            str = String.valueOf((int) value);
        } else {
            str = String.valueOf(value);
        }

        return str;
    }
}