import java.io.OutputStream;
import java.io.FileOutputStream;

public class IsoLeakHelper
{
    private static OutputStream out = null;
    public synchronized static void init(){
        if( out == null ){
            try{
                long l = (new java.util.Random()).nextLong();
                out = new FileOutputStream( "mem" + l + ".log" );
                System.out.println( "IsoLeak output printed to mem" + l + ".log" );
            }catch( Exception e ){
                e.printStackTrace();
            }
        }
    }

    public synchronized static final long __isoLeak_recordScopedSize(){
        init();
        try{
            out.write( 'I' );
            out.write( 'S' );
            out.write( 'o' );
            out.write( 'L' );
            out.write( '1' );
            out.write( ',' );
            __isoLeak_write( javax.realtime.RealtimeThread.getCurrentMemoryArea().hashCode() );
            out.write( ',' );
            __isoLeak_write( javax.realtime.RealtimeThread.currentRealtimeThread().hashCode() );
            out.write( '\n' );
        }catch( Exception e ){
            e.printStackTrace();
        }
        return javax.realtime.RealtimeThread.getCurrentMemoryArea().memoryConsumed();
    }

    public static final long __isoLeak_recordImmortal(){
        return javax.realtime.ImmortalMemory.instance().memoryConsumed();
    }

    public synchronized static final void __isoLeak_checkMemStats( long methodId , long oldMemorySpace , long oldImmortalSpace ){
        try{
            out.write( 'I' );
            out.write( 'S' );
            out.write( 'o' );
            out.write( 'L' );
            out.write( '2' );
            out.write( ',' );
            __isoLeak_write( methodId );
            out.write( ',' );
            __isoLeak_write( javax.realtime.RealtimeThread.getCurrentMemoryArea().hashCode() );
            out.write( ',' );
            __isoLeak_write( javax.realtime.RealtimeThread.currentRealtimeThread().hashCode() );
            out.write( ',' );
            if( javax.realtime.RealtimeThread.getCurrentMemoryArea() instanceof javax.realtime.ScopedMemory ){
                Object portal = ((javax.realtime.ScopedMemory)javax.realtime.RealtimeThread.getCurrentMemoryArea()).getPortal();
                if( portal instanceof IsoLeakAnotated ){
                    __isoLeak_write(((IsoLeakAnotated)portal).__isoLeak_classId());
                }else{
                    __isoLeak_write( -1 );
                }
            }else{
                __isoLeak_write( -2 );
            }

            if( oldMemorySpace !=  javax.realtime.RealtimeThread.getCurrentMemoryArea().memoryConsumed() ){
                out.write( ',' );
                out.write( 'S' );
                out.write( 'l' );
                out.write( 'e' );
                out.write( 'a' );
                out.write( 'k' );
                __isoLeak_write( javax.realtime.RealtimeThread.getCurrentMemoryArea().memoryConsumed() - oldMemorySpace ); 
            }

            if( oldImmortalSpace != javax.realtime.ImmortalMemory.instance().memoryConsumed() ){
                out.write( ',' );
                out.write( 'I' );
                out.write( 'l' );
                out.write( 'e' );
                out.write( 'a' );
                out.write( 'k' );
                __isoLeak_write( javax.realtime.ImmortalMemory.instance().memoryConsumed() - oldImmortalSpace );
            }
            out.write( '\n' );
        }catch( Exception e ){
            e.printStackTrace();
        }
    }

    private static void __isoLeak_write(long a) throws Exception{
        if(a < 0){
            a = -a;
            out.write( '-' );
        }
        for( long i = 10000000000L ; i > 0 ; i /= 10 ){
            byte b = (byte) (a/i);
            out.write( b + '0' );
            a -= (b*i);
        }
    }
}
