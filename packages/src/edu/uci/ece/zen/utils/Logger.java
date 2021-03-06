/*
 * Copyright (c) 2005 by the University of California, Irvine
 * All Rights Reserved.
 * 
 * This software is released under the terms of the RTZen license, which
 * you should have received along with this software. If not, you may
 * obtain a copy here: http://zen.ece.uci.edu/rtzen/license.php
 */

package edu.uci.ece.zen.utils;

import java.io.PrintStream;
import javax.realtime.*;

/** Class to enable loggin in RTZen
 * @author Krishna Raman
 * @author Mark Panahi
 * @author Gunar Schirne
 */
public abstract class Logger{

    // Logging levels
    public static final int PEDANTIC=0;
    public static final int CONFIG=1;
    public static final int INFO=2;
    public static final int WARN=3;
    public static final int SEVERE=4;
    public static final int FATAL=5;
    protected static final String levelLabels[] =
        new String[]{ "PEDANTIC" ,
            "CONFIG" , "INFO" ,
            "WARNING" , "SEVERE" ,
            "FATAL" };

    /** Static instance of the logger */
    private static Logger instance;

    /** Return an instance of the logger. */
    public static Logger instance(){
        if( instance == null ){
            String loggerType = ZenProperties.getGlobalProperty( "edu.uci.ece.zen.logger.type" , "Console" );
            int level = Integer.parseInt( ZenProperties.getGlobalProperty( "edu.uci.ece.zen.logger.level" , "4" ) );
            try{
                Class loggerClass = Class.forName( "edu.uci.ece.zen.utils."+loggerType+"Logger" );
                instance = (Logger) loggerClass.newInstance();
        System.out.println( "Using " + loggerClass + " at level " + level );
            }catch( Exception e ){

                System.err.println("Logger.instance(): " +
                    "Unable to load logger of type " + loggerType + ". Loading NullLogger.");
                instance = new NullLogger();
                e.printStackTrace();

            }
            instance.setLevel(level);
        }
        return instance;
    }

    public abstract void log(String msg);
    public abstract void log(int level, String msg);
    public abstract void log(String thisFunction, String msg);
    public abstract void log(int level, Class thisClass, String thisFunction, String msg);
    public abstract void log(int level, Class thisClass, String thisFunction, String msg, Throwable e);
    public abstract void log(int level, Class thisClass, String thisFunction, Throwable e);

    protected int level=0;
    protected void setLevel( int level ){
        this.level=level;
    }

    public static void printMemStats(){
        printMemStats(ZenBuildProperties.dbgImmortalId,ImmortalMemory.instance());
    }

    public static void printMemStatsImm(int code){
        if( ZenBuildProperties.dbgImmortal ){
//            printMemStats(code, ImmortalMemory.instance());
        }
    }

    public static void printMemStats(int code){
        MemoryArea ma = RealtimeThread.getCurrentMemoryArea();
        printMemStats(code, ma);

    }
    public static void writeln(long a){
        write(a);
        writeln();
    }
   public static void writeln(){
        System.out.write( '\n' );
        System.out.flush();
    }
    public static void write(long a){
        if(a < 0){
            a = -a;
            System.out.write( '-' );
        }
        for( long i = 10000000000L ; i > 0 ; i /= 10 ){
            byte b = (byte) (a/i);
            System.out.write( b + '0' );
            a -= (b*i);
        }
        //System.out.flush();
    }
    public static void printTracePoint( int pos ){
        System.out.write( 'C' );
        System.out.write( 'p' );
        System.out.write( 'o' );
        System.out.write( 's' );
        System.out.write( ' ' );
        write( pos );
        writeln();
    }

    private static long memAreaSizes[];
    synchronized public static void printMemStats(int code, MemoryArea ma){
        if( !ZenBuildProperties.dbgImmortal )
            return;
        /*
        if( !ZenBuildProperties.dbgMap[code] )
            return;
        if( memAreaSizes == null )
            memAreaSizes = new long[10];*/
        long mem = ma.memoryConsumed();
        long rem = ma.memoryRemaining();/*
        if( memAreaSizes[code] >= mem )
            return;
        memAreaSizes[code] = mem;
        */
        write(code);
        System.out.write( ',' );
        write(mem);
        System.out.write( ',' );
        write(rem);
        if(ma instanceof ScopedMemory){
            System.out.write( ',' );
            write(((ScopedMemory)ma).getReferenceCount());
        }
        System.out.write( '\n' );
        System.out.write( '\n' );
        System.out.flush();
    }

    public static void printMemStats(edu.uci.ece.zen.orb.ORB orb){
        printMemStats(0,orb.parentMemoryArea);
        printMemStats(1,orb.orbImplRegion);
        printMemStats();
    }

    public static void printThreadStack(){
        if (edu.uci.ece.zen.utils.ZenBuildProperties.dbgThreadStack)
        {
            System.out.println("Current thread is " + RealtimeThread.currentRealtimeThread());
            System.out.println("cur mem area: " +  RealtimeThread.getCurrentMemoryArea());

            int curInd = RealtimeThread.getMemoryAreaStackDepth()-1;
            System.out.println("cur mem stack pos: " + curInd);

            for(int i = curInd; i >= 0; --i)
                System.out.println("mem area at pos " + i + " is " + RealtimeThread.getOuterMemoryArea(i));
        }
    }

}

class ConsoleLogger extends Logger
{
    protected PrintStream printStream;

    protected ConsoleLogger()
    {
        printStream = System.err;
    }

    protected ConsoleLogger(PrintStream printStream)
    {
        this.printStream = printStream;
    }

    public void log(String msg) {
        log(null, msg);
    }

    public void log(String thisFunction, String msg) {
        log(Logger.INFO, null, thisFunction, msg);
    }
    public void log(int level, String msg) {
        if( level >= this.level ){
            printStream.print( Logger.levelLabels[level] );
            printStream.print(":");


            log(null, msg);
        }

    }

    public void log(int level, Class thisClass, String thisFunction, String msg) {
        if( level >= this.level ){
            printStream.print( Logger.levelLabels[level] );
            printStream.print(":");

            if (thisClass != null)
            {
                printStream.print(thisClass.getName());
                printStream.print(" : ");
            }

            if (thisFunction != null)
            {
                printStream.print(thisFunction);
                printStream.print(" : ");
            }

            printStream.println(msg);
        }
    }

    public void log(int level, Class thisClass, String thisFunction, String msg, Throwable e)
    {
        log(level, thisClass, thisFunction, msg);
        //e.printStackTrace(printStream);
        printStream.println(e);
    }

    public void log(int level, Class thisClass, String thisFunction, Throwable e)
    {
        log(level, thisClass, thisFunction, "", e);
    }
}

class NullLogger extends Logger{
    protected NullLogger(){}
    public void log(String msg) {}
    public void log(int level, String msg) {}
    public void log(String thisFunction, String msg) {}
    public void log(int level, Class thisClass, String thisFunction, String msg) {}
    public void log(int level, Class thisClass, String thisFunction, String msg, Throwable e) {}
    public void log(int level, Class thisClass, String thisFunction, Throwable e) {}
}
