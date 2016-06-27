LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := helloJni
LOCAL_SRC_FILES := com_cyg_learn_HelloJni.cpp \
                   com_cyg_learn_jni_EntryJni.cpp

LOCAL_LDLIBS := -llog -lz
include $(BUILD_SHARED_LIBRARY)