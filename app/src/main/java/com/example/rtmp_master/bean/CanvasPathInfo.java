package com.example.rtmp_master.bean;
import android.graphics.Path;

/**
 * @author rjs
 * @package com.example.rtmp_master
 * @date 2020/7/29
 * @desc
 */
public class CanvasPathInfo {

    private String id;
    public Path path;
    private int paintColor;

    public CanvasPathInfo(String id, Path path, int paintColor) {
        this.id = id;
        this.path = path;
        this.paintColor = paintColor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public int getPaintColor() {
        return paintColor;
    }

    public void setPaintColor(int paintColor) {
        this.paintColor = paintColor;
    }
}
