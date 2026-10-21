package arivihan.technologies.doubtbuzzter2.fragments;

import static arivihan.technologies.doubtbuzzter2.fragments.MainDashboard.callMeBackReferAndWin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.activity.CampaignSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.ProPlanSubscriptionActivity;
import arivihan.technologies.doubtbuzzter2.activity.SubscriptionPaymentScreen;
import arivihan.technologies.doubtbuzzter2.enums.RedirectionSource;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;
import arivihan.technologies.doubtbuzzter2.helper.ActivityLauncher;
import arivihan.technologies.doubtbuzzter2.helper.subscription.SubscriptionNavigationHelper;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.CarouselAdapter;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.common.CommonNavigator;
import arivihan.technologies.doubtbuzzter2.enums.FirebaseEvents;
import arivihan.technologies.doubtbuzzter2.enums.call_me_back.CallMeBackIdentifiers;
import arivihan.technologies.doubtbuzzter2.listener.OnCarouselClickListener;
import arivihan.technologies.doubtbuzzter2.model.carousel.CarouselResponse;

public class CarouselFragment extends Fragment implements OnCarouselClickListener {

    private static final String TAG = CarouselFragment.class.getSimpleName();
    private static final String ARG_LIST = "carousel_list";
    private static long AUTO_SCROLL_DELAY = 2500;

    private ViewPager2 viewPager;

    private Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private Runnable autoScrollRunnable;
    private boolean isUserInteracting = false;

    private int savedCarouselPosition;
    private ArrayList<CarouselResponse> items = new ArrayList<>();
    private ArrayList<CarouselResponse> wrappedItems = new ArrayList<>();
    private OnCarouselClickListener listener;
    private ConstraintLayout cl_parent_layout;

