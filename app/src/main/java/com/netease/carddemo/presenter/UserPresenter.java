package com.netease.carddemo.presenter;

import android.support.v4.app.Fragment;

import com.netease.carddemo.model.bean.UserBean;
import com.netease.carddemo.model.db.UserInfoHelper;
import com.netease.carddemo.presenter.contract.UserContract;
import com.netease.carddemo.ui.fragment.OriginFragment;
import com.netease.carddemo.ui.fragment.PackageFragment;

import java.util.ArrayList;

/**
 * Created by xiejiantao on 2017/3/30.
 */

public class UserPresenter implements UserContract.Presenter {

    private boolean mIsPackage;
    private OriginFragment mOriginFragment;
    private PackageFragment mPackageFragment;
    UserInfoHelper mHelper;

    public UserPresenter(boolean isPackage, Fragment fragment) {
        mIsPackage = isPackage;
        if (mIsPackage) {
            mPackageFragment = (PackageFragment) fragment;
        } else {
            mOriginFragment = (OriginFragment) fragment;
        }
        mHelper = new UserInfoHelper();
    }

    @Override
    public void addData(ArrayList<UserBean> users) {
        mHelper.addData(users);
        mHelper.addData(users);
        mHelper.addData(users);
        if (mIsPackage) {
            mPackageFragment.initContent();
        } else {
            mOriginFragment.initContent();
        }
    }
}
