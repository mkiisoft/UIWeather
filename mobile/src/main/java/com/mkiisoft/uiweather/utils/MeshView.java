package com.mkiisoft.uiweather.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.mkiisoft.uiweather.R;

public class MeshView extends View {

    private Bitmap bitmap;

    private final int MESH_WIDTH = 20;

    private final int MESH_HEIGHT = 20;

    private final int COUNT = (MESH_WIDTH + 1) * (MESH_HEIGHT + 1);

    private float[] verts = new float[COUNT * 2];

    private float[] orig = new float[COUNT * 2];

    public MeshView(Context context) {
        this(context, null);
    }

    public MeshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MeshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(true);

        bitmap = ((BitmapDrawable) getResources().getDrawable(
                R.drawable.ic_logo)).getBitmap();

        float bitmapWidth = bitmap.getWidth();
        float bitmapHeight = bitmap.getHeight();
        int index = 0;
        for (int i = 0; i <= MESH_HEIGHT; i++) {
            float fy = bitmapHeight * i / MESH_HEIGHT;
            for (int j = 0; j < MESH_WIDTH; j++) {
                float fx = bitmapWidth * i / MESH_WIDTH;

                orig[index * 2 + 0] = verts[index * 2 + 0] = fx;
                orig[index * 2 + 1] = verts[index * 2 + 1] = fy;
                index += 1;
            }
        }

        setBackgroundColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmapMesh(bitmap, MESH_WIDTH, MESH_HEIGHT, verts, 0,
                null, 0, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        warp(event.getX(), event.getY());
        return true;
    }

    private void warp(float cx, float cy) {
        for (int i = 0; i < MESH_WIDTH * 2; i += 2) {
            float dx = cx - orig[i + 0];
            float dy = cy - orig[i + 1];
            float dd = dx * dx + dy * dy;

            float d = (float) Math.sqrt(dd);

            float pull = 80000 / (dd * d);

            if(pull >= 1){
                verts[i + 0] = cx;
                verts[i + 1] = cy;
            }else{

                verts[i + 0] = orig[i + 0] * dx * pull;
                verts[i + 1] = orig[i + 1] * dy * pull;
            }
        }

        invalidate();
    }

}