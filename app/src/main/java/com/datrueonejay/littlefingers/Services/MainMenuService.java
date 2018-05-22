package com.datrueonejay.littlefingers.Services;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

import com.datrueonejay.littlefingers.Models.*;
import com.datrueonejay.littlefingers.R;

import java.io.Console;

public class MainMenuService extends Service {

    // region Properties
    private int startId = -1;

    private DisplayMetrics metrics;
    private int deviceWidth;
    private int currHeight;
    private int overlayType;

    private LayoutInflater inflater;
    private WindowManager windowManager;

    private FloatingAppButtonModel floatingAppButtonModel;
    private OptionsMenuModel optionsMenuModel;
    private LockedScreenModel lockedScreenModel;

    // in milliseconds
    private final int timeToHoldButton = 2000;
    //endregion

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       // ensure that this is the first time the service is being started
       if (this.startId == -1)
       {
           this.startId = startId;
           this.inflater = LayoutInflater.from(this.getApplication());
           this.windowManager = (WindowManager) this.getApplication().getSystemService(Context.WINDOW_SERVICE);

           Display d = windowManager.getDefaultDisplay();
           Point size = new Point();
           d.getSize(size);
           int width = size.x;

           this.metrics = new DisplayMetrics();
           windowManager.getDefaultDisplay().getMetrics(metrics);
           int a = getResources().getConfiguration().orientation;
           this.deviceWidth = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                   ? metrics.widthPixels
                   : metrics.heightPixels;
           this.currHeight = metrics.heightPixels;
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
                    removeOptionsMenu();
                    break;
                }

