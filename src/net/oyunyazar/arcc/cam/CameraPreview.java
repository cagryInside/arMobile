package net.oyunyazar.arcc.cam;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import net.oyunyazar.arcc.data.FrameManager;
import net.oyunyazar.arcc.support.Career;
import net.oyunyazar.arcc.support.LogHelper;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.renderscript.Mesh;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;

import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

// ----------------------------------------------------------------------

public class CameraPreview extends Activity {
	private Preview mPreview;
	Camera mCamera;
	int numberOfCameras;
	int cameraCurrentlyLocked;

	// The first rear facing camera
	int defaultCameraId;


	//Game 
	// Used to handle pause and resume...
	private static CameraPreview master = null;

	private GLSurfaceView mGLView;
	private MyRenderer renderer = null;
	private FrameBuffer fb = null;
	private World world = null;
	private RGBColor back = new RGBColor(0, 0, 0, 0);

	private float touchTurn = 0;
	private float touchTurnUp = 0;

	private float xpos = -1;
	private float ypos = -1;

	private Object3D cube = null;
	private Object3D trex = null;
	private int fps = 0;

	private Light sun = null;

	private SimpleVector ARCamParams;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (master != null) {
			copy(master);
		}

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Create a RelativeLayout container that will hold a SurfaceView,
		// and set it as the content of our activity.
		mPreview = new Preview(this);
		setContentView(mPreview);

		// Find the total number of cameras available
		numberOfCameras = Camera.getNumberOfCameras();

		// Find the ID of the default camera
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < numberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				defaultCameraId = i;
			}
		}


		//GAME
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		mGLView = new GLSurfaceView(getApplication());
		new Thread(new Runnable() {
			public void run() {

				mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
				renderer = new MyRenderer();
				mGLView.setRenderer(renderer);
				mGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
				addContentView(mGLView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

			}
		}).start();
		
		//Draw game engine 
		//		DrawGame drawGame = new DrawGame(this);
		//		addContentView(drawGame, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)); 
	}

	public boolean onTouchEvent(MotionEvent me) {

		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			xpos = me.getX();
			ypos = me.getY();
			return true;
		}

		if (me.getAction() == MotionEvent.ACTION_UP) {
			xpos = -1;
			ypos = -1;
			touchTurn = 0;
			touchTurnUp = 0;
			return true;
		}

		if (me.getAction() == MotionEvent.ACTION_MOVE) {
			float xd = me.getX() - xpos;
			float yd = me.getY() - ypos;

			xpos = me.getX();
			ypos = me.getY();

			touchTurn = xd / -100f;
			touchTurnUp = yd / -100f;
			return true;
		}

		//		try {
		//			Thread.sleep(15);
		//		} catch (Exception e) {
		//			// No need for this...
		//		}

		return super.onTouchEvent(me);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Open the default i.e. the first rear facing camera.
		mCamera = Camera.open();
		cameraCurrentlyLocked = defaultCameraId;
		mPreview.setCamera(mCamera);
		mGLView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Because the Camera object is a shared resource, it's very
		// important to release it when the activity is paused.
		if (mCamera != null) {
			mPreview.setCamera(null);
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}else {
			mCamera = Camera.open((cameraCurrentlyLocked + 1) % numberOfCameras);
			//cameraCurrentlyLocked = (cameraCurrentlyLocked + 1)    			% numberOfCameras;
			mPreview.switchCamera(mCamera);

			// Start the preview
			mCamera.startPreview();
			mGLView.onPause();
		}
	}

	//gaME
	class MyRenderer implements GLSurfaceView.Renderer {

		private long time = System.currentTimeMillis();

		public MyRenderer() {
		}

		public void onSurfaceChanged(GL10 gl, int w, int h) {
			if (fb != null) {
				fb.dispose();
			}
			fb = new FrameBuffer(gl, w, h);
			if (master == null) {

				world = new World();
				world.setAmbientLight(20, 20, 20);

				sun = new Light(world);
				sun.setIntensity(250, 250, 250);

				// Create a texture out of the icon...:-)
				Texture texture = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(net.oyunyazar.arcc.R.drawable.expo_stones2)), 64, 64));
				TextureManager.getInstance().addTexture("texture", texture);

				cube = Primitives.getCube(5.0f);
				cube.calcTextureWrapSpherical();
				cube.setTexture("texture");
				cube.strip();
				cube.build();

