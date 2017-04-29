package com.xu.zhihbj.domain;

import java.util.ArrayList;

/**
 * 分类信息封装
 * 
 * 使用Gson解析时,对象书写技巧: 1. 逢{}创建对象, 逢[]创建集合(ArrayList) 2. 所有字段名称要和json返回字段高度一致
 *
 * NewsMenu这个类型无所谓，里面的字段必须要一样
 *
 */

//对象直接打印的话，是内存地址，需要重写tostring，来打印各个字段的名字
public class NewsMenu {
/*
* 技巧，照着json写
* 最外层是大括号，是个对象，就是NewsMenu
* 里面有个retcode，就声明一个整数，还有个extend，是一个中括号，是一个数组，用集合表示，
*
* data是一个大大的集合，里面还有对象，所有在搞个对象NewsMenuData，然后把这个对象方法data集合里面
*
* NewsMenuData里面有具体的信息id，title，type。还有一个url，用不上的话，就不用解析了
*
* children还是中括号，还是一个集合，所以在弄一个集合children，children里面又是一个个对象，所以在写个对象NewsTabData，里面具体的信息等等
*
* 所以集合的泛型都是下层的对象
* */
	public int retcode;
	public ArrayList<Integer> extend;
	public ArrayList<NewsMenuData> data;

	// 侧边栏菜单对象
	public class NewsMenuData {
		public int id;
		public String title;
		public int type;

		public ArrayList<NewsTabData> children;

		@Override
		public String toString() {
			return "NewsMenuData [title=" + title + ", children=" + children
					+ "]";
		}
	}

	// 页签的对象
	public class NewsTabData {
		public int id;
		public String title;
		public int type;
		public String url;

		@Override
		public String toString() {
			return "NewsTabData [title=" + title + "]";
		}

	}

	@Override
	public String toString() {
		return "NewsMenu [data=" + data + "]";
	}

}
