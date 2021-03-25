#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_vnptit_videocallsample_MyApplication_getAccessToken(JNIEnv *env, jobject thiz) {
    std::string access_token = "";

    return env->NewStringUTF(access_token.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_vnptit_videocallsample_MyApplication_getTokenID(JNIEnv *env, jobject thiz) {
    std::string token_id = "";

    return env->NewStringUTF(token_id.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_vnptit_videocallsample_MyApplication_getTokenKey(JNIEnv *env, jobject thiz) {
    std::string token_key = "";
    return env->NewStringUTF(token_key.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_vnptit_videocallsample_MyApplication_getClientID(JNIEnv *env, jobject thiz) {
    std::string client_id = "";

    return env->NewStringUTF(client_id.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_vnptit_videocallsample_MyApplication_getClientSecret(JNIEnv *env, jobject thiz) {
    std::string client_secret = "";

    return env->NewStringUTF(client_secret.c_str());
}