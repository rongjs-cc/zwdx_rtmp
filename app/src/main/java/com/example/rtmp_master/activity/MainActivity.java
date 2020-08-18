package com.example.rtmp_master.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.rtmp_master.config.VideoConfig;
import com.example.rtmp_master.ui.DrawingView;
import com.example.rtmp_master.R;
import com.frank.living.widget.IjkVideoView;
import java.lang.ref.WeakReference;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout main_drawerLayout;
    private IjkVideoView ijkLive;
    private ImageView btnCloor,btnTag,btnSend,btnClear,btnHangup;
    private ImageView btnLeft,iv_mainVdBack;
    private DrawerLayout main_drawer;
    private MyHandle mHandle;
    private String serverPath;
    private DrawingView drawingView;

    /**
     * Handle(静态内部类)
     */
    private static class MyHandle extends Handler {
        private final WeakReference<MainActivity> mActivty;
        public MyHandle(MainActivity activity) {
            mActivty = new WeakReference<MainActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivty.get();
            super.handleMessage(msg);
            if (activity != null) {
                switch (msg.what) {
                    case 1001:
                        break;
                }
            }
        }
    }

    /**
     * 获取布局ID
     * @return
     */
    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    /**
     * 控件初始化
     */
    @Override
    protected void initView() {
        main_drawerLayout = findViewById(R.id.main_drawerLayout);
        main_drawer=findViewById(R.id.main_drawer);
        main_drawer.addDrawerListener(drawerListener);
        iv_mainVdBack=findViewById(R.id.main_page_imgVidback);
        iv_mainVdBack.setOnClickListener(this);
        ijkLive=findViewById(R.id.main_page_ijkLive);
        btnCloor=findViewById(R.id.main_page_btnCloor);
        btnCloor.setOnClickListener(this);
        btnTag=findViewById(R.id.main_page_btnTag);
        btnTag.setOnClickListener(this);
        btnSend=findViewById(R.id.main_page_btnSend);
        btnSend.setOnClickListener(this);
        btnClear=findViewById(R.id.main_page_btnClear);
        btnClear.setOnClickListener(this);
        btnHangup=findViewById(R.id.main_drawer_btnHangup);
        btnHangup.setOnClickListener(this);
        btnLeft=findViewById(R.id.main_btnLeft);
        btnLeft.setOnClickListener(this);
        drawingView=findViewById(R.id.main_page_drawView);
    }

    /**
     * 数据初始化
     */
    @Override
    protected void initData() {
        mHandle = new MyHandle(this);
        Intent intent = getIntent();
        serverPath = intent.getStringExtra("PATH");
    }

    /**
     * 事件处理
     */
    @Override
    protected void doEvent() {
        setBackToDrawer();
        btnClear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clearAllPath();
                Toast.makeText(MainActivity.this, "长按清除全部", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    /**
     * View点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_btnLeft:
                //按钮拉出抽屉
                main_drawer.openDrawer(Gravity.LEFT);
                break;
            case R.id.main_page_imgVidback:
                method_play(VideoConfig.SERVER_URL);
                break;
            case R.id.main_page_btnCloor:
                //弹出颜色框
                showPopForSelectColor();
                break;
            case R.id.main_page_btnTag:
                //选择图标
                showPopForSelectTag();
                break;
            case R.id.main_page_btnClear:
                drawingView.dismissStep();
                break;
            case R.id.main_page_btnSend:
                break;
            case R.id.main_drawer_btnHangup:
                finish();
                break;
        }
    }

    /**
     * 播放视频
     * @param path
     */
    private void method_play(String path) {
        ijkLive.setVideoPath(serverPath);
        iv_mainVdBack.setVisibility(View.GONE);
    }

    /**
     * 清空画布
     */
    private void clearAllPath() {
        drawingView.clearCanvas();
    }

    /**
     * 选择画笔样式（圆，曲线，标记）
     */
    private void showPopForSelectTag() {
        int[] location = new int[2];
        btnTag.getLocationOnScreen(location);
        final PopupWindow popupWindow = new PopupWindow();
        View popView = View.inflate(getBaseContext(), R.layout.pop_layout_tag, null);
        popupWindow.setContentView(popView);
        popupWindow.setHeight(150);
        popupWindow.setWidth(400);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(btnTag,Gravity.NO_GRAVITY, location[0]-btnTag.getWidth()-350,location[1]);

        //箭头标记
        popView.findViewById(R.id.pop_btn_arrows).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.setModeForPaint(1004);
                popupWindow.dismiss();
            }
        });
        //长方形
        popView.findViewById(R.id.pop_btn_reactange).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.setModeForPaint(1003);
                popupWindow.dismiss();
            }
        });
        //圆形
        popView.findViewById(R.id.pop_btn_circle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.setModeForPaint(1002);
                popupWindow.dismiss();
            }
        });
        //曲线
        popView.findViewById(R.id.pop_btn_curve).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.setModeForPaint(1001);
                popupWindow.dismiss();
            }
        });
    }

    /**
     * 弹出选择颜色的窗口
     */
    private void showPopForSelectColor() {
        int[] location = new int[2];
        btnCloor.getLocationOnScreen(location);
        final PopupWindow popupWindow = new PopupWindow();
        View popView = View.inflate(getBaseContext(), R.layout.pop_layout_color, null);
        popupWindow.setContentView(popView);
        popupWindow.setHeight(150);
        popupWindow.setWidth(400);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(btnCloor,Gravity.NO_GRAVITY, location[0]-btnCloor.getWidth()-350,location[1]);
        //画笔设置为红色
        popView.findViewById(R.id.pop_btn_red).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.selectColorForPaint(Color.RED);
                popupWindow.dismiss();
            }
        });
        //画笔设置为绿色
        popView.findViewById(R.id.pop_btn_black).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.selectColorForPaint(Color.BLACK);
                popupWindow.dismiss();
            }
        });
        //画笔设置为黄色
        popView.findViewById(R.id.pop_btn_yellow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.selectColorForPaint(Color.YELLOW);
                popupWindow.dismiss();
            }
        });
        //画笔设置为蓝色
        popView.findViewById(R.id.pop_btn_blue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.selectColorForPaint(Color.BLUE);
                popupWindow.dismiss();
            }
        });
    }

    /**
     * 设置抽屉透明度
     */
    private void setBackToDrawer() {
        main_drawer.setScrimColor(Color.TRANSPARENT);
        main_drawerLayout.getBackground().setAlpha(100);
    }

    /**
     * 抽屉状态监听
     */
    DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(View drawerView) {
            //打开侧滑界面触发
            btnLeft.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            //关闭侧滑界面触发
            btnLeft.setVisibility(View.VISIBLE);
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            //状态改变时触发
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
