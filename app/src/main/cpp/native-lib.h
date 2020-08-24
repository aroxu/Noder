//
// Created by 윤규도 on 2020/08/15.
//

#ifndef NODEEXAMPLE_NATIVE_LIB_H
#define NODEEXAMPLE_NATIVE_LIB_H

#include <jni.h>
#include <android/log.h>
#include <vector>

#define LOGD(...) (__android_log_print(ANDROID_LOG_DEBUG, "Node.js", __VA_ARGS__))

extern "C" {
JNIEXPORT void JNICALL
Java_me_b1ackange1_noder_StarterService_startNode(JNIEnv *env, jobject instance,
                                                jobjectArray args);
JNIEXPORT void JNICALL
Java_me_b1ackange1_noder_StarterService_stopNode(JNIEnv *env, jobject instance);
}

namespace noder {
    void enableLogging();
    void redirectStreamsToPipe();
    void startLoggingFromPipe();
    std::vector<char> makeContinuousArray(JNIEnv *env, jobjectArray fromArgs);
    std::vector<char*> getArgv(std::vector<char>& fromContinuousArray);
}
#endif