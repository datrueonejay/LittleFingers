package com.datrueonejay.littlefingers.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

public class PermissionCheckViewModel extends ViewModel {

    private LiveData<Boolean> isPermissionEnabled;

    public LiveData<Boolean> getIsPermissionEnabled() {
        return isPermissionEnabled;
    }
}
