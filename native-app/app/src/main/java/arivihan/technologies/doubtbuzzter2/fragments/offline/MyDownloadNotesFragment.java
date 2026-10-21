package arivihan.technologies.doubtbuzzter2.fragments.offline;

import static arivihan.technologies.doubtbuzzter2.constants.UrlRoutes.AV_SYNC_DOWNLOADED_NOTE_DATA;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.common.CommonMethods;
import arivihan.technologies.doubtbuzzter2.customView.CancelDownloadBottomSheet;
import arivihan.technologies.doubtbuzzter2.database.helper.NotesMapper;
import arivihan.technologies.doubtbuzzter2.model.DownloadStatus;
import arivihan.technologies.doubtbuzzter2.model.NotesResponse;
import arivihan.technologies.doubtbuzzter2.model.generic.ApiWrappedResponse;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteDto;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.SyncDownloadNotesRequest;
import arivihan.technologies.doubtbuzzter2.viewModel.NotesViewModel;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonArrayRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyJsonObjectRequest;
import arivihan.technologies.doubtbuzzter2.volley.MyVolleySingleton;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

public class MyDownloadNotesFragment extends Fragment {

    private static final String TAG = MyDownloadNotesFragment.class.getSimpleName();

    private LinearLayout noDownloadFound;

    private NotesViewModel notesViewModel;
    private CancelDownloadBottomSheet cancelDownloadBottomSheet;
    private Gson gson;
    private LiveData<NoteEntity> cancelDownloadLiveData;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private RelativeLayout rl_tab_layout;
    private CardView cv_tab_left_scroll, cv_tab_right_scroll;
    private ViewPagerAdapter viewPagerAdapter;
    private ArrayList<String> subjectList = new ArrayList<>();
    private int count = 0;
    private String queryText = "";
    private int tabsSize = 0;

