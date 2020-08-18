package com.example.rtmp_master.ui;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.example.rtmp_master.bean.CanvasPathInfo;
import com.example.rtmp_master.bean.CanvasInfo;
import com.example.rtmp_master.config.CanvasConfig;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author rjs
 * @package com.example.whiteboard_android_master
 * @date 2020/7/27
 * @desc
 */
public class DrawingView extends View {

    //屏幕宽高
    private int view_width=0;
    private int view_height=0;
    //画布宽高
    private int canvas_width=0;
    private int canvas_height=0;
    Bitmap cacheBitmap=null;
    Canvas cacheCanvas=null;
    //画笔
    private Paint paint;
    private Paint prePaint;
    private int paintWidth;
    //绘制时初始坐标
    private float preX,preY;
    //mode 1001:Ellipse 1002:Freehand 1003:Reactange 1004:Arrow
    private int mPaintMode=1001;
    //记录当前画笔颜色
    private int mPaintColor;
    //记录当前路径
    private Path mPath;
    //绘制箭头的坐标
    private float[] loactions;
    //判断事件是否是在移动中
    private boolean isMoving=false;
    //装点容器
    private ArrayList<String> points;
    //canvas路径信息本地集合
    private ArrayList<CanvasPathInfo> canvasPathlist;
    //canvas传输信息集合
    private ArrayList<CanvasInfo> canvaslist;
    //路径ID
    private String pathId;
    private String jsonString;
    private ExecutorService executorServicePool = Executors.newCachedThreadPool();

   private Handler handler=new Handler(){
       @SuppressLint("HandlerLeak")
       @Override
       public void handleMessage(Message msg) {
           super.handleMessage(msg);
           switch (msg.what){
               case 1001:
                   Toast.makeText(getContext(), "撤销一步", Toast.LENGTH_SHORT).show();
                   SingleDisResponse();
                   break;
               case 1002:
                   Toast.makeText(getContext(), "撤销全部", Toast.LENGTH_SHORT).show();
                   AllDisResponse();
                   break;
           }

       }
   };

