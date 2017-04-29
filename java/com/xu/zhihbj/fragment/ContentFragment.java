package com.xu.zhihbj.fragment;

import java.util.ArrayList;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.xu.zhihbj.MainActivity;
import com.xu.zhihbj.R;
import com.xu.zhihbj.base.BasePager;
import com.xu.zhihbj.base.impl.GovAffairsPager;
import com.xu.zhihbj.base.impl.HomePager;
import com.xu.zhihbj.base.impl.NewsCenterPager;
import com.xu.zhihbj.base.impl.SettingPager;
import com.xu.zhihbj.base.impl.SmartServicePager;
import com.xu.zhihbj.view.NoScrollViewPager;

/**
 * 主页面fragment
 * 
 * @author Kevin
 * @date 2015-10-18
 */
public class ContentFragment extends BaseFragment {

	private NoScrollViewPager mViewPager;
	private RadioGroup rgGroup;

	//初始化五个标签页的集合（传基类）
	private ArrayList<BasePager> mPagers;

	@Override
	public View initView() {
		View view = View.inflate(mActivity,R.layout.fragment_content, null);
		mViewPager = (NoScrollViewPager) view.findViewById(R.id.vp_content);
		rgGroup = (RadioGroup) view.findViewById(R.id.rg_group);
		return view;
	}

	@Override
	public void initData() {
		//初始化五个标签页的集合
		mPagers = new ArrayList<BasePager>();

		// 添加五个标签页
		mPagers.add(new HomePager(mActivity));
		mPagers.add(new NewsCenterPager(mActivity));
		mPagers.add(new SmartServicePager(mActivity));
		mPagers.add(new GovAffairsPager(mActivity));
		mPagers.add(new SettingPager(mActivity));

		//给viewpager填充数据
		mViewPager.setAdapter(new ContentAdapter());

		//监听radiobutton的切换事件，改变viewpager的页面位置
		// 底栏标签切换监听
		rgGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			//表示某个标签被切换到了，传当前的RadioGroup对象，当前被选中的RadioGroup的对象的id
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.rb_home:
					// 首页
					// mViewPager.setCurrentItem(0);
					mViewPager.setCurrentItem(0, false);// 参2:表示是否具有滑动动画
					break;
				case R.id.rb_news:
					// 新闻中心
					mViewPager.setCurrentItem(1, false);
					break;
				case R.id.rb_smart:
					// 智慧服务
					mViewPager.setCurrentItem(2, false);
					break;
				case R.id.rb_gov:
					// 政务
					mViewPager.setCurrentItem(3, false);
					break;
				case R.id.rb_setting:
					// 设置
					mViewPager.setCurrentItem(4, false);
					break;

				default:
					break;
				}
			}
		});

		//监听页面，在viewpager在选中的时候，在初始化数据
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			//页面被选中的时候
			@Override
			public void onPageSelected(int position) {
				//拿到当前页面的对象
				BasePager pager = mPagers.get(position);
				//在初始化数据
				pager.initData();

				//只有首页有侧边栏
				if (position == 0 || position == mPagers.size() - 1) {
					// 首页和设置页要禁用侧边栏
					setSlidingMenuEnable(false);
				} else {
					// 其他页面开启侧边栏
					setSlidingMenuEnable(true);
				}
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		// 手动加载第一页数据
		mPagers.get(0).initData();
		// 手动的要在首页禁用侧边栏
		setSlidingMenuEnable(false);
	}

	/**
	 * 开启或禁用侧边栏
	 * 
	 * @param enable
	 */
	protected void setSlidingMenuEnable(boolean enable) {
		// 获取侧边栏对象，侧边栏对象在mainactivity
		MainActivity mainUI = (MainActivity) mActivity;
		//获取侧边栏
		SlidingMenu slidingMenu = mainUI.getSlidingMenu();
		if (enable) {
			//全屏触摸
			slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		} else {
			//不能触摸
			slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		}
	}


	//给viewpager填充数据
	class ContentAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			//获取集合的大小
			return mPagers.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			//布局就在basepager里面，所以先通过mypager拿到一个对应位置的pager
			BasePager pager = mPagers.get(position);
			// 在用viewpager获取当前页面对象的布局
			View view = pager.mRootView;

			// pager.initData();// 初始化数据, viewpager会默认加载下一个页面,
			// 为了节省流量和性能,不要在此处调用初始化数据的方法

			container.addView(view);

			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

	}

	// 获取新闻中心页面,在initdata里面初始化新闻中心的几个页面，新闻中心是第二个添加的
	public NewsCenterPager getNewsCenterPager() {
		// 在initdata里面初始化新闻中心的几个页面，新闻中心是第二个添加的
		NewsCenterPager pager = (NewsCenterPager) mPagers.get(1);
		return pager;
	}

}