    public MyDownloadNotesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_download_notes, container, false);
        noDownloadFound = view.findViewById(R.id.no_downloaded_videos_layout);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        rl_tab_layout = view.findViewById(R.id.rl_tab_layout);
        cv_tab_left_scroll = view.findViewById(R.id.cv_tab_left_scroll);
        cv_tab_right_scroll = view.findViewById(R.id.cv_tab_right_scroll);

        cv_tab_left_scroll.setOnClickListener(v -> tabLayout.smoothScrollBy(-tabLayout.getWidth(), 0));
        cv_tab_right_scroll.setOnClickListener(v -> tabLayout.smoothScrollBy(tabLayout.getWidth(), 0));

        tabLayout.getViewTreeObserver().addOnScrollChangedListener(this::updateTabArrowsVisibility);

        setUpViewModel();

        return view;
    }

    private void updateTabArrowsVisibility() {
        try {
            int scrollX = tabLayout.getScrollX();
            int maxScroll = tabLayout.getChildAt(0).getMeasuredWidth() - tabLayout.getMeasuredWidth();

            cv_tab_left_scroll.setVisibility(scrollX > 0 ? View.VISIBLE : View.GONE);
            cv_tab_right_scroll.setVisibility(scrollX < maxScroll ? View.VISIBLE : View.GONE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void setUpViewModel() {
        Log.d(TAG, "Setting view model");
        notesViewModel = MyApplication.getInstance().getNotesViewModel();
//        notesViewModel.getUniqueSubjectNames(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()))
//                .observe(getViewLifecycleOwner(), this::checkNotes);
        notesViewModel.getNotesCount(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()))
                .observe(getViewLifecycleOwner(), this::checkForNotesCount);

        checkSubjectNotes();

        notesViewModel.getAllDownloadedNote(
                MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()),
                NoteEntity.STATUS_COMPLETED
        ).observe(getViewLifecycleOwner(), new Observer<List<NoteDto>>() {
            @Override
            public void onChanged(List<NoteDto> list) {
                if (list != null && !list.isEmpty()) {
                    syncDownloadedData(list);
                    notesViewModel.getAllDownloadedNote(
                            MyApplication.getStringData(PreferenceKeys.USER_ID.getKey()),
                            DownloadStatus.DOWNLOAD_COMPLETED.name()
                    ).removeObserver(this);
                }
            }
        });
    }

    private void checkSubjectNotes() {
        try {

            Observer<List<String>> observer = new Observer<List<String>>() {
                @Override
                public void onChanged(List<String> subjectNames) {
                    subjectList.clear();
                    subjectList.addAll(subjectNames);
                    notesViewModel.getUniqueSubjectNames(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())).removeObserver(this);
                    Log.d(TAG,"Setting new tab data");
                    setTabData(subjectNames.size());
                }
            };

            notesViewModel.getUniqueSubjectNames(MyApplication.getStringData(PreferenceKeys.USER_ID.getKey())).observe(getViewLifecycleOwner(), observer);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void checkForNotesCount(int count) {

        if (count == 0) {
            noDownloadFound.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);
            rl_tab_layout.setVisibility(View.GONE);
        } else {
            noDownloadFound.setVisibility(View.GONE);
            tabLayout.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.VISIBLE);
            rl_tab_layout.setVisibility(View.VISIBLE);

        }
    }

    private void setTabData(int tabSize) {
        try {
            if(tabsSize==tabSize)
                return;

            viewPagerAdapter = new ViewPagerAdapter(this, subjectList);
            viewPager.setAdapter(viewPagerAdapter);
            viewPager.setOffscreenPageLimit(viewPagerAdapter.getItemCount());

            TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
                    tabLayout,
                    viewPager,
                    (tab, position) -> {
                        tab.setCustomView(getCustomTab(subjectList.get(position), position == 0));
                    }
            );
            tabLayoutMediator.attach();

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    updateTabUI(tab, true);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    updateTabUI(tab, false);
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });
            tabsSize = tabSize;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private View getCustomTab(String title, boolean isSelected) {
        View tabView = LayoutInflater.from(requireContext()).inflate(R.layout.text_tab_layout, null);
        MaterialCardView tabCard = tabView.findViewById(R.id.tab_text_card);
        TextView tabText = tabView.findViewById(R.id.tab_text);
        Typeface customFontUnselected = ResourcesCompat.getFont(requireContext(), R.font.nunito_regular);
        Typeface customFontSelected = ResourcesCompat.getFont(requireContext(), R.font.nunito_semibold);
//        tabText.setText(title);
        tabText.setText(CommonMethods.getShortName(title));
        if (isSelected) {
            tabCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.subscription_blue));
            tabCard.setStrokeWidth(0);
            tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            tabText.setTypeface(customFontSelected);
        } else {
            tabCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
            tabCard.setStrokeWidth(2);
            tabCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.tab_not_selected_text_boundary));
            tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_not_selected_text));
            tabText.setTypeface(customFontUnselected);
        }
        return tabView;
    }

    private void updateTabUI(TabLayout.Tab tab, boolean isSelected) {
        View customView = tab.getCustomView();
        if (customView != null) {
            MaterialCardView tabCard = customView.findViewById(R.id.tab_text_card);
            TextView tabText = customView.findViewById(R.id.tab_text);
            Typeface customFontUnselected = ResourcesCompat.getFont(requireContext(), R.font.nunito_regular);
            Typeface customFontSelected = ResourcesCompat.getFont(requireContext(), R.font.nunito_semibold);
            if (isSelected) {
                tabCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.subscription_blue));
                tabCard.setStrokeWidth(0);
                tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                tabText.setTypeface(customFontSelected);
            } else {
                tabCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
                tabCard.setStrokeWidth(2);
                tabCard.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.tab_not_selected_text_boundary));
                tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_not_selected_text));
                tabText.setTypeface(customFontUnselected);
            }
        }
    }

    private void checkNotes(List<String> subjectList) {
        try {
            this.subjectList = (ArrayList<String>) subjectList;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void syncDownloadedData(List<NoteDto> list) {
        Log.d(TAG, "syncDownloadedData: call #" + (count++) + ", notes count=" + list.size());

        if (!CommonMethods.isOnline(requireContext())) return;

        try {
            List<SyncDownloadNotesRequest> requestBody = NotesMapper.fromNoteDtoListToSyncDownloadNotesRequestList(list);
            JSONArray jsonBody = new JSONArray(new Gson().toJson(requestBody));

            Map<String, Object> params = new HashMap<>();
            String url = CommonMethods.getUrlParam(AV_SYNC_DOWNLOADED_NOTE_DATA, params);

            Log.d(TAG, "syncDownloadedData: REQUEST → " + url + ", notesCount=" + requestBody.size());

            MyJsonArrayRequest request = new MyJsonArrayRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        Log.d(TAG, "syncDownloadedData: RESPONSE → " + response.toString());
                        TypeToken<ApiWrappedResponse<List<NotesResponse>>> token =
                                new TypeToken<ApiWrappedResponse<List<NotesResponse>>>() {};
                        ApiWrappedResponse<List<NotesResponse>> wrapped =
                                new Gson().fromJson(response.toString(), token.getType());

                        if (wrapped == null || !wrapped.isSuccess() || wrapped.getData() == null) {
                            Log.w(TAG, "syncDownloadedData: invalid or unsuccessful response");
                            return;
                        }

                        for (NotesResponse note : wrapped.getData()) {
                            String entityId = NotesMapper.toEntityId(note);
                            if (entityId == null) continue;
                            boolean lockedFromApi = note.getLocked() != null && note.getLocked();

                            notesViewModel.getLiveNotesById(entityId).observe(getViewLifecycleOwner(),
                                    new Observer<NoteEntity>() {
                                        @Override
                                        public void onChanged(NoteEntity entity) {
                                            if (entity != null && entity.isLocked() != lockedFromApi) {
                                                notesViewModel.updateLockStatus(entityId, lockedFromApi);
                                                Log.d(TAG, "syncDownloadedData: updated lock for id=" + entityId + " → " + lockedFromApi);
                                            }
                                            notesViewModel.getLiveNotesById(entityId).removeObserver(this);
                                        }
                                    });
                        }
                    },
                    error -> Log.e(TAG, "syncDownloadedData: ERROR → " + error.getMessage(), error)
            );
            request.setTag("SYNC_API");
            MyVolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
        } catch (Exception e) {
            Log.e(TAG, "syncDownloadedData: request creation error: " + e.getMessage());
        }
    }

    private void observe(String id) {
        if (cancelDownloadLiveData != null)
            cancelDownloadLiveData.removeObservers(this);
        cancelDownloadLiveData = MyApplication.getInstance().getNotesViewModel().getLiveNotesById(id);
        cancelDownloadLiveData.observe(this, noteEntity -> {
            if (noteEntity != null) {
                if (noteEntity.getStatus().equals(DownloadStatus.DOWNLOAD_COMPLETED.name())) {
                    if (cancelDownloadBottomSheet != null) {
                        cancelDownloadBottomSheet.dismiss();
                        cancelDownloadBottomSheet = null;
                    }
                } else if (cancelDownloadBottomSheet != null && cancelDownloadBottomSheet.isVisible()) {
                    cancelDownloadBottomSheet.updateProgress(noteEntity.getProgress());
                }
            }
        });
    }

    public void setFilter(String filter) {
        try {
            Log.d(TAG, "Notes filter: " + filter);
            queryText = filter;
            Collection<Fragment> map = viewPagerAdapter.getAllFragments();
            for (Fragment fragment : map) {
                if (fragment instanceof MyDownloadSubjectNoteFragment) {
                    ((MyDownloadSubjectNoteFragment) fragment).setFilter(filter);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireContext();
        MyVolleySingleton.getInstance(requireContext()).getRequestQueue().cancelAll("SYNC_API");

    }

    @Override
    public void onStop() {
        super.onStop();
        if (cancelDownloadBottomSheet != null) {
            cancelDownloadBottomSheet.dismiss();
            cancelDownloadBottomSheet = null;
        }
    }

    static class ViewPagerAdapter extends FragmentStateAdapter {
        private ArrayList<String> subjectList;
        private final Map<Integer, Fragment> fragmentMap = new HashMap<>();

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<String> subjectList) {
            super(fragmentActivity);
            this.subjectList = subjectList;
        }

        public ViewPagerAdapter(@NonNull Fragment fragment, ArrayList<String> subjectList) {
            super(fragment);
            this.subjectList = subjectList;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment = MyDownloadSubjectNoteFragment.newInstance(subjectList.get(position));
            fragmentMap.put(position, fragment);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return subjectList.size();
        }

        public void updateDataList(ArrayList<String> newDataList) {
            this.subjectList = newDataList;
            fragmentMap.clear();
            notifyDataSetChanged();
        }

        public String getTitle(int position) {
            return subjectList.get(position);
        }

        public Fragment getFragment(int position) {
            return fragmentMap.get(position);
        }

        public Collection<Fragment> getAllFragments() {
            return fragmentMap.values();
        }
    }

}