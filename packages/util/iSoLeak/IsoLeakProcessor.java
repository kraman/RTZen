import java.util.*;
import java.io.*;

class LeakInfo{
    public Integer methodId;
    public Integer size;

    public LeakInfo( Integer methodId , Integer size ){
        this.methodId = methodId;
        this.size = size;
    }
}

class IsoLeakMemRegion{
    Integer regionId;
    Vector leakyMethods;
    Vector memoryAllocationTimeline;
    IsoLeakMemRegion parent;
    String portalObject;
    boolean isRoot = false;
    int leakCount = 0;
    int leakBegin = 0;

    public IsoLeakMemRegion( Integer regionId ){
        this.regionId = regionId;
        leakyMethods = new Vector();
        memoryAllocationTimeline = new Vector();
    }

    public IsoLeakMemRegion( Integer regionId , boolean isRoot ){
        this( regionId );
        this.isRoot = isRoot;
    }

    public void setPortal( String portalObject ){
        this.portalObject = portalObject;
    }

    public void setParent( IsoLeakMemRegion parent ){
        if( parent == this )
            return;
        IsoLeakMemRegion i = parent;
        while( i != null ){
            if( i == this )
                return;
            i = i.parent;
        }
        this.parent = parent;
    }

    public String toString(){
        return regionId + "[parent="+(parent==null?null:parent.regionId)+" ,portalObject="+portalObject+", isRoot="+isRoot+"]";
    }

    public void addMemLeakInfo( Integer size , boolean leakDetect , Integer methodId ){
        Integer oldSize = new Integer(0);
        if( memoryAllocationTimeline.size() > 0 )
            oldSize = (Integer) memoryAllocationTimeline.lastElement();

        if( size == null  )
            size = oldSize;
        else{
            if( leakDetect ){
                leakyMethods.add( new LeakInfo( methodId , size ) );
                leakCount++;
            }
            size = new Integer( size.intValue() + oldSize.intValue() );
        }

        memoryAllocationTimeline.add( size );
    }

    public void markBegin(){
        leakBegin = memoryAllocationTimeline.size();
    }
}

public class IsoLeakProcessor{
    static String mapFile      =null;
    static String inputFile    =null;
    static Hashtable classNameMap = new Hashtable();
    static Hashtable methodNameMap = new Hashtable();
    static Hashtable memoryMap = new Hashtable();
    static Hashtable threadMap = new Hashtable();
    
    public static void main( String args[] ){
        if( args.length != 2 ){
            System.err.println( "Usage:\n\tjava IsoLeakProcessor <map file> <input file>" );
            return;
        }else{
            mapFile = args[0];
            inputFile = args[1];
        }

        readMapFile();
        System.out.println( "Read map" );
        readDataFile();
        System.out.println( "Read Data" );
        writeDotFile();
        System.out.println( "Wrote dot" );
        writeMemLeakFile();
        System.out.println( "Wrote Scoped Leak Info" );
    }

    private static void writeMemLeakFile(){
        try{
            Enumeration keys = memoryMap.keys();
            while( keys.hasMoreElements() ){
                Object key = keys.nextElement();
                IsoLeakMemRegion memory = (IsoLeakMemRegion) memoryMap.get( key );
                String portalObject = memory.portalObject;
                if( portalObject != null )
                    portalObject = portalObject.substring( portalObject.lastIndexOf('.')+1 );

                PrintStream out = new PrintStream( new FileOutputStream( key.toString()+".txt" ) );
                Vector leakInfo = memory.memoryAllocationTimeline;
                for( int i=0;i<leakInfo.size();i++ ){
                    out.println( leakInfo.elementAt(i).toString() );
                }
                out.close();
                out = new PrintStream( new FileOutputStream( "genGraph.sh" ) );
                out.println( "set term jpeg" );
                out.println( "set output \"" + key.toString() + ".jpg\"" );
                out.println( "set title \""+portalObject+"\"" );
                out.println( "set ylabel \"bytes\"" );
                out.println( "set xlabel \"time\"" );
                out.println( "set grid" );
                out.println( "set arrow from " + memory.leakBegin + ",0 to "+ memory.leakBegin+",1000000" );
                out.println( "plot \"" + key.toString() + ".txt\" with lines" );
                out.close();
                Process p = Runtime.getRuntime().exec( "/usr/bin/gnuplot genGraph.sh" );
                p.waitFor();
            }
        }catch( Exception e ){
            e.printStackTrace();
        }
    }

