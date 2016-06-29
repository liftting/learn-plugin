#include "com_cyg_learn_jni_NativeTool.h"
#include "common.h"
#include <stdlib.h>
#include <dlfcn.h>
#include <stdio.h>

#include <android/log.h>

#define  LOG_TAG    "jw"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

// 整个函数意思就是要 利用jni的加载去实现内存加载机制
JNINativeMethod *dvm_dalvik_system_DexFile;
void (*openDexFile)(const u4* args, union JValue* pResult); // 这里是定义了一个函数指针

int lookup(JNINativeMethod *table, const char *name, const char *sig,
           void (**fnPtrout)(u4 const *, union JValue *)) {
    int i = 0;
    while (table[i].name != NULL)
    {
        LOGI("lookup %d %s" ,i,table[i].name);
        if ((strcmp(name, table[i].name) == 0)
            && (strcmp(sig, table[i].signature) == 0))
        {
            *fnPtrout = table[i].fnPtr;
            return 1;
        }
        i++;
    }
    return 0;
}

/* This function will be call when the library first be load.
 * You can do some init in the libray. return which version jni it support.
 */
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {

    void *ldvm = (void*) dlopen("libdvm.so", RTLD_LAZY); //打开so库，因为需要获取一个函数的指针
    dvm_dalvik_system_DexFile = (JNINativeMethod*) dlsym(ldvm, "dvm_dalvik_system_DexFile");

    //openDexFile
    if(0 == lookup(dvm_dalvik_system_DexFile, "openDexFile", "([B)I",&openDexFile)) { //去尝试去拿openDexFile的函数指针，
        openDexFile = NULL;
        LOGI("openDexFile method does not found ");
    }else{
        LOGI("openDexFile method found ! HAVE_BIG_ENDIAN");
    }

    LOGI("ENDIANNESS is %c" ,ENDIANNESS );
    void *venv;
    LOGI("dufresne----->JNI_OnLoad!");
    if ((*vm)->GetEnv(vm, (void**) &venv, JNI_VERSION_1_4) != JNI_OK) {
        LOGI("dufresne--->ERROR: GetEnv failed");
        return -1;
    }
    return JNI_VERSION_1_4;
}

JNIEXPORT jint JNICALL Java_com_cyg_learn_jni_NativeTool_loadDex(JNIEnv* env, jclass jv, jbyteArray dexArray, jlong dexLen)
{
    // header+dex content
    u1 * olddata = (u1*)(*env)-> GetByteArrayElements(env,dexArray,NULL);//因为有一个数据结构体，传入的时候，
    char* arr;
    arr = (char*)malloc(16 + dexLen);
    ArrayObject *ao=(ArrayObject*)arr;
    ao->length = dexLen;
    memcpy(arr+16,olddata,dexLen);
    u4 args[] = { (u4) ao };
    union JValue pResult;
    jint result;
    if(openDexFile != NULL) {
        openDexFile(args,&pResult);//调用，其实内部调用的函数是bytearray
    }else{
        result = -1;
    }

    result = (jint) pResult.l;
    LOGI("Java_cn_wjdiankong_dexfiledynamicload_NativeTool_loadDex %d" , result);
    return result;
}
