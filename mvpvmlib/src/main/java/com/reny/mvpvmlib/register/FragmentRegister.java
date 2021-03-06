package com.reny.mvpvmlib.register;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.reny.mvpvmlib.base.RBasePresenter;

/**
 * Created by reny on 2017/6/5.
 */

public class FragmentRegister {

    private ViewDataBinding binding;
    private RBasePresenter[] presenters;

    public FragmentRegister initBinding(LayoutInflater inflater, ViewGroup container, @LayoutRes int layoutId){
        if(null == binding){
            binding = DataBindingUtil.inflate(inflater, layoutId, container, false);
        }
        return this;
    }

    public ViewDataBinding getBinding() {
        return binding;
    }

    public FragmentRegister register(RBasePresenter... presenters){
        this.presenters = presenters;
        if(null != this.presenters) {
            for (RBasePresenter presenter : this.presenters) {
                if (null != presenter) presenter.onCreate();
            }
        }
        return this;
    }

    public void unRegister(){
        if(null != this.presenters) {
            for (RBasePresenter presenter : this.presenters) {
                if (null != presenter) presenter.onDestroy();
            }
        }
        binding = null;
    }

}
