package net.oyunyazar.arcc;

import net.oyunyazar.arcc.cam.CameraPreview;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;


public class ARCCActivity extends Activity implements OnClickListener{
	
	private Button startButton;
	private Button aboutButton;
	ImageView imageView;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        startButton = (Button) findViewById(R.id.startButton);
        aboutButton = (Button) findViewById(R.id.abaouButton);
        
        startButton.setOnClickListener(this);
        aboutButton.setOnClickListener(this);
        
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	
    }
	
	private void startAR(){
		Intent startARIntent = new Intent(ARCCActivity.this,CameraPreview.class);
		startActivityForResult(startARIntent,1);
	}
	
	private void openAbout(){
		
	}


	@Override
	public void onClick(View pressedButton) {

		if (pressedButton == startButton) {
			
			startAR();
		}else if (pressedButton == aboutButton) {
			
			openAbout();
		}
	}


}