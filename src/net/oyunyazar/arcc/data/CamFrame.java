package net.oyunyazar.arcc.data;

import java.io.FileOutputStream;

import net.oyunyazar.arcc.constans.GeneralConstants;
import android.util.Log;

public class CamFrame {

	private int frameWidth;
	private int frameHeight;
	private byte[] YUVData;
	private int[] convertedData;

	public CamFrame(int width, int height, byte[] data) {
		// TODO Auto-generated constructor stub
		this.frameWidth = width;
		this.frameHeight = height;
		convertedData = new int[frameWidth * frameHeight];
		//this.YUVData = data;
		YUVData = new byte[data.length];
		System.arraycopy(data, 0, YUVData, 0, data.length);
//		toGrayscale();
//		writePGM("/sdcard/qmhUZU.pgm", "P5", "ss");
//		Log.v(GeneralConstants.TAG_GRAY_DATA, "data");

	}


	public int getFrameWidth() {
		return frameWidth;
	}


	public int getFrameHeight() {
		return frameHeight;
	}


	public byte[] getYUVData() {
		return YUVData;
	}

	//Converting YUV color space to RGB 

	public int[] getConvertedData() {
		return convertedData;
	}


	public void setConvertedData(int[] convertedData) {
		this.convertedData = convertedData;
	}


	public void toGrayscale(){

		//		int stride = 4 * (int)Math.ceil(3 * frameWidth / 4.0);
		//		convertedData = new byte[stride * frameHeight];
		convertedData = new int[frameWidth * frameHeight];

		try	{
			for (int y = 0; y < frameHeight; y++){
				for (int x = 0; x < frameWidth; x++){

					byte grey = YUVData[y * frameWidth + x];
					//					convertedData[y * stride + 3 * x] = grey;
					//					convertedData[y * stride + 3 * x + 1] = grey;
					//					convertedData[y * stride + 3 * x + 2] = grey;
					convertedData[y*frameWidth+x] =(int) grey & 0xff;


				}
			}

			//			Bitmap bitmap =(Bitmap) BitmapFactory.decodeByteArray(convertedData, 0, convertedData.length);
			//			Career.getInstance().setBitmap(bitmap);

		}
		catch (Exception e){
		}
	}

	public void writePGM () {

		String savePath = "/sdcard/qmhUZU.pgm";
		String type = "P5";
		String comment = "hay";
		
		try {

			frameHeight /= 2;
			frameWidth /= 2;
			//File out for writing PGM
			FileOutputStream fout=new FileOutputStream(savePath);

			String tempString;
			tempString=type+"\n";
			fout.write(tempString.getBytes());

			// Write comment line
			comment="#"+comment+"\n";
			fout.write(comment.getBytes());

			// Write width
			tempString=Integer.toString(frameWidth)+" ";
			fout.write(tempString.getBytes());

			// Write height
			tempString=Integer.toString(frameHeight)+" ";
			fout.write(tempString.getBytes());

			// Write maxGray
			tempString=Integer.toString(255);
			tempString="\n"+tempString+"\n";
			fout.write(tempString.getBytes());


				for(int row=0;row<this.frameHeight;row++)
				{
					for(int col=0;col<this.frameWidth;col++)
					{
						fout.write(convertedData[row*frameWidth+ col]);
					}
				}

			System.out.printf("Writing is done");
			fout.close();
		}

		catch(Exception e) {
			System.err.println("Error " + e);
		}
	}

}
