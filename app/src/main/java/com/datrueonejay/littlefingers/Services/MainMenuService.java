package com.datrueonejay.littlefingers.Services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.datrueonejay.littlefingers.Models.*;
import com.datrueonejay.littlefingers.R;

public class MainMenuService extends Service {

    // region Properties
    private int startId = -1;
    private int deviceWidth;
    private int overlayType;

    private LayoutInflater inflater;

    private FloatingAppButtonModel floatingAppButtonModel;
    private OptionsMenuModel optionsMenuModel;
    private LockedScreenModel lockedScreenModel;


    // in milliseconds
    private int timeToHoldButton = 2000;
    private WindowManager windowManager;
    //endregion

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       // ensure that this is the first time the service is being started
       if (this.startId == -1)
       {
           this.startId = startId;
           this.inflater = LayoutInflater.from(this.getApplication());
           this.windowManager = (WindowManager) this.getApplication().getSystemService(Context.WINDOW_SERVICE);

           DisplayMetrics metrics = new DisplayMetrics();
           windowManager.getDefaultDisplay().getMetrics(metrics);
           this.deviceWidth = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                   ? metrics.widthPixels
                   : metrics.heightPixels;
           // need to check based on api version with overlay type is
           this.overlayType = Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1
                   ? WindowManager.LayoutParams.TYPE_PHONE
                   : WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

           this.initLockedScreen();
           this.initOptionsMenu();
           this.initFloatingAppButton();

           this.setUpLockedScreen();
           this.setUpOptionsMenu();
           this.setUpFloatingAppButton();

           this.displayFloatingAppButton();
       }
       return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
         return null;
    }


    //region Private Methods
    private void initFloatingAppButton()
    {
        this.floatingAppButtonModel = new FloatingAppButtonModel();
        // create layout params for floatingAppButton
        floatingAppButtonModel.floatingAppButtonParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        floatingAppButtonModel.floatingAppButtonParams.gravity = Gravity.CENTER_HORIZONTAL;
        floatingAppButtonModel.floatingAppButton = inflater.inflate(floatingAppButtonModel.floatingAppButtonLayoutAsInt, null);


}

    private void setUpFloatingAppButton()
    {
        floatingAppButtonModel.floatingAppButton.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked())
            {
                case MotionEvent.ACTION_DOWN:
                {
                    floatingAppButtonModel.currX = floatingAppButtonModel.floatingAppButtonParams.x - event.getRawX();
                    floatingAppButtonModel.currY = floatingAppButtonModel.floatingAppButtonParams.y - event.getRawY();
                    floatingAppButtonModel.lastAction = MotionEvent.ACTION_DOWN;
                    break;
                }

                case MotionEvent.ACTION_MOVE:
                {

                    // see how far input moved
                    float movedY = event.getRawY() + floatingAppButtonModel.currY;
                    float movedX = event.getRawX() + floatingAppButtonModel.currX;

                    // ensure the window moves
                    floatingAppButtonModel.floatingAppButtonParams.y = (int)movedY;
                    floatingAppButtonModel.floatingAppButtonParams.x = (int)movedX;
                    windowManager.updateViewLayout(floatingAppButtonModel.floatingAppButton, floatingAppButtonModel.floatingAppButtonParams);

                    floatingAppButtonModel.lastAction = MotionEvent.ACTION_MOVE;
                    break;
                }

                case MotionEvent.ACTION_UP:
                {
                    if (floatingAppButtonModel.lastAction == MotionEvent.ACTION_DOWN)
                    {
                        view.performClick();
                        this.removeFloatingAppButton();
                        this.displayOptionsMenu();
                    }
                }

                default:
                    return false;
            }
            return true;
        });
    }

    private void displayFloatingAppButton()
    {
        this.windowManager.addView(floatingAppButtonModel.floatingAppButton, floatingAppButtonModel.floatingAppButtonParams);
    }

    private void removeFloatingAppButton()
    {
        this.windowManager.removeViewImmediate(floatingAppButtonModel.floatingAppButton);
    }

    private void initOptionsMenu()
    {
        this.optionsMenuModel = new OptionsMenuModel();
        optionsMenuModel.optionsMenu = inflater.inflate(optionsMenuModel.optionsMenuLayoutAsInt, null);

        optionsMenuModel.optionsMenuParams = new WindowManager.LayoutParams(
                deviceWidth - 100,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
    }

    private void setUpOptionsMenu()
    {
        optionsMenuModel.optionsMenu.findViewById(R.id.options).findViewById(R.id.lockScreenButton).setOnClickListener(v ->
        {
            removeOptionsMenu();
            displayLockedScreen();

        });

        optionsMenuModel.optionsMenu.findViewById(R.id.options).findViewById(R.id.closeAppButton).setOnClickListener(v ->
        {
            removeOptionsMenu();
            stopService(new Intent(getApplication(), MainMenuService.class));
        });
    }

    private void displayOptionsMenu()
    {
        this.windowManager.addView(optionsMenuModel.optionsMenu, optionsMenuModel.optionsMenuParams);
    }

    private void initLockedScreen()
    {
        this.lockedScreenModel = new LockedScreenModel();
        this.lockedScreenModel.lockedScreenParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT);

        this.lockedScreenModel.lockedScreen = inflater.inflate(lockedScreenModel.lockedScreenLayoutAsInt, null);
        this.lockedScreenModel.lockedScreenButtonOne = lockedScreenModel.lockedScreen.findViewById(R.id.buttonOne);
        this.lockedScreenModel.lockedScreenButtonTwo = lockedScreenModel.lockedScreen.findViewById(R.id.buttonTwo);
        this.lockedScreenModel.lockedScreenButtonThree = lockedScreenModel.lockedScreen.findViewById(R.id.buttonThree);
        this.lockedScreenModel.lockedScreenButtonFour = lockedScreenModel.lockedScreen.findViewById(R.id.buttonFour);

    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpLockedScreen()
    {
        Handler handler = new Handler();
        Runnable run1 = () -> {
            lockedScreenModel.isButtonOneHeld = true;
            stopLockedScreen();
        };

        Runnable run2 = () -> {
            lockedScreenModel.isButtonTwoHeld = true;
            stopLockedScreen();
        };

        Runnable run3 = () -> {
            lockedScreenModel.isButtonThreeHeld = true;
            stopLockedScreen();
        };

        Runnable run4 = () -> {
            lockedScreenModel.isButtonFourHeld = true;
            stopLockedScreen();
        };

        lockedScreenModel.lockedScreenButtonOne.setOnTouchListener((v1, e1) ->
        {
            switch (e1.getActionMasked())
            {
                case (MotionEvent.ACTION_DOWN):
                    lockedScreenModel.buttonOneBounds = new Rect(v1.getLeft(), v1.getTop(), v1.getRight(), v1.getBottom());
                    handler.postDelayed(run1, this.timeToHoldButton);
                    break;
                case (MotionEvent.ACTION_UP):
                    lockedScreenModel.isButtonOneHeld = false;
                    handler.removeCallbacks(run1);
                    v1.performClick();
                    break;
                case (MotionEvent.ACTION_MOVE):
                    // ensure that the finger is not moved out of bounds
                    if(!lockedScreenModel.buttonOneBounds.contains(v1.getLeft() + (int) e1.getX(), v1.getTop() + (int) e1.getY())){
                        handler.removeCallbacks(run1);
                    }
                default:
                    return false;
            }
            return true;
        });

        lockedScreenModel.lockedScreenButtonTwo.setOnTouchListener((v1, e1) ->
        {
            switch (e1.getActionMasked())
            {
                case (MotionEvent.ACTION_DOWN):
                    lockedScreenModel.buttonTwoBounds = new Rect(v1.getLeft(), v1.getTop(), v1.getRight(), v1.getBottom());
                    handler.postDelayed(run2, this.timeToHoldButton);
                    break;
                case (MotionEvent.ACTION_UP):
                    handler.removeCallbacks(run2);
                    lockedScreenModel.isButtonTwoHeld = false;
                    v1.performClick();
                    break;
                case (MotionEvent.ACTION_MOVE):
                    // ensure that the finger is not moved out of bounds
                    if(!lockedScreenModel.buttonTwoBounds.contains(v1.getLeft() + (int) e1.getX(), v1.getTop() + (int) e1.getY())){
                        handler.removeCallbacks(run1);
                    }
                default:
                    return false;
            }
            return true;
        });

        lockedScreenModel.lockedScreenButtonThree.setOnTouchListener((v1, e1) ->
        {
            switch (e1.getActionMasked())
            {
                case (MotionEvent.ACTION_DOWN):
                    lockedScreenModel.buttonThreeBounds = new Rect(v1.getLeft(), v1.getTop(), v1.getRight(), v1.getBottom());
                    handler.postDelayed(run3, this.timeToHoldButton);
                    break;
                case (MotionEvent.ACTION_UP):
                    lockedScreenModel.isButtonThreeHeld = false;
                    v1.performClick();
                    break;
                case (MotionEvent.ACTION_MOVE):
                    // ensure that the finger is not moved out of bounds
                    if(!lockedScreenModel.buttonThreeBounds.contains(v1.getLeft() + (int) e1.getX(), v1.getTop() + (int) e1.getY())){
                        handler.removeCallbacks(run1);
                    }
                default:
                    return false;
            }
            return true;
        });

        lockedScreenModel.lockedScreenButtonFour.setOnTouchListener((v1, e1) ->
        {
            switch (e1.getActionMasked())
            {
                case (MotionEvent.ACTION_DOWN):
                    lockedScreenModel.buttonFourBounds = new Rect(v1.getLeft(), v1.getTop(), v1.getRight(), v1.getBottom());
                    handler.postDelayed(run4, this.timeToHoldButton);
                    break;
                case (MotionEvent.ACTION_UP):
                    lockedScreenModel.isButtonFourHeld = false;
                    v1.performClick();
                    break;
                case (MotionEvent.ACTION_MOVE):
                    // ensure that the finger is not moved out of bounds
                    if(!lockedScreenModel.buttonFourBounds.contains(v1.getLeft() + (int) e1.getX(), v1.getTop() + (int) e1.getY())){
                        handler.removeCallbacks(run1);
                    }
                default:
                    return false;
            }
            return true;
        });

    }

    private void displayLockedScreen()
    {
        lockedScreenModel.lockedScreen.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        lockedScreenModel.lockedScreen.setOnSystemUiVisibilityChangeListener(listener ->
                lockedScreenModel.lockedScreen.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE)
        );
        this.windowManager.addView(lockedScreenModel.lockedScreen, lockedScreenModel.lockedScreenParams);
    }

    private void stopLockedScreen()
    {
        if (lockedScreenModel.isButtonOneHeld)
        {
            lockedScreenModel.lockedScreen.setOnSystemUiVisibilityChangeListener(listener -> { });
            lockedScreenModel.lockedScreen.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            this.windowManager.removeViewImmediate(lockedScreenModel.lockedScreen);
            displayFloatingAppButton();
        }

    }

    private void removeOptionsMenu()
    {
        this.windowManager.removeViewImmediate(optionsMenuModel.optionsMenu);
    }
    //endregion

//    // region Overriden Methods
//    @Override
//    public void onDestroy()
//    {
//        if (this.floatingAppButton != null)
//        {
//        }
//        super.onDestroy();
//    }
//    // endregion
}
