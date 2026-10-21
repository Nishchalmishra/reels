package arivihan.technologies.doubtbuzzter2.enums;

public enum LikeDislikeEnum {
    LIKE(true), DISLIKE(false), NOTHING(null);

    private Boolean isLike;

    LikeDislikeEnum(Boolean isLike){
        this.isLike = isLike;
    }

    public Boolean getIsLike(){
        return isLike;
    }
}
