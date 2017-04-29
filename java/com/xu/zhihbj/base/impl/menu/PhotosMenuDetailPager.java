package com.xu.zhihbj.base.impl.menu;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xu.zhihbj.R;
import com.xu.zhihbj.base.BaseMenuDetailPager;
import com.xu.zhihbj.domain.PhotosBean;
import com.xu.zhihbj.global.GlobalConstants;
import com.xu.zhihbj.utils.CacheUtils;

import com.xu.zhihbj.domain.PhotosBean.PhotoNews;

import java.util.ArrayList;


/**
 * 菜单详情页-组图
 *
 */
public class PhotosMenuDetailPager extends BaseMenuDetailPager implements OnClickListener {

	@ViewInject(R.id.lv_photo)
	private ListView lvPhoto;
	@ViewInject(R.id.gv_photo)
	private GridView gvPhoto;

	private ArrayList<PhotoNews> mNewsList;

	private ImageButton btnPhoto;

	public PhotosMenuDetailPager(Activity activity, ImageButton btnPhoto) {
		super(activity);
		//给另一个页面的按钮设置点击事件，这里把这个按钮的引用传过去
		btnPhoto.setOnClickListener(this);// 组图切换按钮设置点击事件
		this.btnPhoto = btnPhoto;
	}

	//初始化布局
	@Override
	public View initView() {
		//加载布局
		View view = View.inflate(mActivity, R.layout.pager_photos_menu_detail,null);
		//用xutils
		ViewUtils.inject(this, view);
		return view;
	}

	//初始化数据
	@Override
	public void initData() {
		//查看有没有缓存
		String cache = CacheUtils.getCache(GlobalConstants.PHOTOS_URL,mActivity);
		//如果有数据的话
		if (!TextUtils.isEmpty(cache)) {
			//解析数据
			processData(cache);
		}
		//加载网络数据
		getDataFromServer();
	}

	//加载网络数据
	private void getDataFromServer() {
		HttpUtils utils = new HttpUtils();
		//发送一个请求，网址是组图信息接口
		utils.send(HttpRequest.HttpMethod.GET, GlobalConstants.PHOTOS_URL,new RequestCallBack<String>() {

					@Override
					public void onSuccess(ResponseInfo<String> responseInfo) {
						//拿到请求的json数据
						String result = responseInfo.result;
						//解析数据
						processData(result);

						//设置缓存
						CacheUtils.setCache(GlobalConstants.PHOTOS_URL, result,mActivity);
					}

					@Override
					public void onFailure(HttpException error, String msg) {
						// 请求失败
						error.printStackTrace();
						Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
					}
				});
	}

	//解析gson数据
	protected void processData(String result) {
		Gson gson = new Gson();
		//gson需要一个对象，来解析存储数据
		PhotosBean photosBean = gson.fromJson(result, PhotosBean.class);

		//data里面有新闻数据，全局变量
		mNewsList = photosBean.data.news;

		//用adapter给listview和gridview填充数据
		// gridview的布局结构和listview完全一致,
		// 所以可以共用一个adapter
		lvPhoto.setAdapter(new PhotoAdapter());
		gvPhoto.setAdapter(new PhotoAdapter());

	}
	//用adapter给listview和gridview填充数据
	class PhotoAdapter extends BaseAdapter {

		//初始化BitmapUtils
		private BitmapUtils mBitmapUtils;

		//构造方法把BitmapUtils传进来
		public PhotoAdapter() {
			//初始化BitmapUtils
			mBitmapUtils = new BitmapUtils(mActivity);
			//设置BitmapUtils默认图片
			mBitmapUtils.configDefaultLoadingImage(R.mipmap.pic_item_list_default);
		}

		@Override
		public int getCount() {
			return mNewsList.size();
		}

		@Override
		public PhotoNews getItem(int position) {
			return mNewsList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = View.inflate(mActivity,R.layout.list_item_photos, null);
				holder = new ViewHolder();
				holder.ivPic = (ImageView) convertView.findViewById(R.id.iv_pic);
				holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			//拿到对应位置的对象
			PhotoNews item = getItem(position);

			//给布局设置数据
			holder.tvTitle.setText(item.title);
			//用bitmap来加载图片
			mBitmapUtils.display(holder.ivPic, item.listimage);

			return convertView;
		}

	}

	static class ViewHolder {
		public ImageView ivPic;
		public TextView tvTitle;
	}

	//在切换listView和graid之前要判断当前是什么才能切换
	private boolean isListView = true;// 标记当前是否是listview展示
	@Override
	public void onClick(View v) {
		if (isListView) {
			// 切成gridview
			lvPhoto.setVisibility(View.GONE);
			gvPhoto.setVisibility(View.VISIBLE);
			btnPhoto.setImageResource(R.mipmap.icon_pic_list_type);

			isListView = false;
		} else {
			// 切成listview
			lvPhoto.setVisibility(View.VISIBLE);
			gvPhoto.setVisibility(View.GONE);
			btnPhoto.setImageResource(R.mipmap.icon_pic_grid_type);

			isListView = true;
		}
	}

}
