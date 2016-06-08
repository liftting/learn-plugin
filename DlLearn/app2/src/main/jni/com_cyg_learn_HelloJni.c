#include "com_cyg_learn_HelloJni.h"
/*
 * Class:     com_cyg_learn_HelloJni
 * Method:    getName
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_cyg_learn_HelloJni_getName
  (JNIEnv *env, jobject obj, jstring str){
     return (*env)->NewStringUTF(env,"wangming");
 }