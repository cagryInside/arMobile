package net.oyunyazar.arcc.support;

import android.util.Log;

import com.threed.jpct.SimpleVector;

public class Career {
	
	private static Career career = null;
	
	public float centerX = 0.0f;
	public float centerY = 0.0f;
	private float angle = 0.0f;
	private float fall = 0.0f;
	private float scaleParam = 1.0f;
	
	private boolean doorKey = false;
	
	
	private Career() {
		// TODO Auto-generated constructor stub
	}
	
	static public Career getInstance(){
		
		if (career == null) {
			
			career = new Career();
		}
		return career;
	}
	
	public float getAngel(){
		
		if (doorKey == true) {
			doorKey = false;
			return angle;
		}else 
			return 0.0f;
	}

	public void setAngle(float angle){
		
		this.angle = angle;
		doorKey = true;
	}

	public float getScaleParam() {
		return scaleParam;
	}

	public void setScaleParam(float scaleParam) {
		this.scaleParam = scaleParam;
	}


}
