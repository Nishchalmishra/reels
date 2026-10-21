package arivihan.technologies.doubtbuzzter2.helper;

/*
 * File: ContextWrapper Copyright 23/7/22 7:32 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 23/7/22 7:32 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public class ContextWrapper extends android.content.ContextWrapper {

    public ContextWrapper(Context base) {
        super(base);
    }

    public static ContextWrapper wrap(Context context, Locale newLocale) {
        Resources res = context.getResources();
        Configuration configuration = res.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (newLocale != null && !newLocale.equals(configuration.getLocales().get(0))) {
                configuration.setLocale(newLocale);
                configuration.setLocales(new LocaleList(newLocale));
                configuration.fontScale = 1f;

                context = context.createConfigurationContext(configuration);
            }
        }

        return new ContextWrapper(context);
    }

}
