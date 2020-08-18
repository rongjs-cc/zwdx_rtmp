package com.example.rtmp_master.bean;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

/**
 * @author rjs
 * @package com.example.rtmp_master.bean
 * @date 2020/8/10
 * @desc
 */
public class CanvasInfo implements Parcelable {

    private String id;
    private String shape;
    private String color;
    private int lineWidth;
    private ArrayList<String> points;
    private int tag;

    public CanvasInfo(String id, String shape, String color, int lineWidth, ArrayList<String> points, int tag) {
        this.id = id;
        this.shape = shape;
        this.color = color;
        this.lineWidth = lineWidth;
        this.points = points;
        this.tag = tag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public ArrayList<String> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<String> points) {
        this.points = points;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public static Creator<CanvasInfo> getCREATOR() {
        return CREATOR;
    }

    protected CanvasInfo(Parcel in) {
        id = in.readString();
        shape = in.readString();
        color = in.readString();
        lineWidth = in.readInt();
        points = in.createStringArrayList();
        tag = in.readInt();
    }

    public static final Creator<CanvasInfo> CREATOR = new Creator<CanvasInfo>() {
        @Override
        public CanvasInfo createFromParcel(Parcel in) {
            return new CanvasInfo(in);
        }

        @Override
        public CanvasInfo[] newArray(int size) {
            return new CanvasInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(shape);
        dest.writeString(color);
        dest.writeInt(lineWidth);
        dest.writeStringList(points);
        dest.writeInt(tag);
    }
}
