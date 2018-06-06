package com.datrueonejay.littlefingers.Models;

import android.graphics.Rect;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.datrueonejay.littlefingers.R;


public class LockedScreenModel {

    public final int lockedScreenLayoutAsInt = R.layout.locked_screen;

    public boolean isShowing;
    public boolean isHelp;

    public boolean isButtonOneHeld = false;
    public boolean isButtonTwoHeld = false;
    public boolean isButtonThreeHeld = false;
    public boolean isButtonFourHeld = false;

    public WindowManager.LayoutParams lockedScreenParams;

    public View lockedScreen;

    public Button lockedScreenButtonOne;
    public Button lockedScreenButtonTwo;
    public Button lockedScreenButtonThree;
    public Button lockedScreenButtonFour;
    public TextView helpInstructions;

    public Rect buttonOneBounds;
    public Rect buttonTwoBounds;
    public Rect buttonThreeBounds;
    public Rect buttonFourBounds;

    public TransitionDrawable buttonOneTransition;
    public TransitionDrawable buttonTwoTransition;
    public TransitionDrawable buttonThreeTransition;
    public TransitionDrawable buttonFourTransition;

}
