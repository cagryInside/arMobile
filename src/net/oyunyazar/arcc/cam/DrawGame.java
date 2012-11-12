package net.oyunyazar.arcc.cam;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class DrawGame extends View{
	
	private Context context;
    public DrawGame(Context context) {
        super(context);
        this.context = context;
}

@Override
protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(30.0f);
        canvas.drawText("DEMO NUMBER 1", 50, 50, paint);

        super.onDraw(canvas);
} 

}
