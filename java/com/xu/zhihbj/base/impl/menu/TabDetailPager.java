package com.xu.zhihbj.base.impl.menu;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.viewpagerindicator.CirclePageIndicator;
import com.xu.zhihbj.NewsDetailActivity;
import com.xu.zhihbj.R;
import com.xu.zhihbj.base.BaseMenuDetailPager;
import com.xu.zhihbj.domain.NewsMenu;
import com.xu.zhihbj.domain.NewsTabBean;
import com.xu.zhihbj.global.GlobalConstants;
import com.xu.zhihbj.utils.CacheUtils;
import com.xu.zhihbj.domain.NewsTabBean.TopNews;
import com.xu.zhihbj.domain.NewsTabBean.NewsData;
import com.xu.zhihbj.utils.PrefUtils;
import com.xu.zhihbj.view.PullToRefreshListView;
import com.xu.zhihbj.view.TopNewsViewPager;

/**
 * 页签页面对象
 * 
 * 只是为了方便继承了BaseMenuDetailPager，父类方法没什么实现的
 *
 * 初始化好页面之后，设置adapter，这样就根据服务器返回的数量来new相应个数的TabDetailPager
 *
 * 这里的viewpager滑动事件，被父控件的页签的的viewpager拦截掉了，所以要自定义一个viewpager，重写dispatchTouchEvent方法
 */
public class TabDetailPager extends BaseMenuDetailPager {
	//需要对应的小对象
	private NewsMenu.NewsTabData mTabData;// 单个页签的网络数据
	// private TextView view;

	@ViewInject(R.id.vp_top_news)
	private TopNewsViewPager mViewPager;

	@ViewInject(R.id.indicator)
	private CirclePageIndicator mIndicator;

	@ViewInject(R.id.tv_title)
	private TextView tvTitle;

	//这里的listView要用添加过头布局的下拉刷新的listview
	@ViewInject(R.id.lv_list)
	private PullToRefreshListView lvList;
	//请求北京标签页里的小模块里的url链接搞出来
	private String mUrl;

	// 下一页数据链接
	private String mMoreUrl;
	//viewpager的自动轮播使用的
	private Handler mHandler;

	private ArrayList<TopNews> mTopNews;
	private ArrayList<NewsData> mNewsList;

	private NewsAdapter mNewsAdapter;

	public TabDetailPager(Activity activity, NewsMenu.NewsTabData newsTabData) {
		super(activity);
		mTabData = newsTabData;
		//请求北京标签页里的小模块里的url链接搞出来
		mUrl = GlobalConstants.SERVER_URL + mTabData.url;
	}

	@Override
	public View initView() {
		// 要给帧布局填充布局对象
		// view = new TextView(mActivity);

		//	代码执行的顺序问题，在父类构造方法里就用到了，还没有初始化的时候就用了数据，会空指针异常，放到初始化数据里面
		//也不能放上面，因为必须先走构造方法，所依放到初始化数据里面
		// // view.setText(mTabData.title);
		//
		// view.setTextColor(Color.RED);
		// view.setTextSize(22);
		// view.setGravity(Gravity.CENTER);

		//填充一个布局文件
		View view = View.inflate(mActivity, R.layout.pager_tab_detail, null);
		//viewpager需要adapter填充
		// 头条新闻数据适配器
		ViewUtils.inject(this, view);

		// 给listview添加头布局，就是添加viewpager的轮播图片
		//初始化头布局
		View mHeaderView = View.inflate(mActivity, R.layout.list_item_header,null);
		ViewUtils.inject(this, mHeaderView);// 此处必须将头布局也注入
		lvList.addHeaderView(mHeaderView);


		//在刷新的时候，通知并进行回调，并保证listener不等于空，到TabDetailPager设置回调方法
		// 5. 前端界面设置回调
		lvList.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {

			@Override
			public void onRefresh() {
				//从服务器在拿一遍数据
				// 刷新数据
				getDataFromServer();

			}

			@Override
			public void onLoadMore() {
				// 判断是否有下一页数据
				if (mMoreUrl != null) {
					// 有下一页，要加载下一页数据
					getMoreDataFromServer();
				} else {
					// 没有下一页
					Toast.makeText(mActivity, "没有更多数据了", Toast.LENGTH_SHORT).show();
					// 没有下一页数据的时候，也要收起控件
					lvList.onRefreshComplete(true);
				}
			}
		});

