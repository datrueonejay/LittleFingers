package com.datrueonejay.littlefingers.ViewModels;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class LockedScreenViewModel extends ViewModel {

    private MutableLiveData<Boolean> isButtonOneHeld;
    private MutableLiveData<Boolean> isButtonTwoHeld;
    private MutableLiveData<Boolean> isButtonThreeHeld;
    private MutableLiveData<Boolean> isButtonFourHeld;

    public MutableLiveData<Boolean> getIsButtonOneHeld() {
        if (isButtonOneHeld == null)
        {
            isButtonOneHeld = new MutableLiveData<>();
        }
        return isButtonOneHeld;
    }

    public MutableLiveData<Boolean> getIsButtonTwoHeld() {
        if (isButtonTwoHeld == null)
        {
            isButtonTwoHeld = new MutableLiveData<>();
        }
        return isButtonTwoHeld;
    }

    public MutableLiveData<Boolean> getIsButtonThreeHeld() {
        if (isButtonThreeHeld == null)
        {
            isButtonThreeHeld = new MutableLiveData<>();
        }
        return isButtonThreeHeld;
    }

    public MutableLiveData<Boolean> getIsButtonFourHeld() {
        if (isButtonFourHeld == null)
        {
            isButtonFourHeld = new MutableLiveData<>();
        }
        return isButtonFourHeld;
    }
}
