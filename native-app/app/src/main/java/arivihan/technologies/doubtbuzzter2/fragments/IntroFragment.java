package arivihan.technologies.doubtbuzzter2.fragments;/* Decompiler 138ms, total 412ms, lines 83 */

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import arivihan.technologies.doubtbuzzter2.listener.MyInterface;

public class IntroFragment extends Fragment implements MyInterface {
    private static final String BACKGROUND_COLOR = "backgroundColor";
    private static final String PAGE = "page";
    private int mBackgroundColor;
    private int mPage;
    private TextView no_of_qs;
    private TextView time;

    public IntroFragment() {
    }

    public static void changeView(int var0, int var1) {
    }

    public static IntroFragment newInstance(int var0, int var1) {
        IntroFragment var2 = new IntroFragment();
        Bundle var3 = new Bundle();
        var3.putInt("backgroundColor", var0);
        var3.putInt("page", var1);
        var2.setArguments(var3);
        return var2;
    }

    public void myCHange(int var1, int var2) {
        TextView var3 = this.time;
        StringBuilder var4 = new StringBuilder();
        var4.append("");
        var4.append(var1);
        var4.append(" : 00\n mins");
        var3.setText(var4.toString());
        var3 = this.no_of_qs;
        var4 = new StringBuilder();
        var4.append("");
        var4.append(var2);
        var4.append(" \nQues");
        var3.setText(var4.toString());
    }

    public void onCreate(Bundle var1) {
        super.onCreate(var1);
        if (this.getArguments().containsKey("backgroundColor")) {
            this.mBackgroundColor = this.getArguments().getInt("backgroundColor");
            if (this.getArguments().containsKey("page")) {
                this.mPage = this.getArguments().getInt("page");
            }
        }
    }

    public View onCreateView(LayoutInflater var1, ViewGroup var2, Bundle var3) {
        int var4;
        if (this.mPage != 0) {
            var4 = 2131492925;
        } else {
            var4 = 2131492924;
        }

        View var5 = this.getActivity().getLayoutInflater().inflate(var4, var2, false);
        if (this.mPage == 0) {
//            this.time = (TextView)var5.findViewById(2131296568);
//            this.no_of_qs = (TextView)var5.findViewById(2131296447);
        }

        var5.setTag(this.mPage);
        return var5;
    }

    public void onViewCreated(View var1, Bundle var2) {
        super.onViewCreated(var1, var2);
       // var1.findViewById(2131296397).setBackgroundColor(this.mBackgroundColor);
    }
}