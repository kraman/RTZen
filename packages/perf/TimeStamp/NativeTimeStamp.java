package perf.TimeStamp;

// THIS SOFTWARE AND ANY ACCOMPANYING DOCUMENTATION IS RELEASED "AS IS."  
// THE U.S. GOVERNMENT MAKES NO WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
// CONCERNING THIS SOFTWARE AND ANY ACCOMPANYING DOCUMENTATION, INCLUDING, 
// WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A 
// PARTICULAR PURPOSE.  IN NO EVENT WILL THE U.S. GOVERNMENT BE LIABLE FOR 
// ANY DAMAGES, INCLUDING ANY LOST PROFITS, LOST SAVINGS OR OTHER INCIDENTAL 
// OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE, OR INABILITY TO USE, 
// THIS SOFTWARE OR ANY ACCOMPANYING DOCUMENTATION, EVEN IF INFORMED IN 
// ADVANCE OF THE POSSIBILITY OF SUCH DAMAGES.
// This file is copyrighted by McDonnell-Douglas Corporation, a wholly
// owned subsidiary of The Boeing Company, Copyright (c) 2002, all rights
// reserved. This file is open source, free software, you are free to
// use, modify, and distribute the source code and object code produced
// from the source, as long as you include this copyright statement,
// along with code built using this file.
// In particular, you can use this file in proprietary software and are
// under no obligation to redistribute any of your source code that is
// built using this file. Note, however, that you may not do anything to
// this file code, such as copyrighting it yourself or claiming
// authorship of this code, that will prevent this file from being
// distributed freely using an open source development model.
// Warranty
// This file is provided as is, with no warranties of any kind, including
// the warranties of design, merchantability and fitness for a particular
// purpose, non-infringement, or arising from a course of dealing, usage
// or trade practice. Moreover, this file is provided with no support and
// without any obligation on the part of McDonnell-Douglas, its
// employees, or others to assist in its use, correction, modification,
// or enhancement.
// Liability
// McDonnell-Douglas, its employees, and agents have no liability with 
// respect to the infringement of copyrights, trade secrets or any 
// patents by this file thereof. Moreover, in no event will 
// McDonnell-Douglas, its employees, or agents be liable for any lost
// revenue or profits or other special, indirect and consequential 
// damages.
// Acknowledgement
// This work was sponsored by the US Air Force Research Laboratory
// Information Directorate, Wright-Patterson Air Force Base.
import java.io.File;
import java.util.StringTokenizer;

public class NativeTimeStamp
{
	private static final String libName = "ttimestampjni";

	// Load the library (lib*.so) that contains our native code.
	// Does this really need to be static (i.e. one copy)?
	static {
		try {
			Runtime.getRuntime().loadLibrary(libName);
		} 
		catch (UnsatisfiedLinkError ule) {
			//hschirne: Have observed on Java 1.4.2 SDE that the classLoader is null
			// in this case (see ClassLoader.java), loadLibrary throws the above exception although 
			// lib is in the path and OK. As a workaround try manually loading (which 
			// does not use the classLoader)
			// Alternative workaround: set sun.boot.library.path and use loadLibrary, then
			// the ClassLoader is neither used
			loadNativeLib(libName);
		}
	}

	// alternative loading of library within the library path
	public static void loadNativeLib(String inName) throws UnsatisfiedLinkError {

		String libName = System.mapLibraryName(inName);
		//System.out.println("Try loading3: "+libName);
		// get the library path and sepearat into individual paths 
		StringTokenizer st = new StringTokenizer( System.getProperty("java.library.path"),File.pathSeparator+" ");
        
		// try loading from each path
		while(st.hasMoreTokens()){
        	try {
                String path = st.nextToken().toString();
				System.out.println("Try loading: '" + path + File.separator + libName + "'");
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


	// Native function declarations.
	// Do these really need to be static (i.e. one copy)?
	public static native void Init(int recordingMode, double stopTime);

	public static native double GetTime();
	public static native void RecordTime(int type);
	public static native void OutputLogRecords();
	// A constructor is used to initialize this timestamp object.
	public NativeTimeStamp()
	{
	}
}
