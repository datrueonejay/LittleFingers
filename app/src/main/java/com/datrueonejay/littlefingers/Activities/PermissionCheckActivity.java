package com.datrueonejay.littlefingers.Activities;

import android.annotation.TargetApi;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.datrueonejay.littlefingers.R;
import com.datrueonejay.littlefingers.Services.MainMenuService;
import com.datrueonejay.littlefingers.ViewModels.PermissionCheckViewModel;

public class PermissionCheckActivity extends AppCompatActivity {

    private final static int OVERLAY_REQUEST_CODE = 0;
    private Context _context;
    private PermissionCheckViewModel permissionCheckViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this._context = this;

        permissionCheckViewModel = ViewModelProviders.of(this).get(PermissionCheckViewModel.class);

        // if below android M, permission is granted at install time, otherwise check if permission has been given
        boolean canDrawOverlay = Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1 || Settings.canDrawOverlays(this._context);
        permissionCheckViewModel.getIsPermissionEnabled().setValue(canDrawOverlay);

        // ask permission to draw over apps if we do not have
        if (!permissionCheckViewModel.getIsPermissionEnabled().getValue())
        {
            setContentView(R.layout.activity_permission_check);
            TextView descriptionTextView = findViewById(R.id.permissionDescription);
            Button continueButton = findViewById(R.id.continueButton);

            permissionCheckViewModel.getIsPermissionEnabled().observe(this, isPermissionEnabled -> {
                boolean hasPermission = permissionCheckViewModel.getIsPermissionEnabled().getValue();

                String descriptionText = hasPermission
                        ? getResources().getString(R.string.permission_description_true)
                        : getResources().getString(R.string.permission_description_false);
                descriptionTextView.setText(descriptionText);

                int buttonColor = hasPermission
                        ? getResources().getColor(R.color.green)
                        : getResources().getColor(R.color.red);

                String buttonText = hasPermission
                        ? getResources().getString(R.string.yes_permission_button_description)
                        : getResources().getString(R.string.no_permission_button_description);
                continueButton.setBackgroundColor(buttonColor);
                continueButton.setText(buttonText);
            });
            // set to false since permission is not given, to update initial UI
            permissionCheckViewModel.getIsPermissionEnabled().setValue(false);

            continueButton.setOnClickListener(button -> {
                if (permissionCheckViewModel.getIsPermissionEnabled().getValue())
                {
                    this.startService(new Intent(this._context, MainMenuService.class));
                    finish();
                }
                else
                {
                    promptDrawOverPermission();
                }
            });
        }
        // start the service if we already have permission
        else
        {
            this.startService(new Intent(this._context, MainMenuService.class));
            finish();
        }


//        createDrawOverPermission();
    }


    @TargetApi(23)
    private void promptDrawOverPermission()
    {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));

        startActivityForResult(intent, OVERLAY_REQUEST_CODE);

    }


    @Override
    @TargetApi(23)
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case (OVERLAY_REQUEST_CODE):
                if (Settings.canDrawOverlays(this._context))
                {
                    permissionCheckViewModel.getIsPermissionEnabled().setValue(true);
                }
        }
    }
}
