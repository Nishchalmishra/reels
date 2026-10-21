package arivihan.technologies.doubtbuzzter2.observer;

import arivihan.technologies.doubtbuzzter2.model.ToDoListState;

public interface ToDoListChangeListener {
    void onToDoListStateChanged(ToDoListState newState);
}
