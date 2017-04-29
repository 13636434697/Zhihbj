package com.xu.zhihbj.base.impl;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.xu.zhihbj.MainActivity;
import com.xu.zhihbj.base.BaseMenuDetailPager;
import com.xu.zhihbj.base.BasePager;
import com.xu.zhihbj.base.impl.menu.InteractMenuDetailPager;
import com.xu.zhihbj.base.impl.menu.NewsMenuDetailPager;
import com.xu.zhihbj.base.impl.menu.PhotosMenuDetailPager;
import com.xu.zhihbj.base.impl.menu.TopicMenuDetailPager;
import com.xu.zhihbj.domain.NewsMenu;
import com.xu.zhihbj.fragment.LeftMenuFragment;
import com.xu.zhihbj.global.GlobalConstants;
import com.xu.zhihbj.utils.CacheUtils;

/**
 * 新闻中心
 * 
 * @author Kevin
 * @date 2015-10-18
 */
public class NewsCenterPager extends BasePager {

	// 初始化4个菜单详情页，需要有一个集合来维护起来
	private ArrayList<BaseMenuDetailPager> mMenuDetailPagers;// 菜单详情页集合
	private NewsMenu mNewsData;// 分类信息网络数据

	public NewsCenterPager(Activity activity) {
		super(activity);
	}

	@Override
	public void initData() {
		System.out.println("新闻中心初始化啦...");

		// // 要给帧布局填充布局对象
		// TextView view = new TextView(mActivity);
		// view.setText("新闻中心");
		// view.setTextColor(Color.RED);
		// view.setTextSize(22);
		// view.setGravity(Gravity.CENTER);
		//
		// flContent.addView(view);

		// 修改页面标题
		tvTitle.setText("新闻");

		// 显示菜单按钮
		btnMenu.setVisibility(View.VISIBLE);

		// 先判断有没有缓存,如果有的话,就加载缓存
		String cache = CacheUtils.getCache(GlobalConstants.CATEGORY_URL,mActivity);
		if (!TextUtils.isEmpty(cache)) {
			System.out.println("发现缓存啦...");
			//不用服务器请求了，直接解析数据
			processData(cache);
		}

		//不管什么情况下都请求一下服务器
		// 开源框架: XUtils
		getDataFromServer();
	}

	/**
	 * 从服务器获取数据 需要权限:<uses-permission android:name="android.permission.INTERNET"
	 * />
	 */
	private void getDataFromServer() {
		HttpUtils utils = new HttpUtils();
		//发送一个请求，参数1:HttpMethod.GET这个是一个类（枚举）,参数2：是一个链接，参数3，是一个回调（里面传的是一个泛型，请求一段字符串）
		utils.send(HttpMethod.GET, GlobalConstants.CATEGORY_URL,new RequestCallBack<String>() {

					// 请求成功
					@Override
					public void onSuccess(ResponseInfo<String> responseInfo) {

						String result = responseInfo.result;// 获取服务器返回结果
						System.out.println("服务器返回结果:" + result);

						//解析json数据
						processData(result);

						// 写缓存
						CacheUtils.setCache(GlobalConstants.CATEGORY_URL,result, mActivity);
					}
					// 请求失败
					@Override
					public void onFailure(HttpException error, String msg) {
						error.printStackTrace();
						Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
					}
				});
	}

	/**
	 * 解析数据
	 */
	protected void processData(String json) {
		// Gson: Google Json
		Gson gson = new Gson();
		//把需要解析的json传进来，需要有一个类，解析数据的时候要javabing这个对象来存储数据，这个就是javabing保存服务器数据
		//返回的对象，把所有的字段都填充好了
		mNewsData = gson.fromJson(json, NewsMenu.class);
		System.out.println("解析结果:" + mNewsData);

		//新闻中心拿到mainactivity的对象，新闻中心就有一个mainactivity
		//先拿到mainactivity
		MainActivity mainUI = (MainActivity) mActivity;
		// 在通过mainactivity获取侧边栏对象
		LeftMenuFragment fragment = mainUI.getLeftMenuFragment();

		// 给侧边栏设置数据
		//这里只需要data里面的title字段
		fragment.setMenuData(mNewsData.data);

		// 初始化4个菜单详情页，需要有一个集合来维护起来
		mMenuDetailPagers = new ArrayList<BaseMenuDetailPager>();
		// 页签的数量是由服务器数据决定的，所以要循环数据，根据网络返回children数量
		//问题就是在新闻中心请求的数据，要传到NewsMenuDetailPager里去
		mMenuDetailPagers.add(new NewsMenuDetailPager(mActivity, mNewsData.data.get(0).children));
		mMenuDetailPagers.add(new TopicMenuDetailPager(mActivity));
		//给另一个页面的按钮设置点击事件，这里把这个按钮的引用传过去，通过构造方法
		mMenuDetailPagers.add(new PhotosMenuDetailPager(mActivity, btnPhoto));
		mMenuDetailPagers.add(new InteractMenuDetailPager(mActivity));

		//在新闻页面的时候默认要显示数据，所有将新闻菜单详情页设置为默认页面
		setCurrentDetailPager(0);
	}

	//暴漏了一个方法
	//设置listView侧边栏的点击详情页面数据
	// 设置菜单详情页
	public void setCurrentDetailPager(int position) {
		// 重新给frameLayout添加内容，添加四个listView侧边栏的点击详情页面数据的对象
		//四个页面，在listView里面选择那一个item就展示哪一个页面
		BaseMenuDetailPager pager = mMenuDetailPagers.get(position);// 获取当前应该显示的页面

		//页面里面有个rootview，而这个rootView刚刚好是根部局
		View view = pager.mRootView;// 当前页面的布局

		//因为是幀布局，添加会一直叠加，所有要添加之前要清除之前的旧的布局
		// 清除之前旧的布局
		flContent.removeAllViews();

		//把布局添加给fragmentlayout
		flContent.addView(view);// 给帧布局添加布局

		//这里需要调用一下父类的方法，初始化页面数据
		pager.initData();

		// 在点击listview的item的时候也要更新标题
		//数据在data里面
		tvTitle.setText(mNewsData.data.get(position).title);


		// 如果是组图页面, 需要显示切换按钮
		//怎么知道是组图页面，pager当前的页面（是基类表示的），通过instanceof到底是哪个子类
		if (pager instanceof PhotosMenuDetailPager) {
			// 显示切换按钮
			//这个按钮是在basepager基类里面找出来的
			btnPhoto.setVisibility(View.VISIBLE);
		} else {
			// 隐藏切换按钮
			//这个按钮是在basepager基类里面找出来的
			btnPhoto.setVisibility(View.GONE);
		}
	}

}
