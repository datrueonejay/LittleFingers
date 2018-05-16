package com.datrueonejay.littlefingers.Models;

import android.view.View;
import android.view.WindowManager;

import com.datrueonejay.littlefingers.R;

public class FloatingAppButtonModel {

    public final int floatingAppButtonLayoutAsInt = R.layout.menu_button;

    public float currX;
    public float currY;
    public int lastAction;

    public WindowManager.LayoutParams floatingAppButtonParams;

    public View floatingAppButton;

}
