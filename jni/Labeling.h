#include <stdio.h>
#include <stdlib.h>

//pre compiled methods
#define INDEX(i,j,w) (j*w+i)


static int SearchDirection[8][2] = {{0,1},{1,1},{1,0},{1,-1},{0,-1},{-1,-1},{-1,0},{-1,1}};

void ContourTracing(int cy, int cx, int labelindex, int tracingdirection, int *tempArray, int *data);
void Tracer(int *cy, int *cx, int *tracingdirection, int *data,int *tempArray);
void ConnectedComponentLabeling(int* pgmData, int *tempArray, int width1, int height2);

int width1, height2;
void ConnectedComponentLabeling(int* pgmData, int *tempArray, int w, int h)
{
	int cx, cy, index, tracingdirection, ConnectedComponentsCount = 0, labelindex = 0;


	width1 = w;
	height2 = h;

	for (cy = 0; cy < height2; cy++){
		for (cx = 0; cx < width1; cx++ ){

			tempArray[INDEX(cx,cy,width1)] = 0;
		}
	}

//	pgmData = sourceBitmap;
//	tempArray = sourceLabelmap;


	for(cy = 1; cy < height2 - 1; cy++){
		for(cx = 1, labelindex = 0; cx < width1 - 1; cx++){

			index = INDEX(cx,cy,width1);

			if(pgmData[index] == 1)// black pixel
			{
				if(labelindex != 0)// use pre-pixel label
				{
					tempArray[index] = labelindex;
				}
				else
				{
					labelindex = tempArray[index];

					if(labelindex == 0)
					{
						labelindex = ++ConnectedComponentsCount;
						tracingdirection = 0;
						ContourTracing(cy, cx, labelindex, tracingdirection, tempArray, pgmData);// external contour
						tempArray[index] = labelindex;
					}
				}
			}
			else if(labelindex != 0)// white pixel & pre-pixel has been labeled
			{
				if(tempArray[index] == 0)
				{
					tracingdirection = 1;
					ContourTracing(cy, cx-1, labelindex, tracingdirection, tempArray, pgmData);// internal contour
				}

				labelindex = 0;
			}
		}
	}

}

void Tracer(int *cy, int *cx, int *tracingdirection, int *data,int *tempArray)
{
	int i, y, x, index;

	for(i = 0; i < 7; i++)
	{
		y = *cy + SearchDirection[*tracingdirection][0];
		x = *cx + SearchDirection[*tracingdirection][1];

		index = INDEX(x,y,width1);

		if(data[index] == 0)
		{
			tempArray[index] = 255;
			*tracingdirection = (*tracingdirection + 1) % 8;
		}
		else
		{
			*cy = y;
			*cx = x;
			break;
		}
	}
}

void ContourTracing(int cy, int cx, int labelindex, int tracingdirection, int *tempArray, int *data)
{
	char tracingstopflag = 0, SearchAgain = 1;
	int fx, fy, sx = cx, sy = cy;

	Tracer(&cy, &cx, &tracingdirection, data, tempArray);

	if(cx != sx || cy != sy)
	{
		fx = cx;
		fy = cy;

		while(SearchAgain)
		{

			tracingdirection = (tracingdirection + 6) % 8;
			tempArray[INDEX(cx,cy,width1)] = labelindex;
			Tracer(&cy, &cx, &tracingdirection,data,tempArray);

			if(cx == sx && cy == sy)
			{
				tracingstopflag = 1;
			}
			else if(tracingstopflag)
			{
				if(cx == fx && cy == fy)
				{
					SearchAgain = 0;
				}
				else
				{
					tracingstopflag = 0;
				}
			}
		}
	}
}
