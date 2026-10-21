package arivihan.technologies.doubtbuzzter2.model;/* Decompiler 5ms, total 326ms, lines 32 */
import android.view.View;
import androidx.viewpager.widget.ViewPager.PageTransformer;

import arivihan.technologies.doubtbuzzter2.R;

public class IntroPageTransformer implements PageTransformer {
    public void transformPage(View var1, float var2) {
        int var5 = (Integer)var1.getTag();
        float var3 = (float)var1.getWidth();
        float var4 = Math.abs(var2);
        if (var2 > -1.0F) {
            if (var2 >= 1.0F) {
                return;
            }

            if (var2 == 0.0F) {
                return;
            }

            var4 = 1.0F - var4;
            if (var5 == 0 && var1 != null) {
                var1.setAlpha(var4);
                var1.setTranslationX(-(var3 * var2) * 1.5F);
            }
        }

    }
}