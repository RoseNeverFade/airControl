package com.demo.aircontrol.util.model;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class MyGLSurfaceView extends GLSurfaceView {
    private final MyGLRenderer renderer;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 24, 0);
        //setEGLConfigChooser(true);


        renderer = new MyGLRenderer();

        // Render the view only when there is a change in the drawing data
        // setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
    }

    public void rotate(double roll, double pitch, double yaw) {
        renderer.rotate(roll, pitch, yaw);
    }

    public MyGLRenderer getModelRenderer() {
        return renderer;
    }

}
