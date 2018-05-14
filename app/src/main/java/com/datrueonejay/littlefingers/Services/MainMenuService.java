package com.datrueonejay.littlefingers.Services;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.arch.lifecycle.ViewModelProviders;
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
import android.widget.Button;
import android.widget.FrameLayout;

import com.datrueonejay.littlefingers.Constants.Constants;
import com.datrueonejay.littlefingers.R;
import com.datrueonejay.littlefingers.ViewModels.LockedScreenViewModel;
import com.datrueonejay.littlefingers.ViewModels.PermissionCheckViewModel;

public class MainMenuService extends Service {

    // region Properties
    private int layoutAsInt;
    private float currX;
    private float currY;
    private int lastAction;
    private int startId = -1;

    private boolean isButtonOneHeld;
    private boolean isButtonTwoHeld;
    private boolean isButtonThreeHeld;
    private boolean isButtonFourHeld;

    private LayoutInflater inflater;

    private View viewInWindow;
    private View lockedScreen;
    private WindowManager windowManager;
    //endregion

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       // ensure that this is the first time the service is being started
       if (this.startId == -1)
       {
           this.startId = startId;
           this.layoutAsInt = intent.getIntExtra(Constants.MAIN_MENU_LAYOUT_EXTRA, 0);
           this.inflater = LayoutInflater.from(this.getApplication());
           displayDraggableView();

       }
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
        params.gravity = Gravity.CENTER_HORIZONTAL;

        this.windowManager = (WindowManager) this.getApplication().getSystemService(Context.WINDOW_SERVICE);
        // base layout for options button
        FrameLayout linearLayout = new FrameLayout(this.getApplication());
        this.viewInWindow = inflater.inflate(layoutAsInt, linearLayout);
        setUpDraggableView(this.viewInWindow, this.windowManager, params);
        this.windowManager.addView(this.viewInWindow, params);
    }

    public void displayLockedScreenView()
    {

    }

    @Override
    public IBinder onBind(Intent intent) {
         return null;
    }


    //region Private Methods
    private void setUpDraggableView(View viewInWindow, WindowManager windowManager, WindowManager.LayoutParams params)
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
                    float movedY = event.getRawY() + currY;
                    float movedX = event.getRawX() + currX;

                    // ensure the window moves
                    params.y = (int)movedY;
                    params.x = (int)movedX;
                    windowManager.updateViewLayout(viewInWindow, params);

                    lastAction = MotionEvent.ACTION_MOVE;
                    break;
                }

                case MotionEvent.ACTION_UP:
                {
                    if (lastAction == MotionEvent.ACTION_DOWN)
                    {
                        view.performClick();
                        AlertDialog.Builder builder = new AlertDialog.Builder(this.getApplication());
                        View optionsMenu = inflater.inflate(R.layout.options_menu, null);
                        Dialog dialog = builder.setView(optionsMenu).create();

                        optionsMenu.findViewById(R.id.options).findViewById(R.id.lockScreenButton).setOnClickListener(v ->
                        {
                            WindowManager.LayoutParams params2 = new WindowManager.LayoutParams(
                                    WindowManager.LayoutParams.MATCH_PARENT,
                                    WindowManager.LayoutParams.MATCH_PARENT,
                                    WindowManager.LayoutParams.TYPE_PHONE,
                                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                    PixelFormat.TRANSLUCENT);
//                            params.gravity = Gravity.TOP | Gravity.LEFT;
//                            params.x = 0;
//                            params.y = 0;
                            lockedScreen = inflater.inflate(R.layout.locked_screen, null);
                            lockedScreen.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
                            lockedScreen.setOnSystemUiVisibilityChangeListener(listener ->
                                lockedScreen.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE)
                            );

                            Button buttonOne = lockedScreen.findViewById(R.id.buttonOne);
                            Button buttonTwo = lockedScreen.findViewById(R.id.buttonTwo);
                            Button buttonThree = lockedScreen.findViewById(R.id.buttonThree);
                            Button buttonFour = lockedScreen.findViewById(R.id.buttonFour);

                            buttonOne.setOnTouchListener((v1, e1) ->
                            {
                                switch (event.getActionMasked())
                                {
                                    case (MotionEvent.ACTION_DOWN):
                                        isButtonOneHeld = true;
                                        break;
                                    case (MotionEvent.ACTION_UP):
                                        isButtonOneHeld = false;
                                        v1.performClick();
                                        break;
                                        default:
                                            return false;
                                }
                                return true;
                            });

                            dialog.dismiss();
                            windowManager.removeView(viewInWindow);
                            windowManager.addView(lockedScreen, params2);

                        });

                        optionsMenu.findViewById(R.id.options).findViewById(R.id.closeAppButton).setOnClickListener(v ->
                        {
                            stopService(new Intent(getApplication(), MainMenuService.class));
                            dialog.dismiss();
                        });
                        // need to check based on api version
                        int overlayType = Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1
                                ? WindowManager.LayoutParams.TYPE_PHONE
                                : WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                        dialog.getWindow().setType(overlayType);
                        dialog.show();
                    }
                }

                default:
                    return false;
            }
            return true;
        });
    }

    private View setUpOptionsMenu(int viewAsInt)
    {
        View view = inflater.inflate(viewAsInt, null);
        view.findViewById(R.id.options).findViewById(R.id.closeAppButton).setOnClickListener(v ->
                {
                    stopSelf();
                });
        return view;
    }
    //endregion

    // region Overriden Methods
    @Override
    public void onDestroy()
    {
        if (viewInWindow != null)
        {
            windowManager.removeView(viewInWindow);
        }
        super.onDestroy();
    }
    // endregion
}
