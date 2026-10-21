package arivihan.technologies.doubtbuzzter2.common;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arivihan.technologies.doubtbuzzter2.R;

public class ArivihanCustomDialog extends Dialog {
    public ArivihanCustomDialog(@NonNull Context context) {
        this(context,R.style.AlertDialogTheme);
    }
    public ArivihanCustomDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }
    protected ArivihanCustomDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
}
