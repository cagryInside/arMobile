LOCAL_PATH := $(call my-dir)
 
include $(CLEAR_VARS)
 
# Here we give our module name and source file(s)
LOCAL_MODULE    := arModule
LOCAL_SRC_FILES := RealTimeProcessor.c
LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
 
include $(BUILD_SHARED_LIBRARY)