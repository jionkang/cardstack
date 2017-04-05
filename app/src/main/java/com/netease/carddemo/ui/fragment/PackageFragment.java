package com.netease.carddemo.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.carddemo.R;
import com.netease.carddemo.model.bean.UserBean;
import com.netease.carddemo.presenter.UserPresenter;
import com.netease.carddemo.presenter.contract.UserContract;
import com.netease.carddemo.ui.activity.AvatarActivity;
import com.netease.carddemo.util.ImageBlur;
import com.netease.cardstack.stack.Layer3DHelper;
import com.netease.cardstack.stack.view.Layer3DLayout;

import java.util.ArrayList;

/**
 * Created by xiejiantao on 2017/3/30.
 */

public class PackageFragment extends Fragment implements UserContract.View {

    private View mPhotoStackView;
    private ImageView mAvatarBg;

    private Layer3DHelper mLayer3dHelper;

    private int mHalfScreenWidth;

    private ArrayList<UserBean> mUsers;

    private UserPresenter mUserPresenter;

    private int mCcurrentId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager wm = getActivity().getWindowManager();
        mHalfScreenWidth = wm.getDefaultDisplay().getWidth() / 2;
        mUserPresenter = new UserPresenter(true, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_package, null);
        initView(root);
        mUsers = new ArrayList<>();
        mUserPresenter.addData(mUsers);
        return root;
    }

    private void initView(View root) {
        mPhotoStackView = root.findViewById(R.id.feature_container);
        mAvatarBg = (ImageView) root.findViewById(R.id.bg_avatar);

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bitmap bitmap = (Bitmap) msg.obj;
            if (mCcurrentId == msg.arg1) {
                mAvatarBg.setImageBitmap(bitmap);
            }

        }
    };

    private void setBlurBackgroud() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(), mUsers.get(0).imageResId);
                if (bitmap == null) {
                    return;
                }
                bitmap = ImageBlur.fastblurJava(bitmap, 25, -1, 1);
                Message msg = Message.obtain();
                msg.obj = bitmap;
                msg.arg1 = mUsers.get(0).imageResId;
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    @Override
    public void initContent() {
        mLayer3dHelper = new Layer3DHelper(mPhotoStackView, new Layer3DHelper.FlingListener() {
            @Override
            public void updateView(View view, final int index, int position) {
                try {
                    final View child = ((Layer3DLayout) view).getChildAt(0).findViewById(com.netease.cardstack.R.id.card);
                    ((ImageView) child.findViewById(R.id.image)).setImageResource(mUsers.get(index).imageResId);
                    ((TextView) child.findViewById(R.id.tv_name)).setText(mUsers.get(index).name);
                    ((TextView) child.findViewById(R.id.tv_age)).setText(mUsers.get(index).age);
                    ((TextView) child.findViewById(R.id.tv_distance)).setText(mUsers.get(index).distance);

                    if (index == 0) {
                        child.findViewById(R.id.left).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mLayer3dHelper.showNext(true);
                                Toast.makeText(getActivity(), "喜欢成功", Toast.LENGTH_SHORT).show();
                            }
                        });
                        child.findViewById(R.id.right).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mLayer3dHelper.showNext(false);
                                Toast.makeText(getActivity(), "不喜欢已丢弃", Toast.LENGTH_SHORT).show();
                            }
                        });
                        mCcurrentId = mUsers.get(0).imageResId;
                        setBlurBackgroud();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void setOnClickListener(int index) {

                Intent intent = new Intent(getActivity(), AvatarActivity.class);
                intent.putExtra("img", mUsers.get(index).imageResId);
                startActivity(intent);
            }

            @Override
            public void removeCurContact(int index) {
                mUsers.remove(0);
                if (mUsers.size() == 0) {
                    Toast.makeText(getActivity(), "再来一组", Toast.LENGTH_SHORT).show();
                    mUserPresenter.addData(mUsers);
                }
            }

            @Override
            public int flingSize() {
                return mUsers.size();
            }

            @Override
            public void onFinishFling(int index) {
                if (index != 0) {
                    removeCurContact(0);
                }
            }

            @Override
            public void onFinshUp(MotionEvent event) {
                if (event.getX() < mHalfScreenWidth) {
                    Toast.makeText(getActivity(), "喜欢成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "不喜欢已丢弃", Toast.LENGTH_SHORT).show();
                }
            }
        }, true);
    }
}
