package arivihan.technologies.doubtbuzzter2.adapter.todo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arivihan.technologies.doubtbuzzter2.fragments.todo.TodoListFragment;
import arivihan.technologies.doubtbuzzter2.model.TodoDateSelectionModel;

public class TaskPagerAdapter extends FragmentStateAdapter {

    private final List<TodoDateSelectionModel> dateList;
    private final Map<Integer, Fragment> fragmentMap = new HashMap<>();

    public TaskPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<TodoDateSelectionModel> dateList) {
        super(fragmentActivity);
        this.dateList = dateList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        TodoDateSelectionModel date = dateList.get(position);
        Fragment fragment = TodoListFragment.newInstance(date, position==(dateList.size()-1));
        fragmentMap.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    public Fragment getFragment(int position) {
        return fragmentMap.get(position);
    }
}

