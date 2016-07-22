#include "com_cyg_learn_jni_LoadJni.h"

JNIEXPORT jint JNICALL Java_com_cyg_learn_jni_LoadJni_loadData
  (JNIEnv *env, jobject obj, jlong uid) {

    jint sum = 10;
    return sum;

}



static const JNINativeMethod method_table[] = {
    {"nativeLoadData", "(JJ)I", (void*)Java_com_cyg_learn_jni_LoadJni_loadData},
};

extern "C" jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
      JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }

    jniRegisterNativeMethods(env, "com/cyg/learn/jni/LoadJni", method_table, NELEM(method_table));

    return JNI_VERSION_1_4;
}
