package com.datrueonejay.littlefingers.Activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.datrueonejay.littlefingers.R;
import com.datrueonejay.littlefingers.Services.MainMenuService;
import com.datrueonejay.littlefingers.ViewModels.PermissionCheckViewModel;

public class PermissionCheckActivity extends AppCompatActivity {

    private final static int OVERLAY_REQUEST_CODE = 0;
    private Context _context;
    private boolean canDrawOverlay;
    private PermissionCheckViewModel permissionCheckViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        PermissionCheckViewModel model = ViewModelProviders.of(this).get(PermissionCheckViewModel.class);
        // if below android M, permission is granted at install time
        canDrawOverlay = Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1 || Settings.canDrawOverlays(this._context);
        this._context = this;
        // ask permission to draw over apps if we do not have
        if (!canDrawOverlay)
        {
            setContentView(R.layout.activity_permission_check);
        }

        createDrawOverPermission();
        this.startService(new Intent(this, MainMenuService.class));
        finish();
    }


    private void createDrawOverPermission()
    {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && !Settings.canDrawOverlays(this._context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));

            startActivityForResult(intent, OVERLAY_REQUEST_CODE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case (OVERLAY_REQUEST_CODE):
                if (resultCode != RESULT_OK)
                {
                    finish();
                }
        }
    }
}
