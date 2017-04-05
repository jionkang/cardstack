package com.netease.carddemo.presenter.contract;

import com.netease.carddemo.model.bean.UserBean;

import java.util.ArrayList;

/**
 * Created by xiejiantao on 2017/3/30.
 */

public interface UserContract {
    interface View {
        void initContent();
    }

    interface Presenter {
        void addData(ArrayList<UserBean> users);
    }

}