    /**
     * View初始化
     * @param context
     */
    public DrawingView(Context context) {
        super(context);
        init();
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 控件初始化
     */
    private void init() {
        setLayerType(LAYER_TYPE_HARDWARE,null);
        view_width=getContext().getResources().getDisplayMetrics().widthPixels;
        view_height=getContext().getResources().getDisplayMetrics().heightPixels;
        cacheBitmap= Bitmap.createBitmap(view_width,view_height, Bitmap.Config.ARGB_8888);
        cacheCanvas = new Canvas();
        cacheCanvas.setBitmap(cacheBitmap);
        canvaslist=new ArrayList<>();
        canvasPathlist=new ArrayList<>();
        initPaint(Color.RED);
    }

    /**
     * 初始化画笔
     * @param color
     */
    private void initPaint(int color){
        mPaintColor=color;
        paint = new Paint(Paint.DITHER_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(3);
        paintWidth=3;
        paint.setAntiAlias(true);
        paint.setDither(true);
    }

    /**
     * 测量画布
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if(widthMode==MeasureSpec.AT_MOST&&heightMode==MeasureSpec.AT_MOST){

        }else if(widthMode==MeasureSpec.EXACTLY&&heightMode==MeasureSpec.EXACTLY){
            canvas_height=heightSize;
            canvas_width=widthSize;
        }else if(widthMode==MeasureSpec.UNSPECIFIED&&heightMode==MeasureSpec.UNSPECIFIED){

        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Canvas绘制
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//绘制透明色
        canvas.drawBitmap(cacheBitmap,0,0,paint);
        if(mPath!=null){
            canvas.drawPath(mPath,paint);
        }
    }

    /**
     * Canvas触摸事件
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if(isMoving==false){
            points = new ArrayList();
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                event_down(x,y);
                break;
            case MotionEvent.ACTION_MOVE:
                event_move(x,y);
                break;
            case MotionEvent.ACTION_UP:
                event_up();
                break;
        }
        return true;
    }

    /**
     * DOWN按下事件
     * @param x
     * @param y
     */
    private void event_down(float x, float y) {
        isMoving=false;
        mPath=new Path();
        mPath.moveTo(x,y);
        preX=x;
        preY=y;
        invalidate();
    }

    /**
     * MOVE拖动事件
     * @param x
     * @param y
     */
    private void event_move(float x, float y) {
        isMoving=true;
        float dx= Math.abs(x-preX);
        float dy= Math.abs(y-preY);
        if(dx>5||dy>5){
            switch (mPaintMode){
                case 1001:
                    //曲线
                    mPath.quadTo(preX,preY,(x+preX)/2,(y+preY)/2);
                    points.add(preX/canvas_width+","+preY/canvas_height);
                    preX=x;
                    preY=y;
                    break;
                case 1002:
                    //圆
                    mPath.reset();
                    drawCircle(x,y);
                    break;
                case 1003:
                    //方形
                    mPath.reset();
                    drawReactange(x,y);
                    break;
                case 1004:
                    //标记
                    mPath.reset();
                    drawArrow((int) preX, (int) preY, (int)(x+preX)/2, (int)(y+preY)/2);
                    break;
            }
        }
        invalidate();
    }

    /**
     * UP手势抬起事件
     */
    private void event_up() {
        isMoving=false;
        String currentColor = colorToRGB(mPaintColor);
        String currentShape =null;
        switch (mPaintMode){
            case 1001:
                currentShape=CanvasConfig.FREEHAND;
                break;
            case 1002:
                currentShape=CanvasConfig.ELLIPSE;
                break;
            case 1003:
                currentShape=CanvasConfig.REACTANGE;
                break;
            case 1004:
                currentShape=CanvasConfig.ARROW;
                break;
        }
        if(mPaintMode!=1001){
            points.add(loactions[0]/canvas_width+","+loactions[1]/canvas_height);
            points.add(loactions[2]/canvas_width+","+loactions[3]/canvas_height);
            points.add(loactions[4]/canvas_width+","+loactions[5]/canvas_height);
            points.add(loactions[6]/canvas_width+","+loactions[7]/canvas_height);
        }
        pathId= UUID.randomUUID().toString();
        CanvasInfo canvasInfo = new CanvasInfo(pathId, currentShape,currentColor,paintWidth,points, 1);
        canvaslist.add(canvasInfo);
        sendData(canvasInfo,"Add");
        canvasPathlist.add(new CanvasPathInfo(pathId,mPath,mPaintColor));
        cacheCanvas.drawPath(mPath,paint);
        mPath=null;
        invalidate();
    }

    /**
     * 绘制圆形加坐标点
     * @param x
     * @param y
     */
    private void drawCircle(float x, float y) {
        RectF rectFCicle = new RectF(preX,preY,(x+preX)/2,(y+preY)/2);
        mPath.addOval(rectFCicle, Path.Direction.CCW);
        if((x+preX)/2>preX){
            loactions=new float[]{(x+3*preX)/4,preY,(x+preX)/2,(y+3*preY)/4,((x+3*preX)/4),(y+preY)/2,preX,(y+3*preY)/4};
        }else{
            loactions=new float[]{(x+3*preX)/4,preY,preX,(y+3*preY)/4,((x+3*preX)/4),(y+preY)/2,(x+preX)/2,(y+3*preY)/4};
        }
    }

    /**
     * 绘制矩形路径加坐标点
     * @param x
     * @param y
     */
    private void drawReactange(float x,float y) {
        RectF rectFSquare = new RectF(preX,preY,(x+100),(y+preY)/2);
        mPath.addRect(rectFSquare, Path.Direction.CCW);
        loactions=new float[]{preX,preY,(x+100),preY,(x+100),(y+preY)/2,preX,(y+preY)/2};
    }

    /**
     * 绘制标记箭头路径路径加坐标点
     */
    private float[] drawArrow(int x1, int y1, int x2, int y2) {
        double lineLength = Math.sqrt(Math.pow(Math.abs(x2-x1),2) + Math.pow(Math.abs(y2-y1),2));//线当前长度
        double H = 0;// 箭头高度
        double L = 0;// 箭头长度
        if(lineLength < 320){//防止箭头开始时过大
            H = lineLength/4 ;
            L = lineLength/6;
        }else {
            H = 80;
            L = 50;
        }
        double arrawAngle = Math.atan(L / H); // 箭头角度
        double arraowLen = Math.sqrt(L * L + H * H); // 箭头的长度
        double[] pointXY1 = rotateAndGetPoint(x2 - x1, y2 - y1, arrawAngle, true, arraowLen);
        double[] pointXY2 = rotateAndGetPoint(x2 - x1, y2 - y1, -arrawAngle, true, arraowLen);
        int x3 = (int) (x2 - pointXY1[0]);
        int y3 = (int) (y2 - pointXY1[1]);
        int x4 = (int) (x2 - pointXY2[0]);
        int y4 = (int) (y2 - pointXY2[1]);

        // 画线
        mPath.moveTo(x1,y1);
        mPath.lineTo(x2,y2);
        mPath.moveTo(x3, y3);
        mPath.lineTo(x2, y2);
        mPath.lineTo(x4, y4);
        loactions = new float[]{x2, y2, x3, y3, x4, y4,x1, y1};
        return loactions;
    }

    public double[] rotateAndGetPoint(int x, int y, double ang, boolean isChLen, double newLen)
    {
        double pointXY[] = new double[2];
        double vx = x * Math.cos(ang) - y * Math.sin(ang);
        double vy = x * Math.sin(ang) + y * Math.cos(ang);
        if (isChLen) {
            double d = Math.sqrt(vx * vx + vy * vy);
            pointXY[0] = vx / d * newLen;
            pointXY[1] = vy / d * newLen;
        }
        return pointXY;
    }

    /**
     * 1001:线形
     * 1002：圆形
     * 1003：矩形
     * 1004：箭头标记
     * @param mode
     */
    public void setModeForPaint(int mode){
        mPaintMode=mode;
    }

    /**
     * 选择画笔颜色
     * @param paintColor
     */
    public void selectColorForPaint(int paintColor){
        switch (paintColor){
            case Color.RED:
                mPaintColor=Color.RED;
                initPaint(Color.RED);
                break;
            case Color.BLUE:
                mPaintColor=Color.BLUE;
                initPaint(Color.BLUE);
                break;
            case Color.BLACK:
                mPaintColor=Color.BLACK;
                initPaint(Color.BLACK);
                break;
            case Color.YELLOW:
                mPaintColor=Color.YELLOW;
                initPaint(Color.YELLOW);
                break;
            default:
                break;
        }

    }

    /**
     * 将颜色的Int值转化为RGB
     */
    private String colorToRGB(int color) {
        String stringColor=null;
        switch (color){
            case Color.RED:
                stringColor="#FF0000";
                break;
            case Color.BLUE:
                stringColor="#0000FF";
                break;
            case Color.YELLOW:
                stringColor="#FFFF00";
                break;
            case Color.BLACK:
                stringColor="#008000";
                break;
        }
        return stringColor;
    }

    /**
     * 将实例对象转化为Json串格式
     * @param info
     * @return
     */
    private String getJsonString(CanvasInfo info) {
        String string=null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            StringWriter stringWriter = new StringWriter();
            JsonGenerator jsonGenerator = new JsonFactory().createJsonGenerator(stringWriter);
            objectMapper.writeValue(jsonGenerator,info);
            jsonGenerator.close();
            string=stringWriter.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return string;
    }

    /**
     * 撤销上一步
     */
    public void dismissStep(){
            CanvasInfo canvasInfo = canvaslist.get(canvaslist.size() - 1);
            canvasInfo.setTag(2);
            sendData(canvasInfo,"SingleDis");
    }


    /**
     * 撤销响应完成本地刷新
     */
    public void SingleDisResponse(){
        // 重新设置画布，相当于清空画布
        cacheBitmap = Bitmap.createBitmap(view_width, view_height,
                Bitmap.Config.ARGB_8888);
        cacheCanvas.setBitmap(cacheBitmap);
        canvaslist.remove(canvaslist.size()-1);
        if (canvasPathlist != null && canvasPathlist.size() > 0) {
            canvasPathlist.remove(canvasPathlist.size() - 1);
            Iterator<CanvasPathInfo> iter = canvasPathlist.iterator();
            while (iter.hasNext()) {
                CanvasPathInfo drawInfo = iter.next();
                int paintColor = drawInfo.getPaintColor();
                switch (paintColor){
                    case Color.RED:
                        initPrePaint(Color.RED);
                        break;
                    case Color.BLUE:
                        initPrePaint(Color.BLUE);
                        break;
                    case Color.BLACK:
                        initPrePaint(Color.BLACK);
                        break;
                    case Color.YELLOW:
                        initPrePaint(Color.YELLOW);
                        break;
                }
                cacheCanvas.drawPath(drawInfo.path, prePaint);
            }
            invalidate();
        }
    }

    /**
     * 撤销时需要更换画笔进行绘制
     * @param color
     */
    private void initPrePaint(int color) {
        prePaint=new Paint(Paint.DITHER_FLAG);
        prePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        prePaint.setStyle(Paint.Style.STROKE);
        prePaint.setStrokeJoin(Paint.Join.ROUND);
        prePaint.setStrokeCap(Paint.Cap.ROUND);
        prePaint.setStrokeWidth(3);
        prePaint.setAntiAlias(true);
        prePaint.setDither(true);
        prePaint.setColor(color);
    }

    /**
     * 清空画布
     */
    public void clearCanvas(){
        for(int i=0;i<canvaslist.size();i++){
            CanvasInfo canvasInfo = canvaslist.get(i);
            canvasInfo.setTag(2);
            sendData(canvasInfo,"AllDis");
        }
    }

    /**
     * 清空画布的时候响应
     */
    private void AllDisResponse() {
        cacheBitmap = Bitmap.createBitmap(view_width, view_height,
                Bitmap.Config.ARGB_8888);
        cacheCanvas.setBitmap(cacheBitmap);
        canvaslist.clear();
        if (canvasPathlist != null && canvasPathlist.size() > 0) {
            canvasPathlist.clear();
            Iterator<CanvasPathInfo> iter = canvasPathlist.iterator();
            while (iter.hasNext()) {
                CanvasPathInfo drawInfo = iter.next();
                cacheCanvas.drawPath(drawInfo.path, paint);
            }
            invalidate();
        }
    }

    /**
     * 发送数据
     * @param canvasInfo
     */
    private void sendData(CanvasInfo canvasInfo, final String describe) {
        jsonString = getJsonString(canvasInfo)+"<EOF>";
        Log.e("==",jsonString+"");
        executorServicePool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket=new Socket("", 11000);
                    OutputStream os=socket.getOutputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    startServerReplyListener(reader,describe);
                    os.write(jsonString.getBytes());
                    os.flush();
                    //防止服务端read方法读阻塞
                    os.close();
                    socket.shutdownOutput();
                    socket.close();
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 接收服务器反馈
     * @param reader
     */
    private void startServerReplyListener(final BufferedReader reader, final String describe) {
        executorServicePool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String response;
                    //得到服务端发来的消息
                    while ((response = reader.readLine()) != null) {
                        Message message = new Message();
                        if(describe.equals("SingleDis")){
                            message.what=1001;
                        }else if(describe.equals("AllDis")){
                            message.what=1002;
                        }
                        message.obj=response;
                        handler.sendMessage(message);
                    }
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
