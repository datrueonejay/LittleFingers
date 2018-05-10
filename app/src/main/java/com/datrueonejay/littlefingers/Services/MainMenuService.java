package com.datrueonejay.littlefingers.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.datrueonejay.littlefingers.Constants.Constants;

public class MainMenuService extends Service {

    // region Properties
    private int layoutAsInt;
    private float currX;
    private float currY;
    int lastAction;
    //endregion

   @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       this.layoutAsInt = intent.getIntExtra(Constants.MAIN_MENU_LAYOUT_EXTRA, 0);
       displayDraggableView();
       return START_STICKY;
    }

    public void displayDraggableView()
    {
        // need to check based on api version
        int overlayType = Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1
                ? WindowManager.LayoutParams.TYPE_PHONE
                : WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;

        WindowManager windowManager = (WindowManager) this.getApplication().getSystemService(Context.WINDOW_SERVICE);
        // base layout for options button
        FrameLayout linearLayout = new FrameLayout(this.getApplication());
        LayoutInflater inflater = LayoutInflater.from(this.getApplication());
        View optionsButton = inflater.inflate(layoutAsInt, linearLayout);
        makeWindowDraggable(optionsButton, windowManager, params);
        windowManager.addView(optionsButton, params);
    }

    @Override
    public IBinder onBind(Intent intent) {
         return null;
    }


    //region Private Methods
    private void makeWindowDraggable(View viewInWindow, WindowManager windowManager, WindowManager.LayoutParams params)
    {
        viewInWindow.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked())
            {
                case MotionEvent.ACTION_DOWN:
                {
                    currX = params.x - event.getRawX();
                    currY = params.y - event.getRawY();
                    lastAction = MotionEvent.ACTION_DOWN;
                    break;
                }

                case MotionEvent.ACTION_MOVE:
                {

                    // see how far input moved
                    float movedX = event.getRawX() + currX;
                    float movedY = event.getRawY() + currY;

                    // ensure the window moves
                    params.x = (int)movedX;
                    params.y = (int)movedY;
                    windowManager.updateViewLayout(viewInWindow, params);

                    lastAction = MotionEvent.ACTION_MOVE;
                    break;
                }

                default:
                    return false;
            }
            return true;
        });
    }
    //endregion
}
