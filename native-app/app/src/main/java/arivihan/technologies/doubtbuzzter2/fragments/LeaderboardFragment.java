package arivihan.technologies.doubtbuzzter2.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.adapter.LeaderboardAdapter;
import arivihan.technologies.doubtbuzzter2.model.RewardsScreenDto;

public class LeaderboardFragment extends Fragment {

    private static final String TAG = "LeaderboardFragment";

    private ImageView ivFirstAvatar, ivSecondAvatar, ivThirdAvatar;
    private TextView  tvFirstName,   tvSecondName,   tvThirdName;
    private RecyclerView rvLeaderboard;

    private LeaderboardAdapter adapter;
    private RewardsScreenDto.LeaderboardSection pendingSection;

    public static LeaderboardFragment newInstance() {
        Log.d(TAG, "newInstance: fragment created");
        return new LeaderboardFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: inflating layout");
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: binding views and setting up RecyclerView");
        bindViews(view);
        setupRecyclerView();

        if (pendingSection != null) {
            Log.d(TAG, "onViewCreated: pendingSection found, applying now → " + pendingSection);
            render(pendingSection);
            pendingSection = null;
        } else {
            Log.w(TAG, "onViewCreated: no pendingSection — waiting for updateLeaderboard() call");
        }
    }

    private void bindViews(View root) {
        ivFirstAvatar  = root.findViewById(R.id.iv_first_avatar);
        ivSecondAvatar = root.findViewById(R.id.iv_second_avatar);
        ivThirdAvatar  = root.findViewById(R.id.iv_third_avatar);
        tvFirstName    = root.findViewById(R.id.tv_first_name);
        tvSecondName   = root.findViewById(R.id.tv_second_name);
        tvThirdName    = root.findViewById(R.id.tv_third_name);
        rvLeaderboard  = root.findViewById(R.id.rv_leaderboard);

        Log.d(TAG, "bindViews: ivFirstAvatar=" + ivFirstAvatar
                + " tvFirstName=" + tvFirstName
                + " rvLeaderboard=" + rvLeaderboard);
    }

    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter(requireContext(), new ArrayList<>());
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvLeaderboard.setAdapter(adapter);
//        rvLeaderboard.setHasFixedSize(true);
        rvLeaderboard.setItemAnimator(null);
        rvLeaderboard.setNestedScrollingEnabled(false);
        Log.d(TAG, "setupRecyclerView: adapter attached");
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void updateLeaderboard(RewardsScreenDto.LeaderboardSection section) {
        Log.d(TAG, "updateLeaderboard called — section=" + section);

        if (section == null) {
            Log.e(TAG, "updateLeaderboard: section is NULL, aborting");
            return;
        }

        if (rvLeaderboard == null) {
            Log.w(TAG, "updateLeaderboard: view not ready yet — caching as pendingSection");
            pendingSection = section;
            return;
        }

        Log.d(TAG, "updateLeaderboard: view is ready, rendering immediately");
        render(section);
    }

    // ── Internal rendering ────────────────────────────────────────────────────

    private void render(RewardsScreenDto.LeaderboardSection section) {
        Log.d(TAG, "render: start");

        List<RewardsScreenDto.LeaderboardSection.LeaderboardItem> performers =
                section.getTopPerformers();

        if (performers == null) {
            Log.e(TAG, "render: topPerformers is NULL — nothing to show");
            return;
        }

        if (performers.isEmpty()) {
            Log.w(TAG, "render: topPerformers list is EMPTY — nothing to show");
            return;
        }

        Log.d(TAG, "render: " + performers.size() + " performers received");
        for (int i = 0; i < performers.size(); i++) {
            Log.d(TAG, "render: [" + i + "] " + performers.get(i));
        }

        // Podium
        Log.d(TAG, "render: setting podium slots");
        setPodiumSlot(performers, 0, tvFirstName,  ivFirstAvatar);
        setPodiumSlot(performers, 1, tvSecondName, ivSecondAvatar);
        setPodiumSlot(performers, 2, tvThirdName,  ivThirdAvatar);

        // List
        Log.d(TAG, "render: updating adapter with " + performers.size() + " items");
        adapter.updateData(performers);
        Log.d(TAG, "render: complete");
    }

    private void setPodiumSlot(
            List<RewardsScreenDto.LeaderboardSection.LeaderboardItem> performers,
            int index,
            TextView nameView,
            ImageView avatarView) {

        if (index >= performers.size()) {
            Log.w(TAG, "setPodiumSlot: index " + index + " out of bounds (size=" + performers.size() + "), skipping");
            return;
        }

        RewardsScreenDto.LeaderboardSection.LeaderboardItem p = performers.get(index);
        String shortName = buildShortName(p.getStudentName());
        Log.d(TAG, "setPodiumSlot[" + index + "]: name='" + shortName + "' imageUrl=" + p.getStudentImageUrl());

        nameView.setText(shortName);

        String imageUrl = p.getStudentImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d(TAG, "setPodiumSlot[" + index + "]: loading image via Glide");
            Glide.with(this)
                    .load(imageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.avatar)
                    .error(R.drawable.avatar)
                    .into(avatarView);
        } else {
            Log.w(TAG, "setPodiumSlot[" + index + "]: no imageUrl, using placeholder");
            avatarView.setImageResource(R.drawable.avatar);
        }
    }

    private String buildShortName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "";
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 1
                ? parts[0] + " " + parts[parts.length - 1].charAt(0) + "."
                : parts[0];
    }
}