package com.xu.zhihbj.view;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xu.zhihbj.R;


/**
 * 页签页面新闻的下拉刷新
 *
 * 下拉刷新的listview
 *
 * listView是先添加的这个头，初始化方法，在这个对象new的时候已经添加了，新闻详情页在初始化布局的时候才添加的
 *
 * 	监听listView的滑动事件
 */
public class PullToRefreshListView extends ListView implements AbsListView.OnScrollListener {

	//下拉刷新，需要声明几种状态
	//下拉刷新
	private static final int STATE_PULL_TO_REFRESH = 1;
	//松开刷新
	private static final int STATE_RELEASE_TO_REFRESH = 2;
	//正在刷新
	private static final int STATE_REFRESHING = 3;

	// 当前状态是刷新状态
	private int mCurrentState = STATE_PULL_TO_REFRESH;

	private View mHeaderView;
	private int mHeaderViewHeight;
	private int startY = -1;

	private View mFooterView;
	private int mFooterViewHeight;

	private TextView tvTitle;
	private TextView tvTime;
	private ImageView ivArrow;

	private RotateAnimation animUp;
	private RotateAnimation animDown;
	private ProgressBar pbProgress;

	public PullToRefreshListView(Context context, AttributeSet attrs,int defStyle) {
		super(context, attrs, defStyle);
		initHeaderView();
		initFooterView();
	}

