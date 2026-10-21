package arivihan.technologies.doubtbuzzter2.listener;

/*
 * File: FragmenChangetListener Copyright 9/7/22 3:02 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 9/7/22 3:02 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import androidx.fragment.app.Fragment;

public interface FragmentChangeListener {
     void setFragment(Fragment fragment);

     void setPhoneNumber(String number);

     void showPhoneNumberPopUp();

     void setName(String name);

     void setClass(String className);

     void setCourse(String course);

     void setExam(String exam);

     void registered(boolean registered);

     void moveToHomeScreen();

     void setCurrentDefaultLocaleStr(String lang);

     void playAnimation();

     void finishActivity();
     void moveToOnBoardingActivity(String phoneNumberToLoginWith, String email, String id);

}
