// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Gcj.java

package org.apache.tools.ant.taskdefs.compilers;

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;

// Referenced classes of package org.apache.tools.ant.taskdefs.compilers:
//            DefaultCompilerAdapter

public class jRateGcj extends DefaultCompilerAdapter
{

    public jRateGcj()
    {
    }

    public boolean execute()
        throws BuildException
    {
        super.attributes.log("Using gcj compiler", 3);
        Commandline cmd = setupGCJCommand();
        int firstFileName = cmd.size();
        logAndAddFilesToCompile(cmd);
        return executeExternalCompile(cmd.getCommandline(), firstFileName) == 0;
    }

    protected Commandline setupGCJCommand()
    {
        Commandline cmd = new Commandline();
        Path classpath = new Path(super.project);
        if(super.bootclasspath != null)
            classpath.append(super.bootclasspath);
        classpath.addExtdirs(super.extdirs);
        if(super.bootclasspath == null || super.bootclasspath.size() == 0)
            super.includeJavaRuntime = false;
        classpath.append(getCompileClasspath());
        if(super.compileSourcepath != null)
            classpath.append(super.compileSourcepath);
        else
            classpath.append(super.src);
        cmd.setExecutable("jRate-gcj");
        if(super.destDir != null)
        {
            cmd.createArgument().setValue("-d");
            cmd.createArgument().setFile(super.destDir);
            if(super.destDir.mkdirs())
                throw new BuildException("Can't make output directories. Maybe permission is wrong. ");
        }
        //cmd.createArgument().setValue("-classpath");
        //cmd.createArgument().setPath(classpath);
        if(super.encoding != null)
            cmd.createArgument().setValue("--encoding=" + super.encoding);
        if(super.debug)
            cmd.createArgument().setValue("-g1");
        if(super.optimize)
            cmd.createArgument().setValue("-O3");
        addCurrentCompilerArgs(cmd);
        return cmd;
    }
}
