package com.netease.carddemo.model.db;

import com.netease.carddemo.R;
import com.netease.carddemo.model.bean.UserBean;

import java.util.ArrayList;

/**
 * Created by xiejiantao on 2017/3/30.
 */

public class UserInfoHelper {

    public void addData(ArrayList<UserBean> list) {
        UserBean beana = new UserBean();
        beana.imageResId = R.drawable.a;
        beana.name = "赵敏";
        beana.age = "18";
        beana.distance = "15 km";
        list.add(beana);
        UserBean beanb = new UserBean();
        beanb.imageResId = R.drawable.b;
        beanb.name = "钱通四";
        beanb.age = "20";
        beanb.distance = "8 km";
        list.add(beanb);
        UserBean beanc = new UserBean();
        beanc.imageResId = R.drawable.c;
        beanc.name = "孙万年";
        beanc.age = "58";
        beanc.distance = "8 km";
        list.add(beanc);
        UserBean beand = new UserBean();
        beand.imageResId = R.drawable.d;
        beand.name = "李秋水";
        beand.age = "88";
        beand.distance = "1 km";
        list.add(beand);
        UserBean beane = new UserBean();
        beane.imageResId = R.drawable.e;
        beane.name = "周伯通";
        beane.age = "8";
        beane.distance = "11 km";
        list.add(beane);
    }
}
