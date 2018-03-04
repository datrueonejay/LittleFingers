package com.datrueonejay.littlefingers.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.datrueonejay.littlefingers.R;
import com.datrueonejay.littlefingers.ViewModels.MainMenuViewModel;

public class MainMenuService extends Service {

    private MainMenuViewModel mainMenu;

    public MainMenuService() {
    }

   @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       // creates the menu button
       mainMenu = new MainMenuViewModel(this);
       mainMenu.displayMenu(R.layout.menu_button);
       return START_STICKY;
    }

     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
}
