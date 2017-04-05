package com.netease.cardstack;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.netease.cardstack.stack.view.Layer3DLayout;
import com.netease.cardstack.stack.view.LayerGestureView;
import com.netease.cardstack.stack.view.MaskView;

/**
 * Created by xiejiantao on 2016/1/14.
 */
public class StackLayout extends RelativeLayout {

    private final String namespace = "http://schemas.android.com/apk/res-auto";

    public StackLayout(Context context) {
        super(context);
    }

    public StackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    @SuppressLint("NewApi")
    public StackLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs);
    }


    private void initView(Context context, AttributeSet attrs) {
        String contentLayout = attrs.getAttributeValue(namespace, "layoutId");
        int contentLayoutId = context.getResources().getIdentifier(contentLayout, "layout",
                context.getPackageName());
        generateCardLayout(context, contentLayoutId, R.id.fling_view_back);
        generateCardLayout(context, contentLayoutId, R.id.fling_view_center);
        MaskView maskView = new MaskView(context);
        maskView.setId(R.id.mask_view);
        maskView.setBackgroundColor(0x000);
        LayoutParams pramasmask = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(maskView, pramasmask);
        generateCardLayout(context, contentLayoutId, R.id.fling_view_front);
        LayerGestureView gestureView = new LayerGestureView(context);
        gestureView.setId(R.id.slide_view);
        gestureView.setBackgroundColor(0x000);
        LayoutParams pramasGesture = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(gestureView, pramasGesture);
    }

    private void generateCardLayout(Context context, int contentLayoutId, int layoutId) {
        Layer3DLayout layoutBack = new Layer3DLayout(context);
        layoutBack.setId(layoutId);
        layoutBack.setBackgroundColor(0x000);
        LinearLayout.LayoutParams pramasContent = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        View view = View.inflate(context, contentLayoutId, null);
        view.setId(R.id.card);
        layoutBack.addView(view, pramasContent);

        LinearLayout.LayoutParams pramasBack = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//        pramasBack.ad
        addView(layoutBack, pramasBack);
    }
}
