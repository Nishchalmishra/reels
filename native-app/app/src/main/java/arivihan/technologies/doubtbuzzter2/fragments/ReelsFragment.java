package arivihan.technologies.doubtbuzzter2.fragments;

/*
 * File: ReelsFragment Copyright 29/5/21 4:47 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 29/5/21 4:47 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import arivihan.technologies.doubtbuzzter2.MyApplication;
import arivihan.technologies.doubtbuzzter2.MyReelsListener;
import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.enums.AnalyticsEvent;
import arivihan.technologies.doubtbuzzter2.helper.ReelsFragmentHelper;
import arivihan.technologies.doubtbuzzter2.helper.ScreenAnalyticsHelper;
import arivihan.technologies.doubtbuzzter2.listener.TapTargetListener;
import arivihan.technologies.doubtbuzzter2.model.ReelsDto;
import arivihan.technologies.doubtbuzzter2.model.ReelsType;
import arivihan.technologies.doubtbuzzter2.utils.EdgeToEdgeHelper;

public class ReelsFragment extends ReelsFragmentHelper implements TapTargetListener {

    private static final String TAG = ReelsFragment.class.getName();
    private int position;
    private String url;
    private String name;
    private ExoPlayer reelsPlayer;
    private ImageView imageView,viewIcon;
    private PlayerView reelsPlayerView;
    private LottieAnimationView lottieAnimationView;
    private ReelsDto reelsDto;
    private MyReelsListener myReelsListener;
    private boolean viewed;
    private TextView likes, views;
    private static final String ARG_PARAM1 = "reels";
    private static final String ARG_PARAM2 = "pos";
    private LinearLayout likeViewLayout;

    public ReelsFragment() {

    }

    public static ReelsFragment newInstance(ReelsDto reelsDto, int position) {
        ReelsFragment fragment = new ReelsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, reelsDto);
        args.putInt(ARG_PARAM2, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            myReelsListener = (MyReelsListener) context;
        } catch (ClassCastException e) {
            Log.e("Error in ", e.getMessage(), e);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reelsDto = (ReelsDto) getArguments().getSerializable(ARG_PARAM1);
            position = getArguments().getInt(ARG_PARAM2);
            name = reelsDto.getName();
            url = reelsDto.getContentUrl();
        }

    }

    // Inflate the view for the fragment based on layout XML
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reels_fragment, container, false);
        TextView contentName = view.findViewById(R.id.name);
        reelsPlayerView = view.findViewById(R.id.reels_player);
        lottieAnimationView = view.findViewById(R.id.lottie);
        ToggleButton like = view.findViewById(R.id.like_button);
        imageView = view.findViewById(R.id.imageView);
        likeViewLayout=view.findViewById(R.id.like_view_layout);

        like.setOnClickListener(v -> {

            if (!like.isChecked()) {
                reelsDto.setLiked(false);
                myReelsListener.setLike(false, reelsDto.getActualLikes(),position);
            } else {
                reelsDto.setLiked(true);

                myReelsListener.setLike(true, reelsDto.getActualLikes(), position);
            }
            likes.setText(reelsDto.getLikes());
            Log.d("likenbutton checked", String.valueOf(like.isChecked()));
            Log.d("likenbutton active", String.valueOf(like.isActivated()));

        });
        like.setChecked(reelsDto.isLiked());
        likes = view.findViewById(R.id.likes);
        views = view.findViewById(R.id.views);
        contentName.setSelected(true);
        //String substring = name.substring(0, name.lastIndexOf("."));
        contentName.setText(name);
        likes.setText(reelsDto.getLikes());
        views.setText(reelsDto.getViews());
        if (reelsDto.getReelsType().equals(ReelsType.VIDEO)) {
            reelsPlayerView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            initializeVideo(url);
        } else {
            initializeImageView(url);
        }
        if(reelsDto.isHideAction()){
            likeViewLayout.setVisibility(View.GONE);
        }else {
           likeViewLayout.setVisibility(View.VISIBLE);
        }
        // Reel media remains behind the system bars; only the overlaid title/actions move into
        // the safe area so they are not hidden by the navigation gesture area.
        EdgeToEdgeHelper.applyArtworkEdgeToEdge(
                view, new View[]{contentName, likeViewLayout}, null);
        return view;
    }

    private void initializeImageView(String url) {
        reelsPlayerView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        Glide.with(this).load(url).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                if(lottieAnimationView.isAnimating()) {
                    lottieAnimationView.cancelAnimation();
                }
                lottieAnimationView.setVisibility(View.GONE);
                //myReelsListener.setWatched(reelsDto.getName());
                return false;
            }
        }).into(imageView);
    }

    private void initializeVideo(String videoUrl) {
        try {
            initializePlayer();
            Uri uri = Uri.parse(videoUrl);
            buildMediaSource(uri);
        } catch (NullPointerException e) {
            Toast.makeText(getContext(), getString(R.string.server_error_later), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("reelsfragment", "onDestroyView" + url);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("reelsfragment", "onDetach" + url);

    }

    private void buildMediaSource(Uri uri) {
        MediaItem mediaItem = new MediaItem.Builder().setUri(uri).setMimeType(MimeTypes.APPLICATION_M3U8).build();
        reelsPlayer.setMediaItem(mediaItem);
        reelsPlayer.prepare();
    }

    private void initializePlayer() {
        if (reelsPlayer == null) {
            reelsPlayer = new ExoPlayer.Builder(getActivity()).build();
            reelsPlayer.addListener(this);
            reelsPlayerView.setPlayer(reelsPlayer);
        }
    }

    private void playPlayer() {
        if (reelsPlayer != null) {
            reelsPlayer.setPlayWhenReady(true);
        }
    }

    private void resumePlayer() {
        if (reelsPlayer != null) {
            reelsPlayer.seekTo(0);
            reelsPlayer.setPlayWhenReady(true);
            reelsPlayer.getPlaybackState();
        }
    }

    private void pausePlayer() {
        if (reelsPlayer != null) {
            reelsPlayer.setPlayWhenReady(false);
        }
    }

    private void stopPlayer() {
        if (reelsPlayerView != null) {
            reelsPlayerView.setPlayer(null);
        }

        if (reelsPlayer != null) {
            reelsPlayer.release();
            reelsPlayer = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("reelsfragment", "onPause" + url);
        if (reelsPlayerView != null && reelsPlayer!=null) {
            pausePlayer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("reelsfragment", "onDestroy" + url);
        if (reelsPlayerView != null && reelsPlayer!=null) {
            stopPlayer();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d("reelsfragment", "onResume" + url);
        if (reelsPlayerView != null && reelsPlayer!=null) {
            resumePlayer();
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        switch (reelsPlayer.getPlaybackState()) {
            case Player.STATE_BUFFERING:
                lottieAnimationView.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_ENDED:
                // Activate the force enable
                reelsPlayer.seekTo(0);
                myReelsListener.setWatched(reelsDto.getName());
                break;
            case Player.STATE_IDLE:

                break;
            case Player.STATE_READY:
                if (reelsPlayerView != null && reelsPlayer != null) {
                    applyAspectRatio(reelsPlayerView, reelsPlayer);
                    lottieAnimationView.setVisibility(View.GONE);
                } else {
                    Log.e("ReelsFragment", "ReelsPlayerView or ReelsPlayer is null");
                }
                if (!viewed) {
                    //reelsDto.setActualViews(reelsDto.getActualViews() + 1);
                    myReelsListener.setViews(1, reelsDto.getName(), position);
                    viewed = true;
                    views.setText(reelsDto.getViews());
                }
                new Handler().postDelayed(() -> {
                    reelsPlayerView.hideController();
                }, 1000);
                break;
            default:
                break;
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void applyAspectRatio(PlayerView container, ExoPlayer exoPlayer) {

        float videoRatio = (float) 9 /16;
        if(exoPlayer!=null && exoPlayer.getVideoFormat()!=null) {
            videoRatio = (float) exoPlayer.getVideoFormat().width / exoPlayer.getVideoFormat().height;
        }

        if (videoRatio > 1) {
            container.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        } else {
            container.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        }
        myReelsListener.setAspectRatio(videoRatio, reelsDto.getName());
        reelsDto.setAspectRatio(String.valueOf(Math.round(videoRatio)));
    }

    @Override
    public void backPressed() {
    }

}
