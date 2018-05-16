package com.datrueonejay.littlefingers.Models;

import android.graphics.Rect;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.datrueonejay.littlefingers.R;


public class LockedScreenModel {

    public final int lockedScreenLayoutAsInt = R.layout.locked_screen;

    public boolean isButtonOneHeld;
    public boolean isButtonTwoHeld;
    public boolean isButtonThreeHeld;
    public boolean isButtonFourHeld;


    public WindowManager.LayoutParams lockedScreenParams;

    public View lockedScreen;

    public Button lockedScreenButtonOne;
    public Button lockedScreenButtonTwo;
    public Button lockedScreenButtonThree;
    public Button lockedScreenButtonFour;

    public Rect buttonOneBounds;
    public Rect buttonTwoBounds;
    public Rect buttonThreeBounds;
    public Rect buttonFourBounds;
}
