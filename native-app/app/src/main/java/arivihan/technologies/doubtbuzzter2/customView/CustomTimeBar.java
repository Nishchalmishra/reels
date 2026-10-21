package arivihan.technologies.doubtbuzzter2.customView;
/*
 * File: CustomTimeBar Copyright 25/09/24, 2:50 am Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 25/09/24, 2:50 am
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.DefaultTimeBar;

import com.google.android.gms.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.R;
import arivihan.technologies.doubtbuzzter2.model.TimeBarThumbData;

@UnstableApi
public class CustomTimeBar extends DefaultTimeBar {

    private static final String TAG = CustomTimeBar.class.getSimpleName();
    private final Drawable adMarkerDoneCorrect, adMarkerDoneWrong, adMarkerDoneActive, adMarkerPending, hiddenThumb;
    private long duration;
    private List<TimeBarThumbData> thumbDataList;
    private final Paint markerPaint;

    public CustomTimeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        adMarkerDoneCorrect = ContextCompat.getDrawable(context, R.drawable.img_interactivity_question_answer_right);
        adMarkerDoneWrong = ContextCompat.getDrawable(context, R.drawable.img_interactivity_question_answer_wrong);
        adMarkerPending = ContextCompat.getDrawable(context, R.drawable.img_interactivity_question_answer_pending);
        adMarkerDoneActive = ContextCompat.getDrawable(context, R.drawable.img_interactivity_question_answer_active);
        hiddenThumb = ContextCompat.getDrawable(context, R.drawable.end_thumb);
        markerPaint = new Paint();
    }

    public void setAdMarkerPositions(List<TimeBarThumbData> thumbDataList, long duration, long trimmedDuration) {
        this.thumbDataList = thumbDataList;
//        this.duration = duration;
//        invalidate();
    }

    @Override
    public void setDuration(long duration) {
        super.setDuration(duration);
        if (this.duration > 0 && duration != this.duration) {
            return;
        }
        this.duration = duration;
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {

        int barHeightPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 7,
                getResources().getDisplayMetrics()
        );

        int centerY = getHeight() / 2;
        int trackTop = centerY - barHeightPx / 2;
        int trackBottom = centerY + barHeightPx / 2;
//        float cornerRadius = barHeightPx / 2f;
//
//        // 1. Draw rounded unplayed (background) track MANUALLY before super
//        Paint bgPaint = new Paint();
//        bgPaint.setAntiAlias(true);
//        bgPaint.setColor(ContextCompat.getColor(getContext(), R.color.exo_player_light_progress_blue));
//        canvas.drawRoundRect(new RectF(0, trackTop, getWidth(), trackBottom), cornerRadius, cornerRadius, bgPaint);

        // 2. Call super WITHOUT clipping — scrubber circle draws freely
        super.onDraw(canvas);

        // 3. Round off the sharp ends by overdrawing with background color on both ends
        Paint startCapPaint = new Paint();
        startCapPaint.setAntiAlias(true);
        startCapPaint.setColor(ContextCompat.getColor(getContext(), R.color.arivihan_secondary_color));

        Paint endCapPaint = new Paint();
        endCapPaint.setAntiAlias(true);
        endCapPaint.setColor(ContextCompat.getColor(getContext(), R.color.exo_player_light_progress_blue));

        // Left end cap circle
//        canvas.drawCircle(24, centerY, barHeightPx / 2f , startCapPaint);
        // Right end cap circle
//        canvas.drawCircle(getWidth()-24, centerY, barHeightPx / 2f, endCapPaint);

        // 4. Draw icons and gold rectangles
        if (!CollectionUtils.isEmpty(thumbDataList) && duration > 0) {
            Log.d("CustomTimeBar", "inside duration " + duration);

            int barWidth = getWidth();

            int iconSizePx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 24,
                    getResources().getDisplayMetrics()
            );

            int rectWidthPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 2,
                    getResources().getDisplayMetrics()
            );

            int scrubberRadius = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    6,
                    getResources().getDisplayMetrics()
            );

            markerPaint.setAntiAlias(true);
            markerPaint.setColor(ContextCompat.getColor(getContext(), R.color.timer_marker_yellow));

            List<TimeBarThumbData> sortedList = new ArrayList<>(thumbDataList);
            Collections.sort(sortedList, (a, b) -> Integer.compare(a.getElevation(), b.getElevation()));

            for (TimeBarThumbData thumbData : sortedList) {

//                int scrubberPadding = (int) TypedValue.applyDimension(
//                        TypedValue.COMPLEX_UNIT_DIP, 16,
//                        getResources().getDisplayMetrics()
//                );
//
//                int trackEnd = getWidth() - scrubberPadding;
//                int trackWidth = trackEnd - scrubberPadding;
//
//                float markerPosition = scrubberPadding + (float) thumbData.getPosition() / duration * trackWidth;

                int trackStart = getPaddingLeft() + scrubberRadius;
                int trackEnd = getWidth() - getPaddingRight() - scrubberRadius;
                int trackWidth = trackEnd - trackStart;

                float markerPosition =
                        trackStart + ((float) thumbData.getPosition() / duration) * trackWidth;

                markerPosition += (float) scrubberRadius/3;

                Drawable thumbIcon;
                if (thumbData.isActive()) {
                    thumbIcon = adMarkerDoneActive;

                    int activeIconSizePx = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 40,
                            getResources().getDisplayMetrics()
                    );

                    int iconLeft = (int) markerPosition - activeIconSizePx / 2;
                    int iconBottom = trackTop + 18;
                    int iconTop = iconBottom - activeIconSizePx;
                    int iconRight = iconLeft + activeIconSizePx;

                    thumbIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    thumbIcon.draw(canvas);
                } else {
                    if (thumbData.isCompleted()) {
                        thumbIcon = thumbData.isCorrect() ? adMarkerDoneCorrect : adMarkerDoneWrong;
                    } else {
                        thumbIcon = adMarkerPending;
                    }

                    int iconLeft = (int) markerPosition - iconSizePx / 2;
                    int iconBottom = trackTop - 4;
                    int iconTop = iconBottom - iconSizePx;
                    int iconRight = iconLeft + iconSizePx;

                    thumbIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    thumbIcon.draw(canvas);
                }

                int rectLeft = (int) markerPosition - rectWidthPx / 2;
                int rectRight = rectLeft + rectWidthPx;
                canvas.drawRect(rectLeft, trackTop, rectRight, trackBottom, markerPaint);
            }
        }
    }

    public long getScrubberTimeOffset() {

        int scrubberRadiusPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16,
                getResources().getDisplayMetrics()
        );

        int trackStart = scrubberRadiusPx;
        int trackEnd = getWidth() - scrubberRadiusPx;
        int trackWidth = trackEnd - trackStart;

        if (duration <= 0 || trackWidth <= 0) return 0;

        float msPerPixel = (float) duration / trackWidth;

        return (long) (scrubberRadiusPx * msPerPixel);
    }

    public int[] getActiveMarkerScreenPosition() {

//        Log.d(TAG, "getActiveMarkerScreenPosition: "+thumbDataList.toString()+", "+duration);
        if (CollectionUtils.isEmpty(thumbDataList) || duration <= 0) return null;

        int scrubberPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16,
                getResources().getDisplayMetrics()
        );
        int trackWidth = (getWidth() - scrubberPadding) - scrubberPadding;

        int barHeightPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics()
        );
        int iconSizePx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()
        );

        int centerY = getHeight() / 2;
        int trackTop = centerY - barHeightPx / 2;
        int iconBottom = trackTop + 18;
        int iconTop = iconBottom - iconSizePx;
        int iconCenterY = iconTop + iconSizePx / 2;

        int[] location = new int[2];
        getLocationOnScreen(location);

        for (TimeBarThumbData thumbData : thumbDataList) {
            if (thumbData.isActive()) {
                float markerPosition = scrubberPadding + (float) thumbData.getPosition() / duration * trackWidth;
                return new int[]{
                        location[0] + (int) markerPosition,
                        location[1] + iconCenterY,
                        iconSizePx
                };
            }
        }
        return null;
    }

}




