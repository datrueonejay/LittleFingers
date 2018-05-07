package com.datrueonejay.littlefingers.ViewModels;

import android.arch.lifecycle.ViewModel;
import android.content.Context;

abstract class BaseViewModel extends ViewModel {
    Context context;

    BaseViewModel(Context context)
    {
        this.context = context;
    }
}
