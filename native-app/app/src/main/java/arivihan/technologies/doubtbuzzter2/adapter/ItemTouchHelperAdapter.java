package arivihan.technologies.doubtbuzzter2.adapter;

public interface ItemTouchHelperAdapter {

    void OnItemMove(int fromposition,int toposition);
    default void clearView(){};

}
