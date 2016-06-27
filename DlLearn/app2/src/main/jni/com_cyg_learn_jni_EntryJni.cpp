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
#define DEBUG
#define   LOG_TAG    "LOG_TEST"
#define   LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define   LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
// 获取数组的大小
# define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
// 指定要注册的类，对应完整的java类名
#define JNIREG_CLASS "com/cyg/learn/entry/EntryApplication"
//Function  Statements

// char 的字符转换成jstring
jstring   CharConvertJstring(JNIEnv*   env,   char*   str);


jobject invokeStaticMethodss (JNIEnv *env,jstring class_name, jstring method_name, jobjectArray pareTyple, jobjectArray pareVaules);


jobject   getFieldOjbect(JNIEnv *env,		jstring class_name, jobject obj, jstring fieldName);


void     setFieldOjbect(JNIEnv *env, jstring class_name, jstring fieldName, jobject obj, jobject filedVaule);

void exec_get_dex( JNIEnv* env,jobject obj,jobject context);

jobject exec_protect( JNIEnv* env,jobject thiz,jobject context);

void write_dex_file(JNIEnv* env,jobject obj,jobject context);

// 声明给jni调用的
JNIEXPORT void JNICALL go_one( JNIEnv* env,jobject thiz,jobject context )
{
	exec_get_dex(env,thiz,context );
}

JNIEXPORT jobject JNICALL go_two( JNIEnv* env,jobject thiz,jobject context )

{
    exec_protect(env,thiz,context);
}


// Java和JNI函数的绑定表
// c 函数使用前要声明
static JNINativeMethod method_table[] = {
	{ "entryProtectExec", "(Landroid/content/Context;)Ljava/lang/Object;", (void*)go_two },//绑定Ericky3
	{ "entryRunDex", "(Landroid/content/Context;)V", (void*)go_one },//Ericky1
};

// 注册native方法到java中
static int registerNativeMethods(JNIEnv* env, const char* className,
        JNINativeMethod* gMethods, int numMethods)
{
	jclass clazz;
	clazz = env->FindClass( className);
	if (clazz == NULL) {
		return JNI_FALSE;
	}
	if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
		return JNI_FALSE;
	}

	return JNI_TRUE;
}

int register_ndk_load(JNIEnv *env)
{
	// 调用注册方法
    return registerNativeMethods(env, JNIREG_CLASS,
            method_table, NELEM(method_table));
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
 JNIEnv* env = NULL;
 jint result = -1;

 if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
     return result;
 }

 register_ndk_load(env);

// 返回jni的版本
 return JNI_VERSION_1_4;
}



// 从 so 中获取到dex 文件
void exec_get_dex( JNIEnv* env,jobject obj,jobject context )
{
	jclass native_context_class = env->GetObjectClass(context);
	jmethodID method_packname = env->GetMethodID(native_context_class,"getPackageName","()Ljava/lang/String;");
	jstring packname = (jstring)(env->CallObjectMethod(context,method_packname));
	//拿到包名

	//构建路径
	const char *c_packname = env->GetStringUTFChars(packname,0);
	char dest[128];

	const char *c_pre_path = "/data/data", *c_last_cache = "/.cache/";

    strcpy(dest,c_pre_path);
    strcat(dest,c_packname);
    strcat(dest,c_last_cache);

    LOGI("jni_protect:package cache path is %s:",dest);

//释放掉，一定要
    env->ReleaseStringUTFChars(packname,c_packname);

    //开始修改目标文件夹的权限
    jint i_dest_dir = mkdir(dest,777);

    LOGI("jni_protect:has create dir");

    const char *c_file_chmod = "chmod 755 ";
    char cmd[128];
    strcpy(cmd,c_file_chmod);
    strcat(cmd,dest);
    jstring jcmd = CharConvertJstring(env,cmd);


    LOGI("jni_protect:beging to get runtime");
    jclass runtime = env->FindClass("java/lang/Runtime");
    jmethodID runtime_func = env->GetStaticMethodID(runtime,"getRuntime","()Ljava/lang/Runtime;");
    jobject exec_runtime_obj = env->CallStaticObjectMethod(runtime,runtime_func);// 拿到实例对象，用于执行函数的，
    jclass runtime_java_class = env->GetObjectClass(exec_runtime_obj); // 拿到类的对象，查找函数的

    LOGI("jni_protect:begin to get runtime process");
    // 在从runtime的内部内，里面执行得到Process
    jmethodID exec_process_func = env->GetMethodID(runtime_java_class,"exec","(Ljava/lang/String;)Ljava/lang/Process;");
    jobject process_obj = env->CallObjectMethod(exec_runtime_obj,exec_process_func,jcmd);
    jclass process_java_class = env->GetObjectClass(process_obj); // 拿到Process的类字节，class


    LOGI("jni_protect:begin to invoke waitfor ");
    // 3 执行 waitFor() 方法
    jmethodID waitfor_func = env->GetMethodID(process_java_class,"waitFor","()I");
    jint waitfor_result = env->CallIntMethod(process_obj,waitfor_func);
    LOGI("jni_protect: call waitFor %d",waitfor_result);


    //
    LOGI("jni_protect:begin to write dex file");
    write_dex_file(env,obj,context);


}

