package arivihan.technologies.doubtbuzzter2.model;

import java.io.Serializable;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.model.chapter.live.ChapterLiveDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * File: ChapterListInfoDto Copyright 12/2/21 12:20 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 12/2/21 12:20 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterListInfoDto implements Serializable {
    private String chapterId;
    private String imageUrl;
    private String videoTitle;
    private String videoSubtitle;
    private String teacherIntroImage;
    private String selectedChapter;
    private String selectedSubject;
    private String selectedChapterCode;
    private boolean locked;
    private String teacherName;
    private String chapterInfoList;
    private List<String> chapterTitleList;
    private String chapterCompletion;
    private boolean completed;
    private String chapterType;
    private String chapterStatus;
    private String subject;
    private String lockMessage;
    private String notesUrl;
    private boolean customLecture;
    private boolean inProgress;
    private boolean unwatched;
    private int progress;
    private boolean board;
    private String type;
    private boolean newLecture;
    private String chapterCode;
    private ChapterLiveDto chapterLiveDto;
    private String rescheduledMessage;
    private String subjectGroupName;
    private String subjectGroupId;
    private String subjectCode;
    private String subjectName;

}
