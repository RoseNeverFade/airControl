package com.demo.aircontrol;

import android.content.Context;
import android.opengl.GLSurfaceView;

import java.util.ArrayList;

public class MyGLSurfaceView extends GLSurfaceView {
    public static ObjLoaderUtil.ObjData objData;
    private final MyGLRenderer renderer;
    private ArrayList<ObjLoaderUtil.ObjData> objectList = new ArrayList<ObjLoaderUtil.ObjData>();

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 24, 0);
        //setEGLConfigChooser(true);

        try {
            objectList = ObjLoaderUtil.load("model/untitled.obj", getResources());
            objData = objectList.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }


        renderer = new MyGLRenderer();

        // Render the view only when there is a change in the drawing data
        // setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
    }


}
