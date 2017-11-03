LOCAL_PATH:= $(call my-dir)


include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

#LOCAL_SRC_FILES := $(call all-java-files-under, src) 
LOCAL_SRC_FILES := src/com/webtv/youporn/MainActivity.java

LOCAL_JAVA_LIBRARIES := stb_webtv_core 


LOCAL_PACKAGE_NAME := Youporn

LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

