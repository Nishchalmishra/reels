package arivihan.technologies.doubtbuzzter2.reelsdemo;

import org.json.JSONObject;

/** Typed public manifest. Grading keys never belong in this DTO or the APK. */
public final class DiscoveryMedia {
    public final String id,title,topic,subject,chapterId,source,concept;
    public final int contentVersion;
    public final double durationSeconds;
    public final JSONObject metadata;
    public DiscoveryMedia(JSONObject row){
        id=row.optString("id");title=row.optString("title");topic=row.optString("topic");subject=row.optString("subject");
        chapterId=row.optString("chapterId");source=row.optString("source");concept=row.optString("concept");
        contentVersion=row.optInt("contentVersion",1);durationSeconds=row.optDouble("duration");metadata=row;
        if(id.isEmpty()||durationSeconds<=0||!source.startsWith("http://127.0.0.1:8766/media/"))throw new IllegalArgumentException("Invalid local lesson manifest");
    }
}
