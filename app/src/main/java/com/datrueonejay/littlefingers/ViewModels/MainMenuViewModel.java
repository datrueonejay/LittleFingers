package com.datrueonejay.littlefingers.ViewModels;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.datrueonejay.littlefingers.R;

/**
 * Created by jayden on 3/3/2018.
 * View model for the main menu
 */

public class MainMenuViewModel extends View {

    private Context context;
    private LinearLayout linearLayout;
    private WindowManager windowManager;
    private boolean isClick;

    public MainMenuViewModel (Context context)
    {
        super(context);
        this.context = context;
        this.linearLayout = new LinearLayout(context);
    }

    public void displayMenu(int layout)
    {
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.LEFT;

        this.windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(linearLayout, params);
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // inflate the main menu layout
        inflater.inflate(layout, this.linearLayout);
    }
}