	public PullToRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initHeaderView();
		initFooterView();
	}

	public PullToRefreshListView(Context context) {
		super(context);
		initHeaderView();
		initFooterView();
	}

	/**
	 * 初始化头布局
	 */
	private void initHeaderView() {
		mHeaderView = View.inflate(getContext(), R.layout.pull_to_refresh_header, null);
		this.addHeaderView(mHeaderView);

		tvTitle = (TextView) mHeaderView.findViewById(R.id.tv_title);
		tvTime = (TextView) mHeaderView.findViewById(R.id.tv_time);
		ivArrow = (ImageView) mHeaderView.findViewById(R.id.iv_arrow);
		pbProgress = (ProgressBar) mHeaderView.findViewById(R.id.pb_loading);

		// 隐藏头布局，先测量，因为直接拿高度是拿不到的
		mHeaderView.measure(0, 0);
		//获取高度
		mHeaderViewHeight = mHeaderView.getMeasuredHeight();
		//设置高度，这就是隐藏头布局的效果
		mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);

		//在初始化头布局的时候，就初始化这个动画
		initAnim();

		//在初始化的时候也要调用一下，设置刷新时间
		setCurrentTime();
	}


	/**
	 * 初始化脚布局
	 */
	private void initFooterView() {
		mFooterView = View.inflate(getContext(),R.layout.pull_to_refresh_footer, null);
		//添加view对象
		this.addFooterView(mFooterView);

		//测量计算脚布局的高度
		mFooterView.measure(0, 0);
		//计算脚布局的高度
		mFooterViewHeight = mFooterView.getMeasuredHeight();
		//隐藏脚布局
		mFooterView.setPadding(0, -mFooterViewHeight, 0, 0);

		//这就是一个回调方法，设置回调接口，传进来，滑倒最底部，就调用下面的2个方法
		//监听listView的滑动事件，
		this.setOnScrollListener(this);// 滑动监听
	}


	// 设置刷新时间
	//格式是四位二位（可以自己修改显示时间的格式）或者大写小写（坐标是0开始的，大写的M是1月开始，时间小写的话是12进制，大写的话是24进制）
	private void setCurrentTime() {
		//拿到时间里面传一个格式
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//new Date()默认时间就是当前的时间
		String time = format.format(new Date());
		//设置文本
		tvTime.setText(time);
	}

	//设置listView可以下拉刷新，然后把隐藏的头布局显示出来
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		//判断动作
		switch (ev.getAction()) {
			//按下
		case MotionEvent.ACTION_DOWN:
			//按下的时候要记录一下按下的坐标，因为下拉是竖直方向，和左右没有关系，只记录Y坐标
			startY = (int) ev.getY();
			break;
			//移动
		case MotionEvent.ACTION_MOVE:
			//为了安全起见，这里默认是-1，如果是-1就是无效的
			if (startY == -1) {
				// 当用户按住头条新闻的viewpager进行下拉时,ACTION_DOWN会被viewpager消费掉,
				// 导致startY没有赋值,此处需要重新获取一下
				startY = (int) ev.getY();
			}


			//下拉刷新的时候刷新，在加载数据，但是还需要能滑动，同时数据还在加载中
			// 因为没有越过刷新状态，而且在任何状态下一摸就开始运作，应该把正在刷新的状态跳过去
			if (mCurrentState == STATE_REFRESHING) {
				// 如果是正在刷新, 跳出循环
				//不能写return，因为return是必须返回东西的
				break;
			}

			//终点的Y坐标
			int endY = (int) ev.getY();
			//计算偏移量，这里计算好之后，就可以知道下拉刷新padding多少了
			int dy = endY - startY;


			int firstVisiblePosition = getFirstVisiblePosition();// 当前显示的第一个item的位置
			// 必须下拉,并且当前显示的是第一个item
			if (dy > 0 && firstVisiblePosition == 0) {
				int padding = dy - mHeaderViewHeight;// 计算当前下拉控件的padding值
				//下拉多少就显示多少隐藏的头布局
				mHeaderView.setPadding(0, padding, 0, 0);

				//如果下拉刷新的头布局完全拉出，就是高度是0的时候，并且已经是下拉刷新的状态
				if (padding > 0 && mCurrentState != STATE_RELEASE_TO_REFRESH) {
					// 把当前状态改为松开刷新
					mCurrentState = STATE_RELEASE_TO_REFRESH;
					//把当前整个标签页的界面要全部刷新一下
					refreshState();
					//如果下拉刷新的头布局未完全拉出，高度还是负的时候，并且已经是下拉刷新的状态
				} else if (padding < 0&& mCurrentState != STATE_PULL_TO_REFRESH) {
					// 把当前状态改为下拉刷新
					mCurrentState = STATE_PULL_TO_REFRESH;
					//把当前整个标签页的界面要全部刷新一下
					refreshState();
				}

				//这个事件就消费掉了，就是全权处理（设置）
				return true;
			}

			break;
			//抬起
		case MotionEvent.ACTION_UP:
			//如果用户下拉一半listView头布局的时候，就不应该刷新，回去就可以了
			//归原始状态
			startY = -1;
			//如果当前状态是下拉刷新
			if (mCurrentState == STATE_RELEASE_TO_REFRESH) {
				//改为正在刷新（就是在加载数据的界面）
				mCurrentState = STATE_REFRESHING;
				//把状态刷新下
				refreshState();

				// 完整展示头布局，显示正在刷新中（加载网络数据）
				mHeaderView.setPadding(0, 0, 0, 0);

				//在刷新的时候，通知并进行回调，并保证listener不等于空，到TabDetailPager设置回调方法
				// 4. 进行回调
				if (mListener != null) {
					mListener.onRefresh();
				}

				//如果状态是下拉刷新的状态的话
			} else if (mCurrentState == STATE_PULL_TO_REFRESH) {
				// 隐藏头布局
				mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
			}

			break;

		default:
			break;
		}

		return super.onTouchEvent(ev);
	}

	/**
	 * 初始化箭头动画
	 */
	private void initAnim() {
		//页面刷新头布局的箭头方向从下变成上，逆时针旋转0到180度
		animUp = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF, 0.5f);
		animUp.setDuration(200);
		//要保持住变化的状态
		animUp.setFillAfter(true);
		//页面刷新头布局的箭头方向从上变成下，顺时针旋转-180度回到0
		animDown = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animDown.setDuration(200);
		//要保持住变化的状态
		animDown.setFillAfter(true);
	}

	/**
	 * 根据当前状态刷新界面
	 */
	//把当前整个标签页的界面要全部刷新一下
	private void refreshState() {
		//把当前状态传进来
		switch (mCurrentState) {
		case STATE_PULL_TO_REFRESH:
			tvTitle.setText("下拉刷新");
			pbProgress.setVisibility(View.INVISIBLE);
			ivArrow.setVisibility(View.VISIBLE);
			//箭头向下的动画
			ivArrow.startAnimation(animDown);
			break;
		case STATE_RELEASE_TO_REFRESH:
			tvTitle.setText("松开刷新");
			pbProgress.setVisibility(View.INVISIBLE);
			ivArrow.setVisibility(View.VISIBLE);
			//箭头向上的动画
			ivArrow.startAnimation(animUp);
			break;
		case STATE_REFRESHING:
			tvTitle.setText("正在刷新...");

			// 清除箭头动画,否则无法隐藏
			ivArrow.clearAnimation();
			//显示进度条
			pbProgress.setVisibility(View.VISIBLE);
			//把箭头隐藏掉
			ivArrow.setVisibility(View.INVISIBLE);
			break;

		default:
			break;
		}
	}

	/**
	 *	在网络请求结束之后，收起下拉刷新控件
	 *
	 * 刷新结束,收起控件
	 */
	public void onRefreshComplete(boolean success) {
		//隐藏布局
		mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
		//状态都要归零状态等等
		mCurrentState = STATE_PULL_TO_REFRESH;
		tvTitle.setText("下拉刷新");
		pbProgress.setVisibility(View.INVISIBLE);
		ivArrow.setVisibility(View.VISIBLE);

		//刷新结束后，收起刷新控件的时候
		if (success) {// 只有刷新成功之后才更新时间
			setCurrentTime();
		}else {
			//没有更多数据或者加载完一页数据之后要隐藏掉listview的脚
			//隐藏布局
			mFooterView.setPadding(0, -mFooterViewHeight, 0, 0);
			//下一页数据没有加载中
			isLoadMore = false;
		}
	}

	//怎么让标签详情页TabDetailPager知道，这个控件在刷新，这里用到了回调方法
	//

	// 3. 定义成员变量,接收监听对象
	private OnRefreshListener mListener;

	/**
	 * 2. 暴露接口,设置监听
	 */
	public void setOnRefreshListener(OnRefreshListener listener) {
		mListener = listener;
	}

	/**
	 * 1. 下拉刷新的回调接口
	 */
	public interface OnRefreshListener {
		public void onRefresh();

		//下拉加载更多
		public void onLoadMore();
	}

	//因为可能会重复加载很多次，所以要限制掉
	private boolean isLoadMore;// 标记是否正在加载更多

	//listView滑动监听事件，就是一个回调方法，设置回调接口，传进来，滑倒最底部，就调用下面的2个方法
	// 滑动状态发生变化
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		//只要滑动停下来，就要计算
		if (scrollState == SCROLL_STATE_IDLE) {// 空闲状态
			//获取当前界面最后一个item的位置
			int lastVisiblePosition = getLastVisiblePosition();

			//因为可能会重复加载很多次，所以要限制掉
			// 当前显示的是最后一个item并且没有正在加载更多，并且没有在加载中
			if (lastVisiblePosition == getCount() - 1 && !isLoadMore) {
				// 到底了
				System.out.println("加载更多...");

				//标记为正在加载更多下一页数据中
				isLoadMore = true;

				// 显示加载更多的布局
				mFooterView.setPadding(0, 0, 0, 0);

				//加载到底部，还要在拉一下，才能出现加载更多
				// 所以将listview显示在最后一个item上,从而加载更多会直接展示出来, 无需手动滑动
				setSelection(getCount() - 1);

				//通知主界面加载下一页数据
				if(mListener!=null) {
					//给接口加了一个回调方法
					mListener.onLoadMore();
				}
			}
		}
	}

	// 滑动过程回调
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
						 int visibleItemCount, int totalItemCount) {

	}
}
