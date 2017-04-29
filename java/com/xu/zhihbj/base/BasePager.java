package com.xu.zhihbj.base;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.xu.zhihbj.MainActivity;
import com.xu.zhihbj.R;

/**
 * 五个标签页的基类
 * 
 * @author Kevin
 * @date 2015-10-18
 */
public class BasePager {

	public Activity mActivity;

	public TextView tvTitle;
	public ImageButton btnMenu;
	// 空的帧布局对象, 要动态添加布局
	public FrameLayout flContent;

	public ImageButton btnPhoto;//组图切换按钮

	//在构造方法里面调用，new之后马上初始化布局，把这个view保留，页面的根部局文件
	// 当前页面的布局对象
	public View mRootView;
	//需要上下文的对象，通过构造方法传过来
	public BasePager(Activity activity) {
		mActivity = activity;
		//在构造方法里面调用，new之后马上初始化布局
		mRootView = initView();
	}

	// 初始化布局
	public View initView() {
		//需要上下文的对象，通过构造方法传过来
		View view = View.inflate(mActivity, R.layout.base_pager, null);
		tvTitle = (TextView) view.findViewById(R.id.tv_title);
		btnMenu = (ImageButton) view.findViewById(R.id.btn_menu);
		btnPhoto = (ImageButton) view.findViewById(R.id.btn_photo);//组图切换按钮
		flContent = (FrameLayout) view.findViewById(R.id.fl_content);

		//给左上角的按钮设置点击事件
		btnMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toggle();
			}
		});

		return view;
	}

	/**
	 * 打开或者关闭侧边栏
	 * 开开侧边栏是侧边栏的方法，先拿到侧边栏的对象
	 */
	protected void toggle() {
		MainActivity mainUI = (MainActivity) mActivity;
		SlidingMenu slidingMenu = mainUI.getSlidingMenu();
		// 如果当前状态是开, 调用后就关; 反之亦然
		slidingMenu.toggle();
	}

	// 初始化数据
	public void initData() {

	}
}
