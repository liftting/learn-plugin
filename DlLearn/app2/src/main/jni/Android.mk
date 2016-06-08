LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := helloJni
LOCAL_SRC_FILES := com_cyg_learn_HelloJni.c

include $(BUILD_SHARED_LIBRARY)