// 将so 中的dex 文件写入到 cache的jar包中，
void write_dex_file(JNIEnv* env,jobject obj,jobject context) {

    jclass native_clazz2 = env->GetObjectClass(context);
    jmethodID methodID_func2 = env->GetMethodID(native_clazz2,"getPackageName", "()Ljava/lang/String;");
    jstring packagename2 =(jstring)(env->CallObjectMethod(context, methodID_func2));

    LOGI("jni_protect:has get the packageName,and begin to create path");
    const char *str2 = env->GetStringUTFChars(packagename2, 0);
    char destination2[128];
    const char *blank2 ="/data/data/", *c2 = "/.cache/classdex.jar";
    strcpy(destination2, blank2);
    strcat(destination2, str2);
    strcat(destination2, c2);

    LOGI("jni_protect:have create save path");
    env->ReleaseStringUTFChars(packagename2, str2);
    jstring jdestination = CharConvertJstring(env,destination2); // 写的目的地， /data/data/packagename/.cache/classdex.jar

    LOGI("jni_protect:begint to get ApplicationInfo");
    jmethodID methodID_getApplicationInfo = env->GetMethodID(native_clazz2,"getApplicationInfo", "()Landroid/content/pm/ApplicationInfo;");
    //
    jobject ApplicationInfo =env->CallObjectMethod(context, methodID_getApplicationInfo);
    //
    jclass cls_ApplicationInfo=env->GetObjectClass(ApplicationInfo);

    jfieldID JarFieldId = env->GetFieldID(cls_ApplicationInfo,"publicSourceDir", "Ljava/lang/String;"); //

    LOGI("jni_protect:begint to get public sourceDir");
    jstring SourceDir =(jstring)(env->GetObjectField(ApplicationInfo,JarFieldId)); // 获得publicSourceDir路径
        const char *strs = env->GetStringUTFChars(SourceDir, 0);
        char destinations[128];
        strcpy(destinations, strs); // destinations 存着上面的路径
        jbyteArray bytes = env->NewByteArray(65536);
        LOGI("jni_protect:begin to write: %s",destinations);//
        env->ReleaseStringUTFChars(SourceDir, strs);


        //initial strings
        char *classdex_jar2="assets/class.so";//包里的文件
        jstring jclassdex_jar = CharConvertJstring(env,classdex_jar2); // 这里面 .so 包含着我们的dex 文件
        ////////////new JarFile
        jclass cls_JarFile = env->FindClass("java/util/jar/JarFile");
        jmethodID methodID_JarFile = env->GetMethodID(cls_JarFile,"<init>", "(Ljava/lang/String;)V");
    jobject localJarFile = env->NewObject(cls_JarFile, methodID_JarFile,SourceDir);

LOGI("jni_protect:begint to get Entry ZipEntry");
        //localjarFile getEntry method
        jmethodID methodID_getentry = env->GetMethodID(cls_JarFile,"getEntry", "(Ljava/lang/String;)Ljava/util/zip/ZipEntry;");
        jobject LocalZipEntry = env->CallObjectMethod(localJarFile,methodID_getentry,jclassdex_jar);
        //localjarFile getInputStream method
        jmethodID methodID_getInputStream = env->GetMethodID(cls_JarFile,"getInputStream", "(Ljava/util/zip/ZipEntry;)Ljava/io/InputStream;");
        jobject BufferinputstreamParam = env->CallObjectMethod(localJarFile,methodID_getInputStream,LocalZipEntry); // 构建 ErickyProtect.so的流

LOGI("jni_protect:begint to get File");
        //new a File class
        jclass cls_File = env->FindClass("java/io/File");
        jmethodID methodID_File = env->GetMethodID(cls_File,"<init>", "(Ljava/lang/String;)V");
        jobject localFile = env->NewObject(cls_File, methodID_File,jdestination);

LOGI("jni_protect:begint to get BufferedInputStream");
        //new  BufferedInputStream
        jclass cls_BufferedInputStream = env->FindClass("java/io/BufferedInputStream");
        jmethodID methodID_BufferedInputStream = env->GetMethodID(cls_BufferedInputStream,"<init>", "(Ljava/io/InputStream;)V");
        jobject localBufferedInputStream = env->NewObject(cls_BufferedInputStream, methodID_BufferedInputStream,BufferinputstreamParam);
        //new FileoutputStream

LOGI("jni_protect:begint to get FileOutputStream");
        jclass cls_FileOutputStream = env->FindClass("java/io/FileOutputStream");
        jmethodID methodID_FileOutputStream= env->GetMethodID(cls_FileOutputStream,"<init>", "(Ljava/io/File;)V");
        jobject BufferoutputstreamParam = env->NewObject(cls_FileOutputStream, methodID_FileOutputStream,localFile);


LOGI("jni_protect:begint to get BufferedOutputStream");
        //new BufferedOutputStream
        jclass cls_BufferedOutputStream = env->FindClass("java/io/BufferedOutputStream");
        jmethodID methodID_BufferedOutputStream = env->GetMethodID(cls_BufferedOutputStream,"<init>", "(Ljava/io/OutputStream;)V");
        jobject localBufferedOutputStream = env->NewObject(cls_BufferedOutputStream, methodID_BufferedOutputStream,BufferoutputstreamParam);

        LOGI("jni_protect:has get BufferedOutStream class success,begin to get methodID");

        //some preparations
        jmethodID methodID_write = env->GetMethodID(cls_BufferedOutputStream,"write", "([BII)V");
        jmethodID methodID_read = env->GetMethodID(cls_BufferedInputStream,"read", "([B)I");
        jmethodID output_flush = env->GetMethodID(cls_BufferedOutputStream,"flush", "()V");
        jmethodID output_close = env->GetMethodID(cls_BufferedOutputStream,"close", "()V");
        jmethodID input_close = env->GetMethodID(cls_BufferedInputStream,"close", "()V");

LOGI("jni_protect:begint to write file stream");
        while(true){
            jint i =env->CallIntMethod(localBufferedInputStream,methodID_read,bytes); // 从so 中读取数据，然后写到
            if (i <= 0){
                env->CallVoidMethod(localBufferedOutputStream,output_flush);
                env->CallVoidMethod(localBufferedOutputStream,output_close);
                env->CallVoidMethod(localBufferedInputStream,input_close);
                return;
            }
            env->CallVoidMethod(localBufferedOutputStream,methodID_write,bytes,0,i); //localBufferedOutputStream 写入到这个流里面
            LOGI("afterwrite %d",i);

        }
        LOGI("jni_protect:end to write all");

}


