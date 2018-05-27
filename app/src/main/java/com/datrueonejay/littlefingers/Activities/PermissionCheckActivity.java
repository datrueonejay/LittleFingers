package com.datrueonejay.littlefingers.Activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.datrueonejay.littlefingers.R;
import com.datrueonejay.littlefingers.Services.MainMenuService;

public class PermissionCheckActivity extends AppCompatActivity {

    //region Properties
    private final static int OVERLAY_REQUEST_CODE = 0;
    private Context _context;
    //endregion

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this._context = this.getApplicationContext();

        // if below android M, permission is granted at install time, otherwise check if permission has been given
        boolean canDrawOverlay = Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1 || Settings.canDrawOverlays(this._context);

        // ask permission to draw over apps if we do not have
        if (!canDrawOverlay)
        {
            setContentView(R.layout.activity_permission_check);
            TextView descriptionTextView = findViewById(R.id.permissionDescription);
            Button permissionButton = findViewById(R.id.continueButton);

            // if android o set up layout so they must restart app after giving permission
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O)
            {
                // set text to android o text
                descriptionTextView.setText(R.string.permission_description_false_android_o);
                Button restartAppButton = findViewById(R.id.restartButton);
                restartAppButton.setVisibility(View.VISIBLE);
                restartAppButton.setOnTouchListener((view, event) ->
                {
                    if (Settings.canDrawOverlays(this._context))
                    {
                        finishAndRemoveTask();
                        startMainMenuService();
                    }
                    return false;
                });
            }
            // otherwise set up normal text
            else
            {
                descriptionTextView.setText(R.string.permission_description_false);
            }
            permissionButton.setOnClickListener(listener -> {
                promptDrawOverPermission();
            });

        }
        // start the service if we already have permission
        else
        {
            this.startMainMenuService();
            finishAndRemoveTask();
        }
    }
    //region Private Methods
    @TargetApi(23)
    private void promptDrawOverPermission()
    {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));

        startActivityForResult(intent, OVERLAY_REQUEST_CODE);

    }

    public void startMainMenuService()
    {
        Intent mainMenuService = new Intent(this._context, MainMenuService.class);
        this._context.startService(mainMenuService);
    }
    //endregion


    //region Overriden methods
    @Override
    @TargetApi(23)
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case (OVERLAY_REQUEST_CODE):
                if (Settings.canDrawOverlays(this))
                {
                    this.startMainMenuService();
                    finishAndRemoveTask();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
    //endregion
}
