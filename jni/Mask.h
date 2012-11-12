#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define T (0.15f)

//pre compiled methods
#define INDEX(i,j,w) (j*w+i)
#define AVERAGE(x,y) ((x+y)/2)

void adaptiveThreshold2(int* imageDataArray, int width, int height, int* resultImage);
void ScaleMinifyByTwo(int *Target, int *Source, int SrcWidth, int SrcHeight);
void applySobel(int* imageDataArray, int width, int height, int* tempArray);

void adaptiveThreshold2(int* imageDataArray, int width, int height, int* resultImage){

	float newT = 50.0;		//first value of threshold
	float oldT;
	float m1, m2;
	int i,j;

	float epsilon = 0.00001;//values for stop
	float f1,f2;
	float p[256];			//probability of grey values
	int greyCount[256];		//count of each grey values in the pixels
	int numberOfPixels = width*height; //total pixels

	for (i = 0; i < 256; i++) {
		greyCount[i] = 0;
	}

	//count gray values
	for (i = 0; i < height; i++) {
		for (j = 0; j < width; j++) {
			greyCount[imageDataArray[INDEX(j,i,width)]]++;
		}
	}

	//calculate probabilities
	for (i = 0; i < 256; i++) {
		p[i]= (float)greyCount[i] / (float)numberOfPixels;
	}

	//find suitable threshold value by iteration
	do{
		f1 = 0.0;
		f2 = 0.0;
		m1 = 0.0;
		m2 = 0.0;

		oldT = newT;

		for (i = 0; i < oldT; i++) {
			f1 += p[i];
		}
		for (i = oldT; i <256; i++) {
			f2 += p[i];
		}
		for (i = 0; i < oldT; i++) {
			m1 += i*p[i]/f1;
		}
		for (i = oldT; i < 256; i++) {
			m2 += i*p[i]/f2;
		}
		newT = (m1+m2)/2;

	}while(abs(newT-oldT) > epsilon);

	for (i = 0; i < width; i++) {				//pixels value equalize "0" which are located..
		resultImage[INDEX(i,0,width)]=0;						//up and down border of PGM dataMatrix
		resultImage[INDEX(i,(height-1),width)]=0;
	}

	for (i = 1; i < height-1;i++) {				//pixels value equalize "0" which are located..
		resultImage[INDEX(0,i,width)]=0;						//right and left border of PGM dataMatrix
		resultImage[INDEX((width-1),i,width)]=0;
	}

	for (i = 0; i < height; i++) {
		for (j = 0; j < width; j++) {

			if (imageDataArray[INDEX(j,i,width)] > (int)newT) {	//do threshold according to threshold value
//				resultImage[INDEX(j,i,width)] = 255;
				resultImage[INDEX(j,i,width)] = 1;
			}else
				resultImage[INDEX(j,i,width)] = 0;
		}
	}

}

void ScaleMinifyByTwo(int *Target, int *Source, int SrcWidth, int SrcHeight)
{
	int x, y, x2, y2;
	int TgtWidth, TgtHeight;
	int p, q;

	TgtWidth = SrcWidth / 2;
	TgtHeight = SrcHeight / 2;
	for (y = 0; y < TgtHeight; y++) {
		y2 = 2 * y;
		for (x = 0; x < TgtWidth; x++) {
			x2 = 2 * x;
			p = AVERAGE(Source[y2*SrcWidth + x2], Source[y2*SrcWidth + x2 + 1]);
			q = AVERAGE(Source[(y2+1)*SrcWidth + x2], Source[(y2+1)*SrcWidth + x2 + 1]);
			Target[y*TgtWidth + x] = AVERAGE(p, q);
		} /* for */
	} /* for */
}

void applySobel(int* imageDataArray, int width, int height, int* tempArray){
	//apply these matrixs to our dataMatrix
	int gY=0,gX=0,i,j;


	for (i = 1; i < (width-1); i++) {
		for (j = 1; j < (height-1); j++) {

			//convulution with gYmatrix
			gY = (imageDataArray[INDEX((i-1),(j-1),width)] + (imageDataArray[INDEX(i,(j-1),width)]*2) + imageDataArray[INDEX((i+1),(j-1),width)]
			      + (imageDataArray[INDEX((i-1),(j+1),width)]*(-1))  + (imageDataArray[INDEX(i,(j+1),width)]*(-2))
			      + (imageDataArray[INDEX((i+1),(j+1),width)]*(-1)));

			gX = (imageDataArray[INDEX((i-1),(j-1),width)] + (imageDataArray[INDEX((i-1),j,width)]*2) + imageDataArray[INDEX((i-1),(j+1),width)]
			      + (imageDataArray[INDEX((i+1),(j-1),width)]*(-1)) + (imageDataArray[INDEX((i+1),j,width)]*(-2)) + (imageDataArray[INDEX((i+1),(j+1),width)]*(-1)));
			//pixels' new values
			if ( ((int) abs(gX) + (int) abs(gY)) < 255) {
				tempArray[INDEX(i,j,width)] = (int) abs(gX) + (int) abs(gY);
			}else{
				tempArray[INDEX(i,j,width)] = 255;
			}
		}
	}
}

