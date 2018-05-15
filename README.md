# Protable-AR-Sandbox

This project try to lower down the cost of the development and make the whole device more portable and affordable for public users and education institutions.

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

The main scripts are inside app/java/com/projecttango/examples/java/point cloud.

1. PointCloud.java 
- Using color to indicate the depth distance. Linear interpolation for x-axis. Data size control. 

2. Points.java 
- A Point primitive for Rajawali.

3. FrustumAxes.java

4. Grid.java

5. PointCloudActivity.java
- Main Activity of application to connect Tango service and collect the point cloud data to OPEN GL. Setup RajawaliRednerer.

6. PointCloudRajawaliRenderer.java 
- rendering point cloud data

7. TouchViewHandler.java 
- Handle the event which including first person view button, top-up view button and zoom in/out event.


