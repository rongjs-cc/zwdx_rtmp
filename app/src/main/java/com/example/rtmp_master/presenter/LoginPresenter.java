package com.example.rtmp_master.presenter;

import android.os.Handler;
import android.os.Message;
import com.example.rtmp_master.activity.LaunchActivity;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author rjs
 * @package com.example.rtmp_master.presenter
 * @date 2020/8/17
 * @desc
 */
public class LoginPresenter {

    private LaunchActivity launchActivity;
    private ExecutorService executorServicePool = Executors.newCachedThreadPool();
    private MyHandle myHandle;

    /**
     * Handle(静态内部类)
     */
    private static class MyHandle extends Handler {
        private final WeakReference<LaunchActivity> mActivty;
        public MyHandle(LaunchActivity activity) {
            mActivty = new WeakReference<LaunchActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            LaunchActivity activity = mActivty.get();
            super.handleMessage(msg);
            if (activity != null) {
                switch (msg.what) {

                }
            }
        }
    }


    public LoginPresenter(LaunchActivity launchActivity) {
        this.launchActivity = launchActivity;
        myHandle=new MyHandle(launchActivity);
    }

    public void login(String name,String password){
        executorServicePool.execute(new Runnable() {
            @Override
            public void run() {

            }
        });

    }

    public void register(String name,String password){

    }
}
