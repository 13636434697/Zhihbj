package com.xu.zhihbj;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

//分享暂时不学
//import cn.sharesdk.framework.ShareSDK;
//import cn.sharesdk.onekeyshare.OnekeyShare;
//import cn.sharesdk.onekeyshare.OnekeyShareTheme;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * 新闻详情页面
 *
 * 响应点击事件，所以继承了OnClickListener
 */
public class NewsDetailActivity extends Activity implements OnClickListener {

	@ViewInject(R.id.ll_control)
	private LinearLayout llControl;
	@ViewInject(R.id.btn_back)
	private ImageButton btnBack;
	@ViewInject(R.id.btn_textsize)
	private ImageButton btnTextSize;
	@ViewInject(R.id.btn_share)
	private ImageButton btnShare;
	@ViewInject(R.id.btn_menu)
	private ImageButton btnMenu;
	@ViewInject(R.id.wv_news_detail)
	private WebView mWebView;
	@ViewInject(R.id.pb_loading)
	private ProgressBar pbLoading;
	private String mUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_news_detail);
		//注解方式
		ViewUtils.inject(this);

		//设置显示
		llControl.setVisibility(View.VISIBLE);
		//设置显示
		btnBack.setVisibility(View.VISIBLE);
		//设置隐藏
		btnMenu.setVisibility(View.GONE);

		//设置3个点击事件
		btnBack.setOnClickListener(this);
		btnTextSize.setOnClickListener(this);
		btnShare.setOnClickListener(this);

		//获取传过来的网址，从服务器数据里面提取出来需要跳转的网址
		mUrl = getIntent().getStringExtra("url");

		//用这个控件加载一个网页
		// mWebView.loadUrl("http://www.itheima.com");
		mWebView.loadUrl(mUrl);

		//需要设置各种事件
		WebSettings settings = mWebView.getSettings();
		settings.setBuiltInZoomControls(true);// 显示缩放按钮(wap网页不支持)
		settings.setUseWideViewPort(true);// 支持双击缩放(wap网页不支持)
		settings.setJavaScriptEnabled(true);// 支持js功能

		//因为有时候会跳到游览器里面的，现在全部操作都要在应用里面操作，相当于webview的监听
		mWebView.setWebViewClient(new WebViewClient() {
			// 开始加载网页
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				System.out.println("开始加载网页了");
				//设置一个进度，在加载网页的时候显示出来，加载完毕的时候隐藏掉
				pbLoading.setVisibility(View.VISIBLE);
			}

			// 网页加载结束
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				System.out.println("网页加载结束");
				//设置一个进度，在加载网页的时候显示出来，加载完毕的时候隐藏掉
				pbLoading.setVisibility(View.INVISIBLE);
			}

			// 所有链接跳转会走此方法
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				System.out.println("跳转链接:" + url);
				view.loadUrl(url);// 在跳转链接时强制在当前webview中加载
				return true;
			}
		});

		// mWebView.goBack();//跳到上个页面
		// mWebView.goForward();//跳到下个页面

		//需要知道webView的加载进度
		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				// 进度发生变化
				System.out.println("进度:" + newProgress);
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				// 网页标题
				System.out.println("网页标题:" + title);
			}
		});
	}

	//点击事件
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			//返回
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_textsize:
			// 修改网页字体大小
			showChooseDialog();
			break;
			//分享
		case R.id.btn_share:
//			//分享暂时不学
//			showShare();
			break;

		default:
			break;
		}
	}

	private int mTempWhich;// 记录临时选择的字体大小(点击确定之前)
	private int mCurrenWhich = 2;// 记录当前选中的字体大小(点击确定之后), 默认正常字体
	/**
	 * 展示选择字体大小的弹窗
	 */
	private void showChooseDialog() {
		//弹出窗口
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		//设置窗体的标题
		builder.setTitle("字体设置");
		//弹出窗体内容的字符串数组
		String[] items = new String[] { "超大号字体", "大号字体", "正常字体", "小号字体","超小号字体" };
		//字符数组，默认第几个被选中，监听事件
		builder.setSingleChoiceItems(items, mCurrenWhich,new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						//下面的which不能用，要用这里的which，所以定义成全局的
						mTempWhich = which;
					}
				});
		//一个确定的按钮
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			//这里的which不能用，要用上面的which
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 根据选择的字体来修改网页字体大小

				WebSettings settings = mWebView.getSettings();
				switch (mTempWhich) {
				case 0:
					// 超大字体，传的是枚举方法
					settings.setTextSize(TextSize.LARGEST);
					// settings.setTextZoom(22);
					break;
				case 1:
					// 大字体，传的是枚举方法
					settings.setTextSize(TextSize.LARGER);
					break;
				case 2:
					// 正常字体，传的是枚举方法
					settings.setTextSize(TextSize.NORMAL);
					break;
				case 3:
					// 小字体，传的是枚举方法
					settings.setTextSize(TextSize.SMALLER);
					break;
				case 4:
					// 超小字体，传的是枚举方法
					settings.setTextSize(TextSize.SMALLEST);
					break;

				default:
					break;
				}
				//把选择的位置，赋给当前位置
				mCurrenWhich = mTempWhich;
			}
		});

		//一个取消的按钮，什么都不用做，自动finish掉
		builder.setNegativeButton("取消", null);

		builder.show();
	}

//	//分享暂时不学
//	// 确保SDcard下面存在此张图片test.jpg
//	private void showShare() {
	//初始化sdk
//		ShareSDK.initSDK(this);
	//核心类，一键分享
//		OnekeyShare oks = new OnekeyShare();
//
//		oks.setTheme(OnekeyShareTheme.SKYBLUE);//修改主题样式
//
//		// 关闭sso授权
//		oks.disableSSOWhenAuthorize();
//
//		// 分享时Notification的图标和文字 2.5.9以后的版本不调用此方法
//		// oks.setNotification(R.drawable.ic_launcher,
//		// getString(R.string.app_name));
//		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
//		oks.setTitle(getString(R.string.share));
//		// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
//		oks.setTitleUrl("http://sharesdk.cn");
//		// text是分享文本，所有平台都需要这个字段
//		oks.setText("我是分享文本");
//		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
//		oks.setImagePath("/sdcard/test.jpg");// 确保SDcard下面存在此张图片
//		// url仅在微信（包括好友和朋友圈）中使用
//		oks.setUrl("http://sharesdk.cn");
//		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
//		oks.setComment("我是测试评论文本");
//		// site是分享此内容的网站名称，仅在QQ空间使用
//		oks.setSite(getString(R.string.app_name));
//		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
//		oks.setSiteUrl("http://sharesdk.cn");
//
//		// 启动分享GUI
//		oks.show(this);
//	}
}
