package com.xu.zhihbj.utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * 内存缓存
 * 因为从 Android 2.3 (API Level 9)开始，垃圾回收器会更倾向于回收持有软引用或弱引用的对象，这让软引用和弱引用变得不再可靠。Google建议使用LruCache
 *
 *
 */
public class MemoryCacheUtils {

	//这个就是内存缓存，bitmap缺点就是很大，容易内存溢出
	// private HashMap<String, Bitmap> mMemoryCache = new HashMap<String,Bitmap>();
	// private HashMap<String, SoftReference<Bitmap>> mMemoryCache = new HashMap<String, SoftReference<Bitmap>>();

	//因为安卓垃圾回收器，会回收软引用和弱引用，这样就没有意义，所依安卓提供了这个类
	private LruCache<String, Bitmap> mMemoryCache;

	//在构造方法里面初始化类
	public MemoryCacheUtils() {
		// LruCache 可以将最近最少使用的对象回收掉, 从而保证内存不会超出范围
		// Lru: least recentlly used 最近最少使用算法
		long maxMemory = Runtime.getRuntime().maxMemory();// 获取分配给app的内存大小
		System.out.println("maxMemory:" + maxMemory);

		//这个集合最大存储量（默认16兆空间）保守就是八分之一
		mMemoryCache = new LruCache<String, Bitmap>((int) (maxMemory / 8)) {

			// 返回每个对象的大小（重写这个方法）
			@Override
			protected int sizeOf(String key, Bitmap value) {
				// int byteCount = value.getByteCount();//字节总大小，但是版本兼容有问题
				int byteCount = value.getRowBytes() * value.getHeight();// 计算图片大小:每行字节数*高度
				//要返回每个对象的大小，才能控制大小
				return byteCount;
			}
		};
	}

	/**
	 * 写缓存
	 */
	public void setMemoryCache(String url, Bitmap bitmap) {
		// mMemoryCache.put(url, bitmap);
		// SoftReference<Bitmap> soft = new SoftReference<Bitmap>(bitmap);//
		// 使用软引用将bitmap包装起来
		// mMemoryCache.put(url, soft);
		mMemoryCache.put(url, bitmap);
	}

	/**
	 * 读缓存
	 */
	public Bitmap getMemoryCache(String url) {
		// SoftReference<Bitmap> softReference = mMemoryCache.get(url);
		//
		// if (softReference != null) {
		// Bitmap bitmap = softReference.get();
		// return bitmap;
		// }

		return mMemoryCache.get(url);
	}
}
