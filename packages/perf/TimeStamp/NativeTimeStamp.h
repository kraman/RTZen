#ifndef TimeStamp_NativeTimeStamp
#define TimeStamp_NativeTimeStamp

#include <sys/types.h>

#ifdef _MSC_VER
#include <time.h>
typedef clock_t uint64_t;
//typedef clock_t u_int64_t;
#else
#include <inttypes.h>
#endif

class NativeTimeStamp 
{
   public:

	  NativeTimeStamp ();

	  virtual ~NativeTimeStamp ();
     
      double GetTime();

      void SetStartTime();

      void Init(int recordingMode, double stopRecording);

      void RecordTime(int type);

      void OutputLogRecords();


   private:
      u_int64_t rdtsc();

      double setCpsVal();

      // We save some values as globals.
      const int DISABLE_RECORDING;
      const int RECORD_ALWAYS_ON;
      const int START_STOP_RECORDING;

      const int START_OF_20HZ;
      const int START_OF_5HZ;
      const int START_OF_1HZ;

      u_int64_t startTime_;
      double    cpsvalDouble_;

      double*   timeStampPtrs_;
      int*      typePtrs_;
      int*      sourceIdPtrs_;
      int       currentRecord_;
      int       maxTimeRecords_;
      int       recordingMode_;
      double    startRecording_;
      double    stopRecording_;
      bool      recordingOn_;

};

#endif











