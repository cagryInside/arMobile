package net.oyunyazar.arcc.data;

import net.oyunyazar.arcc.support.Career;
import android.util.Log;



public class FrameManager extends Thread{
	
	private static FrameManager frameManager = null;
	private CamFrame camFrame;
	
	//declare native function
//	private native float[] processImage(int width, int height, byte[] yuvData);
	private native float[] processImage(int width, int height, byte[] yuvData);
	
	private FrameManager() {
		
	}
	
	static {
		System.loadLibrary("arModule");
	}
	
	static public FrameManager getInstance(){
		
		if (frameManager == null) {
			
			frameManager = new FrameManager();
		}
		return frameManager;
	}
	
	public void initCamFrame(int width, int height, byte[] data){
		
		camFrame = new CamFrame(width, height, data);
		this.run();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		float[] convertedData;
		convertedData = processImage( camFrame.getFrameWidth(),camFrame.getFrameHeight(), camFrame.getYUVData());
//		
		Career career = Career.getInstance();
		career.centerX = convertedData[0];
		career.centerY = convertedData[1];
		career.setAngle(convertedData[2]);
//		career.fall = convertedData[3];
//		career.setOldScaleParam(convertedData[4]);
		career.setScaleParam(convertedData[4]);
		
		Log.v("recsize ", String.valueOf(convertedData[4]));
		//Log.v("y ", String.valueOf(convertedData[1]));
		
		//X1Y2 , X2Y2 .......
//		for (int i = 0; i < convertedData.length; i++) {
//			career.coordinate[i] = (int)convertedData[i+2];
//		}
		
//		int minX = career.coordinate[0];
//		int minY = career.coordinate[1];
//		int maxX = career.coordinate[0];
//		int maxY = career.coordinate[1];
//		
//		int minXIndex, minYIndex, maxXIndex, maxYIndex;
//		
//		for (int i = 0; i < convertedData.length; i++) {
//			if(minX > career.coordinate[i]){
//				career.coordinate[minXIndex] = career.coordinate[i];
//				minXIndex = i;
//			}
//			
//		}

//		
//		Log.v("x coord ", String.valueOf(convertedData[2]));
//		Log.v("y coord", String.valueOf(convertedData[3]));
//		camFrame.setConvertedData(processImage( camFrame.getFrameWidth(),camFrame.getFrameHeight(), camFrame.getYUVData()));
//		boolean ss = false;
////		if(ss){
//		camFrame.writePGM();
//		camFrame.notify();
////		}
	}

}
