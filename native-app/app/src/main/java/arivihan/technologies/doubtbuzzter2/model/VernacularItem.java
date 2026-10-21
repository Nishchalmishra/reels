package arivihan.technologies.doubtbuzzter2.model;

/*
 * File: VernacularItem Copyright 10/9/22 4:40 PM Arivihan technologies private limited. All rights reserved.
 * <p>
 * This software is the confidential and proprietary information of Arivihan technologies private limited.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into
 * with Arivihan technologies private limited.
 * <p>
 * Created on: 10/9/22 4:40 PM
 * Created by: Sonu Prashant
 * Version: 1.0
 * Project Name: Arivihan
 * Last updated / updated by: Sonu Prashant
 */

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.io.Serializable;

import arivihan.technologies.doubtbuzzter2.R;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VernacularItem implements Serializable {
    public String id;
    public String name;
    public String url;
    public boolean checked;
    public boolean board;
    private Drawable context;
    private int imageUrl;
    private String videoUrl;


    public VernacularItem() {

    }

    public VernacularItem(String id, String name) {
        this.id =  id;
        this.name = name;
    }

    public VernacularItem(String id, String name,Boolean board,String url) {
        this.id =  id;
        this.name = name;
        this.board = board;
        this.url = url;
    }


    public VernacularItem(String id, String name, boolean checked, Context context) {
        this.id =  id;
        this.name = name;
        this.checked = checked;
    }

    public VernacularItem(String id, String name, boolean checked, Context context,Boolean board,String url) {
        this.id =  id;
        this.name = name;
        this.checked = checked;
        this.board = board;
        this.url = url;
    }

    public VernacularItem(String id, String name, String url, boolean checked) {
        this.id =  id;
        this.name = name;
        this.url = url;
    }

}
