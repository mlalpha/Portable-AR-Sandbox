# Protable-AR-Sandbox

This project try to lower down the cost of the development and make the whole device more portable and affordable for public users and education institutions.

This project take Google Example projects for Project Tango Java API(https://github.com/googlearchive/tango-examples-java) and jloehr Augmented Sandbox with Unity3D and Kinect(https://github.com/jloehr/AR-Sandbox) as references.

The demo video is [here](https://www.youtube.com/watch?v=OLO_ZaWwkNw).

## Getting Started

### Prerequisites


1. Android Studio
2. Projector
3. Sand
4. Holders

### Device requirement

Android smartphone with depth sensing camera
1. ASUS Zenfone AR
2. Lenovo Phab 2 Pro

### Installing

Download the code and import into Android Studio.
And run the code on a smartphone with depth sensing camera.

For the hardware installation, please visit https://www.youtube.com/watch?v=LyA6n_P8ZJI&feature=youtu.be

### Built With

* [Tango](https://developers.google.com/tango/apis/overview) - The augmented reality platform used
* [OpenGL ES 2.0](https://www.khronos.org/opengles/) - Used to generate the graphic by point cloud.

### File description

The main scripts are inside app/java/com/projecttango/examples/java/pointcloud.

1. PointCloud.java - Using color to indicate the depth distance. Linear interpolation for x-axis. Data size control. 

2. Points.java - Passing point cloud data and color array to rendering process. Using for processing the point data such as duplicate, interpolate, crop, points and map the depth valute to color from the customing palette.

3. FrustumAxes.java - Display frame.

4. Grid.java - Display Grid.

5. PointCloudActivity.java - A Main Activity of application to connect Tango service and collect the point cloud data to OPEN GL. Setup RajawaliRednerer. Calculate the average depth distance.

6. PointCloudRajawaliRenderer.java - Get point cloud data and pass it to customized java functions.

7. TouchViewHandler.java - Handle the events which including first person view button, top-up view button and zoom in/out event.


