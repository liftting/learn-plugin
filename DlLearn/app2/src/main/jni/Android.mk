LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := helloJni
LOCAL_SRC_FILES := com_cyg_learn_HelloJni.cpp \
                    NativeTool.c \
                    LoadJniTool.cpp

LOCAL_LDLIBS := -llog -lz
include $(BUILD_SHARED_LIBRARY)