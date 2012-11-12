#include <string.h>
#include <jni.h>
#include <android/log.h>
#include "Mask.h"
#include "Labeling.h"
#include "RectengleDetector.h"

#define DEBUG_TAG "NDK_NativeLib"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, __VA_ARGS__)

JNIEXPORT jfloatArray JNICALL  Java_net_oyunyazar_arcc_data_FrameManager_processImage(JNIEnv* env, jobject javaThis, jint width, jint height, jbyteArray arr) {

	jint *convertedData;
	convertedData = (jint*)malloc((width*height) * sizeof(jint));

	jintArray result = (*env)->NewIntArray(env, width*height/4);
	jfloatArray returnParams = (*env)->NewIntArray(env, 5);


	jint y,x;
	jbyte grey;

	jsize len = (*env)->GetArrayLength(env, arr);
	jbyte *YUVData = (*env)->GetByteArrayElements(env, arr, 0);

	for (y = 0; y < height; y++){
		for (x = 0; x < width; x++){

			grey = YUVData[y * width + x];
			convertedData[y*width+x] =(jint) grey & 0xff;

		}
	}

	width = width/2;
	height = height/2;
	int *resultImage2 = (int*)malloc((width*height) * sizeof(int));
	ScaleMinifyByTwo(resultImage2, convertedData, width*2, height*2);



	///////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////
	/////////////////     APPLY MASK        /////////////////////////
	////////////////////////////////////////////////////////////////

	int i;
	int *resultImage = (int*)malloc((width*height) * sizeof(int));
	//int *segmentedImage = (int*)malloc((width*height) * sizeof(int));
	applySobel(resultImage2, width, height, resultImage);
	adaptiveThreshold2(resultImage, width, height, resultImage2);
	//LOGD("native-C [%d]",0);


	ConnectedComponentLabeling(resultImage2, resultImage, width, height);
	float *params = (float*) malloc(5 * sizeof(float));
	findSquere(resultImage, width, height, params);

	/*for (i = 0; i < width; i++) {

		LOGD("rec [%d]",segmentedImage[i]);
	}*/

//	(*env)->SetIntArrayRegion(env, result, 0, (width*height),resultImage2);
	(*env)->SetFloatArrayRegion(env, returnParams, 0, 5,params);
	//Release params


	free(convertedData);
	free(resultImage2);
	//free(segmentedImage);
	free(resultImage);
	free(params);

	(*env)->ReleaseByteArrayElements(env, arr, YUVData, JNI_ABORT);

	return returnParams;
}

