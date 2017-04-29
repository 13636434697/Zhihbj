package com.xu.zhihbj.domain;

import java.util.ArrayList;
/**
 * 组图对象
 *
 * 外面是一个对象。里面也是一个对象data，需要写个data的object
 */
public class PhotosBean {
	//这里也要声明一下，用data这个字段，这个字段里面是PhotosData
	public PhotosData data;

	//这个字段里面是PhotosData,是一个集合news
	public class PhotosData {
		public ArrayList<PhotoNews> news;
	}
	//news集合里面又手势一个个的对象，需要在写一个对象
	public class PhotoNews {
		//对象里面的字段
		public int id;
		public String listimage;
		public String title;
	}
}