		//给页签的listView设置点击事件，也是回调
		lvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			//参数：1，当前的listView，2，当前被点的item的view对象
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {

				//因为有2个头布局，所有item的位置会大2个
				int headerViewsCount = lvList.getHeaderViewsCount();// 获取头布局数量
				//这个才是真实item的位置
				position = position - headerViewsCount;// 需要减去头布局的占位
				System.out.println("第" + position + "个被点击了");

				//通过位置拿到新闻的对象
				NewsData news = mNewsList.get(position);

				//每条新闻记录他的标识，nerws里面的id
				// read_ids: 1101,1102,1105,1203,
				String readIds = PrefUtils.getString(mActivity, "read_ids", "");

				// 只有不包含当前id,才追加,
				if (!readIds.contains(news.id + "")) {
					// 避免重复添加同一个id
					//追加已经的id，加在原来的id上
					readIds = readIds + news.id + ",";// 1101,1102,
					//在保存一下
					PrefUtils.setString(mActivity, "read_ids", readIds);
				}

				//在NewsAdapter加载视图的时候，就要识别已读还是未读
				// 要将被点击的item的文字颜色改为灰色, 局部刷新, view对象就是当前被点击的对象
				TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
				//实现了局部刷新直接修改textView
				tvTitle.setTextColor(Color.GRAY);
				// mNewsAdapter.notifyDataSetChanged();//全局刷新, 浪费性能

				// 跳到新闻详情页面
				Intent intent = new Intent(mActivity, NewsDetailActivity.class);
				//news里面的url字段传过去
				intent.putExtra("url", news.url);
				//因为自己不是activity所以需要上下文来开启新的activity，不能直接开启
				mActivity.startActivity(intent);
			}
		});

		return view;
	}

	//	代码执行的顺序问题，在父类构造方法里就用到了，还没有初始化的时候就用了数据，会空指针异常，就放到这里了
	//初始化数据的时候，就不会在构造方法里面走
	@Override
	public void initData() {
		//在这里初始化titile，但是拿不到view，就把view变成全局的
		// view.setText(mTabData.title);
		//获取是否有缓存
		String cache = CacheUtils.getCache(mUrl, mActivity);
		//如果有缓存的话
		if (!TextUtils.isEmpty(cache)) {
			//就直接解析数据，数据不是在加载中
			processData(cache, false);
		}

		//请求数据的方法
		getDataFromServer();
	}

	/*
	* 请求网络的时候，放在子线程的，但是这里没有放子线程
	* 因为xutils底层已经封装好了，里面已经new了子线程
	*
	* 在子线程里也不能更新ui，但是xutils也帮封装好了
	* */
	private void getDataFromServer() {
		HttpUtils utils = new HttpUtils();
		//发送一个请求：
		//请求北京标签页里的小模块里的url链接搞出来
		utils.send(HttpMethod.GET, mUrl, new RequestCallBack<String>() {

			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				//拿到数据
				String result = responseInfo.result;
				//解析数据，数据不是在加载中
				processData(result, false);
				//添加网络数据的缓存
				CacheUtils.setCache(mUrl, result, mActivity);

				// 在网络请求结束之后，收起下拉刷新控件
				// 只有刷新成功之后才更新时间，所以传了一个true
				lvList.onRefreshComplete(true);
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				// 请求失败
				error.printStackTrace();
				Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();

				// 在网络请求结束之后，收起下拉刷新控件
				lvList.onRefreshComplete(false);
			}
		});
	}


	/**
	 * 加载下一页数据
	 */
	protected void getMoreDataFromServer() {
		HttpUtils utils = new HttpUtils();
		utils.send(HttpMethod.GET, mMoreUrl, new RequestCallBack<String>() {

			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				//请求成功后的一个结果
				String result = responseInfo.result;
				//解析这个结果，数据是加载中
				processData(result, true);

				// 收起下拉刷新控件（是加载下一页数据的控件）
				lvList.onRefreshComplete(true);
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				// 请求失败
				error.printStackTrace();
				Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();

				// 收起下拉刷新控件（是加载下一页数据的控件）
				lvList.onRefreshComplete(false);
			}
		});
	}


	//解析数据
	protected void processData(String result, boolean isMore) {
		Gson gson = new Gson();
		//解析到集合里面
		NewsTabBean newsTabBean = gson.fromJson(result, NewsTabBean.class);

		//把解析出来的数据的more链接提取出来，在网络数据的对象里面取出来
		String moreUrl = newsTabBean.data.more;
		//可能是空，所以要判断一下，可能是空字符串，不是null，所以要用isEmpty来判断
		if (!TextUtils.isEmpty(moreUrl)) {
			// 下一页数据链接
			mMoreUrl = GlobalConstants.SERVER_URL + moreUrl;
		} else {
			mMoreUrl = null;
		}

		//如果没有更多下一页数据的话
		if (!isMore) {
			//解析完成之后，就可以使用了
			// 拿出头条新闻的集合，填充数据
			//数据填充到viewpager，需要adapter来填充TopNewsAdapter
			mTopNews = newsTabBean.data.topnews;
			if (mTopNews != null) {
				mViewPager.setAdapter(new TopNewsAdapter());
				//给页签里面的轮播图也设置viewpager
				mIndicator.setViewPager(mViewPager);
				//默认是false，会跟着手指动，true是一跳一跳的
				mIndicator.setSnap(true);// 快照方式展示

				//给viewpager设置事件监听，滑动的时候能改变viewpager上面的文字
				// 事件要设置给Indicator
				mIndicator.setOnPageChangeListener(new OnPageChangeListener() {

					//viewpager选中的时候
					@Override
					public void onPageSelected(int position) {
						// 更新头条新闻viewpager的标题
						TopNews topNews = mTopNews.get(position);
						tvTitle.setText(topNews.title);
					}

					@Override
					public void onPageScrolled(int position, float positionOffset,
											   int positionOffsetPixels) {

					}

					@Override
					public void onPageScrollStateChanged(int state) {

					}
				});

				//第一页不会自动加载数据的，所有需要手动更新
				// 更新第一个头条新闻标题
				tvTitle.setText(mTopNews.get(0).title);

				//会有bug，切其他页签的时候，会初始化viewpager的图，但是指示器的点没有初始化，指示器底层记住位置了
				// 默认让第一个选中(解决页面销毁后重新初始化时,Indicator仍然保留上次圆点位置的bug)
				mIndicator.onPageSelected(0);
			}

			//在网络加载数据之后，在加载图标新闻viewpager的切图，在设置这里
			// 列表新闻
			mNewsList = newsTabBean.data.news;
			if (mNewsList != null) {
				//adapter全局要用到的，列表需要adapter来填充数据，设置adapter
				mNewsAdapter = new NewsAdapter();
				lvList.setAdapter(mNewsAdapter);
			}

			//在填充viewpager之后，就可以使用轮播了
			//viewpager的自动轮播使用的
			//用户可能下拉刷新很多次，这样会发送很多次延迟的消息，所以handler判断为空的话，就保证只发送一次
			if (mHandler == null) {
				mHandler = new Handler() {
					public void handleMessage(android.os.Message msg) {
						//延迟3秒后，会走到这个方法里面，当前的页面更新到下一页
						//先拿到当前的页面
						int currentItem = mViewPager.getCurrentItem();
						currentItem++;

						//循环的话，数值会越来越大，到第四个页面的时候要轮训（数量是网络数据）
						if (currentItem > mTopNews.size() - 1) {
							currentItem = 0;// 如果已经到了最后一个页面,跳到第一页
						}

						//设置viewpager页面
						mViewPager.setCurrentItem(currentItem);

						//让图片无限轮播，所有在这里循环发消息
						mHandler.sendEmptyMessageDelayed(0, 3000);// 继续发送空消息延时3秒的消息,形成内循环
					};
				};

				//handler发一个延时的消息
				// 保证启动自动轮播逻辑只执行一次
				mHandler.sendEmptyMessageDelayed(0, 3000);// 发送延时3秒的消息

				//viewpager的触摸事件，按住之后就应该停止播放
				//当用户按住的时候，handler应该停止发送消息，手抬起之后在发送消息继续轮播
				mViewPager.setOnTouchListener(new View.OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						switch (event.getAction()) {
							//手指按下的时候
							case MotionEvent.ACTION_DOWN:
								System.out.println("ACTION_DOWN");
								// 停止广告自动轮播
								// 删除handler的所有消息
								mHandler.removeCallbacksAndMessages(null);
								// mHandler.post(new Runnable() {
								//
								// @Override
								// public void run() {
								// //在主线程运行，是一个回调方法，这个方法的好处，就是不用重写message方法
								// }
								// });
								break;
							//还有一种情况就是乱划的话，轮播就停止了（就是滑倒其他地方去了）
							// 取消事件,
							case MotionEvent.ACTION_CANCEL:
								// 当按下viewpager后,直接滑动listview,导致抬起事件无法响应,但会走此事件
								System.out.println("ACTION_CANCEL");
								// 启动广告，继续发送消息
								mHandler.sendEmptyMessageDelayed(0, 3000);
								break;

							//手指抬起的话
							case MotionEvent.ACTION_UP:
								System.out.println("ACTION_UP");
								// 启动广告，继续发送消息
								mHandler.sendEmptyMessageDelayed(0, 3000);
								break;

							default:
								break;
						}
						return false;
					}
				});
			}



		} else {

			// 加载更多下一页数据，第二页的数据
			ArrayList<NewsData> moreNews = newsTabBean.data.news;
			// 将数据追加在原来的集合中
			mNewsList.addAll(moreNews);
			// 刷新listview
			mNewsAdapter.notifyDataSetChanged();
		}
	}


	//viewpager需要adapter填充
	// 头条新闻数据适配器
	class TopNewsAdapter extends PagerAdapter {

		private BitmapUtils mBitmapUtils;

		public TopNewsAdapter() {
			mBitmapUtils = new BitmapUtils(mActivity);
			//用xutils的bitmap先加载一张默认的图片，然后等有了图片之后在替换
			mBitmapUtils.configDefaultLoadingImage(R.mipmap.topnews_item_default);// 设置加载中的默认图片
		}

		//大小是根据服务器返回的数据定的，所以需要获取网络数据
		@Override
		public int getCount() {
			return mTopNews.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		//这个viewpager比较简单，只是图片
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ImageView view = new ImageView(mActivity);
			//这里是因为图片不能填充父窗体，所以提供了一个方法
			// view.setImageResource(R.drawable.topnews_item_default);
			view.setScaleType(ScaleType.FIT_XY);// 设置图片缩放方式, 宽高填充父控件

			//加载网络图片，图片在标签页数据里面有图片的地址
			String imageUrl = mTopNews.get(position).topimage;// 图片下载链接

			// 下载图片-将图片设置给imageview-避免内存溢出-缓存
			// BitmapUtils-XUtils，都做了处理，在构造方法里面new好了，
			mBitmapUtils.display(view, imageUrl);

			//view添加到container
			container.addView(view);

			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

	}

	//给标签页里面的数据的listview设置adapter
	class NewsAdapter extends BaseAdapter {
		//图片用bitmap加载
		private BitmapUtils mBitmapUtils;

		public NewsAdapter() {
			//图片用bitmap加载
			mBitmapUtils = new BitmapUtils(mActivity);
			//默认加载的图片
			mBitmapUtils.configDefaultLoadingImage(R.mipmap.news_pic_default);
		}

		@Override
		public int getCount() {
			return mNewsList.size();
		}

		@Override
		public NewsData getItem(int position) {
			return mNewsList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		//这里的convertView用到了重用
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				//加载布局文件
				convertView = View.inflate(mActivity, R.layout.list_item_news,null);
				holder = new ViewHolder();
				holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
				holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
				holder.tvDate = (TextView) convertView.findViewById(R.id.tv_date);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			NewsData news = getItem(position);
			holder.tvTitle.setText(news.title);
			holder.tvDate.setText(news.pubdate);

			// 根据本地记录来标记已读未读
			String readIds = PrefUtils.getString(mActivity, "read_ids", "");
			if (readIds.contains(news.id + "")) {
				holder.tvTitle.setTextColor(Color.GRAY);
			} else {
				//因为这里view会重用，所以一定要设置颜色
				holder.tvTitle.setTextColor(Color.BLACK);
			}

			//图片用bitmap加载
			mBitmapUtils.display(holder.ivIcon, news.listimage);

			return convertView;
		}

	}

	static class ViewHolder {
		public ImageView ivIcon;
		public TextView tvTitle;
		public TextView tvDate;
	}

}
