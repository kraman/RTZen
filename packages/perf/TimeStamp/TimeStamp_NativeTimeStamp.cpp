// This file is copyrighted by McDonnell-Douglas Corporation, a wholly
// owned subsidiary of The Boeing Company, Copyright (c) 2002, all rights
// reserved. This file is open source, free software, you are free to
// use, modify, and distribute the source code and object code produced
// from the source, as long as you include this copyright statement,
// along with code built using this file.

// In particular, you can use this file in proprietary software and are
// under no obligation to redistribute any of your source code that is
// built using this file. Note, however, that you may not do anything to
// this file code, such as copyrighting it yourself or claiming
// authorship of this code, that will prevent this file from being
// distributed freely using an open source development model.

// Warranty
// This file is provided as is, with no warranties of any kind, including
// the warranties of design, merchantability and fitness for a particular
// purpose, non-infringement, or arising from a course of dealing, usage
// or trade practice. Moreover, this file is provided with no support and
// without any obligation on the part of McDonnell-Douglas, its
// employees, or others to assist in its use, correction, modification,
// or enhancement.

// Liability
// McDonnell-Douglas, its employees, and agents have no liability with 
// respect to the infringement of copyrights, trade secrets or any 
// patents by this file thereof. Moreover, in no event will 
// McDonnell-Douglas, its employees, or agents be liable for any lost
// revenue or profits or other special, indirect and consequential 
// damages.

// Acknowledgement
// This work was sponsored by the US Air Force Research Laboratory
// Information Directorate, Wright-Patterson Air Force Base.
//
// TimeStamp_NativeTimeStamp.cpp
//  Implementation for class TimeStamp_NativeTimeStamp

//  This file is used to create a shared library containing calls used by
//  java programs.  The java native interface is used to make the calls.

//  The methods it contains are used to measure elapsed time in java tests
//  implemented to the Real-Time Specification for Java (RTSJ) spec.

//  The file can be build for either Timesys Linux RT, or
//  QNX RTP, depending on whether the makefile defines LINUX or QNX.

#include "TimeStamp_NativeTimeStamp.h"
#include "NativeTimeStamp.h"
#ifndef _MSC_VER
#include <inttypes.h>
#endif
NativeTimeStamp nativeTimeStamp_;

JNIEXPORT void JNICALL Java_perf_TimeStamp_NativeTimeStamp_Init
(JNIEnv *env, jclass jco, jint recordingMode, jdouble stopRecording)
{
    nativeTimeStamp_.Init(recordingMode, stopRecording);
}

JNIEXPORT jdouble JNICALL Java_perf_TimeStamp_NativeTimeStamp_GetTime
(JNIEnv *env, jclass juo)
{
    return nativeTimeStamp_.GetTime();
}



JNIEXPORT void JNICALL Java_perf_TimeStamp_NativeTimeStamp_RecordTime
(JNIEnv *, jclass, jint type)
{
    nativeTimeStamp_.RecordTime(type);
}

JNIEXPORT void JNICALL Java_perf_TimeStamp_NativeTimeStamp_OutputLogRecords
(JNIEnv *, jclass)
{
    nativeTimeStamp_.OutputLogRecords();
}


