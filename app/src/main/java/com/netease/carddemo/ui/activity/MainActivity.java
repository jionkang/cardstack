package com.netease.carddemo.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

import com.netease.carddemo.R;
import com.netease.carddemo.ui.fragment.OriginFragment;
import com.netease.carddemo.ui.fragment.PackageFragment;

/**
 * Created by xiejiantao on 2017/3/30.
 */

public class MainActivity extends AppCompatActivity {

    private View mChoiceLayout;
    private PackageFragment mPackageFragment;
    private OriginFragment mOriginFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mChoiceLayout = findViewById(R.id.rl_choice);
        mPackageFragment = new PackageFragment();
        mOriginFragment = new OriginFragment();
        castToPackageFragment();
        setEvent();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mChoiceLayout.getVisibility() == View.VISIBLE) {
            mChoiceLayout.setVisibility(View.GONE);
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void setEvent() {
        findViewById(R.id.ic_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickMore();
            }
        });
        findViewById(R.id.tv_origin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChoiceLayout.setVisibility(View.GONE);
                castToOriginFragment();
            }
        });
        findViewById(R.id.tv_package).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChoiceLayout.setVisibility(View.GONE);
                castToPackageFragment();
            }
        });

    }

    private void clickMore() {
        if (mChoiceLayout.getVisibility() == View.VISIBLE) {
            mChoiceLayout.setVisibility(View.GONE);
        } else {
            mChoiceLayout.setVisibility(View.VISIBLE);
        }
    }

    private void castToPackageFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mPackageFragment).commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    private void castToOriginFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mOriginFragment).commit();
        getSupportFragmentManager().executePendingTransactions();
    }

}
