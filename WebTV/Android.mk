ifeq ($(findstring $(SYSCONFIG_TARGET_CLASS),dvb_s dvb_t dvb_t2 dvb_c), $(SYSCONFIG_TARGET_CLASS))

LOCAL_PATH:= $(call my-dir)


########################################
#    build for stb_webtv_core jar
########################################

include $(CLEAR_VARS)

WEBTV_CORE_SRC_FILES := $(call all-java-files-under, src/com/ott/webtv/core)

LOCAL_SRC_FILES := $(WEBTV_CORE_SRC_FILES)

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE := stb_webtv_core

include $(BUILD_JAVA_LIBRARY)


########################################
#    prebuild for stb_webtv_core.xml
########################################
include $(CLEAR_VARS)

LOCAL_MODULE := stb_webtv_core.xml
LOCAL_MODULE_TAGS := optional

LOCAL_MODULE_CLASS := ETC

# This will install the file in /system/etc/permissions #
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/permissions

LOCAL_SRC_FILES := $(LOCAL_MODULE)

include $(BUILD_PREBUILT)


########################################
#    build for WEBTV APK
########################################


include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES := $(filter-out $(WEBTV_CORE_SRC_FILES),$(LOCAL_SRC_FILES))

LOCAL_JAVA_LIBRARIES := stb_webtv_core stb_input_KeyboardDialogUtil

LOCAL_PACKAGE_NAME := WebTV

LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

endif