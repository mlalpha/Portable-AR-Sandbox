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
import java.util.ArrayList;

/**
 * Renders a point cloud using colors to indicate distance to the depth sensor.
 * Coloring is based on the light spectrum: closest points are in red, farthest in violet.
 */
public class PointCloud extends Points {
    // Maximum depth range used to calculate coloring (min = 0).
    public static final double CLOUD_MAX_Z = 1;

    private static final float APPROX_ERROR = 0.05f; // Approximation error of any data, i.e. error = (+/-) 5%

    private float[] mColorArray;
    private final int[] mPalette;
    public static final int PALETTE_SIZE = 15;
    public static final float HUE_BEGIN = 0;
    public static final float HUE_END = 320;

    public static final double MIN_DEPTH = 0.30f;
    public static final double MAX_DEPTH = 0.50f;

    private int arraySize = 0;
    public static final int PRODUCT_RATIO = (int) (PALETTE_SIZE / (MAX_DEPTH - MIN_DEPTH));

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
        // FloatBuffter cropData = cropPointsIntoRectangle(pointCount, pointBuffer);
        float[] cropPoints = cropPointsIntoRectangle(pointCount, pointBuffer);
        cropPoints = duplicatePoints(cropPoints);
        calculateColors(arraySize, cropPoints, avgDepth);
        // Log.d("FLTBUF","FLTBUF: "+pointBuffer+" , ptCount: "+pointCount);
        updatePoints(arraySize, cropPoints, mColorArray);
    }

    // Linear interpolation for x-axis, constant interpolation for y-axis.
    public float[] duplicatePoints(float[] pointBuffer){
        ArrayList<Float> dupPoints = new ArrayList<Float>();
//        ArrayList<Float> rowPoints = new ArrayList<Float>();
        float x1, x2, y1, y2, z1, z2, c1, c2;
        int point_num = arraySize - 1;
//        int increase_size = 0, size=0;
        for (int i = 0; i < point_num; i++) {
            // comparing every 2-tuple dots per "row"
            x1 = pointBuffer[i * mFloatsPerPoint]; // rightmost x (smaller value)
            y1 = pointBuffer[i * mFloatsPerPoint + 1]; // y of rightmost x (smaller value)
            z1 = pointBuffer[i * mFloatsPerPoint + 2]; // y of rightmost x (smaller value)
            c1 = pointBuffer[i * mFloatsPerPoint + 3]; // y of rightmost x (smaller value)

            x2 = pointBuffer[(i+1) * mFloatsPerPoint]; // leftmost x (larger value)
            y2 = pointBuffer[(i+1) * mFloatsPerPoint + 1]; // y of leftmost x (larger value)
            z2 = pointBuffer[(i+1) * mFloatsPerPoint + 2]; // y of leftmost x (larger value)
            c2 = pointBuffer[(i+1) * mFloatsPerPoint + 3]; // y of leftmost x (larger value)

            dupPoints.add(x1);
            dupPoints.add(y1);
            dupPoints.add(z1);
            dupPoints.add(c1);

            if (x1 < x2){ // if x1 is on the right side of x2
                dupPoints.add((x1 + x2)/2); // linear interpolation
                dupPoints.add((y1 + y2)/2);
                dupPoints.add((z1 + z2)/2);
                dupPoints.add((c1 + c2)/2);

//                arraySize = arraySize + 1;

            }else{ // push and duplicate row

            }

            if(i == point_num - 1){
                dupPoints.add(x2);
                dupPoints.add(y2);
                dupPoints.add(z2);
                dupPoints.add(c2);
//                Log.d("LAST DATA ","RUN ONCE");
            }
        }

        // convert arraylist to float array
        float[] output = new float[dupPoints.size()];
        int i = 0;
        for (Float f : dupPoints) {
            output[i++] = (f != null ? f : Float.NaN);
        }
        arraySize = dupPoints.size()/4;
//        Log.d("DUP_SIZE ",""+arraySize+", "+output.length);


        return output;
    }

    public float[] cropPointsIntoRectangle(int pointCount, FloatBuffer pointCloudBuffer){
        float[] points = new float[pointCount * 4];
        ArrayList<Float> cropPoints = new ArrayList<Float>();
        pointCloudBuffer.rewind(); //set the position zero and content not change.
        pointCloudBuffer.get(points);
        pointCloudBuffer.rewind();

        arraySize = 0;
        float x, y, z, c;
        float MAX_APPROX = 1 + APPROX_ERROR;

        for (int i = 0; i < pointCount; i++) {
            x = points[i * mFloatsPerPoint];
            y = points[i * mFloatsPerPoint + 1];
            z = points[i * mFloatsPerPoint + 2]; // axis-z is at every 3rd place
            c = points[i * mFloatsPerPoint + 3];

            if ((x <= (0.1665 - 0.04) * MAX_APPROX) && (x >= (-0.1665 - 0.03) * MAX_APPROX) && (y <= (0.1125 - 0.008) * MAX_APPROX) && (y >= (-0.1125 + 0.008) * MAX_APPROX) ) {
                cropPoints.add(x);
                cropPoints.add(y);
                cropPoints.add(z);
                cropPoints.add(c);
                arraySize = arraySize + 1;
//                Log.d("accepted z: ",""+z);

            }
        }
        // convert arraylist to float array
        float[] output = new float[cropPoints.size()];
        int i = 0;
        for (Float f : cropPoints) {
            output[i++] = (f != null ? f : Float.NaN);
        }
//        Log.d("CROP_SIZE ",""+arraySize+", "+output.length);

        return output;

    }

    /**
     * Pre-calculate a palette to be used to translate between point distance and RGB color.
     */
    private int[] createPalette() {
        int[] palette = new int[PALETTE_SIZE];
        float[] hsv = new float[3];
        /*
        hsv[1] = hsv[2] = 1;
        for (int i = 0; i < PALETTE_SIZE; i++) {
            hsv[0] = (HUE_END - HUE_BEGIN) * i / PALETTE_SIZE + HUE_BEGIN;
            palette[i] = Color.HSVToColor(hsv);
        }*/
        //double[] height = {-40.0, -30.0, -20.0, -12.5, -0.75, -0.25, -0.05, 0.0, 0.25, 2.5, 6.0, 9.0, 14.0, 20.0, 25.0};
        int[] rgb = {
                204,0,0, //dark red
                255,0,0, // red
                255,179,179,
                255,153,51, //orange
                255,204,0,
                255,255,0, //yellow
                0,255,0, // green
                51,204,51, // light green
                102,153,0, // dark green
                0,204,153,
                0,255,255, //light blue
                51,204,204,
                0,153,255, //dark blue
                102,102,255,
                0,0,255}; // blue
        int[] r = new int[15];
        int[] g = new int[15];
        int[] b = new int[15];
        for (int i = 0; i < 15 ; i++){
            r[14-i]=rgb[i*3];
            g[14-i]=rgb[i*3+1];
            b[14-i]=rgb[i*3+2];
        }
        for (int i = 0; i < PALETTE_SIZE; i++) {
            Color.RGBToHSV(r[i], g[i], b[i], hsv);
            //heightColor.put(height[i], hsv);
            palette[PALETTE_SIZE-i-1] = Color.HSVToColor(hsv);
        }
        return palette;
    }

    /**
     * Calculate the right color for each point in the point cloud.
     */
    private void calculateColors(int pointCount, float[] pointCloudBuffer, double avgDepth) {
//        float[] points = new float[pointCount * 4];
//        pointCloudBuffer.rewind(); //set the position zero and content not change.
//        pointCloudBuffer.get(points);
//        pointCloudBuffer.rewind();

        int color = 0;
        int colorIndex;
        float x,y,z,c;
        float avg_x = 0f, avg_y = 0f, avg_z = 0f;
        float MAX_APPROX = 1 + APPROX_ERROR;
//        Log.d("COUNT: ",""+pointCount);
        for (int i = 0; i < pointCount; i++) {
//            Log.d("COUNT: ",""+i);

            x = pointCloudBuffer[i * mFloatsPerPoint];
            y = pointCloudBuffer[i * mFloatsPerPoint + 1];
            z = pointCloudBuffer[i * mFloatsPerPoint + 2]; // axis-z is at every 3rd place
            c = pointCloudBuffer[i * mFloatsPerPoint + 3];

            // avg_x = avg_x + x;
            // avg_y = avg_y + y;
            // avg_z = avg_z + z;

            colorIndex = (int) Math.min(z / CLOUD_MAX_Z * mPalette.length, mPalette.length - 1);
            colorIndex = Math.max(colorIndex, 0);
            color = mPalette[colorIndex];
//            Log.d("Depth data: ",""+z);
            if (z > MIN_DEPTH && z <= MAX_DEPTH) { // if the depth is inside the range.
                int depth = Math.min((int) ((z - MIN_DEPTH) * PRODUCT_RATIO), mPalette.length - 1);
                    color  = mPalette[depth];
            } else if (z <= 0.21f) { // if the depth distance is between camera and MIN
                color = mPalette[0];
            } else { // if the depth distance is below MAX
                color = 0xFF000000;
            }

//            // check the rendering order
//            if (i > 150){
//                colorIndex = (int)360.00 * i / pointCount;
//                color = mPalette[colorIndex];
//            }else{
//                color = mPalette[200];
//            }

            // render inside the rectangle
//            if ((x <= 0.1665 * MAX_APPROX) && (x >= -0.1665 * MAX_APPROX) && (y <= 0.1125 * MAX_APPROX) && (y >= -0.1125 * MAX_APPROX) ){
//                color = mPalette[200];
//            }else{
//                color = 0xFF000000;
//            }
            // if (((x <= 0.001) && (x >= -0.001)) || ((y <= 0.001) && (y >= -0.001)) ) {
            //     color = mPalette[0];
            // }

            mColorArray[i * 4] = Color.red(color) / 255f;
            mColorArray[i * 4 + 1] = Color.green(color) / 255f;
            mColorArray[i * 4 + 2] = Color.blue(color) / 255f;
            mColorArray[i * 4 + 3] = Color.alpha(color) / 255f;

        }
        // float leftx = points[150 * mFloatsPerPoint];
        // float rightx = points[10 * mFloatsPerPoint];
        // avg_x = avg_x * 100 / pointCount;
        // avg_y = avg_y * 100 / pointCount;
        // avg_z = avg_z * 100 / pointCount;

//        Log.d("BUFFER: ","data: "+pointCloudBuffer.get());
//        Log.d("avg:",""+avg_x+" , "+avg_y+" , "+avg_z+", leftx: "+ leftx + " rightx: " + rightx);
//        Log.d("avg:",""+avg_x+" , "+avg_y+" , "+avg_z+", leftx: "+ leftx + " rightx: " + rightx);
        // System.err.println("MAX: " + max_depth);
        // System.err.println("Depth->"+(Color.red(-10))+","+(Color.green(-40))+","+(Color.blue(-10))+","+(Color.alpha(-500))); 
        createContourLine(pointCount);
    }

    private void createContourLine(int pointCount){
        int color;
        for(int i = 0;i < pointCount-1; i++){
            float red1 = mColorArray[i * 4];
            float green1 = mColorArray[i * 4 + 1];
            float blue1 = mColorArray[i * 4 + 2];
            float alpha1 = mColorArray[i * 4 + 3];
            float red2 = mColorArray[(i+1) * 4];
            float green2 = mColorArray[(i+1) * 4 + 1];
            float blue2 = mColorArray[(i+1) * 4 + 2];
            float alpha2 = mColorArray[(i+1) * 4 + 3];
            if(red1 == red2 && green1 == green2 && blue1 == blue2 && alpha1 == alpha2){
                continue;
            }else{
                color = Color.WHITE;
                mColorArray[i * 4] = Color.red(color) / 255f;
                mColorArray[i * 4 + 1] = Color.green(color) / 255f;
                mColorArray[i * 4 + 2] = Color.blue(color) / 255f;
                mColorArray[i * 4 + 3] = Color.alpha(color) / 255f;
            }
        }
    }
}
