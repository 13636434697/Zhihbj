package com.xu.zhihbj;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xu.zhihbj.utils.PrefUtils;

import java.util.ArrayList;

/**
 * 新手引导页面
 *
 */
public class GuideActivity extends Activity {

	private ViewPager mViewPager;
	private LinearLayout llContainer;
	private ImageView ivRedPoint;// 小红点
	private Button btnStart;

	private ArrayList<ImageView> mImageViewList; // imageView集合

	// 引导页图片id数组
	private int[] mImageIds = new int[] { R.mipmap.guide_1, R.mipmap.guide_2, R.mipmap.guide_3 };

	// 小红点移动距离
	private int mPointDis;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题,
														// 必须在setContentView之前调用
		setContentView(R.layout.activity_guide);

		mViewPager = (ViewPager) findViewById(R.id.vp_guide);
		llContainer = (LinearLayout) findViewById(R.id.ll_container);
		ivRedPoint = (ImageView) findViewById(R.id.iv_red_point);
		btnStart = (Button) findViewById(R.id.btn_start);

		//这里要调用一下初始化数据的方法
		initData();
		//先初始化数据，集合里面有数据之后，才可以设置adapter
		mViewPager.setAdapter(new GuideAdapter());

		//viewpager要知道移动了多少，所以设置监听
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			//当某个页面被选中的时候
			@Override
			public void onPageSelected(int position) {
				// 某个页面被选中
				// 如果最后一个页面被选中显示开始体验的按钮
				//这里位置尽量不要写死，根据图片的数量来确定
				if (position == mImageViewList.size() - 1) {
					btnStart.setVisibility(View.VISIBLE);
				} else {
					btnStart.setVisibility(View.INVISIBLE);
				}
			}

			//当页面滑动过程中的时候
			//参数，当前位置，移动偏移量的百分比，具体移动了多少个像素
			@Override
			public void onPageScrolled(int position, float positionOffset,int positionOffsetPixels) {
				// 当页面滑动过程中不断更新点的位置
				System.out.println("当前位置:" + position + ";移动偏移百分比:" + positionOffset);
				// 根据当前移动的白封闭，来更新小红点距离，
				//移动的距离，移动百分比，小红点和小黑点之间的间距，就是小红线移动的距离
				//移动的距离乘以移动的百分比，加上，当前位置乘以移动的距离
				int leftMargin = (int) (mPointDis * positionOffset) + position * mPointDis;// 计算小红点当前的左边距
				//拿当前的布局参数，小红点的父控件，是相对布局的布局参数
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ivRedPoint.getLayoutParams();
				//更新修改小红点的布局参数
				params.leftMargin = leftMargin;// 修改左边距

				// 重新设置小红点的布局参数
				ivRedPoint.setLayoutParams(params);
			}

			// 页面状态发生变化的回调
			@Override
			public void onPageScrollStateChanged(int state) {
				// 页面状态发生变化的回调
			}
		});

		// 这样算出来是0，因为涉及到布局绘制的流程，测量-确定位置-然后在画，然而这些流程在activity的onCreate方法执行结束之后才会走此流程
		// 计算两个圆点的距离，移动距离=第二个圆点left值 - 第一个圆点left值
		// measure->layout(确定位置)->draw(activity的onCreate方法执行结束之后才会走此流程)
		// mPointDis = llContainer.getChildAt(1).getLeft() - llContainer.getChildAt(0).getLeft();
		// System.out.println("圆点距离:" + mPointDis);


		// 这样算出来是0，因为涉及到布局绘制的流程，测量-确定位置-然后在画，然而这些流程在activity的onCreate方法执行结束之后才会走此流程
		//所以这里要设置监听，监听layout方法结束的事件,位置确定好之后再获取圆点间距
		// getViewTreeObserver，视图树的观察者，里面可以添加全局的layout的监听
		ivRedPoint.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					// layout方法执行结束的回调，在这里才可以确定绘画的绘制，在拿距离
					@Override
					public void onGlobalLayout() {
						//这个方法在系统底层可能会调用好几次，要重绘好几次，所以调用了好几次
						// 先拿到视图树，移除监听,避免重复回调
						ivRedPoint.getViewTreeObserver().removeGlobalOnLayoutListener(this);
						//ivRedPoint.getViewTreeObserver().removeOnGlobalLayoutListener(this);//这个版本不兼容

						// 计算两个圆点的距离，移动距离=第二个圆点left值 - 第一个圆点left值
						mPointDis = llContainer.getChildAt(1).getLeft() - llContainer.getChildAt(0).getLeft();
						System.out.println("圆点距离:" + mPointDis);
					}
				});


		//给按钮设置一个点击事件
		btnStart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//更新sp, 已经不是第一次进入了
				PrefUtils.setBoolean(getApplicationContext(), "is_first_enter", false);
				
				//跳到主页面
				startActivity(new Intent(getApplicationContext(), MainActivity.class));
				finish();
			}
		});
	}

	// 初始化数据
	private void initData() {
		mImageViewList = new ArrayList<ImageView>();
//		根据id数组的个数，生成imageview对象
		for (int i = 0; i < mImageIds.length; i++) {
			//初始化数组的时候，根据图片的个数初始化了，对应数量的imageView的对象
			ImageView view = new ImageView(this);
			//给每个imaeView设置了对应的图片的id
			view.setBackgroundResource(mImageIds[i]);// 通过设置背景,可以让宽高填充布局
			//设置图片资源
			// view.setImageResource(resId)

			//图片添加到集合，需要new一个集合的
			//这样根据图片的小小，存放了3个imageView对象
			mImageViewList.add(view);


			//动态添加小圆点，图片多少就添加多少小远点，所有在这里for循环里面，顺便小圆点也初始化好
			// 初始化小圆点
			ImageView point = new ImageView(this);
			//写小圆点的内容，这里没有用background，因为这里没有必要填充父窗体
			point.setImageResource(R.drawable.shape_point_gray);// 设置图片(shape形状)

			// 初始化布局参数, 宽高包裹内容,父控件是谁,就是谁声明的布局参数
			//传宽高，宽高已经在形状里面确定好了，这里就包裹内容就可以了
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);


			//因为小圆点之间的距离太小，所有要设置间距，
				if (i > 0) {
				// 从第二个点开始设置左边距
					//设置左边距的话，需要LayoutParams，布局参数来设置左边距
				params.leftMargin = 10;
			}

			point.setLayoutParams(params);// 设置布局参数

			llContainer.addView(point);// 给容器添加圆点
		}
	}

	//给viewpager填充数据
	class GuideAdapter extends PagerAdapter {

		// item的个数
		@Override
		public int getCount() {
			//获取集合的大小
			return mImageViewList.size();
		}

		//一般都这么写，判断当前object是不是view对象
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		// 初始化item布局，先把imageview做出来initdata
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			//集合里面获取，对应的对象位置
			ImageView view = mImageViewList.get(position);
			//然后塞给容器
			container.addView(view);
			//返回视图
			return view;
		}

		// 销毁item
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

	}
}