// 这个方法整体操作还是替换掉LoadApk 中的classLoader 即可，
JNIEXPORT jobject JNICALL exec_protect( JNIEnv* env,jobject thiz,jobject context )

{

	jclass native_clazz = env->GetObjectClass(context);
	// å¾—åˆ° getPackageName æ–¹æ³•çš„ ID ç¬¬ä¸€ä¸ªå�‚æ•°æ˜¯ç±»å�¥æŸ„ï¼Œç¬¬äºŒä¸ªå�‚æ•°æ˜¯æ–¹æ³•å��å­—ï¼Œç¬¬ä¸‰ä¸ªå�‚æ•°æ˜¯ç­¾å��æ ‡è¯†
	jmethodID methodID_func = env->GetMethodID(native_clazz,"getPackageName", "()Ljava/lang/String;"); // 准备获取包名
	// èŽ·å¾—å½“å‰�åº”ç”¨çš„åŒ…å��
	jstring packagename =(jstring)(env->CallObjectMethod(context, methodID_func)); // 包名
	//**************************************
	char *aa="android.app.ActivityThread", *bb = "currentActivityThread";
	jstring jaa= CharConvertJstring(env,aa);
	jstring jbb = CharConvertJstring(env,bb);
	jobjectArray pareTyple;
	jobjectArray pareVaules;
	jobject currentActivityThread = invokeStaticMethodss(env,jaa,jbb,pareTyple,pareVaules );
	char *cc="android.app.ActivityThread", *dd = "mPackages";
	jstring jcc= CharConvertJstring(env,cc);
	jstring jdd = CharConvertJstring(env,dd);
	// newä¸€ä¸ªhashmapå¯¹è±¡
	        jclass class_hashmap=env->FindClass("java/util/HashMap");
	        jmethodID hashmap_construct_method=env->GetMethodID(class_hashmap, "<init>","()V");
	        jobject obj_hashmap =env->NewObject(class_hashmap, hashmap_construct_method, "");
	        obj_hashmap = getFieldOjbect(env,jcc, currentActivityThread,jdd);
	   // newä¸€ä¸ªWeakReferenceå¯¹è±¡java.lang.ref.WeakReference   get_get_obj ==wr.get();
	        jclass class_WeakReference=env->FindClass("java/lang/ref/WeakReference");
	        if(class_WeakReference==NULL){
	        	LOGI("class_WeakReference Not Found ");

	        }
	        jmethodID WeakReference_get_method=env->GetMethodID(class_WeakReference, "get","()Ljava/lang/Object;");
	        if(WeakReference_get_method==NULL){
	        	        	LOGI("WeakReference_get_method Not Found ");

	        	        }
	        jmethodID get_func = env->GetMethodID(class_hashmap,"get","(Ljava/lang/Object;)Ljava/lang/Object;");
	        if(get_func==NULL){
	       	        	        	LOGI("get_func Not Found ");
	       	}
	        jobject get_obj =env->CallObjectMethod(obj_hashmap, get_func,packagename);
	        if(get_obj==NULL){
	        	       	        	        	LOGI("get_obj Not Found ");
	        }
	        jobject get_get_obj = env->CallObjectMethod(get_obj, WeakReference_get_method);
	        if(get_get_obj==NULL){
	        	        	       	        	        	LOGI("get_get_obj Not Found ");
	         }
	        //Strings prepared
	        const char *str = env->GetStringUTFChars(packagename, 0);
	        char parameter1[128];
	        char parameter2[128];
	        char parameter3[128];
	        const char *qz ="/data/data/", *hz1 = "/.cache/classdex.jar" ,*hz2 = "/.cache",*hz3 = "/.cache/";
	        strcpy(parameter1, qz);
	        strcat(parameter1, str);
	        strcat(parameter1, hz1); // parameter1 = /data/data/packagename/.cache/classdex.jar

	        strcpy(parameter2, qz);
	        strcat(parameter2, str);
	       	strcat(parameter2, hz2); // parameter2 = /data/data/packagename/.cache


	       	strcpy(parameter3, qz);
	       	strcat(parameter3, str);
	       	strcat(parameter3, hz3); // parameter2 = /data/data/packagename/.cach/、
	       	env->ReleaseStringUTFChars(packagename, str);
	       	jstring jparameter1 = CharConvertJstring(env,parameter1);
	       	jstring jparameter2 = CharConvertJstring(env,parameter2);
	       	jstring jparameter3 = CharConvertJstring(env,parameter3);
	       	char *loadapk = "android.app.LoadedApk",*mClassLoader = "mClassLoader";
	       	jstring jloadapk= CharConvertJstring(env,loadapk);
	       	jstring jmClassLoader = CharConvertJstring(env,mClassLoader);
		// newä¸€ä¸ªDexClassloaderå¯¹è±¡  dalvik.system.DexClassLoader
	        jclass class_DexClassloader=env->FindClass("dalvik/system/DexClassLoader");
	        if(class_DexClassloader==NULL){
	        			LOGI("class_DexClassloader Not Found ");
	       	         }
	        jmethodID DexClassloader_construct_method=env->GetMethodID(class_DexClassloader, "<init>","(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V");
	        if(DexClassloader_construct_method==NULL){
	       	        	        	       	        	        	LOGI("DexClassloader_construct_method Not Found ");
	       	         }
	        jobject obj_DexClassloader =env->NewObject(class_DexClassloader, DexClassloader_construct_method,jparameter1,jparameter2,jparameter3,getFieldOjbect(env,jloadapk,get_get_obj,jmClassLoader));
	        if(obj_DexClassloader==NULL){
	       	        	        	       	        	        	LOGI("obj_DexClassloader Not Found ");
	       	 }
	        setFieldOjbect(env,jloadapk,jmClassLoader,get_get_obj, obj_DexClassloader);

	        return get_get_obj;
}



