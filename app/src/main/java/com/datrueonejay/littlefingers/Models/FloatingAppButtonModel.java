package com.datrueonejay.littlefingers.Models;

import android.view.View;
import android.view.WindowManager;

import com.datrueonejay.littlefingers.R;

public class FloatingAppButtonModel {

    public final int floatingAppButtonLayoutAsInt = R.layout.menu_button;

    public boolean isShowing;

    public float currX;
    public float currY;

    public float pressDownRawX;
    public float pressDownRawY;


    public int lastAction;

    public WindowManager.LayoutParams floatingAppButtonParams;

    public View floatingAppButton;

}
