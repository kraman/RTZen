package edu.uci.ece.zen.orb.transport.serial;


/**
Factory methods for simplifying the creation of serial port drivers.
*/
public class SerialPortFactory
{
    private static SerialPort _instance;

    public static void setSerialPort(SerialPort sp){
        _instance = sp;
    }

    public synchronized static SerialPort instance(){

        if (_instance == null)
        {
            try{
                _instance = (NativeSerialPort) javax.realtime.ImmortalMemory.instance().newInstance( NativeSerialPort.class );
            }catch(java.lang.IllegalAccessException e){
                //for some reason, non-rt jvm comes here
                _instance = new NativeSerialPort();
            }catch(java.lang.InstantiationException e){
                e.printStackTrace();
            }
        }

        return _instance;
    }


}
