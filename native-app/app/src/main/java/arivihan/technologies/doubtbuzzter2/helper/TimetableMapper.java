//package arivihan.technologies.doubtbuzzter2.mapper;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//import arivihan.technologies.doubtbuzzter2.dto.DayDto;
//import arivihan.technologies.doubtbuzzter2.dto.SessionDto;
//import arivihan.technologies.doubtbuzzter2.dto.TimetableDataDto;
//import arivihan.technologies.doubtbuzzter2.enums.ChapterEnum.ChapterStatus;
//import arivihan.technologies.doubtbuzzter2.model.UiSession;
//import arivihan.technologies.doubtbuzzter2.model.UiTimetableDay;
//
///**
// * Single responsibility: converts backend DTOs → UI model objects.
// * Status resolution and date parsing live here.
// * Colour derivation lives in the adapter (it only needs subjectColor + status).
// */
//public class TimetableMapper {
//
//    public static List<UiTimetableDay> map(TimetableDataDto data, String todayDate) {
//        if (data == null || data.getDays() == null) return Collections.emptyList();
//
//        List<UiTimetableDay> result = new ArrayList<>();
//
//        for (DayDto dayDto : data.getDays()) {
//            int     dayNumber = parseDayNumber(dayDto.getDate());
//            boolean isToday   = dayDto.getDate().equals(todayDate);
//
//            result.add(new UiTimetableDay(
//                    dayNumber,
//                    dayDto.getDayLabel(),
//                    isToday,
//                    mapSessions(dayDto.getSessions())
//            ));
//        }
//
//        return result;
//    }
//
//    // ── Sessions ──────────────────────────────────────────────────────────────
//
//    private static List<UiSession> mapSessions(List<SessionDto> dtos) {
//        if (dtos == null || dtos.isEmpty()) return Collections.emptyList();
//
//        List<UiSession> list = new ArrayList<>();
//        for (SessionDto dto : dtos) list.add(toUiSession(dto));
//        return list;
//    }
//
//    private static UiSession toUiSession(SessionDto dto) {
//        UiSession session = new UiSession();
//
//        session.setId(nullSafe(dto.getId()));
//        session.setSessionDate(nullSafe(dto.getSessionDate()));
//        session.setTitle(nullSafe(dto.getTitle()));
//        session.setSubject(nullSafe(dto.getSubject()));
//        session.setSubjectColor(nullSafe(dto.getSubjectColor(), "#E3F0FF")); // fallback if null
//        session.setStartTime(nullSafe(dto.getStartTime()));
//        session.setEndTime(nullSafe(dto.getEndTime()));
//        session.setDuration(nullSafe(dto.getDuration()));
//        session.setThumbnailUrl(nullSafe(dto.getThumbnailUrl()));
//        session.setStreamUrl(nullSafe(dto.getStreamUrl()));
//        session.setChapterLiveDto(dto.getChapterLiveDto());   // adapter uses getLiveUrl() on this
//        session.setLocked(dto.isLocked());
//
//        // locked:true always wins over the status string
//        session.setStatus(resolveStatus(dto.getStatus(), dto.isLocked()));
//
//        return session;
//    }
//
//    // ── Helpers ───────────────────────────────────────────────────────────────
//
//    /** locked:true always wins over status string. */
//    private static ChapterStatus resolveStatus(String raw, boolean locked) {
//        if (locked) return ChapterStatus.LOCKED;
//        if (raw == null) return ChapterStatus.UPCOMING;
//        switch (raw.toUpperCase()) {
//            case "LIVE":     return ChapterStatus.LIVE;
//            case "RECORDED": return ChapterStatus.RECORDED;
//            case "LOCKED":   return ChapterStatus.LOCKED;
//            default:         return ChapterStatus.UPCOMING;
//        }
//    }
//
//    /** Extracts day integer from "YYYY-MM-DD". Returns 0 on failure. */
//    private static int parseDayNumber(String date) {
//        try {
//            return Integer.parseInt(date.split("-")[2]);
//        } catch (Exception e) {
//            return 0;
//        }
//    }
//
//    private static String nullSafe(String value) {
//        return value != null ? value : "";
//    }
//
//    /** Null-safe with a fallback for fields that need a default (e.g. colour). */
//    private static String nullSafe(String value, String fallback) {
//        return (value != null && !value.isEmpty()) ? value : fallback;
//    }
//}