package com.xu.zhihbj.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;

/**
 * 本地缓存
 *
 */
public class LocalCacheUtils {

	//生成一个文件路径
	private static final String LOCAL_CACHE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/zhbj74_cache";

	// 写本地缓存
	public void setLocalCache(String url, Bitmap bitmap) {
		//新建一个文件夹
		File dir = new File(LOCAL_CACHE_PATH);
		//判断文件存不存在，并且判断是不是一个文件夹
		if (!dir.exists() || !dir.isDirectory()) {
			dir.mkdirs();// 创建文件夹
		}

		try {
			String fileName = MD5Encoder.encode(url);

			//怎么给文件名称命名，但是有很多特殊符号，就用到md5
			File cacheFile = new File(dir, fileName);

			//存储图片，当前图片压缩到本地
			// 参1:图片格式;参2:压缩比例0-100; 参3:输出流
			bitmap.compress(CompressFormat.JPEG, 100, new FileOutputStream(cacheFile));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 读本地缓存，图片的url传进来
	public Bitmap getLocalCache(String url) {
		try {
			//新建一个文件夹。缓存文件
			File cacheFile = new File(LOCAL_CACHE_PATH, MD5Encoder.encode(url));

			//判断缓存文件存不存在
			if (cacheFile.exists()) {
				//读取缓存
				Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(cacheFile));
				return bitmap;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
