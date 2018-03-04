package com.datrueonejay.littlefingers.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.datrueonejay.littlefingers.Services.MainMenuService;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        this.startService(new Intent(this, MainMenuService.class));
        finish();
    }
}
