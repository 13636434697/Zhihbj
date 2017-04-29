package com.xu.zhihbj.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment {

	public Activity mActivity;//这个activity就是MainActivity

	// Fragment创建
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = getActivity();// 获取当前fragment所依赖的activity
	}

	// 初始化fragment的布局
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		//让子类去实现的方法，父类在这里调用就可以了
		View view = initView();
		return view;
	}

	// fragment所依赖的activity的onCreate方法执行结束
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//让子类去实现的方法，父类在这里调用就可以了
		// 初始化数据
		initData();
	}

	//因为不知道实现什么，所有让子类来实现，而且必须要子类实现，就变成抽象方法
	// 初始化布局, 必须由子类实现
	public abstract View initView();
	//因为不知道实现什么，所有让子类来实现，而且必须要子类实现，就变成抽象方法
	// 初始化数据, 必须由子类实现
	public abstract void initData();
}
