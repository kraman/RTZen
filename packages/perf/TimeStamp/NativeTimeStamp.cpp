// TimeStamp_NativeTimeStamp.cpp
//  Implementation for class TimeStamp_NativeTimeStamp

//  This file is used to create a shared library containing calls used by
//  java programs.  The java native interface is used to make the calls.

//  The methods it contains are used to measure elapsed time in java tests
//  implemented to the Real-Time Specification for Java (RTSJ) spec.

//  The file can be build for either Timesys Linux RT, or
//  QNX RTP, depending on whether the makefile defines LINUX or QNX.

#include "NativeTimeStamp.h"
#include <string>

#ifndef _MSC_VER
#include <inttypes.h>
#endif
#include <stdlib.h>
#include <stdio.h>
#ifndef _MSC_VER
#include <pthread.h>
#endif
#include <time.h>
#ifndef _MSC_VER
#include <unistd.h>
#include <sys/time.h>
#else
#include <time.h>
#endif


#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>


NativeTimeStamp::NativeTimeStamp()
: DISABLE_RECORDING(0)
, RECORD_ALWAYS_ON(1)
, START_STOP_RECORDING (2)
, START_OF_20HZ(7)
, START_OF_5HZ(4)
, START_OF_1HZ(1)
{
}

NativeTimeStamp::~NativeTimeStamp()
{
}
double NativeTimeStamp::setCpsVal()
{
#ifndef _MSC_VER
    const int LINE_LENGTH = 80;

    char inLine[LINE_LENGTH + 1];
    FILE* cpuInfo;
    char  firstString[LINE_LENGTH];
    char  secondString[LINE_LENGTH];
    char  thirdString[LINE_LENGTH];
    double cpuMHzDoub;

    // Open file /proc/cpuinfo.
    cpuInfo = fopen("/proc/cpuinfo", "r");

    // Parse through it looking for the string "cpu MHz"
    while (fgets(inLine, LINE_LENGTH, cpuInfo) != NULL)
    {
        if (strncmp(inLine,"cpu MHz", strlen("cpu MHz")) == 0)
        {
            // Parse the line including the cpu MHz value.
            sscanf(inLine, "%s%*c%s%*c%s%*c%lf%*c", firstString, secondString, thirdString, &cpuMHzDoub);

            break;      
        }

    }

    fclose(cpuInfo);

    // Now return the cpu speed.
    return(cpuMHzDoub * 1000000.0);
#else
    return static_cast<double>(CLOCKS_PER_SEC);
#endif
}
// This procedure attempts to read TSC on linux.
u_int64_t  NativeTimeStamp::rdtsc()
{
   u_int64_t   d = 0;

#ifndef _MSC_VER
   __asm__ __volatile__ ("rdtsc" : "=&A" (d));
#else
   d = clock();
#endif

   return d;
}

void NativeTimeStamp::Init(int recordingMode, double stopRecording)
{
    const double DURATION           = 20.0; // 20 second duration.
    const double REGISTRATION_DELTA = 1.0; 

    recordingMode_   = recordingMode;
    stopRecording_   = stopRecording + REGISTRATION_DELTA;
    startRecording_  = stopRecording - DURATION - REGISTRATION_DELTA;

    if (recordingMode_ == DISABLE_RECORDING)
    {
        maxTimeRecords_         = 1;
        recordingOn_     = false;
    }
    else if (recordingMode_ == RECORD_ALWAYS_ON)
    {
        maxTimeRecords_         = 3600000;
        recordingOn_     = true;
    }
    else
    {
        maxTimeRecords_         = 3600000;
        recordingOn_     = false;
    }

    timeStampPtrs_  = new double[maxTimeRecords_];
    typePtrs_       = new int[maxTimeRecords_];
    sourceIdPtrs_   = 0;
    currentRecord_  = 0;

    const int RESIDUAL_RECORDING_RECORDS = 4;
    maxTimeRecords_ -= RESIDUAL_RECORDING_RECORDS;

    cpsvalDouble_        = setCpsVal();
//    ifstream iFile("/usr/local/bin/StartTime.txt", ios::in );
//    iFile >> startTime_;
//    iFile.close();
	startTime_ = rdtsc();
}

double NativeTimeStamp::GetTime()
{
    // This gives microsecond accuracy (i.e. each tic is 1 microsecond).
    return((double)(rdtsc() - startTime_))/ cpsvalDouble_;
}
    
void NativeTimeStamp::SetStartTime()
{
//    ofstream oFile("/usr/local/bin/StartTime.txt", ios::out );
//    oFile << rdtsc();
//    oFile.close();
}

void NativeTimeStamp::RecordTime(int type)
{
    if (recordingMode_ != DISABLE_RECORDING)
    {
        if (currentRecord_ < maxTimeRecords_)
        {
		  double recordTime =  ((double)(rdtsc() - startTime_));
            if (recordingOn_)
            {
                if (stopRecording_ >= recordTime)
                {
                    timeStampPtrs_[currentRecord_] = recordTime;
                    typePtrs_[currentRecord_] = type;
                    currentRecord_++;
                }
                else
                {
                    timeStampPtrs_[currentRecord_] = recordTime;
                    typePtrs_[currentRecord_] = type;
                    currentRecord_++;

                    // wait for convient place to stop recording.
                    if (type == START_OF_1HZ)
                    {
                        recordingMode_ = DISABLE_RECORDING;
                    }
                }
            }
            else
            {
                if (recordTime >= startRecording_)
                {
                    // wait for convient time to start recording.
                    if (type == START_OF_20HZ)
                    {
                        timeStampPtrs_[currentRecord_] = recordTime;
                        typePtrs_[currentRecord_] = type;
                    }
                    else if (type == START_OF_5HZ)
                    {
                        timeStampPtrs_[currentRecord_ + 1] = recordTime;
                        typePtrs_[currentRecord_ + 1] = type;
                    }
                    else if (type == START_OF_1HZ)
                    {
                        // Now here is a good starting place.
                        timeStampPtrs_[currentRecord_ + 2] = recordTime;
                        typePtrs_[currentRecord_ + 2] = type;
                        currentRecord_ += 3;
                        recordingOn_ = true;
                    }
                }
            }
        }
        else
        {
            printf("ERROR - RAN OUT OF TIME RECORD SPACE!\n");
            recordingMode_ = DISABLE_RECORDING;
        }
    }
}

void NativeTimeStamp::OutputLogRecords()
{
    printf ("******* timeRecordIndex_ = %d **********************\n",currentRecord_);
    FILE* timeRecordsFile;

    timeRecordsFile = fopen ("timeRecords.txt", "w");


    for (int i = 0; i < currentRecord_; ++i)
    {
        fprintf (timeRecordsFile, "%d", i);
        fprintf (timeRecordsFile, ",%d", typePtrs_[i]);
        fprintf (timeRecordsFile, ",%.8f", timeStampPtrs_[i]/cpsvalDouble_);
        fprintf (timeRecordsFile, "\n");
    }
    fclose (timeRecordsFile);
}



