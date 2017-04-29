package com.xu.zhihbj.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 头条新闻自定义viewpager
 * 
 * 需要优化，就是滑倒最后一页的时候，就切换页签的viewpager，滑倒第一页的时候，在切换标签页的viewpager
 * 这里要分几种情况，什么时候拦截或者什么时候不拦截，
 */
public class TopNewsViewPager extends ViewPager {

	private int startX;
	private int startY;

	public TopNewsViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TopNewsViewPager(Context context) {
		super(context);
	}

	/**
	 * 1. 上下滑动需要拦截
	 * 2. 向右滑动并且当前是第一个页面,需要拦截
	 * 3. 向左滑动并且当前是最后一个页面,需要拦截
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		 //上来就请求一下父控件，先不要拦截，才会有机会走下面的判断方法
		getParent().requestDisallowInterceptTouchEvent(true);

		//判断动作，是上下滑动还是左右滑动，看水平方向还是竖直方向的偏移量大
		switch (ev.getAction()) {
			//按下
		case MotionEvent.ACTION_DOWN:
			//按下的时候就记录下X和Y的坐标
			startX = (int) ev.getX();
			startY = (int) ev.getY();
			break;
			//移动
		case MotionEvent.ACTION_MOVE:
			//移动之后的X和Y的值
			int endX = (int) ev.getX();
			int endY = (int) ev.getY();

			//计算，水平和垂直方向的X和Y值
			int dx = endX - startX;
			int dy = endY - startY;
			//判断偏移量，按绝对值来判断
			if (Math.abs(dy) < Math.abs(dx)) {
				//viewpager这个方法可以拿到当前是第几个页面
				int currentItem = getCurrentItem();
				// 左右滑动
				if (dx > 0) {
					// dx > 0的话向右划
					if (currentItem == 0) {
						// 第一个页面,需要拦截
						getParent().requestDisallowInterceptTouchEvent(false);
					}
				} else {
					// 否者就是向左划
					//viewpager可以拿到和他绑定的数据适配器，里面就是方法就是可以获取
					int count = getAdapter().getCount();// item总数
					if (currentItem == count - 1) {
						// 最后一个页面,需要拦截
						getParent().requestDisallowInterceptTouchEvent(false);
					}
				}

			} else {
				// 上下滑动,需要拦截
				getParent().requestDisallowInterceptTouchEvent(false);
			}

			break;

		default:
			break;
		}

		return super.dispatchTouchEvent(ev);
	}

}
