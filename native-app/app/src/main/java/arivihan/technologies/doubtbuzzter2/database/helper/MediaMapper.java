package arivihan.technologies.doubtbuzzter2.database.helper;

import java.util.ArrayList;
import java.util.List;

import arivihan.technologies.doubtbuzzter2.model.DownloadMedia;
import arivihan.technologies.doubtbuzzter2.model.SyncDownloadRequest;

/**
 * Helper class for converting between Room DB models and API request/response models
 * for micro-lecture downloads.
 *
 * DownloadMedia is not modified — conversion is handled here.
 */
public class MediaMapper {

    private MediaMapper() {}

    /**
     * Converts a {@link DownloadMedia} to a {@link SyncDownloadRequest} for the sync request body.
     * Only microLectureId is available in DownloadMedia; remaining fields default to null/0.
     */
    public static SyncDownloadRequest fromDownloadMedia(DownloadMedia downloadMedia) {
        SyncDownloadRequest request = new SyncDownloadRequest();
        request.setMicroLectureId(downloadMedia.getMicroLectureId());
        return request;
    }

    /**
     * Converts a list of {@link DownloadMedia} to a list of {@link SyncDownloadRequest}.
     */
    public static List<SyncDownloadRequest> fromDownloadMediaList(List<DownloadMedia> list) {
        List<SyncDownloadRequest> result = new ArrayList<>();
        for (DownloadMedia item : list) {
            result.add(fromDownloadMedia(item));
        }
        return result;
    }
}