jstring   CharConvertJstring(JNIEnv*   env,   char*   str)
{
    jsize   len   =   strlen(str);

    jclass   clsstring   =   env->FindClass("java/lang/String");
    jstring   strencode   =   env->NewStringUTF("GB2312");

    jmethodID   mid   =   env->GetMethodID(clsstring,   "<init>",   "([BLjava/lang/String;)V");
    jbyteArray   barr   =   env-> NewByteArray(len);

    env-> SetByteArrayRegion(barr,0,len,(jbyte*)str);
    return (jstring)env-> NewObject(clsstring,mid,barr,strencode);
}
jobject   getFieldOjbect(JNIEnv *env,
		jstring class_name, jobject obj, jstring fieldName)
{
			jclass context = env->FindClass("java/lang/Class");
			jmethodID forName_func = env->GetStaticMethodID(context,"forName","(Ljava/lang/String;)Ljava/lang/Class;");
			jobject class_obj =	env->CallStaticObjectMethod(context, forName_func,class_name);
			jclass class_java = env->GetObjectClass(class_obj);

			jmethodID getField_func = env->GetMethodID(class_java,"getDeclaredField","(Ljava/lang/String;)Ljava/lang/reflect/Field;");
			jobject method_obj =env->CallObjectMethod(class_obj, getField_func,fieldName);
			jclass class_method_obj= env->GetObjectClass(method_obj);

			jmethodID setaccess_func = env->GetMethodID(class_method_obj,"setAccessible","(Z)V");
			env->CallVoidMethod(method_obj,setaccess_func,true);

			jmethodID get_func = env->GetMethodID(class_method_obj,"get","(Ljava/lang/Object;)Ljava/lang/Object;");
			jobject get_obj =env->CallObjectMethod(method_obj,get_func,obj);

			env->DeleteLocalRef(class_java);
			env->DeleteLocalRef(method_obj);
			return get_obj;

}
jobject  invokeStaticMethodss (JNIEnv *env,jstring class_name, jstring method_name, jobjectArray pareTyple, jobjectArray pareVaules)
{
		jclass context = env->FindClass("java/lang/Class");
		jmethodID forName_func = env->GetStaticMethodID(context,"forName","(Ljava/lang/String;)Ljava/lang/Class;");
		jobject class_obj =	env->CallStaticObjectMethod(context, forName_func,class_name);
		jclass class_java = env->GetObjectClass(class_obj);

		jmethodID getMethod_func = env->GetMethodID(class_java,"getMethod","(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
		jobject method_obj =env->CallObjectMethod(class_obj, getMethod_func,method_name,pareTyple);
		jclass class_method_obj= env->GetObjectClass(method_obj);

		jmethodID invoke_func = env->GetMethodID(class_method_obj,"invoke","(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
		jobject invoke_obj =env->CallObjectMethod(method_obj,invoke_func,NULL,pareVaules);
		env->DeleteLocalRef(class_java);
		env->DeleteLocalRef(method_obj);
		return invoke_obj;
}
void     setFieldOjbect(JNIEnv *env, jstring class_name, jstring fieldName, jobject obj, jobject filedVaule)

{
				jclass context = env->FindClass("java/lang/Class");
				jmethodID forName_func = env->GetStaticMethodID(context,"forName","(Ljava/lang/String;)Ljava/lang/Class;");
				jobject class_obj =	env->CallStaticObjectMethod(context, forName_func,class_name);
				jclass class_java = env->GetObjectClass(class_obj);

				jmethodID getField_func = env->GetMethodID(class_java,"getDeclaredField","(Ljava/lang/String;)Ljava/lang/reflect/Field;");
				jobject method_obj =env->CallObjectMethod(class_obj, getField_func,fieldName);
				jclass class_method_obj= env->GetObjectClass(method_obj);

				jmethodID setaccess_func = env->GetMethodID(class_method_obj,"setAccessible","(Z)V");
				env->CallVoidMethod(method_obj,setaccess_func,true);

				jmethodID set_func = env->GetMethodID(class_method_obj,"set","(Ljava/lang/Object;Ljava/lang/Object;)V");
				env->CallVoidMethod(method_obj,set_func,obj,filedVaule);

				env->DeleteLocalRef(class_java);
				env->DeleteLocalRef(method_obj);



}






