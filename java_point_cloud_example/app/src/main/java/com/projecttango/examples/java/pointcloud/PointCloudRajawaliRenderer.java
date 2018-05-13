/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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
package com.projecttango.examples.java.pointcloud;

import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;

import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.RajawaliRenderer;

import com.projecttango.examples.java.pointcloud.rajawali.FrustumAxes;
import com.projecttango.examples.java.pointcloud.rajawali.Grid;
import com.projecttango.examples.java.pointcloud.rajawali.PointCloud;
import com.projecttango.examples.java.pointcloud.rajawali.ContourLine;

/**
 * Renderer for Point Cloud data.
 */
public class PointCloudRajawaliRenderer extends RajawaliRenderer {

    private static final float CAMERA_NEAR = 0.01f;
    private static final float CAMERA_FAR = 200f;
    private static final int MAX_NUMBER_OF_POINTS = 100000; // can it increase?

    private TouchViewHandler mTouchViewHandler;

    // Objects rendered in the scene.
    private PointCloud mPointCloud;
    private FrustumAxes mFrustumAxes;
    private Grid mGrid;
    private ContourLine mContourLine;
    private double avgDepth;
    private boolean isHidenGrid = true;

    public PointCloudRajawaliRenderer(Context context) {
        super(context);
        mTouchViewHandler = new TouchViewHandler(mContext, getCurrentCamera());
    }

    public void setAvgDepth(Double _avgDepth){
        this.avgDepth = _avgDepth;
    }

    @Override
    protected void initScene() {
        mGrid = new Grid(1, 0.05f, 0.1f, 0xFF888888);
        mGrid.setPosition(0, -1.3f, 0);
//        getCurrentScene().addChild(mGrid);

        mContourLine = new ContourLine(1,0.1f,1, 0xFFCCCCCC);

        mFrustumAxes = new FrustumAxes(3);
        getCurrentScene().addChild(mFrustumAxes);

        // Indicate four floats per point since the point cloud data comes
        // in XYZC format.
        mPointCloud = new PointCloud(MAX_NUMBER_OF_POINTS, 4);
        getCurrentScene().addChild(mPointCloud);

        getCurrentScene().setBackgroundColor(Color.BLACK);
        getCurrentCamera().setNearPlane(CAMERA_NEAR);
        getCurrentCamera().setFarPlane(CAMERA_FAR);
        getCurrentCamera().setFieldOfView(50); // set scale of camera depth
    }
    

    /**
     * Updates the rendered point cloud. For this, we need the point cloud data and the device pose
     * at the time the cloud data was acquired.
     * NOTE: This needs to be called from the OpenGL rendering thread.
     */
    public void updatePointCloud(TangoPointCloudData pointCloudData, float[] openGlTdepth) {
        // mContourLine.updateCloud(pointCloudData.numPoints, pointCloudData.points, avgDepth);
        mPointCloud.updateCloud(pointCloudData.numPoints, pointCloudData.points, avgDepth);
        Matrix4 openGlTdepthMatrix = new Matrix4(openGlTdepth);
        mPointCloud.setPosition(openGlTdepthMatrix.getTranslation());
        // Conjugating the Quaternion is needed because Rajawali uses left-handed convention.
        mPointCloud.setOrientation(new Quaternion().fromMatrix(openGlTdepthMatrix).conjugate());
    }

    /**
     * Updates our information about the current device pose.
     * NOTE: This needs to be called from the OpenGL rendering thread.
     */
    public void updateCameraPose(TangoPoseData cameraPose) {
        float[] rotation = cameraPose.getRotationAsFloats();
        float[] translation = cameraPose.getTranslationAsFloats();
        Quaternion quaternion = new Quaternion(rotation[3], rotation[0], rotation[1], rotation[2]);
        mFrustumAxes.setPosition(translation[0], translation[1], translation[2]);
        // Conjugating the Quaternion is needed because Rajawali uses left-handed convention for
        // quaternions.
        mFrustumAxes.setOrientation(quaternion.conjugate());
        mTouchViewHandler.updateCamera(new Vector3(translation[0], translation[1], translation[2]), quaternion);
    }

    @Override
    public void onOffsetsChanged(float v, float v1, float v2, float v3, int i, int i1) {
    }

    @Override
    public void onTouchEvent(MotionEvent motionEvent) { //scale + change direction
        mTouchViewHandler.onTouchEvent(motionEvent);
    }

    public void setFirstPersonView() { // first-person view
        mTouchViewHandler.setFirstPersonView();
    }

    public void setTopDownView() { // top-view
        mTouchViewHandler.setTopDownView();
    }

    public void setThirdPersonView() { // third-person view
        mTouchViewHandler.setThirdPersonView();
    }

    public void displayGrid(){
        if (isHidenGrid == true){
            getCurrentScene().addChild(mGrid);
            isHidenGrid = false;
        }else{
            getCurrentScene().removeChild(mGrid);
            isHidenGrid = true;
        }
    }
}
