package com.datrueonejay.littlefingers.ViewModels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class PermissionCheckViewModel extends ViewModel {

   private MutableLiveData<Boolean> isPermissionEnabled;

    public MutableLiveData<Boolean> getIsPermissionEnabled() {
        if (isPermissionEnabled == null)
        {
            isPermissionEnabled = new MutableLiveData<>();
        }
        return isPermissionEnabled;
    }
}
