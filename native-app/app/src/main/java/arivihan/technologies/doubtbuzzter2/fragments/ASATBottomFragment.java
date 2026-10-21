package arivihan.technologies.doubtbuzzter2.fragments;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.CALL_ME_BACK;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.ReelsActivity;
import arivihan.technologies.doubtbuzzter2.activity.asat.ASATActivity;
import arivihan.technologies.doubtbuzzter2.adapter.ToppersAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.datetime.DateTimeUtils;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.ResultInfoStatus;
import arivihan.technologies.doubtbuzzter2.enums.call_me_back.CallMeBackIdentifiers;
import arivihan.technologies.doubtbuzzter2.helper.StyledTextBuilder;
import arivihan.technologies.doubtbuzzter2.helper.WrapContentLinearLayoutManager;
import arivihan.technologies.doubtbuzzter2.model.ToppersHomeScreenModel;
import arivihan.technologies.doubtbuzzter2.model.asat.ASATScreenProcessInfo;
import arivihan.technologies.doubtbuzzter2.model.asat.CallBackResponse;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.volley.VolleyGlobalErrorListener;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class ASATBottomFragment extends Fragment implements ToppersAdapter.OnTopperClickListener {

    private static final String TAG = ASATBottomFragment.class.getSimpleName();
    private String asatMainScreenWhatsappMsg, asatResultScreenWhatsappMsg, asat_message_date_format, asatRulesAndRegulation, asatSyllabus, asat_call_back_start_time, asat_call_back_end_time;
    private float asat_main_screen_share_dates_count, asat_call_back_restrict_duration;

    //UI Variables

    private MaterialCardView cv_rules_regulations_expandable, cv_rules_regulations, cv_syllabus_expandable, cv_syllabus,
            cv_guidance_call_now, cv_call_now, cv_share_with_friends, cv_share_on;
    private TextView tv_rules_regulations_expandable, tv_rules_regulation, tv_syllabus, tv_syllabus_expandable, tv_cheer_up_text, tv_champion_title;
    private ImageView iv_rules_regulation_arrow, iv_syllabus_arrow;
    private RelativeLayout rl_cheer_up, rl_rules_regulations, rl_syllabus;
    private LinearLayout ll_main_layout, ll_topper_section;
    private RecyclerView rv_toppers;
    private ProgressBar pb_call_back;
    ToppersAdapter toppersAdapter;
    private ShimmerFrameLayout shimmer_toppers;

    //Data Variables
    private boolean isRulesAndRegulationsExpanded = false;
    private boolean isSyllabusExpanded = false;
    ArrayList<ToppersHomeScreenModel> toppersList = new ArrayList<>();
    Gson gson = new Gson();
    ASATScreenProcessInfo asatScreenProcessInfo;
    boolean isResultScreen = false;
    String rulesRegulations, syllabus, toppersListString;

    public ASATBottomFragment() {
        // Required empty public constructor
    }

    public static ASATBottomFragment newInstance(String param1, String param2) {
        ASATBottomFragment fragment = new ASATBottomFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_a_s_a_t_bottom, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            asatScreenProcessInfo = getArguments().getSerializable(ASATActivity.ASAT_SCREEN_PROCESS_INFO, ASATScreenProcessInfo.class);
        } else {
            asatScreenProcessInfo = (ASATScreenProcessInfo) getArguments().getSerializable(ASATActivity.ASAT_SCREEN_PROCESS_INFO);
        }
        isResultScreen = getArguments().getBoolean(ASATActivity.ASAT_SCREEN_TYPE_RESULT, false);
        rulesRegulations = getArguments().getString(ASATActivity.ASAT_RULES,"");
        syllabus = getArguments().getString(ASATActivity.ASAT_SYLLABUS,"");
        toppersListString = getArguments().getString(ASATActivity.ASAT_TOPPERS_LIST,"");

        ll_main_layout = view.findViewById(R.id.ll_main_layout);
        cv_rules_regulations_expandable = view.findViewById(R.id.cv_rules_regulations_expandable);
        cv_rules_regulations = view.findViewById(R.id.cv_rules_regulations);
        tv_rules_regulations_expandable = view.findViewById(R.id.tv_rules_regulations_expandable);
        tv_rules_regulation = view.findViewById(R.id.tv_rules_regulation);

        iv_rules_regulation_arrow = view.findViewById(R.id.iv_rules_regulation_arrow);

        cv_syllabus_expandable = view.findViewById(R.id.cv_syllabus_expandable);
        cv_syllabus = view.findViewById(R.id.cv_syllabus);
        tv_syllabus = view.findViewById(R.id.tv_syllabus);
        tv_syllabus_expandable = view.findViewById(R.id.tv_syllabus_expandable);
        iv_syllabus_arrow = view.findViewById(R.id.iv_syllabus_arrow);

        cv_guidance_call_now = view.findViewById(R.id.cv_guidance_call_now);
        cv_call_now = view.findViewById(R.id.cv_call_now);
        cv_share_with_friends = view.findViewById(R.id.cv_share_with_friends);
        cv_share_on = view.findViewById(R.id.cv_share_on);

        rl_cheer_up = view.findViewById(R.id.rl_cheer_up);
        rl_rules_regulations = view.findViewById(R.id.rl_rules_regulations);
        rl_syllabus = view.findViewById(R.id.rl_syllabus);
        ll_topper_section = view.findViewById(R.id.ll_topper_section);
        tv_cheer_up_text = view.findViewById(R.id.tv_cheer_up_text);
        tv_champion_title = view.findViewById(R.id.tv_champion_title);

        rv_toppers = view.findViewById(R.id.rv_toppers);
        shimmer_toppers = view.findViewById(R.id.shimmer_toppers);
        pb_call_back = view.findViewById(R.id.pb_call_back);

        pb_call_back.setVisibility(View.GONE);

        rv_toppers.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        cv_rules_regulations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRulesAndRegulationsExpanded = toggleCard(cv_rules_regulations_expandable, iv_rules_regulation_arrow, isRulesAndRegulationsExpanded);
                if(isRulesAndRegulationsExpanded) {
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.asat_rules_regulations_click.getActionName());
                }
            }
        });

        cv_syllabus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSyllabusExpanded = toggleCard(cv_syllabus_expandable, iv_syllabus_arrow, isSyllabusExpanded);
                if(isSyllabusExpanded) {
                    MyApplication.logFirebaseBundleCreation(FirebaseEvents.asat_syllabus_click.getActionName());
                }
            }
        });

        cv_guidance_call_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callForGuidance();
            }
        });

        cv_share_with_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareWithFriends();
            }
        });

        if(isResultScreen) {
            // For Result Screen
            rl_cheer_up.setVisibility(View.VISIBLE);
            ll_topper_section.setVisibility(View.VISIBLE);
            rl_rules_regulations.setVisibility(View.VISIBLE);
            rl_syllabus.setVisibility(View.GONE);
            cv_guidance_call_now.setVisibility(View.VISIBLE);
            cv_share_with_friends.setVisibility(View.VISIBLE);
            tv_champion_title.setVisibility(View.GONE);
        }
        else {
            // For Main Screen
            rl_cheer_up.setVisibility(View.GONE);
            ll_topper_section.setVisibility(View.VISIBLE);
            rl_rules_regulations.setVisibility(View.VISIBLE);
            rl_syllabus.setVisibility(View.VISIBLE);
            cv_guidance_call_now.setVisibility(View.GONE);
            cv_share_with_friends.setVisibility(View.VISIBLE);
            tv_champion_title.setVisibility(View.VISIBLE);
        }

        if(getArguments().getBoolean("hide_contact_section",false)){
            cv_guidance_call_now.setVisibility(View.GONE);
            cv_share_with_friends.setVisibility(View.GONE);
        }

        setRemoteConfigData();
        setHtmlText();
        setAdapters();
        toppersExtraction();
        return  view;
    }

    @Override
    public void onPause() {
        super.onPause();

        MyVolleySingleton.getInstance(requireContext()).getRequestQueue().cancelAll(TAG);
    }

    private void setRemoteConfigData()
    {
        try {
            asatMainScreenWhatsappMsg = MyApplication.getFirebaseRemoteConfig()
                    .getString(getString(R.string.asat_main_screen_whatsapp_message) + "_" +
                            MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

            asatResultScreenWhatsappMsg = MyApplication.getFirebaseRemoteConfig()
                    .getString(getString(R.string.asat_result_screen_whatsapp_message) + "_" +
                            MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

//            asatRulesAndRegulation = MyApplication.getFirebaseRemoteConfig()
//                    .getString(getString(R.string.asat_rules_regulation) + "_" +
//                            MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));
//
//            asatSyllabus = MyApplication.getFirebaseRemoteConfig()
//                    .getString(getString(R.string.asat_syllabus) + "_" +
//                            MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

//            asatGuidanceNumber = MyApplication.getFirebaseRemoteConfig()
//                    .getString(getString(R.string.asat_guidance_number));

            asat_main_screen_share_dates_count = MyApplication.getFirebaseRemoteConfig()
                    .getLong(getString(R.string.asat_main_screen_share_dates_count));

            asat_call_back_restrict_duration = MyApplication.getFirebaseRemoteConfig()
                    .getLong(getString(R.string.asat_call_back_restrict_duration));

            asat_message_date_format = MyApplication.getFirebaseRemoteConfig()
                    .getString(getString(R.string.asat_message_date_format));

            asat_call_back_start_time = MyApplication.getFirebaseRemoteConfig()
                    .getString(getString(R.string.asat_call_back_start_time));
            asat_call_back_end_time = MyApplication.getFirebaseRemoteConfig()
                    .getString(getString(R.string.asat_call_back_end_time));

//            if(rulesRegulations.isEmpty()) {
//                rulesRegulations = asatRulesAndRegulation;
//            }
//            if(syllabus.isEmpty()) {
//                syllabus = asatSyllabus;
//            }
            tv_rules_regulations_expandable.setText(MyApplication.fromHtmlCompat(rulesRegulations));
            tv_syllabus_expandable.setText(MyApplication.fromHtmlCompat(syllabus));
        } catch (Exception ignored) {}

    }

    private void setAdapters()
    {
        toppersAdapter = new ToppersAdapter(getContext(), toppersList, this);
        rv_toppers.setAdapter(toppersAdapter);
    }

    private void toppersExtraction() {
        shimmer_toppers.startShimmer();
        if (getContext() != null) {
            try {
                if (StringUtils.isNotEmpty(toppersListString)) {
                    TypeToken<ArrayList<ToppersHomeScreenModel>> typeToken
                            = new TypeToken<>() {
                    };
                    toppersList.clear();
                    toppersList.addAll(gson.fromJson(toppersListString, typeToken.getType()));
                    if (!CollectionUtils.isEmpty(toppersList)) {
                        toppersAdapter.notifyDataSetChanged();
                        rv_toppers.setVisibility(View.VISIBLE);
                        shimmer_toppers.hideShimmer();
                        shimmer_toppers.stopShimmer();
                    }

                }
            } catch (Exception e) {
                Log.d("toppersExtraction", e.getMessage());
            }
        }
    }

    private void setHtmlText()
    {
        String asat_result_topper_header = MyApplication.getFirebaseRemoteConfig()
                .getString(getString(R.string.asat_result_topper_header) + "_" +
                        MyApplication.getStringData(PreferenceKeys.MY_LANG.getKey()));

        tv_cheer_up_text.setText(StyledTextBuilder.fromJson(requireContext(), asat_result_topper_header));
    }

    private boolean toggleCard(final MaterialCardView expandableLayout, final ImageView arrowIcon, boolean isExpanded) {
        final int duration = 300;

        if (isExpanded) {
            // Collapse
            int initialHeight = expandableLayout.getMeasuredHeight();

            ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
            animator.setDuration(duration);
            animator.addUpdateListener(animation -> {
                Integer value = (Integer) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = expandableLayout.getLayoutParams();
                layoutParams.height = value;
                expandableLayout.setLayoutParams(layoutParams);
            });

            animator.start();
            expandableLayout.setVisibility(View.GONE);
            rotateArrow(arrowIcon, 180f, 0f);
            return false;

        } else {
            // Expand
            expandableLayout.setVisibility(View.VISIBLE);

            expandableLayout.measure(
                    View.MeasureSpec.makeMeasureSpec(((View) expandableLayout.getParent()).getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.UNSPECIFIED);
            int targetHeight = expandableLayout.getMeasuredHeight();

            ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
            animator.setDuration(duration);
            animator.addUpdateListener(animation -> {
                Integer value = (Integer) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = expandableLayout.getLayoutParams();
                layoutParams.height = value;
                expandableLayout.setLayoutParams(layoutParams);
            });

            animator.start();
            rotateArrow(arrowIcon, 0f, 180f);
            return true;
        }
    }

    private void rotateArrow(ImageView arrow, float from, float to) {
        ObjectAnimator rotate = ObjectAnimator.ofFloat(arrow, "rotation", from, to);
        rotate.setDuration(300);
        rotate.start();
    }

    private void callForGuidance()
    {
//        openWhatsAppPhone();
        String lastDateTime = MyApplication.getStringData(PreferenceKeys.ASAT_LAST_CALL_BACK_TIME.getKey());
        if(lastDateTime!=null && DateTimeUtils.getDifferenceInMinutes(lastDateTime)<=asat_call_back_restrict_duration) {
            if(DateTimeUtils.isCurrentTimeInRange(asat_call_back_start_time, asat_call_back_end_time)) {
                CommonMethods.showPositiveSnackbar(ll_main_layout, getString(R.string.aapko_30_min_mein_call_aayega), 3000);
            }
            else {
                CommonMethods.showPositiveSnackbar(ll_main_layout, getString(R.string.aapko_subah_tak_call_kiya_jayega), 3000);
            }
        } else {
            callMeBack();
        }
    }

    private void shareWithFriends()
    {
        try {
            StringBuilder messageBuilder = new StringBuilder();
            if(isResultScreen && asatScreenProcessInfo.getUserAsatInfo()!=null
                    && asatScreenProcessInfo.getUserAsatInfo().getResultInfo()!=null
                    && asatScreenProcessInfo.getUserAsatInfo().getResultInfo().getPercentDiscount()>0.0f
                    && !asatScreenProcessInfo.getUserAsatInfo().getResultInfo().getInfoStatus().equals(ResultInfoStatus.PENDING.name())
            ){
                messageBuilder.append(MessageFormat.format(asatResultScreenWhatsappMsg, ""+asatScreenProcessInfo.getUserAsatInfo().getResultInfo().getPercentDiscount()+"%"));
            } else {
                List<String> dateList = getFormattedDates((int)asat_main_screen_share_dates_count, asat_message_date_format);
                String dates = String.join("\n", dateList);
                messageBuilder.append(MessageFormat.format(asatMainScreenWhatsappMsg, dates));
            }

            shareOnWhatsApp(messageBuilder.toString());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void shareOnWhatsApp(String message) {

        try {

            Log.d(TAG, "Message here: "+message);
            String encodedMessage = message.replace("\\n", "\n");

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, encodedMessage);
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp"); // Targets WhatsApp directly
            startActivity(sendIntent);

            MyApplication.logFirebaseBundleCreation(isResultScreen?FirebaseEvents.asat_result_whatsapp_share_click.getActionName():FirebaseEvents.asat_main_whatsapp_share_click.getActionName());

        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), getResources().getString(R.string.whatsapp_not_found), Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {}
    }

    private void openDialPad(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (Exception ignored) {}
    }

    private void openWhatsAppPhone(String phone)
    {
        try {
            String url = "https://api.whatsapp.com/send?phone=" + phone;
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage("com.whatsapp");
            intent.setData(uri);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), getResources().getString(R.string.whatsapp_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    public List<String> getFormattedDates(int count, String dateFormat) {
        List<String> dateList = new ArrayList<>();

        // Handle negative or zero count
        if (count <= 0) {
            Log.w(TAG, "Invalid count: " + count);
            return dateList; // return empty list
        }

        // Attempt to create date formatter
        SimpleDateFormat formatter;
        try {
            formatter = new SimpleDateFormat(dateFormat, Locale.getDefault());
        } catch (IllegalArgumentException e) {
            Log.e("DateUtil", "Invalid date format: " + dateFormat + ", defaulting to 'yyyy-MM-dd'", e);
            formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // fallback format
        }

        // Generate formatted dates
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < count; i++) {
            try {
                dateList.add(formatter.format(calendar.getTime()));
            } catch (Exception e) {
                Log.e("DateUtil", "Date formatting failed on iteration " + i, e);
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        return dateList;
    }

    private void callMeBack(){

        try {
            cv_guidance_call_now.setClickable(false);
            cv_guidance_call_now.setFocusable(false);
            cv_guidance_call_now.setEnabled(false);
            pb_call_back.setVisibility(View.VISIBLE);

            Gson gson = new Gson();
            final Map<String, Object> map = new HashMap<>();
            JSONObject requestBody = new JSONObject();
            requestBody.put("phoneNumber", MyApplication.getStringData(PreferenceKeys.PHONE_NUMBER.getKey()));
            requestBody.put("userComment", "");
            requestBody.put(PreferenceKeys.USER_ID.getKey(), MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()));
            requestBody.put("raisedFrom", CallMeBackIdentifiers.ASAT.name());
            Log.d(TAG, "Request Body: "+requestBody.toString());
            MyJsonObjectRequest myJsonObjectRequest = new MyJsonObjectRequest(Request.Method.POST,
                    CommonMethods.getUrlParam(CALL_ME_BACK, map), requestBody,
                    response -> {
                        TypeToken<CallBackResponse> token = new TypeToken<CallBackResponse>() {
                        };
                        CallBackResponse callBackResponse = gson.fromJson(response.toString(), token);
                        if(callBackResponse.isSuccess()) {
                            if(DateTimeUtils.isCurrentTimeInRange(asat_call_back_start_time, asat_call_back_end_time)) {
                                CommonMethods.showPositiveSnackbar(ll_main_layout, getString(R.string.aapko_30_min_mein_call_aayega), 3000);
                            }
                            else {
                                CommonMethods.showPositiveSnackbar(ll_main_layout, getString(R.string.aapko_subah_tak_call_kiya_jayega), 3000);
                            }
                            MyApplication.saveData(PreferenceKeys.ASAT_LAST_CALL_BACK_TIME.getKey(),DateTimeUtils.getCurrentDateTime());
                        } else {
                            CommonMethods.showNegativeSnackbar(ll_main_layout, getString(R.string.something_went_wrong), 3000);
                        }

                        cv_guidance_call_now.setClickable(true);
                        cv_guidance_call_now.setFocusable(true);
                        cv_guidance_call_now.setEnabled(true);
                        pb_call_back.setVisibility(View.GONE);

                        Log.d(TAG, "Response: " + response.toString());
                    },
                    new VolleyGlobalErrorListener(requireContext()) {
                        @Override
                        public void postError(VolleyError error) {
                            Log.e(TAG, "Error updating video selection: " + error.getMessage());

                            Toast.makeText(requireContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            cv_guidance_call_now.setClickable(true);
                            cv_guidance_call_now.setFocusable(true);
                            cv_guidance_call_now.setEnabled(true);
                            pb_call_back.setVisibility(View.GONE);
                        }
                    });
            myJsonObjectRequest.setTag(TAG);
            MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(myJsonObjectRequest);

            MyApplication.logFirebaseBundleCreation(FirebaseEvents.asat_guidance_click.getActionName());
        }
        catch (Exception ignored){
            cv_guidance_call_now.setClickable(true);
            cv_guidance_call_now.setFocusable(true);
            cv_guidance_call_now.setEnabled(true);
            pb_call_back.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTopperClick(String videoUrl) {
        try {
            if (StringUtils.isNotEmpty(videoUrl)) {
                Intent intent = new Intent(requireActivity(), ReelsActivity.class);
                intent.putExtra(MainDashboard.KEY_VIDEO_URI, videoUrl);
                Map<String, String> params = new HashMap<>();
                params.put("video_url", videoUrl);
                params.put("open_from", TAG);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.champions_of_arivihan_clicked.getActionName(), params);
                MyApplication.logFirebaseBundleCreation(FirebaseEvents.asat_toppers_click.getActionName(), params);

                startActivity(intent);
            }

        }catch (Exception ignored){}
    }
}