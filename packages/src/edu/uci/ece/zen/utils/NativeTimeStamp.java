package edu.uci.ece.zen.utils;

import javax.realtime.AbsoluteTime;
import javax.realtime.Clock;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.IOException;

public class NativeTimeStamp {
    public static final int DISABLE_RECORDING = 0;
    public static final int RECORD_ALWAYS_ON = 1;
    public static final int START_STOP_RECORDING = 2;

    public static final int START_OF_20HZ = 7; 
    public static final int START_OF_5HZ = 4;
    public static final int START_OF_1HZ = 1;

    private static double    startTime;

    private static double[]  timeStampPtrs_;
    private static int[]     typePtrs_;
    private static int       currentRecord_;
    private static int       maxTimeRecords_;
    private static int       recordingMode_;
    private static double    startRecording_;
    private static double    stopRecording_;
    private static boolean   recordingOn_;

    /** Measurement units per second */
    private static final double cpsvalDouble_ = 1000000000;
    /** Stored absolute time */
    private static final AbsoluteTime at = new AbsoluteTime();

    public static void Init(int recordingMode, double stopRecording)
    {
        double DURATION           = 20.0; // 20 second duration.
        double REGISTRATION_DELTA = 1.0; 

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
        currentRecord_  = 0;

        int RESIDUAL_RECORDING_RECORDS = 4;
        maxTimeRecords_ -= RESIDUAL_RECORDING_RECORDS;

        Clock.getRealtimeClock().getTime(at);
        startTime = getAbsoluteTime();
    }

    /**
     * Gets absolute time. Doesn't affect stored initial time.
     * @return absolute time in nanoseconds.
     */
    private static double getAbsoluteTime( ) {
    Clock.getRealtimeClock().getTime(at);
    return (double) getNanoseconds(at);
    }
    /**
     * Gets relative time. Doesn't affect stored initial time.
     * @return Relative time elapsed from the initial time.  
     */
    private static double getRelativeTime( ) {
    return (getAbsoluteTime() - startTime);
    }
    /**
     * Converts <code>HighPrecisionTime</code> to nanoseconds.
     * @param time to convert
     * @return converted time in nanoseconds
     */
    private static long getNanoseconds(AbsoluteTime time) {
    return time.getNanoseconds() + time.getMilliseconds() * 1000000; 
    }

    public static double GetTime()
    {
        // This gives microsecond accuracy (i.e. each tic is 1 microsecond).
        return getRelativeTime() / cpsvalDouble_;
    }
    
    public static void SetStartTime()
    {
    //    ofstream oFile("/usr/local/bin/StartTime.txt", ios::out );
    //    oFile << rdtsc();
    //    oFile.close();
    }

    public synchronized static void RecordTime(int type)
    {
        if (recordingMode_ != DISABLE_RECORDING)
        {
            if (currentRecord_ < maxTimeRecords_)
            {
                double recordTime =  GetTime();
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
                System.out.println("ERROR - RAN OUT OF TIME RECORD SPACE!");
                recordingMode_ = DISABLE_RECORDING;
            }
        }
    }

    public static void OutputLogRecords()
    {
        System.out.println ("******* timeRecordIndex_ = " + currentRecord_ + "  **********************");
    try {
            PrintWriter file = new PrintWriter(new FileOutputStream("timeRecords.txt"));
            for (int i = 0; i < currentRecord_; ++i)
            {
                file.print("" + i);
                file.print("," + typePtrs_[i]);
                file.println("," + timeStampPtrs_[i]);
            }
            file.close();
        } catch (IOException e ) {
        System.out.println("Error writing to file: " + e);
        }
    }
}
