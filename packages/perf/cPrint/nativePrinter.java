package perf.cPrint;
import java.io.File;
import java.util.StringTokenizer;


public class nativePrinter {

	private static final String nativeLib = "CPrinter";

	// Load the library (lib*.so) that contains our native code.
	// Does this really need to be static (i.e. one copy)?
	static {
		try {
			Runtime.getRuntime().loadLibrary(nativeLib);
		} 
		catch (UnsatisfiedLinkError ule) {
			//hschirne: Have observed on Java 1.4.2 SDE that the classLoader is null
			// in this sense, loadling throws the above exception although 
			// everything should be fine. As a workaround try manually loading (does 
			// not use the classloader
			loadNativeLib(nativeLib);
		}
	}

	// alternative loading of library within the library path
	public static void loadNativeLib(String inName) throws UnsatisfiedLinkError {

		String[] paths;
		String libName = System.mapLibraryName(inName);
		System.out.println("Try loading3: "+libName);
		// get the library path and sepearat into individual paths 
		//paths = System.getProperty("java.library.path").split(File.pathSeparator);
		StringTokenizer st = new StringTokenizer( System.getProperty("java.library.path"),File.pathSeparator+" ");
        
		int i = 0;
		// try loading from each path
		//for ( i = 0; i< paths.length; i++) {
		while(st.hasMoreTokens()){
        	try {
                String path = st.nextToken().toString();
				//System.out.println("Try loading: '" + paths[i] + File.separator + libName + "'");
				System.out.println("Try loading: '" + path + File.separator + libName + "'");
				//System.load(paths[i] + File.separator + libName);
				System.load(path + File.separator + libName);
				// success if did not throw exception
				return;
			}
			catch (UnsatisfiedLinkError enf) {
			}
		}
		throw new UnsatisfiedLinkError("lib '" + inName + "' not found in " +  
									   System.getProperty("java.library.path"));
	}

	// empty constructor
	public nativePrinter() {
	}

	public static native void print(int a, int b, int c);

}
