package com.example.rtmp_master.util;

import android.util.Log;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author rjs
 * @package com.example.rtmp_master.util
 * @date 2020/7/31
 * @desc
 */
public class RetrofitUtil {

    private static final String TAG="RetrofitUtil";
    private static volatile RetrofitUtil instance;
    private Retrofit mRetrofit;

    public static RetrofitUtil getInstance(){
        if(instance==null){
            synchronized (RetrofitUtil.class){
                if(instance==null){
                    instance=new RetrofitUtil();
                }
            }
        }
        return instance;
    }

    private RetrofitUtil(){
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(createRequestInterceptor())
                .addNetworkInterceptor(createLogInterceptor())
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        mRetrofit=new Retrofit.Builder()
                .baseUrl("")
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /***
     * 创建 Log拦截器
     * @return Log拦截器
     */
    private HttpLoggingInterceptor createLogInterceptor() {
        HttpLoggingInterceptor mLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.e(TAG, message);
            }
        });
        mLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return mLoggingInterceptor;
    }

    /***
     * 自定义拦截器
     * @return
     */
    private Interceptor createRequestInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                //拦截请求
                Request request = chain.request();
                //拦截响应
                Response response = chain.proceed(request);
                //添加头信息
                Request.Builder requestBuilder = request.newBuilder()
                        .addHeader("Content-Type", "application-json")
                        .addHeader("charest", "utf-8");
                //判断响应码
                if (response.code() == 401) {
                    requestBuilder.addHeader("Authorization", "bearer " + requestToken());
                }
                return chain.proceed(requestBuilder.build());
                }
        };
    }

    /***
     * 同步请求 token
     * @return 请求到的token
     */
    private String requestToken() {
        return "";
    }

    /***
     * @Desc 创建API
     * @param clazz
     * @param <T>
     * @return api
     */
    public <T> T create(Class<T> clazz) {
        return mRetrofit.create(clazz);
    }
}
