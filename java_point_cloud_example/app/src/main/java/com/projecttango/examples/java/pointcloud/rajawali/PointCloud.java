/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.projecttango.examples.java.pointcloud.rajawali;

import android.graphics.Color;
import android.util.Log;

import org.rajawali3d.materials.Material;

import java.nio.FloatBuffer;
import java.text.DecimalFormat;

/**
 * Renders a point cloud using colors to indicate distance to the depth sensor.
 * Coloring is based on the light spectrum: closest points are in red, farthest in violet.
 */
public class PointCloud extends Points {
    // Maximum depth range used to calculate coloring (min = 0).
    public static final double CLOUD_MAX_Z = 1;

    private float[] mColorArray;
    private final int[] mPalette;
    public static final int PALETTE_SIZE = 360;
    public static final float HUE_BEGIN = 0;
    public static final float HUE_END = 320;

    public static final double MIN_DEPTH = 0.31f;
    public static final double MAX_DEPTH = 0.50f;

    public PointCloud(int maxPoints, int floatsPerPoint) {
        super(maxPoints, floatsPerPoint, true);
        Log.d("MYINT", "value: " + maxPoints + "," + floatsPerPoint); //maxPoints = 60000, floatsPerPoint = 4;
        mPalette = createPalette();
        mColorArray = new float[maxPoints * 4]; // set array size to maxPoints
        Material m = new Material();
        m.useVertexColors(true);
        setMaterial(m);
    }

    /**
     * Update the points and colors in the point cloud.
     */
    // pointCount = pointCloudData.numPoints, is the total # of points read by the camera
    // pointBuffer = pointCloudData.points
    public void updateCloud(int pointCount, FloatBuffer pointBuffer, double avgDepth) { 
        calculateColors(pointCount, pointBuffer, avgDepth);
        // Log.d("FLTBUF","FLTBUF: "+pointBuffer+" , ptCount: "+pointCount);
        updatePoints(pointCount, pointBuffer, mColorArray);
    }

    /**
     * Pre-calculate a palette to be used to translate between point distance and RGB color.
     */
    private int[] createPalette() {
        int[] palette = new int[PALETTE_SIZE];
        float[] hsv = new float[3];
        hsv[1] = hsv[2] = 1;
        for (int i = 0; i < PALETTE_SIZE; i++) {
            hsv[0] = (HUE_END - HUE_BEGIN) * i / PALETTE_SIZE + HUE_BEGIN;
            palette[i] = Color.HSVToColor(hsv);
        }
        return palette;
    }

    /**
     * Calculate the right color for each point in the point cloud.
     */
    private void calculateColors(int pointCount, FloatBuffer pointCloudBuffer, double avgDepth) {
        float[] points = new float[pointCount * 4];
        pointCloudBuffer.rewind(); //set the position zero and content not change.
        pointCloudBuffer.get(points);
        pointCloudBuffer.rewind();

        int color;
        int colorIndex;
        float z;

        for (int i = 0; i < pointCount; i++) {
            z = points[i * mFloatsPerPoint + 2]; // axis-z is at every 3rd place
            colorIndex = (int) Math.min(z / CLOUD_MAX_Z * mPalette.length, mPalette.length - 1);
            colorIndex = Math.max(colorIndex, 0);
            color = mPalette[colorIndex];

            // int param = (360 / (MAX_DEPTH - MIN_DEPTH));
            if (z > MIN_DEPTH && z <= MAX_DEPTH){ // if the depth is inside the range.
               int depth = (int)((z - MIN_DEPTH) * 1894);
               try{
                   color  = mPalette[depth];
               }catch (Exception e){
                   Log.d("Teest", ""+ depth +":"+z);
               }
            }else if (z <= 0.21f){ // if the depth distance is between camera and MIN
                // color = 0xFFFFFFFF;
                color = 0xFF000000;
            }else{ // if the depth distance is below MAX
                color = 0xFF000000;
            }

            mColorArray[i * 4] = Color.red(color) / 255f;
            mColorArray[i * 4 + 1] = Color.green(color) / 255f;
            mColorArray[i * 4 + 2] = Color.blue(color) / 255f;
            mColorArray[i * 4 + 3] = Color.alpha(color) / 255f;


        }
        // System.err.println("MAX: " + max_depth);
        // System.err.println("Depth->"+(Color.red(-10))+","+(Color.green(-40))+","+(Color.blue(-10))+","+(Color.alpha(-500))); 
    }
}
