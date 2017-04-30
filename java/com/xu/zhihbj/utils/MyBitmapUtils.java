package com.xu.zhihbj.utils;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.xu.zhihbj.R;

/**
 * 自定义三级缓存图片加载工具
 *
 * bitmap基本逻辑，优先从内存里面取，然后磁盘，最后才到网络取图片
 */
public class MyBitmapUtils {

	//全局变量
	private NetCacheUtils mNetCacheUtils;
	private LocalCacheUtils mLocalCacheUtils;
	private MemoryCacheUtils mMemoryCacheUtils;

	//在构造方法里面就初始化好
	public MyBitmapUtils() {
		mMemoryCacheUtils = new MemoryCacheUtils();
		mLocalCacheUtils = new LocalCacheUtils();
		mNetCacheUtils = new NetCacheUtils(mLocalCacheUtils, mMemoryCacheUtils);
	}

	//展现图片的方法，传的图对象和下载链接
	public void display(ImageView imageView, String url) {
		// 设置默认图片
		imageView.setImageResource(R.mipmap.pic_item_list_default);

		// 优先从内存中加载图片, 速度最快, 不浪费流量
		Bitmap bitmap = mMemoryCacheUtils.getMemoryCache(url);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
			System.out.println("从内存加载图片啦");
			return;
		}

		// 其次从本地(sdcard)加载图片, 速度快, 不浪费流量
		bitmap = mLocalCacheUtils.getLocalCache(url);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
			System.out.println("从本地加载图片啦");

			// 写内存缓存
			mMemoryCacheUtils.setMemoryCache(url, bitmap);
			return;
		}

		// 最后从网络下载图片, 速度慢, 浪费流量，参数：图片对象和url
		mNetCacheUtils.getBitmapFromNet(imageView, url);
	}

}
