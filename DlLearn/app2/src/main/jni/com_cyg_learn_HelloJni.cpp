#include "com_cyg_learn_HelloJni.h"
#include <string.h>
#include <stdlib.h>


JNIEXPORT jstring JNICALL Java_com_cyg_learn_HelloJni_getName
  (JNIEnv *env, jobject obj, jstring str) {

 jstring data = env->NewStringUTF("HelloJni");
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

  string value("hello");

  ret = (jobjectArray)(env->NewObjectArray(len,env->FindClass("java/lang/String"),0));

  for(int i=0;i<len;i++)
  {
    str = env->NewStringUTF(value.c_str());
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

  jfieldID m_field_1 = env->GetFieldID(m_cls,"ssid","Ljava/lang/String");
  jfieldID m_field_2 = env->GetFieldID(m_cls,"mac","Ljava/lang/String");
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

  jclass list_cls = env->FindClass("Ljava/util/ArrayList");
  if(list_cls ==NULL)
  {

    cout << "list class is null\n";

  }

  jmethodID list_construct = env->GetMethodID(list_cls,"<init>","()V");

  jobject list_obj = env->NewObject(list_cls,list_construct);


  jmethodID list_add = env->GetMethodID(list_cls,"add","(Ljava/lang/Object)Z");

  jclass stu_cls = env->FindClass("con/cyg/learn/Student");

  jmethodID stu_construct = env->GetMethodID(stu_cls,"<init>","(ILjava/lang/String;)V");

  for(int i=0;i<3;i++) {
    jstring str = env->NewStringUTF("native");
    jobject stu_obj = enc->NewObject(stu_cls,stu_construct,10,str);

    env->ClassBooleanMethod(list_obj,list_add,stu_obj);

  }

  return list_obj;



}

