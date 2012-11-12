package net.oyunyazar.arcc.support;

import android.util.Log;

public class LogHelper {

	private static final String TAG1 = "General1";
	private static final String TAG_INFO = "Info------->";
	private static final String TAG_ERROR= "Error------->";
	
	
	public static void printLog(String message){
		
		Log.v(TAG1, message);
	}
	
	public static void printInfo(Object message){
		
		Log.i(TAG_INFO, String.valueOf(message));
	}
	
	public static void printError(String message){
		
		Log.e(TAG_ERROR, message);
	}
}