    private static void writeDotFile(){
        try{
            PrintStream out = new PrintStream( new FileOutputStream( "out.dot" ) );
            out.println( "digraph zen_mem_hierarchy{ " );
            Enumeration keys = memoryMap.keys();
            while( keys.hasMoreElements() ){
                Object key = keys.nextElement();
                IsoLeakMemRegion memory = (IsoLeakMemRegion) memoryMap.get( key );
                String portalObject = memory.portalObject;
                if( portalObject != null ){
                    portalObject = portalObject.substring( portalObject.lastIndexOf('.')+1 );
                }else{
                    if( memory.parent == null )
                        portalObject += " (Application)";
                    else
                        portalObject += " (Temporary)";
                }
                switch( memory.leakCount ){
                    case 0:
                        out.println( memory.regionId + " [ label=\"" + portalObject + "\" href=\"" + key + ".jpg\" style=\"filled\" fillcolor=\"#aaffaa\" color=\"#000000\" ]" );
                        break;
                    case 1:
                    case 2:
                        out.println( memory.regionId + " [ label=\"" + portalObject + "\" href=\"" + key + ".jpg\" style=\"filled\" fillcolor=\"#aaff00\" color=\"#000000\" ]" );
                        break;
                    default:
                        out.println( memory.regionId + " [ label=\"" + portalObject + "\" href=\"" + key + ".jpg\" style=\"filled\" fillcolor=\"#ffaaaa\" color=\"#000000\" ]" );
                        break;
                }
            }
            keys = memoryMap.keys();
            while( keys.hasMoreElements() ){
                IsoLeakMemRegion memory = (IsoLeakMemRegion) memoryMap.get( keys.nextElement() );
                if( memory.parent != null ){
                    out.println( memory.parent.regionId + "->" + memory.regionId );
                }
            }
            out.flush();
            out.println( "}" );
            out.close();
            Process p = Runtime.getRuntime().exec( "/usr/bin/dot -Tjpeg out.dot -o memAreas.jpeg" );
            p.waitFor();
            p = Runtime.getRuntime().exec( "/usr/bin/dot -Tcmapx out.dot -o memAreas.map" );
            p.waitFor();
            
            BufferedReader bin = new BufferedReader( new InputStreamReader( new FileInputStream( "memAreas.map" ) ) );
            out = new PrintStream( new FileOutputStream( "index.html" ) );
            out.println( "<img src=\"memAreas.jpeg\" ismap usemap=\"#zen_mem_hierarchy\" border=0/>" );
            String str;
            while( (str = bin.readLine()) != null )
                out.println( str );
            out.close();
        }catch( Exception e ){
            e.printStackTrace();
        }
    }

    private static void readDataFile(){
        try{
            BufferedReader bin = new BufferedReader( new InputStreamReader( new FileInputStream( inputFile ) ) );
            String line;
            boolean isRoot = true;
            boolean leakDetect = false;
            while( (line = bin.readLine()) != null ){
                if( line.startsWith( "ISoL1" ) ){
                    StringTokenizer strtok = new StringTokenizer( line , "," );
                    strtok.nextToken();
                    Integer scopedMemoryId = new Integer( strtok.nextToken() );
                    Integer threadId = new Integer( strtok.nextToken() );
                    
                    IsoLeakMemRegion memory = (IsoLeakMemRegion)memoryMap.get( scopedMemoryId );
                    if( memory == null ){
                        memory = new IsoLeakMemRegion( scopedMemoryId , isRoot );
                        if( isRoot ){ isRoot = false; }
                        memoryMap.put( scopedMemoryId , memory );
                    }
                    Stack thread = (Stack)threadMap.get( threadId );
                    if( thread == null ){
                        thread = new Stack();
                        threadMap.put( threadId , thread );
                    }
                    
                    if( thread.size() > 0 )
                        memory.setParent( (IsoLeakMemRegion)thread.peek() );
                    thread.push( memory );
                }
                if( line.startsWith( "ISoL2" ) ){
                    StringTokenizer strtok = new StringTokenizer( line , "," );
                    strtok.nextToken();
                    Integer methodId = new Integer( strtok.nextToken() );
                    Integer scopedMemoryId = new Integer( strtok.nextToken() );
                    Integer threadId = new Integer( strtok.nextToken() );
                    Integer portalId = new Integer( strtok.nextToken() );
                    String memLeakInfo = null;
                    if( strtok.hasMoreTokens() )
                        memLeakInfo = strtok.nextToken();

                    String portalObject = (String) classNameMap.get( portalId );
                    IsoLeakMemRegion memory = (IsoLeakMemRegion) memoryMap.get( scopedMemoryId );
                    if( memory == null ){
                        memory = new IsoLeakMemRegion( scopedMemoryId , isRoot );
                        if( isRoot ){ isRoot = false; }
                        memoryMap.put( scopedMemoryId , memory );
                    }
                    Stack thread = (Stack) threadMap.get( threadId );
                    if( thread == null ){
                        thread = new Stack();
                        threadMap.put( threadId , thread );
                    }

                    if( memLeakInfo != null && memLeakInfo.startsWith( "Sleak" ) )
                        memory.addMemLeakInfo( new Integer( memLeakInfo.substring( 6 ) ) , leakDetect , methodId );
                    else
                        memory.addMemLeakInfo( null , leakDetect , methodId );
                    
                    memory.setPortal( portalObject );
                    thread.pop();
                }
                if( line.startsWith( "ISoBeginLeakDetect" ) ){
                    leakDetect = true;
                    Enumeration keys = memoryMap.keys();
                    while( keys.hasMoreElements() ){
                        Object key = keys.nextElement();
                        IsoLeakMemRegion memory = (IsoLeakMemRegion) memoryMap.get( key );
                        memory.markBegin();
                    }
                }
            }
        }catch( Exception e ){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void readMapFile(){
        try{
            BufferedReader bin = new BufferedReader( new InputStreamReader( new FileInputStream( mapFile ) ) );
            String line;
            while( (line = bin.readLine()) != null ){
                if( line.startsWith( "class" ) ){
                    StringTokenizer strtok = new StringTokenizer( line , "," );
                    strtok.nextToken();
                    String name = strtok.nextToken();
                    Integer id = new Integer( Integer.parseInt(strtok.nextToken()) );
                    classNameMap.put( id , name );
                }
                if( line.startsWith( "method" ) ){
                    StringTokenizer strtok = new StringTokenizer( line , "," );
                    strtok.nextToken();
                    String name = strtok.nextToken();
                    Integer id = new Integer( Integer.parseInt(strtok.nextToken()) );
                    methodNameMap.put( id , name );
                }
            }
        }catch( Exception e ){
            System.err.println( "Unable to read map file" );
            System.exit( -1 );
        }
    }
}
