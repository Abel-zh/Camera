package com.example.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class CameraView extends SurfaceView implements Callback, Camera.PictureCallback {

	private SurfaceHolder holder;
	private Camera camera;
	private Camera.Parameters parameters;
	private Activity act;
	private Handler handler = new Handler();
	private Context context;
	private SurfaceView surfaceView;
	private AudioManager audio;
	private int current;
	private boolean isEnd = false;

	public CameraView(Context context) {
		super(context);
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CameraView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CameraView(Context context, Activity act) {// 在此定义一个构造方法用于拍照过后把CameraActivity给finish掉
		this(context);
		this.act = act;
		surfaceView = this;
		audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		current = audio.getRingerMode();
		audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		this.context = context;
		holder = getHolder();// 生成Surface Holder
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 指定Push Buffer
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// camera.takePicture(null, null, CameraView.this);
				if (camera == null) {
					surfaceCreated(holder);
					// 由于启动camera需要时间，在此让其等两秒再进行聚焦直到camera不为空
					handler.postDelayed(this, 1 * 1000);
				} else {
					if (!isEnd) {
						camera.autoFocus(null);
						camera.takePicture(new ShutterCallback() {

							@Override
							public void onShutter() {

							}
						}, null, CameraView.this);
					}
					// camera.autoFocus(new AutoFocusCallback() {
					//
					// @Override
					// public void onAutoFocus(boolean success, Camera camera) {
					// if (success) {
					// camera.takePicture(null, null, CameraView.this);
					// }
					// }
					// });
				}
			}
		}, 2 * 1000);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		parameters = camera.getParameters();
		// parameters.setPreviewSize(width, height);
		camera.setParameters(parameters);// 设置参数
		camera.startPreview();// 开始预览
	}

	@Override
	public void surfaceCreated(final SurfaceHolder holder) {
		// for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
		// CameraInfo info = new CameraInfo();
		// Camera.getCameraInfo(i, info);
		// if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {// 这就是前置摄像头，亲。
		// camera = Camera.open(i);
		// }
		// }
		//
		// if (camera == null) {
		// camera = Camera.open();
		// }

		int cameraCount = 0;

		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras(); // get cameras number

		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				camera = Camera.open(camIdx);
			}
//			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) { // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
//				try {
//					camera = Camera.open(camIdx);
//				} catch (RuntimeException e) {
//					e.printStackTrace();
//				}
//			}
		}

		// try {
		// camera = Camera.open();
		// camera.setPreviewDisplay(holder);
		// } catch (Exception e) {
		// e.printStackTrace();
		// camera = null;
		// }

		// handler.postDelayed(new Runnable() {
		//
		// @Override
		// public void run() {
		// if (holder != null) {
		// try {
		// camera.setPreviewDisplay(holder);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// }
		// }, 2 * 1000);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.setPreviewCallback(null);
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		try {

			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
			String time = format.format(date);

			// 在SD卡上创建文件夹
			File file = new File(Environment.getExternalStorageDirectory() + "/myCamera/pic");
			if (!file.exists()) {
				file.mkdirs();
			}

			String path = Environment.getExternalStorageDirectory() + "/myCamera/pic/" + time
					+ ".jpg";
			data2file(data, path);
			isEnd = true;
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
			holder.removeCallback(CameraView.this);
			audio.setRingerMode(current);
			act.finish();
		} catch (Exception e) {

		}
	}

	private void data2file(byte[] w, String fileName) throws Exception {// 将二进制数据转换为文件的函数
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(fileName);
			out.write(w);
			out.close();
		} catch (Exception e) {
			if (out != null)
				out.close();
			throw e;
		}
	}

}
