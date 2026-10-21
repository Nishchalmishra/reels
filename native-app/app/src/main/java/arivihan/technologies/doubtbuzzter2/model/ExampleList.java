package arivihan.technologies.doubtbuzzter2.model;

public class ExampleList {
    private String mImageResource;
    private String videoName;
    private String videoDescription;
    private String videoReference;
    private String videoLikedBy;
    private String videoId;
    private boolean isLikebale;


    //ye tumne variables declare kr die

    public  ExampleList(String imageResource, String videoName, String videoDescription, String videoReference, String videoLikedBy, String videoId){
        this.mImageResource = imageResource;
        this.videoName = videoName;
        this.videoDescription = videoDescription;
        this.videoReference = videoReference;
        this.videoLikedBy = videoLikedBy;
        this.videoId = videoId;
        //ye tumne constructor ka use krke variables ko respective value assign kr di
    }
    // now we will use getter methods to get the values of our variables


    public String getmImageResource() {
        return mImageResource;
    }

    public void setmImageResource(String mImageResource) {
        this.mImageResource = mImageResource;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoDescription() {
        return videoDescription;
    }

    public void setVideoDescription(String videoDescription) {
        this.videoDescription = videoDescription;
    }

    public String getVideoReference() {
        return videoReference;
    }

    public void setVideoReference(String videoReference) {
        this.videoReference = videoReference;
    }

    public String getVideoLikedBy() {
        return videoLikedBy;
    }

    public void setVideoLikedBy(String videoLikedBy) {
        this.videoLikedBy = videoLikedBy;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public boolean isLikebale() {
        return isLikebale;
    }

    public void setLikebale(boolean likebale) {
        isLikebale = likebale;
    }
}