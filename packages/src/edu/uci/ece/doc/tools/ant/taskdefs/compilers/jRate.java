/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package edu.uci.ece.doc.tools.ant.taskdefs.compilers;


import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.DirectoryScanner;
import java.io.File;

/**
<p>An Ant wrapper for the <a href="http://www.cs.wustl.edu/~corsaro/jRate/">jRate</a>
compiler. It assumes that <code>jRate-gcj</code> is in the command path.</p>

<p>The example below shows how this task could be used to build jRate's MemoryAreaDemo:</p>

<pre>
&lt;taskdef name="jrate" classname="edu.uci.ece.doc.tools.ant.taskdefs.compilers.jRate"/&gt;

&lt;target name="compile"&gt;
	&lt;jrate
		optimizeLevel="2"
		includeDirs="../../src"
		libDirs="../../lib"
		main="MemoryAreaDemo"
		out="memoryDemo"&gt;
		&lt;include name="*.java"/&gt;
	&lt;/jrate&gt;
&lt;/target&gt;
</pre>

<p>In addition to the attributes shown above, there are also the following:</p>

<ul>
<li>extdirs</li>
<li>bootclasspath</li>
<li>encoding</li>
<li>debug</li>
<li>classpath</li>
<li>destdir</li>
</ul>

<p>See the setter method documentation below for more details on each attribute.</p>

<p>Note: To specify source files to be compiled, you may use any of the tags
supported by <a href="http://ant.apache.org/manual/CoreTypes/fileset.html">FileSet</a>.</p>

<p>Requires Ant 1.6 or higher.</p>

@author <a href="mailto:trevor@vocaro.com">Trevor Harmon</a>
*/
public class jRate extends MatchingTask {

	private Path extdirs;
	private Path bootclasspath;
	private char optimizeLevel;
	private String encoding;
	private boolean debug;
	private Path classpath;
	private Path destdir;
	private Path libDirs;
	private String main;
    private String additionalCompileOptions;
    private String additionalLinkOptions;
	private File out;
    private boolean sharedLib;
    private java.util.Vector fileSets = new java.util.Vector();
	
	/**
	Sets the --extdirs parameter (only for compiling)
	*/
	public void setExtdirs(Path extdirs)
	{
		this.extdirs = extdirs;
	}
	
	/**
	Sets the --bootclasspath parameter (only for compiling)
	*/
	public void setBootclasspath(Path bootclasspath)
	{
		this.bootclasspath = bootclasspath;
	}

	/**
	Sets the optimization level. The given character may be any of those
	allowed by gcj's -O parameter: 1, 2, 3, or s. If not set, optimization
	is turned off. (Affects both compiling and linking.)
	*/
	public void setOptimizeLevel(char optimizeLevel)
	{
		this.optimizeLevel = optimizeLevel;
	}

	/**
	Sets the --encoding parameter (only for compiling)
	*/
	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	/**
	If true, -g is added to the command line (for compiling and linking)
	*/
	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

    /**
	If true, -shared is added to the command line (for linking)
	*/
    public void setSharedLib(boolean sharedLib){
        this.sharedLib = sharedLib;
    }

	/**
	Sets the --classpath parameter (only for compiling)
	*/
	public void setClasspath(Path classpath)
	{
		this.classpath = classpath;
	}

	/**
	Sets the -d parameter (only for compiling)
	*/
	public void setDestdir(Path destdir)
	{
		this.destdir = destdir;
	}

	/**
	Sets the -L parameter for each of the given pathnames (only for linking)
	*/
	public void setLibDirs(Path libDirs)
	{
		this.libDirs = libDirs;
	}
	
	/**
	Sets the --main parameter (only for linking)
	*/
	public void setMain(String main)
	{
		this.main = main;
	}

	public void setAdditionalCompileOptions(String additionalCompileOptions)
	{
		this.additionalCompileOptions = additionalCompileOptions;
	}

	public void setAdditionalLinkOptions(String additionalLinkOptions)
	{
		this.additionalLinkOptions = additionalLinkOptions;
	}
	
	/**
	Sets the -o parameter (only for linking)
	*/
	public void setOut(File out)
	{
		this.out = out;
	}

    public void add( FileSet fileSet ){
        fileSets.add( fileSet );
    }
	
