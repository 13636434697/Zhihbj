package com.xu.zhihbj.fragment;

import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xu.zhihbj.MainActivity;
import com.xu.zhihbj.R;
import com.xu.zhihbj.base.impl.NewsCenterPager;
import com.xu.zhihbj.domain.NewsMenu.NewsMenuData;

/**
 * 侧边栏fragment
 *
 * 在新闻中心已经拿到了网络的数据，然后给侧边栏填充新闻中心的数据，
 *
 * 数据传递：把新闻中心的数据传递给侧边栏，（通过新闻中心，拿到侧边栏对象的引用，然后在调用侧边栏的方法，把数据放到侧边栏）
 * 新闻中心是属于contentfragment的，contentfragment又属于mainactivity的，（通过新闻中心拿到mainactivity的对象）
 * LeftMenuFragment侧边栏的对象也属于mainactivity的，在通过mainactivity拿到LeftMenuFragment侧边栏的对象，这样就能拿到了
 *
 * 新闻中心拿到mainactivity的对象，新闻中心就有一个mainactivity
 *
 */
public class LeftMenuFragment extends BaseFragment {

	@ViewInject(R.id.lv_list)
	private ListView lvList;

	//给listView填充数据，需要用到
	private ArrayList<NewsMenuData> mNewsMenuData;// 侧边栏网络数据对象

	//listView列表只能是一个可用，其他不可用的情况（只能一个被选中）
	private int mCurrentPos;// 当前被选中的item的位置

	private LeftMenuAdapter mAdapter;

	@Override
	public View initView() {
		View view = View.inflate(mActivity, R.layout.fragment_left_menu, null);
		// lvList = (ListView) view.findViewById(R.id.lv_list);
		//这里用了xutil的方法
		ViewUtils.inject(this, view);// 注入view和事件
		return view;
	}

	@Override
	public void initData() {
	}

	// 给侧边栏设置数据
	public void setMenuData(ArrayList<NewsMenuData> data) {

		//在listView切换item的时候，在切换主页标签，然后在切回新闻中心的时候，发现viewpager是第一个页面，
		// 因为重写初始化了，但是listview还是第二个item，这样就不同步了，重新切回新闻中心的时候，当前位置应该归零
		//切回新闻中心的时候，会调用processdata重新解析数据，把这些数据重新传给侧边栏setMenuData，就在这里位置应该归零
		mCurrentPos = 0;//当前选中的位置归零

		//给listView填充数据
		// 更新页面
		mNewsMenuData = data;

		//有数据了之后，在设置adapter
		mAdapter = new LeftMenuAdapter();
		lvList.setAdapter(mAdapter);

		//响应listView的点击事件，来进行切换item
		lvList.setOnItemClickListener(new OnItemClickListener() {

			//设置item的点击事件
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				// 更新当前被选中的位置
				mCurrentPos = position;
				// 刷新listview
				mAdapter.notifyDataSetChanged();

				//在点击好listView的item之后，侧边栏会收起来
				toggle();

				// 侧边栏里面的listview被点击之后, 要修改新闻中心的FrameLayout中的内容
				setCurrentDetailPager(position);
			}
		});
	}

	/**
	 *
	 * 设置listView侧边栏的点击详情页面数据
	 *
	 * 设置当前菜单详情页
	 * 
	 * 需要传一个位置，修改新闻中心的fragment
	 *
	 * 要通过侧边栏，获取新闻中心
	 */
	protected void setCurrentDetailPager(int position) {
		// 获取新闻中心的对象
		MainActivity mainUI = (MainActivity) mActivity;
		// 通过mainactivity获取ContentFragment
		ContentFragment fragment = mainUI.getContentFragment();
		// 通过fragment获取NewsCenterPager
		NewsCenterPager newsCenterPager = fragment.getNewsCenterPager();
		// 修改新闻中心的FrameLayout的布局，剩下的就交给新闻中心来做了
		newsCenterPager.setCurrentDetailPager(position);
	}

	/**
	 * 打开或者关闭侧边栏
	 *
	 * 收起侧边栏是侧边栏的方法，先拿到侧边栏的对象
	 */
	protected void toggle() {
		MainActivity mainUI = (MainActivity) mActivity;
		SlidingMenu slidingMenu = mainUI.getSlidingMenu();
		// 如果当前状态是开, 调用后就关; 反之亦然
		slidingMenu.toggle();
	}

	//给listView填充数据
	class LeftMenuAdapter extends BaseAdapter {

		//数据的大小
		@Override
		public int getCount() {
			return mNewsMenuData.size();
		}

		//获取数据的位置，返回的是NewsMenuData
		@Override
		public NewsMenuData getItem(int position) {
			return mNewsMenuData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		//listView设置adapter的时候，会走一遍这个方法，把所有item的布局给初始化一下
		//这里listView就4个，就不用重用convertView了
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//填充布局
			View view = View.inflate(mActivity, R.layout.list_item_left_menu,null);
			//找出ID
			TextView tvMenu = (TextView) view.findViewById(R.id.tv_menu);

			//拿到item，文字的对象
			NewsMenuData item = getItem(position);
			//设置文字
			tvMenu.setText(item.title);

			//listView列表只能是一个可用，其他不可用的情况（只能一个被选中）
			//listView设置adapter的时候，会走一遍这个方法，把所有item的布局给初始化一下
			if (position == mCurrentPos) {
				// 被选中
				tvMenu.setEnabled(true);// 文字变为红色
			} else {
				// 未选中
				tvMenu.setEnabled(false);// 文字变为白色
			}

			return view;
		}

	}

}