    public static CarouselFragment newInstance(ArrayList<CarouselResponse> list) {
        CarouselFragment fragment = new CarouselFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LIST, list);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (getParentFragment() instanceof OnCarouselClickListener) {
            listener = (OnCarouselClickListener) getParentFragment();
        } else if (context instanceof OnCarouselClickListener) {
            listener = (OnCarouselClickListener) context;
        } else {
            throw new RuntimeException(
                    "Parent must implement OnCarouselClickListener"
            );
        }
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_carousel, container, false);
        viewPager = view.findViewById(R.id.viewPagerCarousel);
        cl_parent_layout = view.findViewById(R.id.cl_parent_layout);
        cl_parent_layout.setClipChildren(false);
        AUTO_SCROLL_DELAY = MyApplication.getFirebaseRemoteConfig().getLong("carousel_list_auto_scroll_timer")+4000;

        if (getArguments() != null) {
            items = (ArrayList<CarouselResponse>) getArguments().getSerializable(ARG_LIST);
        }
        savedCarouselPosition = MyApplication.getIntData(PreferenceKeys.HOME_CAROUSEL_POSITION.getKey());
        if (savedCarouselPosition <= 0) {
            savedCarouselPosition = 1;
        }

        if (items == null || items.isEmpty()) return view;

        setViewPagerAspectRatio();
        setupCarousel();
        startAutoScroll();
        return view;
    }


    private void setupCarousel() {
        if (items == null || items.isEmpty()) return;

        wrappedItems = new ArrayList<>();

        wrappedItems.add(items.get(items.size() - 1));
        wrappedItems.addAll(items);
        wrappedItems.add(items.get(0));

        CarouselAdapter adapter = new CarouselAdapter(wrappedItems, requireContext(), this);

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(savedCarouselPosition, false);

        RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer((page, position) -> {
            float scale = 1f - 0.12f * Math.abs(position);
            page.setScaleX(scale);
            page.setScaleY(scale);
            page.setAlpha(0.6f + (1f - Math.abs(position)) * 0.4f);
        });
        viewPager.setPageTransformer(transformer);

        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv,
                                                 @NonNull MotionEvent e) {

                switch (e.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        isUserInteracting = true;
                        stopAutoScroll();
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isUserInteracting = false;
                        startAutoScroll();
                        break;
                }

                return false;
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    viewPager.post(() -> viewPager.setCurrentItem(items.size(), false));
                } else if (position == wrappedItems.size() - 1) {
                    viewPager.post(() -> viewPager.setCurrentItem(1, false));
                }
                MyApplication.saveData(PreferenceKeys.HOME_CAROUSEL_POSITION.getKey(), position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    isUserInteracting = true;
                    stopAutoScroll();
                }

                if (state == ViewPager2.SCROLL_STATE_IDLE && !isUserInteracting) {
                    startAutoScroll();
                }
            }
        });

        startAutoScroll();
    }

    private void startAutoScroll() {

        stopAutoScroll();

        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {

                if (viewPager != null
                        && !isUserInteracting
                        && items != null
                        && !items.isEmpty()) {

                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);

                    autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY);
                }
            }
        };

        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
    }

    private void stopAutoScroll() {

        if (autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoScroll();

        if (viewPager != null) {
            savedCarouselPosition = viewPager.getCurrentItem();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewPager == null || viewPager.getAdapter() == null) return;

        // Clamp position in case list shrank
        if (wrappedItems != null && savedCarouselPosition >= wrappedItems.size()) {
            savedCarouselPosition = 1;
        }

        viewPager.setCurrentItem(savedCarouselPosition, false);

        if (!isUserInteracting) {
            startAutoScroll();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoScroll();
        autoScrollHandler = null;
        autoScrollRunnable = null;
    }

    @Override
    public void onCarouselClicked(@NonNull CarouselResponse dto) {
        try {
            Log.d(TAG, "Clicked item: " + dto);

            Map<String, String> eventParams = new HashMap<>();

            // 1️⃣ External link
            if (isExternalLink(dto)) {
                String link = String.valueOf(dto.getActivityParams().get("link"));
                openDeeplink(link);

                eventParams.put("click_action", "external_link");
                eventParams.put("link", link);
                logCarouselClick(eventParams);
                return;
            }

            // 2️⃣ Call Me Back
            if (isCallMeBack(dto)) {
                handleCallMeBack(dto);

                eventParams.put("click_action", "call_me_back");
                eventParams.put("identifier", String.valueOf(dto.getActivityParams().get("identifier")));
                logCarouselClick(eventParams);
                return;
            }

            // 3️⃣ Internal Activity
            if (isValid(dto.getClassName())) {

                if (CampaignSubscriptionActivity.class.getName().equals(dto.getClassName())
                        || SubscriptionPaymentScreen.class.getName().equals(dto.getClassName())
                        || ProPlanSubscriptionActivity.class.getName().equals(dto.getClassName())
                        || "CampaignSubscriptionActivity".equals(dto.getClassName())
                        || "SubscriptionPaymentScreen".equals(dto.getClassName())
                        || "ProPlanSubscriptionActivity".equals(dto.getClassName())) {
                    Log.d("CarouselFragment", "onItemSelected: Navigating to subscription page from CAROUSEL");
                    SubscriptionNavigationHelper.navigateToSubscriptionPage(requireContext(), RedirectionSource.CAROUSEL);
                    return;
                }

                if(dto.getNavigationType()!=null) {
                    CommonMethods.navigateFromActivityConfig(requireContext(), dto.getNavigationType(), dto.getClassName(), dto.getActivityParams());
                    return;
                }

                eventParams.put("click_action", "activity");
                eventParams.put("className", dto.getClassName());
                eventParams.put("activityParams", dto.getActivityParams() != null ? dto.getActivityParams().toString() : "");
                launchActivity(dto);
                logCarouselClick(eventParams);
            }

        } catch (Exception e) {
            Log.e("CarouselError", "Carousel click failed", e);
        }
    }

    private boolean isValid(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isExternalLink(CarouselResponse dto) {
        return "external_link".equalsIgnoreCase(dto.getClassName())
                && dto.getActivityParams() != null
                && dto.getActivityParams().containsKey("link");
    }

    private boolean isCallMeBack(CarouselResponse dto) {
        return "call_me_back".equalsIgnoreCase(dto.getClassName())
                && dto.getActivityParams() != null
                && dto.getActivityParams().containsKey("identifier");
    }

    private void openDeeplink(String link) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Unable to redirect", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("DeeplinkError", "Invalid deeplink: " + link, e);
        }
    }

    private void launchActivity(CarouselResponse dto) {
        try {
            Class<?> targetClass = ActivityLauncher.resolveClass(dto.getClassName());
            Intent intent = new Intent(requireContext(), targetClass);
            intent.putExtra(SubscriptionNavigationHelper.EXTRA_SOURCE, RedirectionSource.CAROUSEL);

            if (dto.getActivityParams() != null) {
                for (Map.Entry<String, Object> entry : dto.getActivityParams().entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof String) intent.putExtra(entry.getKey(), (String) value);
                    else if (value instanceof Integer) intent.putExtra(entry.getKey(), (Integer) value);
                    else if (value instanceof Boolean) intent.putExtra(entry.getKey(), (Boolean) value);
                }
            }
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            Log.e("CarouselError", "Activity not found", e);
            MyApplication.openGooglePlayStore(requireActivity());
        }
    }

    private void logCarouselClick(Map<String, String> params) {
        MyApplication.logFirebaseBundleCreation(
                FirebaseEvents.hs_carousel_click.getActionName(),
                params
        );
    }

    private void handleCallMeBack(CarouselResponse dto) {
        String identifier = String.valueOf(dto.getActivityParams().get("identifier"));

        if (CallMeBackIdentifiers.REFER_WIN.name().equalsIgnoreCase(identifier)) {
            callMeBackReferAndWin(
                    requireContext(),
                    cl_parent_layout,
                    null
            );

            Map<String, String> params = new HashMap<>();
            params.put("clickFrom", "carousel");
            MyApplication.logFirebaseBundleCreation(
                    FirebaseEvents.refer_win_click.getActionName(),
                    params
            );
        }
    }

    private int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    private void setViewPagerAspectRatio() {
        int screenWidth = getScreenWidth();
        int calculatedHeight = (int) (screenWidth / 2.24);

        ViewGroup.LayoutParams params = viewPager.getLayoutParams();
        params.height = calculatedHeight;
        viewPager.setLayoutParams(params);

        // ← ADD THESE 4 LINES
        int peekPadding = CommonMethods.dpToPx(requireContext(), 40);
        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);
        viewPager.setPadding(peekPadding, 0, peekPadding, 0);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
