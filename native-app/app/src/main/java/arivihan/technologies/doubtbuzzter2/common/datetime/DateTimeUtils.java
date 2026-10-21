package arivihan.technologies.doubtbuzzter2.common.datetime;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils {

    private static final String TAG = DateTimeUtils.class.getSimpleName();

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public static boolean isCurrentTimeInRange(String startTimeStr, String endTimeStr) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return isTimeInRangeApi26(startTimeStr, endTimeStr);
        } else {
            return isTimeInRangeLegacy(startTimeStr, endTimeStr);
        }
    }

    // For API 26 and above
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static boolean isTimeInRangeApi26(String startStr, String endStr) {
        try {
            DateTimeFormatter formatterWithZone = DateTimeFormatter.ofPattern("HH:mmXXX");
            DateTimeFormatter formatterLocal = DateTimeFormatter.ofPattern("HH:mm");

            OffsetTime now;
            OffsetTime startTime;
            OffsetTime endTime;

            boolean hasZoneStart = startStr.contains("+") || startStr.contains("Z") || startStr.contains("-");
            boolean hasZoneEnd = endStr.contains("+") || endStr.contains("Z") || endStr.contains("-");

            if (hasZoneStart) {
                startTime = OffsetTime.parse(startStr, formatterWithZone);
            } else {
                startTime = LocalTime.parse(startStr, formatterLocal)
                        .atOffset(ZoneId.systemDefault().getRules().getOffset(Instant.now()));
            }

            if (hasZoneEnd) {
                endTime = OffsetTime.parse(endStr, formatterWithZone);
            } else {
                endTime = LocalTime.parse(endStr, formatterLocal)
                        .atOffset(ZoneId.systemDefault().getRules().getOffset(Instant.now()));
            }

            now = OffsetTime.now(startTime.getOffset());

            if (endTime.isBefore(startTime)) {
                return !now.isBefore(startTime) || !now.isAfter(endTime);
            } else {
                return !now.isBefore(startTime) && !now.isAfter(endTime);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // For below API 26
    private static boolean isTimeInRangeLegacy(String startStr, String endStr) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            format.setTimeZone(TimeZone.getDefault());

            Date now = format.parse(format.format(new Date()));
            Date startTime = format.parse(startStr);
            Date endTime = format.parse(endStr);

            if (endTime.before(startTime)) {
                return !now.before(startTime) || !now.after(endTime);
            } else {
                return !now.before(startTime) && !now.after(endTime);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to get current date-time as string
    public static String getCurrentDateTime() {
        return formatter.format(new Date());
    }

    // Method to get difference in minutes
    public static long getDifferenceInMinutes(String startDateTime) {
        try {
            Date startDate = formatter.parse(startDateTime);
            Date currentDate = new Date();
            long differenceMillis = currentDate.getTime() - startDate.getTime();
            return differenceMillis / (60 * 1000);  // Convert to minutes
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}
