#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define INDEX(i,j,w) (j*w+i)

void findSquere(int* imageDataArray, int width, int height, float *returnParams);

void findSquere(int* imageDataArray, int width, int height, float *returnParams){

	int i,j,k,index, lastIndex = 0,jumpOut, tempIndex,rectIndex =0,check;
	//	ImagePGM result;

	int *rectUpdate = (int*)malloc(sizeof(int)*(height));
	int *rectSize = (int*)malloc(sizeof(int)*(height));
	int *rect = (int*)malloc(sizeof(int)*(height*width));
	int *rectX= (int*)malloc(sizeof(int)*(height*width));
	int *rectLastY= (int*)malloc(sizeof(int)*20);
	int *rectLastX= (int*)malloc(sizeof(int)*20);
	int *rectLast = (int*)malloc(sizeof(int)*20);


	for (i = 0; i < (height); i++) {

		rectUpdate[i] = -1;
		rectSize[i] = 0;
	}

	for (i = 0; i < (height*width); i++) {

		rectX[i] = 0;
		rect[i] = -1;
	}

	for (i = 0; i < 20; i++) {

		rectLastX[i] = 0;
		rectLastY[i] = 0;
		rectLast[i] = 0;
	}

	//	result=dataPGM;
	int step =0;
	int label = 0,labelIndex = 0;


	//yatayda kontrol
	for (i = 0; i < height; i++) {
		//		findWhite = 0;
		step = 0;
		label = -1;
		for (j = 0; j < width; j++) {

			index = INDEX(j,i,width);
			switch (step) {

			//ilk label arama
			case 0:
				jumpOut = 0;
				if ((imageDataArray[index] != 0) && (imageDataArray[index] != 255)) {

					if (imageDataArray[index] == label) {

						rect[rectIndex] = label;
						rectX[rectIndex] = (int) ((j + labelIndex)/2);
						rectSize[rectIndex] =(int) (j -labelIndex);
					/*	resultImage[index-1] = 150;
						resultImage[index] = 150;
						resultImage[index+1] = 150;
						resultImage[index+2] = 150;*/
						rectIndex++;
						step = 1;
						label = -1;
					} else {
						//jumpOut = 0;
						tempIndex = rectIndex;
						while((tempIndex > -1) && (jumpOut == 0)){

							if (rect[tempIndex] == imageDataArray[index]) {
								jumpOut = 1;
							}
							tempIndex--;
						}

						if (jumpOut == 0) {

							label = imageDataArray[index];
							labelIndex = j;
							step = 1;

						/*	resultImage[index-1] = 100;
							resultImage[index] = 100;
							resultImage[index+1] = 100;
							resultImage[index+2] = 100;*/
						}
					}
				}

				break;
				//ilk labeldan sonra beyaz arama
			case 1:

				if((imageDataArray[index] == 255)){
					step = 0;
				}
				//resultImage[index] = 255;
				//step = 2;
				break;
			default:
				break;
			}
		}
	}

	int rectIndexUpdate =0;
	int temX = 0;

	for (j = 0; j < rectIndex; j++) {
		step = 0;
		for (i = 0; i < height; i++) {

			temX = rectX[j];
			index = INDEX(temX,i,width);
			switch (step) {
			//ilk label arama
			case 0:
				if (imageDataArray[index] == rect[j]) {

					labelIndex = i;
					step = 1;
				}
				break;

				//label sonrasý beyaz arama
			case 1:
				if(imageDataArray[index] == 255)
					step = 2;
				break;

				//son label arama
			case 2:
				if (imageDataArray[index] == rect[j]) {

					//						rectSize[j] += i-labelIndex;
					rectSize[rectIndexUpdate] = (int) (i-labelIndex); //size atama

					rectUpdate[rectIndexUpdate] = rect[j]; //label atama
					rectX[rectIndexUpdate] =(int) ((i + labelIndex)/2); //uyan labeldaki orta x deðeri atama
					rectIndexUpdate++;

					i = height;
				}
				break;

			default:
				break;
			}

		}
	}

	int rectIndexLast =0;
	int masterJump = 0;
	for (i = 0; i < rectIndexUpdate && masterJump == 0; i++) {
		step = 0;
		for (j = 0; j < width && masterJump == 0; j++) {

			temX = rectX[i];
			index = INDEX(j,temX,width);

			switch (step){
			//ilk label bulma
			case 0:
				if(imageDataArray[index] == rectUpdate[i]){
					labelIndex = j;
					step = 1;
				}
				break;
				//beyaz bulma
			case 1:
				if(imageDataArray[index] == 255)
					step = 2;
				break;
				//2. label bulma
			case 2:
				if(imageDataArray[index] == rectUpdate[i]){

					rectSize[i] += (int) (j - labelIndex);

					if ((rectSize[i] > 120) && ((j-labelIndex) < ((int) ((rectSize[i]*60)/100))) && ((j-labelIndex) >((int) ((rectSize[i]*40)/100)))) {

						rectLast[rectIndexLast] = rectUpdate[i];
						rectLastX[rectIndexLast] = rectX[i];
						rectLastY[rectIndexLast] = (int) ((j+labelIndex)/2);
						returnParams[4] = (float)rectSize[i];
						rectIndexLast++;
						masterJump = 1;
					}
					j = width;
				}

				break;
			default:
				break;
			}
		}
	}

	if (rectLastX[0] != 0) {

		returnParams[0] = ((((float) (320 - rectLastY[0])) / (320.0f)) * (26.0f)); //rect coord x
		returnParams[1] = ((((float) (180 - rectLastX[0])) / (180.0f)) * (15.0f)); //rect coord y
		returnParams[4] /= 400.0f;

	}else {
		returnParams[0] = 100.0f; //rect coord x
		returnParams[1] = 0.0f; //rect coord y
		returnParams[2] = 0.0f; //angle
		returnParams[3] = 0.0f;
		returnParams[4] = 1.0f; //scale


	}

	////////////////////////////////////////////////////////////////////////////////
	//Find angle

	if (returnParams[0] != 0) {

		//left to right horizontal
		//minX
		jumpOut = 0;
		i = 0;
		while(jumpOut == 0 && i < height*width){

			if (imageDataArray[i] == rectLast[0]) {
				jumpOut = 1;
				rect[2] = i % width; //width
				rect[3] = (float)((int)i/width); //height

			}
			i++;
		}

		//maxX
		jumpOut = 0;
		i = height*width;
		while(jumpOut == 0 && i > 0){
			if (imageDataArray[i] == rectLast[0]) {
				jumpOut = 1;
				rect[4] = i%width; //width
				rect[5] = (float)((int) i/width); //height
			}
			i--;
		}

		//minY
		jumpOut = 0;
		i = 0;
		while(jumpOut == 0 && i < width){
			j = 0;
			while(jumpOut == 0 && j< height){

				if (imageDataArray[INDEX(i,j,width)] == rectLast[0]) {
					jumpOut = 1;
					rect[6] = j; //width
					rect[7] = i; //height
				}
				j++;
			}
			i++;
		}

		//maxY
		jumpOut = 0;
		i = width;
		while(jumpOut == 0 && i > 0){
			j = 0;
			while(jumpOut == 0 && j< height){

				if (imageDataArray[INDEX(i,j,width)] == rectLast[0]) {
					jumpOut = 1;
					rect[8] = j; //width
					rect[9] = i; //height
				}
				j++;
			}
			i--;
		}

		//calculate angles
		float a = rect[9] - rect[5];
		float b = rect[4] - rect[8];

		float angle1 = atan2(b,a);

		a = rect[3] - rect[7];
		b = rect[6] - rect[2];

		float angle2 = atan2(b,a);

/*		if( abs(angle1 - angle2) > 0.09){
			angle1 = 0;
		}
*/
		returnParams[2] = angle1;
	}


	free(rect);
	free(rectSize);
	free(rectX);
	free(rectLast);
	free(rectUpdate);
	free(rectLastX);
	free(rectLastY);
}