                case MotionEvent.ACTION_UP:
                {
                    if (floatingAppButtonModel.lastAction == MotionEvent.ACTION_DOWN)
                    {
                        view.performClick();
                        ValueAnimator translationDown = ValueAnimator.ofFloat(floatingAppButtonModel.floatingAppButtonParams.y, currHeight/2 - floatingAppButtonModel.floatingAppButton.getHeight()/2);
                        ValueAnimator translationCenter = ValueAnimator.ofFloat(floatingAppButtonModel.floatingAppButtonParams.x, 0);
                        translationCenter.addUpdateListener(animation -> {
                            floatingAppButtonModel.floatingAppButtonParams.x = Math.round((float)translationCenter.getAnimatedValue());
                            windowManager.updateViewLayout(floatingAppButtonModel.floatingAppButton, floatingAppButtonModel.floatingAppButtonParams);
                        });
                        translationDown.addUpdateListener(animation -> {
                            floatingAppButtonModel.floatingAppButtonParams.y = Math.round((float)translationDown.getAnimatedValue());
                            windowManager.updateViewLayout(floatingAppButtonModel.floatingAppButton, floatingAppButtonModel.floatingAppButtonParams);
                        });

                        translationDown.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                displayOptionsMenu();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                            }
                        });
                        translationDown.setDuration(500);
                        translationCenter.setDuration(500);
                        translationDown.start();
                        translationCenter.start();

                        windowManager.updateViewLayout(floatingAppButtonModel.floatingAppButton, floatingAppButtonModel.floatingAppButtonParams);

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
        try
        {
            if (!floatingAppButtonModel.isShowing)
            {
                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                fadeIn.setDuration(1000);
                this.windowManager.addView(floatingAppButtonModel.floatingAppButton, floatingAppButtonModel.floatingAppButtonParams);
                floatingAppButtonModel.floatingAppButton.findViewById(R.id.floatingButtonContainer).startAnimation(fadeIn);
                floatingAppButtonModel.isShowing = true;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void removeFloatingAppButton()
    {
        try
        {
            if (floatingAppButtonModel.isShowing)
            {
                floatingAppButtonModel.isShowing = false;

                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
                fadeOut.setDuration(100);
                floatingAppButtonModel.floatingAppButton.findViewById(R.id.floatingButtonContainer).startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        windowManager.removeViewImmediate(floatingAppButtonModel.floatingAppButton);

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
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
        optionsMenuModel.isShowing = false;

    }

    private void setUpOptionsMenu()
    {
        optionsMenuModel.optionsMenu.findViewById(R.id.options).findViewById(R.id.lockScreenButton).setOnClickListener(v ->
        {
            if (optionsMenuModel.isShowing)
            {
                removeOptionsMenu();
                removeFloatingAppButton();
                displayLockedScreen();
            }
        });

        optionsMenuModel.optionsMenu.findViewById(R.id.options).findViewById(R.id.closeAppButton).setOnClickListener(v ->
        {
            if (optionsMenuModel.isShowing)
            {
                removeOptionsMenu();
                removeFloatingAppButton();
                stopService(new Intent(getApplication(), MainMenuService.class));
            }

        });
    }

    private void displayOptionsMenu()
    {

        optionsMenuModel.optionsMenuParams.y = currHeight/2 - floatingAppButtonModel.floatingAppButton.getHeight() - 75;
        try
        {
            if (!optionsMenuModel.isShowing)
            {
                windowManager.addView(optionsMenuModel.optionsMenu, optionsMenuModel.optionsMenuParams);
                Animation slideUp = AnimationUtils.loadAnimation(getApplication(), R.anim.slide_up);
                optionsMenuModel.optionsMenu.findViewById(R.id.optionsMenuContainer).startAnimation(slideUp);
                optionsMenuModel.isShowing = true;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }


    }

    private void removeOptionsMenu()
    {
        try
        {
            if (optionsMenuModel.isShowing)
            {
                optionsMenuModel.isShowing = false;
                Animation slideDown = AnimationUtils.loadAnimation(getApplication(), R.anim.slide_down);
                optionsMenuModel.optionsMenu.findViewById(R.id.optionsMenuContainer).startAnimation(slideDown);
                slideDown.setAnimationListener(new Animation.AnimationListener()
                {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        Handler handler = new Handler();
                        handler.postDelayed(() -> {
                            windowManager.removeViewImmediate(optionsMenuModel.optionsMenu);
                            },
                                10);

                    }
                });

            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
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
            removeLockedScreen();
        };

        Runnable run2 = () -> {
            lockedScreenModel.isButtonTwoHeld = true;
            removeLockedScreen();
        };

        Runnable run3 = () -> {
            lockedScreenModel.isButtonThreeHeld = true;
            removeLockedScreen();
        };

        Runnable run4 = () -> {
            lockedScreenModel.isButtonFourHeld = true;
            removeLockedScreen();
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
                        handler.removeCallbacks(run2);
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
                        handler.removeCallbacks(run3);
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
                        handler.removeCallbacks(run4);
                    }
                default:
                    return false;
            }
            return true;
        });

    }

    private void displayLockedScreen()
    {
        // ensure locked screen is full screen
        lockedScreenModel.lockedScreen.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        // hides notification bar and nav bar if user swipes them
        lockedScreenModel.lockedScreen.setOnSystemUiVisibilityChangeListener(listener ->
                lockedScreenModel.lockedScreen.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE)
        );
        try
        {
            if (!lockedScreenModel.isShowing)
            {
                this.windowManager.addView(lockedScreenModel.lockedScreen, lockedScreenModel.lockedScreenParams);
                lockedScreenModel.isShowing = true;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void removeLockedScreen()
    {
        if (lockedScreenModel.isButtonOneHeld)
        {
            lockedScreenModel.lockedScreen.setOnSystemUiVisibilityChangeListener(listener -> { });
            lockedScreenModel.lockedScreen.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            try
            {
                if (lockedScreenModel.isShowing)
                {
                    this.windowManager.removeViewImmediate(lockedScreenModel.lockedScreen);
                    lockedScreenModel.isShowing = false;
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            displayFloatingAppButton();
        }

    }
    //endregion

    // region Overriden Methods
@Override
public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    // Checks the orientation of the screen
    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        this.currHeight = metrics.heightPixels;
    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
        this.currHeight = metrics.heightPixels;
    }
}
//    @Override
//    public void onDestroy()
//    {
//        if (this.floatingAppButton != null)
//        {
//        }
//        super.onDestroy();
//    }
    // endregion
}