    /**
    Performs a compile and link using the jRate compiler.
	
	@throws BuildException if there was an error running the compiler
    */
    public void execute() throws BuildException{

        //2 step process.
        //  -first compile all files to obj code (*.o)
        //  -then link *.o to form final executable or shared lib

        java.util.Vector vfiles = new java.util.Vector();

        for( int i=0;i<fileSets.size();i++ ){
            FileSet fs = (FileSet) fileSets.elementAt(i);
            DirectoryScanner ds = fs.getDirectoryScanner( getProject() );
            //ds.setBasedir( getProject().getBaseDir() );
            ds.scan();
            String[] files = ds.getIncludedFiles();
            for( int j=0;j<files.length;j++ ){
                if( files[j].endsWith( ".java" ) )
                    vfiles.add( fs.getDir(getProject()) + System.getProperty("file.separator") + files[j] );
            }
        }

		log("Compiling and linking " + vfiles.size() + " source file"
			+ (vfiles.size() == 1 ? "" : "s")
			+ (destdir != null ? " to " + destdir : ""));

        Execute procs[] = new Execute[vfiles.size()];
        log( "Starting compilation of files using jrate..." );
        int percentDrawn=0;
        File destdirFile = new File( destdir.toString() ); 
        for( int i=0;i<vfiles.size();i++ ){
            String file = vfiles.elementAt(i).toString();
            String objFileName = file.substring(0, file.lastIndexOf("java")).replace(
                    System.getProperty("file.separator").charAt(0) , '_' ) + "o";
            Commandline cmd = getCompileCommand();
            cmd.createArgument().setValue( "-o" + objFileName );
            cmd.createArgument().setValue( file );
            
            //progress bar
            int percentDone = ((int)(i*100.0/vfiles.size()));
            if( percentDone-percentDrawn > 1 ){
                log(percentDone + "% complete");
                percentDrawn = percentDone ;
            }

            procs[i] = new Execute();
            procs[i].setCommandline( cmd.getCommandline() );
            procs[i].setWorkingDirectory(destdirFile);
            //procs[i].setWorkingDirectory( new File( destdir.toString() ) );
            try{
                /*
                String[] cmdline = cmd.getCommandline();
                for( int j=0;j<cmdline.length;j++ ){
                    System.out.println( cmdline[j] );
                }*/
                procs[i].execute();
            }catch( Exception e ){
                e.printStackTrace();
            }
        }

        log( "Linking files" );
        Commandline cmd = getLinkCommand( vfiles.toArray() );
        Execute linkProc = new Execute();
        linkProc.setCommandline( cmd.getCommandline() );
        linkProc.setWorkingDirectory( new File( destdir.toString() ) );
        try{/*
            String[] cmdline = cmd.getCommandline();
            for( int i=0;i<cmdline.length;i++ ){
                System.out.println( cmdline[i] );
            }*/
            linkProc.execute();
        }catch( Exception e ){
            e.printStackTrace();
        }
    }

	/**
	Constructs and returns a Commandline object containing the first portion
	(minus the input and output files) of a jRate compile command according to
	the currently specified attributes.
	*/
    protected Commandline getCompileCommand() {
        Commandline cmd = new Commandline();

		cmd.setExecutable("jRate-gcj");
        if (bootclasspath != null) {
			cmd.createArgument().setValue("--bootclasspath=" + bootclasspath);
        }

        if (classpath != null) {
			cmd.createArgument().setValue("--classpath=" + classpath);
        }

        if (extdirs != null) {
			cmd.createArgument().setValue("--extdirs=" + bootclasspath);
        }

        if (encoding != null) {
            cmd.createArgument().setValue("--encoding=" + encoding);
        }

        if (debug) {
            cmd.createArgument().setValue("-g");
        }

        if (optimizeLevel != '\0') {
            cmd.createArgument().setValue("-O" + optimizeLevel);
        }

        if( additionalCompileOptions != null )
            cmd.createArgument().setLine(additionalCompileOptions);

		cmd.createArgument().setValue("-c");
		
        return cmd;
    }

	/**
	Constructs and returns a complete Commandline object of a jRate link
	command according to the currently specified attributes.
	
	@param files a list of files to be linked. These are assumed to be Java
	files; their .java extensions will be replaced by .o for the command line.
	*/
    protected Commandline getLinkCommand(Object[] files) {
        Commandline cmd = new Commandline();

		cmd.setExecutable("jRate-gcj");

        if (optimizeLevel != '\0') {
            cmd.createArgument().setValue("-O" + optimizeLevel);
        }

		if (main != null)
		{
			cmd.createArgument().setValue("--main=" + main);
		}

        if( sharedLib ){
            cmd.createArgument().setValue("-shared");
        }
		
		for (int i = 0; i < files.length; i++) {
			String file = (String) files[i];
            String objFileName = file.substring(0, file.lastIndexOf("java"));
            objFileName = objFileName.replace( System.getProperty("file.separator").charAt(0) , '_' );
            objFileName += "o";
			cmd.createArgument().setValue(objFileName);
		}
			
		if (out != null)
		{
			cmd.createArgument().setValue("-o");
			cmd.createArgument().setFile(out);
		}

        if (libDirs != null)
		{
			String[] dirs = libDirs.list();
			for (int i = 0; i < dirs.length; i++)
			{
				cmd.createArgument().setValue("-L" + dirs[i]);
			}
		}

        if( additionalLinkOptions != null )
            cmd.createArgument().setLine(additionalLinkOptions);

		cmd.createArgument().setValue("-ljRateRT");
		cmd.createArgument().setValue("-ljRate");
		cmd.createArgument().setValue("-ljRateCore");
		cmd.createArgument().setValue("-lrt");
		
		return cmd;		
	}
}
