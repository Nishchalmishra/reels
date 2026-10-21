package arivihan.technologies.doubtbuzzter2.helper;

import androidx.annotation.Keep;

@Keep
public class LikeAndView {

    private int likes;
    private int views;
    private String aspectRatio;

    public LikeAndView(int likes, int views) {
        this.likes = likes;
        this.views = views;
    }

    public LikeAndView() {

    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public String getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(String aspectRatio) {
        this.aspectRatio = aspectRatio;
    }
}
