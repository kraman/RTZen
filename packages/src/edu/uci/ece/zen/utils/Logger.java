package edu.uci.ece.zen.utils;

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
            }catch( Exception e ){
				System.err.println("Logger.instance(): " +
					"Unable to load logger of type " + loggerType + ". Loading NullLogger.");
				instance = new NullLogger(); 
            }
            instance.setLevel(level);
        }
        return instance;
    }

	public abstract void log(String msg);
	public abstract void log(String thisFunction, String msg);
    public abstract void log(int level, Class thisClass, String thisFunction, String msg);
    public abstract void log(int level, Class thisClass, String thisFunction, String msg, Throwable e);
    public abstract void log(int level, Class thisClass, String thisFunction, Throwable e);

    protected int level=0;
    protected void setLevel( int level ){
        this.level=level;
    }

    public static void printMemStats(){
        printMemStats(-1);
    }
    
    
    public static void printMemStats(int code){
    
        MemoryArea ma = RealtimeThread.getCurrentMemoryArea();
        printMemStats(code, ma);
    
    }
        
    public static void printMemStats(int code, MemoryArea ma){
	    long mem = ma.memoryConsumed();
        //System.out.println(ma.memoryConsumed()+","+ma.memoryRemaining());            
        if(edu.uci.ece.zen.utils.ZenProperties.memDbg){
             if(ma instanceof ScopedMemory)
                perf.cPrint.nativePrinter.print(code,(int)mem,((ScopedMemory)ma).getReferenceCount());
            else
                perf.cPrint.nativePrinter.print(code,(int)mem,0);


        }
    }
      
    public static void printMemStats(edu.uci.ece.zen.orb.ORB orb){
	    printMemStats(0,orb.parentMemoryArea);
	    printMemStats(1,orb.orbImplRegion);
        //System.out.println("orb,"+orb.orbImplRegion.memoryConsumed()+","+orb.orbImplRegion.memoryRemaining());            
	    //System.out.println("client,"+orb.parentMemoryArea.memoryConsumed()+","+orb.parentMemoryArea.memoryRemaining());            

    }
    
    public static void printThreadStack(){
		if (edu.uci.ece.zen.utils.ZenProperties.devDbg) {
			System.out.println("Current thread is " + RealtimeThread.currentRealtimeThread());
			System.out.println("cur mem area: " +  RealtimeThread.getCurrentMemoryArea());            
            
			int curInd = RealtimeThread.getMemoryAreaStackDepth()-1;
			System.out.println("cur mem stack pos: " + curInd);
			
			for(int i = curInd; i >= 0; --i)
				System.out.println("mem area at pos " + i + " is " + RealtimeThread.getOuterMemoryArea(i));
		}
    }

}

class ConsoleLogger extends Logger{
    protected ConsoleLogger(){}
	
    public void log(String msg) {
		log(null, msg);
	}

	public void log(String thisFunction, String msg) {
		log(Logger.INFO, null, thisFunction, msg);
	}
    public void log(int level, Class thisClass, String thisFunction, String msg) {
        if( level >= this.level ){
            System.err.print( Logger.levelLabels[level] + ":");
			
			if (thisClass != null)
				System.err.print(thisClass.getName() + " : ");
			
			if (thisFunction != null)
				System.err.print(thisFunction + " : ");
			
			System.err.println(msg);
        }
    }
    public void log(int level, Class thisClass, String thisFunction, String msg, Throwable e)
	{
		log(level, thisClass, thisFunction, msg);
		e.printStackTrace();
	}
    public void log(int level, Class thisClass, String thisFunction, Throwable e)
	{
		log(level, thisClass, thisFunction, "", e);
	}
}

class NullLogger extends Logger{
    protected NullLogger(){}
    public void log(String msg) {}
    public void log(String thisFunction, String msg) {}
    public void log(int level, Class thisClass, String thisFunction, String msg) {}
    public void log(int level, Class thisClass, String thisFunction, String msg, Throwable e) {}
    public void log(int level, Class thisClass, String thisFunction, Throwable e) {}
}
