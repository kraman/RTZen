package javax.realtime;

/**
 * This class provides a stub for the RTSJ memory regions. It is used on Java 
 * implementations that dont support RTSJ as a placeholder so that ZEN can
 * compile.
 */
public final class ImmortalMemory
    extends MemoryArea{

    private static ImmortalMemory singleton = null;

    public static ImmortalMemory instance(){
        if( singleton == null )
            singleton = new ImmortalMemory();
        return singleton;
    }
}
