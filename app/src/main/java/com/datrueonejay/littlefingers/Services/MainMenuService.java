package com.datrueonejay.littlefingers.Services;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.arch.lifecycle.ViewModelProviders;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.datrueonejay.littlefingers.R;
import com.datrueonejay.littlefingers.ViewModels.LockedScreenViewModel;
import com.datrueonejay.littlefingers.ViewModels.PermissionCheckViewModel;

public class MainMenuService extends Service {

    // region Properties
    private float currX;
    private float currY;
    private int lastAction;
    private int startId = -1;

    private int deviceWidth;

    private int overlayType;

    private boolean isButtonOneHeld;
    private boolean isButtonTwoHeld;
    private boolean isButtonThreeHeld;
    private boolean isButtonFourHeld;

    private Button lockedScreenButtonOne;
    private Button lockedScreenButtonTwo;
    private Button lockedScreenButtonThree;
    private Button lockedScreenButtonFour;

    // in milliseconds
    private int timeToHoldButton = 2000;

    private Rect rect1;
    private Rect rect2;
    private Rect rect3;
    private Rect rect4;

    private LayoutInflater inflater;

    private View floatingAppButton;
    private int floatingAppButtonAsInt = R.layout.menu_button;
    private WindowManager.LayoutParams floatingAppButtonParams;


    private AlertDialog.Builder builder;
    private Dialog optionsDialog;


    private View optionsMenu;
    private final int optionsMenuLayoutAsInt = R.layout.options_menu;
    private WindowManager.LayoutParams optionsMenuParams;

