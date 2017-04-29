package com.xu.zhihbj.base;

import android.app.Activity;
import android.view.View;

/**
 * 菜单详情页基类
 * 
 * 是新闻中心侧边栏的，listview详情页面
 */
public abstract class BaseMenuDetailPager {

	public Activity mActivity;
	// 菜单详情页根布局
	public View mRootView;

	//构造方法
	public BaseMenuDetailPager(Activity activity) {
		mActivity = activity;
		mRootView = initView();
	}

	//几个view没有共性，不能抽取，让子类来实现就可以了
	// 初始化布局,必须子类实现
	public abstract View initView();

	// 初始化数据
	public void initData() {

	}

}
