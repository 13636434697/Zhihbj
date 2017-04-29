package com.xu.zhihbj.base.impl.menu;

import android.app.Activity;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.viewpagerindicator.TabPageIndicator;
import com.xu.zhihbj.MainActivity;
import com.xu.zhihbj.domain.NewsMenu.NewsTabData;
import com.xu.zhihbj.R;
import com.xu.zhihbj.base.BaseMenuDetailPager;

import java.util.ArrayList;


/**
 * 菜单详情页-新闻
 *
 * ViewPagerIndicator使用流程: 1.引入库 2.解决support-v4冲突(让两个版本一致) 3.从例子程序中拷贝布局文件
 * 4.从例子程序中拷贝相关代码(指示器和viewpager绑定; 重写getPageTitle返回标题) 5.在清单文件中增加样式 6.背景修改为白色
 * 7.修改样式-背景样式&文字样式
 *
 * 页签指示器只有文字没有其他样式，是因为需要在样式里面需要设置主题,这个主题是类库定义的，在mainactivity里面，然后在xml里面设置白色的背景
 *
 * 标签页的触摸事件，会被其他事件拦截点，但是具体不知道是哪个，所以要在标签的类库里面重写一个方法dispatchTouchEvent
 */
public class NewsMenuDetailPager extends BaseMenuDetailPager implements ViewPager.OnPageChangeListener {

	@ViewInject(R.id.vp_news_menu_detail)
	private ViewPager mViewPager;

	//声明页签指示器
	@ViewInject(R.id.indicator)
	private TabPageIndicator mIndicator;

	// 页签的数量是由服务器数据决定的，所以要循环数据，根据网络返回children数量
	//问题就是在新闻中心请求的数据，要传到这里来
	private ArrayList<NewsTabData> mTabData;// 页签网络数据

	private ArrayList<TabDetailPager> mPagers;// 页签页面集合

	public NewsMenuDetailPager(Activity activity,ArrayList<NewsTabData> children) {
		super(activity);
		mTabData = children;
	}

	//初始化布局，需要一个adapter来填充viewpager页面
	@Override
	public View initView() {
		View view = View.inflate(mActivity, R.layout.pager_news_menu_detail,null);
		ViewUtils.inject(this, view);
		return view;
	}

	//在初始化数据里面，初始化里面的布局TabDetailPager页签
	@Override
	public void initData() {

		//需要用集合来装这个类的页面
		mPagers = new ArrayList<TabDetailPager>();
		// 页签的数量是由服务器数据决定的，所以要循环数据，根据网络返回children数量
		//问题就是在新闻中心请求的数据，要传到这里来
		for (int i = 0; i < mTabData.size(); i++) {
			//在new的时候传对应的小对象
			TabDetailPager pager = new TabDetailPager(mActivity,mTabData.get(i));
			//将页面添加到集合
			mPagers.add(pager);
		}

		//初始化好页面之后，设置adapter，这样就根据服务器返回的数量来new相应个数的TabDetailPager
		mViewPager.setAdapter(new NewsMenuDetailAdapter());

		// 将viewpager和指示器绑定在一起.注意:必须在viewpager设置完数据之后再绑定
		mIndicator.setViewPager(mViewPager);


		//在滑动的时候，标签页会被侧边栏拦截事件，但是又不能让侧边栏完全不拦截事件，只需要在第一个标签页的时候拦截，其他不拦截
		// 设置页面滑动监听
		// mViewPager.setOnPageChangeListener(this);
		// 此处必须给指示器设置页面监听,不能设置给viewpager
		mIndicator.setOnPageChangeListener(this);
	}


	//初始化布局，需要一个adapter来填充viewpager页面
	class NewsMenuDetailAdapter extends PagerAdapter {

		//获取页签指示器的标题，在这里重写一下方法，FragmentPagerAdapter也继承pageradapter都有这个方法
		@Override
		public CharSequence getPageTitle(int position) {
			//根据位置拿到对应的页签
			NewsTabData data = mTabData.get(position);
			//把页签的标题返回出去
			return data.title;
		}

		//返回页面的个数
		@Override
		public int getCount() {
			//在初始化数据里面初始化页面之后，就可以得到viewpager的数量大小
			return mPagers.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		//初始化每个页面的布局
		//页面用对象来封装，思路和5个标签页一样，用basebager这个对象来，页面都一样的，所以不用父类了，直接一个对象就可以了
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			//viewpager集合里面获取对应的页面对象
			TabDetailPager pager = mPagers.get(position);
			//在pager里面拿到根部局
			View view = pager.mRootView;
			//添加view
			container.addView(view);
			//初始化pager的数据
			pager.initData();

			return view;
		}

		//销毁页面
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

	}

	//viewpager设置滑动监听的三个方法
	@Override
	public void onPageScrolled(int position, float positionOffset,
							   int positionOffsetPixels) {

	}

	//viewpager设置滑动监听的三个方法
	//在滑动的时候，标签页会被侧边栏拦截事件，但是又不能让侧边栏完全不拦截事件，只需要在第一个标签页的时候拦截，其他不拦截
	@Override
	public void onPageSelected(int position) {
		System.out.println("当前位置:" + position);
		if (position == 0) {
			// 开启侧边栏
			setSlidingMenuEnable(true);
		} else {
			// 禁用侧边栏
			setSlidingMenuEnable(false);
		}

	}

	//viewpager设置滑动监听的三个方法
	@Override
	public void onPageScrollStateChanged(int state) {

	}

	/**
	 * 开启或禁用侧边栏
	 *
	 * @param enable
	 */
	protected void setSlidingMenuEnable(boolean enable) {
		// 获取侧边栏对象
		MainActivity mainUI = (MainActivity) mActivity;
		SlidingMenu slidingMenu = mainUI.getSlidingMenu();
		if (enable) {
			slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		} else {
			slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		}
	}

//	指示器的页签的小箭头，添加了点击事件，也可以用xutil绑定事件的写法
	@OnClick(R.id.btn_next)
	public void nextPage(View view) {
		// 跳到下个页面，可能会角标越界，但是viewpager底层事件已经处理好了
		//拿到当前的位置
		int currentItem = mViewPager.getCurrentItem();
		currentItem++;
		//设置位置
		mViewPager.setCurrentItem(currentItem);
	}

}