    private View lockedScreen;
    private final int lockedScreenLayoutAsInt = R.layout.locked_screen;
    private WindowManager.LayoutParams lockedScreenParams;

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
        // create layout params for floatingAppButton
        this.floatingAppButtonParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        this.floatingAppButtonParams.gravity = Gravity.CENTER_HORIZONTAL;
        this.floatingAppButton = inflater.inflate(floatingAppButtonAsInt, null);


    }

    private void setUpFloatingAppButton()
    {
        this.floatingAppButton.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked())
            {
                case MotionEvent.ACTION_DOWN:
                {
                    currX = this.floatingAppButtonParams.x - event.getRawX();
                    currY = this.floatingAppButtonParams.y - event.getRawY();
                    lastAction = MotionEvent.ACTION_DOWN;
                    break;
                }

                case MotionEvent.ACTION_MOVE:
                {

                    // see how far input moved
                    float movedY = event.getRawY() + currY;
                    float movedX = event.getRawX() + currX;

                    // ensure the window moves
                    this.floatingAppButtonParams.y = (int)movedY;
                    this.floatingAppButtonParams.x = (int)movedX;
                    windowManager.updateViewLayout(this.floatingAppButton, this.floatingAppButtonParams);

                    lastAction = MotionEvent.ACTION_MOVE;
                    break;
                }

                case MotionEvent.ACTION_UP:
                {
                    if (lastAction == MotionEvent.ACTION_DOWN)
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
        this.windowManager.addView(this.floatingAppButton, this.floatingAppButtonParams);
    }

    private void removeFloatingAppButton()
    {
        this.windowManager.removeViewImmediate(this.floatingAppButton);
    }

    private void initOptionsMenu()
    {
        this.optionsMenu = inflater.inflate(this.optionsMenuLayoutAsInt, null);
        this.builder = new AlertDialog.Builder(this.getApplication());
        this.optionsDialog = builder.setView(this.optionsMenu).create();
        this.optionsDialog.getWindow().setType(this.overlayType);

        this.optionsMenuParams = new WindowManager.LayoutParams(
                deviceWidth - 100,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
    }

    private void setUpOptionsMenu()
    {
        optionsMenu.findViewById(R.id.options).findViewById(R.id.lockScreenButton).setOnClickListener(v ->
        {
            this.optionsDialog.dismiss();
            removeOptionsMenu();
            displayLockedScreen();

        });

        optionsMenu.findViewById(R.id.options).findViewById(R.id.closeAppButton).setOnClickListener(v ->
        {
            this.optionsDialog.dismiss();
            removeOptionsMenu();
            stopService(new Intent(getApplication(), MainMenuService.class));
        });
    }

    private void displayOptionsMenu()
    {
        this.windowManager.addView(this.optionsMenu, this.optionsMenuParams);
    }

    private void initLockedScreen()
    {
        this.lockedScreen = inflater.inflate(this.lockedScreenLayoutAsInt, null);
        this.lockedScreenButtonOne = lockedScreen.findViewById(R.id.buttonOne);
        this.lockedScreenButtonTwo = lockedScreen.findViewById(R.id.buttonTwo);
        this.lockedScreenButtonThree = lockedScreen.findViewById(R.id.buttonThree);
        this.lockedScreenButtonFour = lockedScreen.findViewById(R.id.buttonFour);

        this.lockedScreenParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpLockedScreen()
    {
        Handler handler = new Handler();
        Runnable run1 = () -> {
            isButtonOneHeld = true;
            stopLockedScreen();
        };

        Runnable run2 = () -> {
            isButtonTwoHeld = true;
            stopLockedScreen();
        };

        Runnable run3 = () -> {
            isButtonThreeHeld = true;
            stopLockedScreen();
        };

        Runnable run4 = () -> {
            isButtonFourHeld = true;
            stopLockedScreen();
        };

        this.lockedScreenButtonOne.setOnTouchListener((v1, e1) ->
        {
            switch (e1.getActionMasked())
            {
                case (MotionEvent.ACTION_DOWN):
                    rect1 = new Rect(v1.getLeft(), v1.getTop(), v1.getRight(), v1.getBottom());
                    handler.postDelayed(run1, this.timeToHoldButton);
                    break;
                case (MotionEvent.ACTION_UP):
                    isButtonOneHeld = false;
                    handler.removeCallbacks(run1);
                    v1.performClick();
                    break;
                case (MotionEvent.ACTION_MOVE):
                    if(!rect1.contains(v1.getLeft() + (int) e1.getX(), v1.getTop() + (int) e1.getY())){
                        handler.removeCallbacks(run1);
                    }
                default:
                    return false;
            }
            return true;
        });

        this.lockedScreenButtonTwo.setOnTouchListener((v1, e1) ->
        {
            switch (e1.getActionMasked())
            {
                case (MotionEvent.ACTION_DOWN):
                    rect2 = new Rect(v1.getLeft(), v1.getTop(), v1.getRight(), v1.getBottom());
                    handler.postDelayed(run2, this.timeToHoldButton);
                    break;
                case (MotionEvent.ACTION_UP):
                    handler.removeCallbacks(run2);
                    isButtonTwoHeld = false;
                    v1.performClick();
                    break;
                case (MotionEvent.ACTION_MOVE):
                    if(!rect2.contains(v1.getLeft() + (int) e1.getX(), v1.getTop() + (int) e1.getY())){
                        handler.removeCallbacks(run1);
                    }
                default:
                    return false;
            }
            return true;
        });

        this.lockedScreenButtonThree.setOnTouchListener((v1, e1) ->
        {
            switch (e1.getActionMasked())
            {
                case (MotionEvent.ACTION_DOWN):
                    rect3 = new Rect(v1.getLeft(), v1.getTop(), v1.getRight(), v1.getBottom());
                    handler.postDelayed(run3, this.timeToHoldButton);
                    break;
                case (MotionEvent.ACTION_UP):
                    isButtonThreeHeld = false;
                    v1.performClick();
                    break;
                case (MotionEvent.ACTION_MOVE):
                    if(!rect3.contains(v1.getLeft() + (int) e1.getX(), v1.getTop() + (int) e1.getY())){
                        handler.removeCallbacks(run1);
                    }
                default:
                    return false;
            }
            return true;
        });

        this.lockedScreenButtonFour.setOnTouchListener((v1, e1) ->
        {
            switch (e1.getActionMasked())
            {
                case (MotionEvent.ACTION_DOWN):
                    rect4 = new Rect(v1.getLeft(), v1.getTop(), v1.getRight(), v1.getBottom());
                    handler.postDelayed(run4, this.timeToHoldButton);
                    break;
                case (MotionEvent.ACTION_UP):
                    isButtonFourHeld = false;
                    v1.performClick();
                    break;
                case (MotionEvent.ACTION_MOVE):
                    if(!rect4.contains(v1.getLeft() + (int) e1.getX(), v1.getTop() + (int) e1.getY())){
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
        lockedScreen.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        lockedScreen.setOnSystemUiVisibilityChangeListener(listener ->
                lockedScreen.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE)
        );
        this.windowManager.addView(this.lockedScreen, this.lockedScreenParams);
    }

    private void stopLockedScreen()
    {
        if (isButtonOneHeld)
        {
            lockedScreen.setOnSystemUiVisibilityChangeListener(listener ->
                    {
                        return;
                    }
            );
            lockedScreen.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            this.windowManager.removeViewImmediate(lockedScreen);
            displayFloatingAppButton();
        }

    }

    private void removeOptionsMenu()
    {
        this.windowManager.removeViewImmediate(this.optionsMenu);
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
