package com.xu.zhihbj.domain;

import java.util.ArrayList;

/**
 * 页签详情数据对象
 *
 * 上来就大括号，javabean
 * 大括号里面是一个数据，但是用不上，可以不写
 * 还有data是一个对象，
 * 里面就是tab字段返回的就是tab对象，里面还有news集合和top的集合其他都不要
 * */
public class NewsTabBean {

	//data是一个对象
	public NewsTab data;


	public class NewsTab {
		public String more;
		public ArrayList<NewsData> news;
		public ArrayList<TopNews> topnews;
	}

	// 新闻列表对象
	public class NewsData {
		public int id;
		public String listimage;
		public String pubdate;
		public String title;
		public String type;
		public String url;
	}

	// 头条新闻
	public class TopNews {
		public int id;
		public String topimage;
		public String pubdate;
		public String title;
		public String type;
		public String url;
	}
}
