package arivihan.technologies.doubtbuzzter2;

/*
 * File: MyReelsListener Copyright 31/5/21 1:24 AM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 31/5/21 1:24 AM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import java.util.Set;

public interface MyReelsListener {
    void setLike(boolean liked, long likes, int position);

    void setViews(int views, String reelsDtoName, int position);

    void setWatched(String reelsDtoName);

    void setAspectRatio(double aspectRatio, String reelsDtoName);
}

