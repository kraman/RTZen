package edu.uci.ece.zen.utils;

public abstract class Logger{
    public static final int PEDANTIC=0;
    public static final int CONFIG=1;
    public static final int INFO=2;
    public static final int WARN=3;
    public static final int SEVERE=4;
    public static final int FATAL=5;
    protected static final String levelLabels[] = new String[]{ "PEDANTIC" , "CONFIG" , "INFO" , "WARNING" , "SEVERE" , "FATAL" };

    private static Logger _instance;
    public static Logger instance(){
        if( _instance == null ){
            String loggerType = ZenProperties.getGlobalProperty( "edu.uci.ece.zen.logger.type" , "Console" );
            int level = Integer.parseInt( ZenProperties.getGlobalProperty( "edu.uci.ece.zen.logger.level" , "4" ) );
            try{
                Class loggerClass = Class.forName( "edu.uci.ece.zen.utils."+loggerType+"Logger" );
                _instance = (Logger) loggerClass.newInstance();
            }catch( Exception e ){
                _instance = new ConsoleLogger();
                _instance.log( INFO , "edu.uci.ece.zen.utils.Logger" , "instance()" , "Unable to load logger of type " + loggerType
                        + ". Loading ConsoleLogger" );
            }
            _instance.setLevel(level);
        }
        return _instance;
    }
    public abstract void log( int level , String thisClass , String thisFunction , String msg );

    protected int level=0;
    protected void setLevel( int level ){
        //this.level=level;
    }
}

class ConsoleLogger extends Logger{
    protected ConsoleLogger(){}
    public void log( int level , String thisClass , String thisFunction , String msg ){
        if( level >= this.level ){
            System.err.println( Logger.levelLabels[level] + ":" + thisClass + " : " + thisFunction + " : " + msg );
        }
    }
}

class NullLogger extends Logger{
    protected NullLogger(){}
    public void log( int level , String thisClass , String thisFunction , String msg ){
    }
}
