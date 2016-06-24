#include "com_cyg_learn_HelloJni.h"
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <jni.h>
#include <android/log.h>
#include <sys/stat.h>
#include <unistd.h>
#include <sys/types.h>
#include <elf.h>
#include <sys/mman.h>
#include <assert.h>

jstring   CharTojstring(JNIEnv*   env,   char*   str);

JNIEXPORT jstring JNICALL Java_com_cyg_learn_HelloJni_getName
  (JNIEnv *env, jobject obj, jstring str) {

 const char *java_data = env->GetStringUTFChars(str,NULL);
 char dest[128];

 strcpy(dest,java_data);

 env->ReleaseStringUTFChars(str,java_data);

 jstring data = CharTojstring(env,dest);
 return data;

}

/*
 * Class:     com_cyg_learn_HelloJni
 * Method:    getArray
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_cyg_learn_HelloJni_getArray
  (JNIEnv *env, jobject obj) {

  jobjectArray ret = 0;
  jsize len = 5;
  jstring str;

  char *value = "hello";

  ret = (jobjectArray)(env->NewObjectArray(len,env->FindClass("java/lang/String"),0));

  for(int i=0;i<len;i++)
  {
    str = CharTojstring(env,value);
    env->SetObjectArrayElement(ret,i,str);

  }

  return ret;


}

/*
 * Class:     com_cyg_learn_HelloJni
 * Method:    getResult
 * Signature: ()Lcom/cyg/learn/ScanResult;
 */
JNIEXPORT jobject JNICALL Java_com_cyg_learn_HelloJni_getResult
  (JNIEnv *env, jobject obj) {

  jclass m_cls = env->FindClass("com/cyg/learn/ScanResult");
  jmethodID m_mid = env->GetMethodID(m_cls,"<init>","()V");

  jfieldID m_field_1 = env->GetFieldID(m_cls,"ssid","Ljava/lang/String;");
  jfieldID m_field_2 = env->GetFieldID(m_cls,"mac","Ljava/lang/String;");
  jfieldID m_field_3 = env->GetFieldID(m_cls,"level","I");


  jobject m_obj = env->NewObject(m_cls,m_mid);

  env->SetObjectField(m_obj,m_field_1,env->NewStringUTF("AP1"));
  env->SetObjectField(m_obj,m_field_2,env->NewStringUTF("00-11-22"));
  env->SetIntField(m_obj,m_field_3,10);

  return m_obj;


}

/*
 * Class:     com_cyg_learn_HelloJni
 * Method:    getArrayList
 * Signature: ()Ljava/util/ArrayList;
 */
JNIEXPORT jobject JNICALL Java_com_cyg_learn_HelloJni_getArrayList
  (JNIEnv *env, jobject obj) {

  jclass list_cls = env->FindClass("java/util/ArrayList");


  jmethodID list_construct = env->GetMethodID(list_cls,"<init>","()V");

  jobject list_obj = env->NewObject(list_cls,list_construct);


  jmethodID list_add = env->GetMethodID(list_cls,"add","(Ljava/lang/Object;)Z");

  jclass stu_cls = env->FindClass("com/cyg/learn/Student");

  jmethodID stu_construct = env->GetMethodID(stu_cls,"<init>","(ILjava/lang/String;)V");

  for(int i=0;i<3;i++) {
    jstring str = env->NewStringUTF("native");
    jobject stu_obj = env->NewObject(stu_cls,stu_construct,10,str);

    env->CallBooleanMethod(list_obj,list_add,stu_obj); //调用java的方法

  }

  return list_obj;

}


jstring   CharTojstring(JNIEnv*   env,   char*   str)
{
    jsize   len   =   strlen(str);

    jclass   clsstring   =   env->FindClass("java/lang/String");
    jstring   strencode   =   env->NewStringUTF("GB2312");

    jmethodID   mid   =   env->GetMethodID(clsstring,   "<init>",   "([BLjava/lang/String;)V");
    jbyteArray   barr   =   env-> NewByteArray(len);

    env-> SetByteArrayRegion(barr,0,len,(jbyte*)str);
    return (jstring)env-> NewObject(clsstring,mid,barr,strencode);
}