//				cube.rotateX(2.00f);
				
				world.addObject(cube);
				

				com.threed.jpct.Camera cam = world.getCamera();
				cam.moveCamera(com.threed.jpct.Camera.CAMERA_MOVEOUT, 50);
				cam.lookAt(cube.getTransformedCenter());

				SimpleVector sv = new SimpleVector();
				sv.set(cube.getTransformedCenter());
				sv.y -= 100;
				sv.z -= 100;
				sun.setPosition(sv);
				MemoryHelper.compact();

				if (master == null) {
					Logger.log("Saving master Activity!");
					master = CameraPreview.this;
				}
			}
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		}

		//Main Game Loop
		public void onDrawFrame(GL10 gl) {
			if (touchTurn != 0) {
				cube.rotateY(touchTurn);
				touchTurn ++;
			}

			//			if (touchTurnUp != 0) {
			//				cube.rotateX(touchTurnUp);
			//				touchTurnUp = 0;
			//			}
			fb.clear(back);
			//			fb.dispose();
			world.renderScene(fb);
			world.draw(fb);
			fb.display();

//			world.getCamera().setPosition(0, 0, -20);
//			cube.rotateZ(0.01f);
//			cube.rotateX(0.02f);
//			cube.translate(0, 0.1f, 0);
//			cube.rotateY(0.02f);
			
			Career career = Career.getInstance();
			world.getCamera().setPosition(career.centerX, career.centerY, -50);
//			cube.rotateZ(career.getAngel());
//			cube.scale(career.getScaleParam());
			cube.setScale(career.getScaleParam());
			
//			SimpleVector bla = new SimpleVector( (20 - world.getCamera().getXAxis().x)*0.001f ,(20 - world.getCamera().getYAxis().y)*0.001f , -50);
//			world.getCamera().setPosition(bla);
//			world.getCamera().moveCamera(new SimpleVector( 4 - world.getCamera().getXAxis().x ,4 - world.getCamera().getYAxis().y , 0), 0.02f);
			
//			world.getCamera().rotateCameraY(0.002f);
//			world.getCamera().rotateCameraX(0.002f);
//			world.getCamera().rotateCameraZ(0.002f);
			
			if (System.currentTimeMillis() - time >= 1000) {
				Logger.log(fps + "fps");
				fps = 0;
				time = System.currentTimeMillis();
			}
			fps++;
		}
	}
	
	private void copy(Object src) {
		try {
			Logger.log("Copying data from master Activity!");
			Field[] fs = src.getClass().getDeclaredFields();
			for (Field f : fs) {
				f.setAccessible(true);
				f.set(this, f.get(src));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


}


// ----------------------------------------------------------------------

/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered preview of the Camera
 * to the surface. We need to center the SurfaceView because not all devices have cameras that
 * support preview sizes at the same aspect ratio as the device's display.
 */
class Preview extends ViewGroup implements SurfaceHolder.Callback {
	private final String TAG = "Preview";
	FrameManager frameManager = FrameManager.getInstance();

	SurfaceView mSurfaceView;
	SurfaceHolder mHolder;
	Size mPreviewSize;
	List<Size> mSupportedPreviewSizes;
	Camera mCamera;
	Camera.Parameters params;

	Preview(Context context) {
		super(context);

		mSurfaceView = new SurfaceView(context);
		addView(mSurfaceView);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void setCamera(Camera camera) {
		mCamera = camera;
		if (mCamera != null) {
			mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
			requestLayout();
		}
	}

	public void switchCamera(Camera camera) {
		setCamera(camera);
		try {
			camera.setPreviewDisplay(mHolder);
		} catch (IOException exception) {
			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		}
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		requestLayout();

		camera.setParameters(parameters);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// We purposely disregard child measurements because act as a
		// wrapper to a SurfaceView that centers the camera preview instead
		// of stretching it.
		final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
		setMeasuredDimension(width, height);

		if (mSupportedPreviewSizes != null) {
			mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
		}
	}


	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed && getChildCount() > 0) {
			final View child = getChildAt(0);

			final int width = r - l;
			final int height = b - t;

			int previewWidth = width;
			int previewHeight = height;
			if (mPreviewSize != null) {
				previewWidth = mPreviewSize.width;
				previewHeight = mPreviewSize.height;
			}

			// Center the child SurfaceView within the parent.
			if (width * previewHeight > height * previewWidth) {
				final int scaledChildWidth = previewWidth * height / previewHeight;
				child.layout((width - scaledChildWidth) / 2, 0,
						(width + scaledChildWidth) / 2, height);
			} else {
				final int scaledChildHeight = previewHeight * width / previewWidth;
				child.layout(0, (height - scaledChildHeight) / 2,
						width, (height + scaledChildHeight) / 2);
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		try {
			if (mCamera != null) {
				mCamera.setPreviewDisplay(holder);
				
				//Getting camera preview data
				mCamera.setPreviewCallback(new PreviewCallback() {
					@Override
					public void onPreviewFrame(byte[] data, Camera camera) {
						// TODO Auto-generated method stub
						//Log.v("image format", params.getSupportedPreviewSizes().toString());
						//Frame captureing via frameManager
//						frameManager.initCamFrame(1280, 720,data);
						params = camera.getParameters();
						LogHelper.printInfo("cam");
						frameManager.initCamFrame(params.getPreviewSize().width, params.getPreviewSize().height,data);
						
						
					}
				});

			}
		} catch (IOException exception) {
			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		Log.v("destroyed", "1");
		if (mCamera != null) {
			Log.v("destroyed", "1");
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
		}
	}


	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null) return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		requestLayout();	

		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}
}