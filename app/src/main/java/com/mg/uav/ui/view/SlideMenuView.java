package com.mg.uav.ui.view;

import android.app.Activity;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.mg.uav.R;

public class SlideMenuView extends SlidingMenu {



    private Activity activity;

    private int selectPosition=0;

    public SlideMenuView(Activity context) {
        super(context);
        this.activity = context;
        this.selectPosition = selectPosition;
        initMenu();
    }

    public void initMenu() {
        setMode(SlidingMenu.RIGHT);
        // 设置触摸屏幕的模式
        setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        setShadowWidthRes(R.dimen.x600);
        //menu.setShadowDrawable(R.color.colorAccent);
        // 设置滑动菜单视图的宽度
        setBehindOffsetRes(R.dimen.x530);
        // 设置渐入渐出效果的值
        // 设置渐入渐出效果的值
        //setFadeDegree(1f);
        setFadeEnabled(false);
        attachToActivity(activity, SlidingMenu.SLIDING_CONTENT);
        //为侧滑菜单设置布局
        setMenu(R.layout.activity_edit_mission);
        initViews();
//        initDatas();
    }


    private void initViews() {
    }

    public void initDatas(int selectPosition) {

    }

}