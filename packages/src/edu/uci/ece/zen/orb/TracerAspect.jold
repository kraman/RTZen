/**
 * @author Mark Panahi
 * @author Bruce Miller
 */

package edu.uci.ece.zen.util;

import org.aspectj.lang.JoinPoint;
//import com.vladium.utils.*;
//import java.util.Hashtable;


import java.util.*;
import java.util.zip.*;
import java.io.*;
import java.lang.reflect.*;

public aspect TracerAspect{
    public static Hashtable hash = new Hashtable();
    public static Hashtable classes = new Hashtable();
    static int buf = 0;
    static int numMeth = 0;

    pointcut tracer( ):
        //execution( * edu..zen..*.*(..) ) || execution( * demo..*.*(..) ) || execution( * perf..*.*(..) );
        execution( * *..*.*(..) )
        //execution( * edu..zen..*.*(..) ) && !execution( * *..TracerAspect.*(..))
            && !execution( * *..TracerAspect.*(..))
            && !execution( * demo..*.*(..))
            && !cflow(call( * *..TracerAspect.printTrace(..) ));

/*
    Object around():tracer(){

        proceed();

    }
*/
/*
    before( ): tracer() {
        hash.put(thisJoinPoint.getSignature().toString(),"");
        System.out.println(thisJoinPoint.getSignature().toString());
        //hash.put(thisJoinPoint.toString(),"");
    }
*/

    before(): tracer() {
        String sig = thisJoinPoint.getSignature().toLongString();
        String sighash = sig.replaceAll(", ",",").replace('$','.');
        hash.put(sighash, sighash);

        //Object thisObj = thisJoinPoint.getTarget().getClass().getName();
        //String classname =  thisJoinPoint.getStaticPart().getKind();//"static";
        //String classname =  thisObj.toString();
        //if(thisObj != null)
            //classname = thisObj.toString();//.getClass().getName();
        String classname =  thisJoinPoint.getSignature().getDeclaringType().getName();

        classes.put(classname,"");
        //if(sig.indexOf("Logger")<0)
        {
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i<buf; ++i)
            {
                if(i%2==0) sb.append("-");
                else       sb.append("|");

            }
            System.out.println(sb + "entering: " + sig );
            buf++;
        }
    }

    after( ): tracer() {
        String sig = thisJoinPoint.getSignature().toLongString();
        //if(sig.indexOf("Logger")<0)
        {
            buf--;
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i<buf; ++i)
            {
                if(i%2==0) sb.append("-");
                else       sb.append("|");

            }
            System.out.println(sb + "exiting: " + sig );
        }
    }

    after() throwing(Exception e): tracer() {
        String sig = thisJoinPoint.getSignature().toLongString();
        {
            buf--;
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i<buf; ++i)
            {
                if(i%2==0) sb.append("-");
                else       sb.append("|");

            }
            System.out.println(sb + "exiting: " + sig + " as exception: " + e);
        }
    }
/*
    private static int pmem = 0;
    after(Object obj):(execution( edu..*.new(..) )
            ||
            execution( org..*.new(..) )

            //|| call( java..*.new(..) )

            )
            && !execution( *..TracerAspect.new(..))
            && target(obj)

            //&& !call( demo..*.new(..))
            && !cflow(call( * *..TracerAspect.printTrace(..) ))
            //&& !cflow(call( *..*.new(..) ))
            {

        System.out.println("new: " + thisJoinPoint.getSignature().toLongString());


        IObjectProfileNode profile = ObjectProfiler.profile (obj);
        System.out.println ("obj size = " + profile.size () + " bytes");
        pmem += profile.size ();
        System.out.println ("total so far = " + pmem + " bytes");
        System.out.println (profile.dump ());
    }
    */

/*
trying to profile java libs

    after():(call( java..String.new(..) )
    //|| call( java..StringBuffer.new(..) )

    )

            {

        System.out.println("calljava: " + thisJoinPoint.getSignature().toLongString());
    }

    after():initialization( java..*.new(..) )

            {

        System.out.println("initializejava: " + thisJoinPoint.getSignature().toLongString());
    }

    after():preinitialization( java..*.new(..) )

            {

        System.out.println("preinitializejava: " + thisJoinPoint.getSignature().toLongString());
    }
*/

    //some static initialization block give trouble
    void around(): (staticinitialization(edu.uci.ece.zen.poa.mechanism.*)
    || staticinitialization(edu.uci.ece.zen.orb.any.pluggable.Any)


    )
    //staticinitialization(class *..*.*)
    //staticinitialization(java.lang.Object+)
    && cflow(call( * *..TracerAspect.printTrace(..) )){
        System.out.println("statinit: " + thisJoinPointStaticPart.getSignature().toLongString());

    }


    public static void printTrace()
    {
        java.util.Enumeration enum = hash.elements();

        String [] trace = new String[hash.size()];

        System.out.println("\n\n****************** TRACE: ordered list of calls...");
        numMeth = 0;
        int i = 0;
        for (i = 0; enum.hasMoreElements() ; ++i) {
            trace[i] = enum.nextElement().toString();
            System.out.println(trace[i]);
        }
        numMeth = i;

        java.util.Arrays.sort(trace);

        System.out.println("\n\n****************** TRACE: alphabetized list of calls...");
        for (i = 0; i < trace.length ; ++i)
            System.out.println(trace[i]);


        enum = classes.keys();

        trace = new String[classes.size()];

        for (i = 0; i < trace.length ; ++i)
            trace[i] = enum.nextElement().toString();

        java.util.Arrays.sort(trace);

        java.io.FileInputStream file = null;

        int total = 0;

        System.out.println("\n\n****************** TRACE: list of classes...");
        for (i = 0; i < trace.length ; ++i){
            System.out.print(trace[i]);
            try{
                String clsname = "../../../classes/"+trace[i].replace('.','/')+".class";
                file = new java.io.FileInputStream(clsname);
                int size = (int)file.getChannel().size();
                total += size;
                System.out.println("\t" + size);

/*
                Object obj = null;//new String [] {new String ("JavaWorld"), new String ("JavaWorld")};
                try{
                    obj = Class.forName(trace[i]).newInstance();
                    IObjectProfileNode profile = ObjectProfiler.profile (obj);
                    System.out.println ("obj size = " + profile.size () + " bytes");
                }catch(Exception e){
                    System.out.println ("can't get obj size");
                    //e.printStackTrace();
                }

*/
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        System.out.println("\n\n TOTAL: " + total);
/*
        try{
            ZipInputStream zip = new ZipInputStream(new FileInputStream("../../../packages/demo/hello_min/jar/shrunk.jar"));
            ZipEntry ze = null;
            int plus = 0;
            int minus = 0;
            Vector matchV = new Vector();
            Vector unmatchV = new Vector();

            while((ze = zip.getNextEntry()) != null){

                String name = ze.getName();
                int ind = -1;
                if((ind = name.indexOf(".class")) > -1){
                    name = name.replace('/','.');
                    name = name.substring(0,ind);

                    System.out.println(name);
                    //System.out.println();
                    try{
                        Class cls = Class.forName(name);
                        Method [] meth = cls.getDeclaredMethods();
                        for(i = 0; i < meth.length; ++i){
                            String methstr = meth[i].toString().replaceAll(" *throws.*","").replace('$','.');
                            Object match = hash.get(methstr);
                            if( match != null && match.equals(methstr)){

                                System.out.print("++++++");
                                matchV.add(methstr);
                                plus++;

                            }
                            else{
                                System.out.print("------");
                                unmatchV.add(meth[i]);
                                minus++;
                            }

                            System.out.println(meth[i].toString());
                        }
                        //System.out.println(.toString());
                    }catch(Throwable t){
                        //e.printStackTrace();
                        System.out.println(t);
                    }
                    System.out.println("************************************");
                }
                //System.out.println(ze.getName());
            }

            System.out.println("------: " + minus);
            System.out.println("++++++: " + plus);
            System.out.println("num of meth for trace: " + numMeth);

            String [] matches = new String[matchV.size()];
            for(i = 0; i < matches.length; ++i)
                matches[i] = matchV.get(i).toString();
            java.util.Arrays.sort(matches);
            for(i = 0; i < matches.length; ++i)
                System.out.println(matches[i]);



            BufferedWriter bw = new BufferedWriter( new FileWriter("AroundAspect.java") );
            bw.write("package edu.uci.ece.zen.features;\n");
            bw.write("import org.aspectj.lang.JoinPoint;\n");
            bw.write("public privileged aspect AroundAspect{\n");


            for(i = 0; i < unmatchV.size(); ++i){
                Method m = (Method)unmatchV.get(i);
                int mod = m.getModifiers();
                if(!Modifier.isAbstract(mod)){
                    Class retcls = m.getReturnType();
                    String ret = retcls.getName();
                    if(m.toString().indexOf("Aspect") < 0 && !m.toString().matches(".*\\$[0-9]+.*")
                    ){
                        if(retcls.isArray())
                            ret = "java.lang.Object";

                        bw.write("    " + ret.replace('$','.') +
                                        " " + "around(): execution("+
                                        m.toString().replaceAll(" *throws.*","").replace('$','.') + ") {\n");
                        //bw.write("        throw new Exception(\"This code has been removed.\");\n");

                        bw.write("        System.out.println(\"This code has been removed.\");\n");
                        bw.write("        System.out.println(\"new: \" + thisJoinPoint.getSignature().toLongString());\n");
                        if(!ret.equals("void")){
                            bw.write("        return ");
                            if(ret.equals("boolean"))
                                bw.write("false");
                            else if(retcls.isPrimitive())
                                bw.write("0");
                            else
                                bw.write("null");

                            bw.write(";\n");
                        }


                        bw.write("    }\n");
                    }
                }
            }
            bw.write("}\n");
            bw.close();
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("done");
        }
*/

    }


    pointcut includeClient():
            execution(* *..Client.main(..)) ;


    after():includeClient() {
        try{Thread.currentThread().sleep(1000);}catch(Exception e){e.printStackTrace();}
        edu.uci.ece.zen.util.TracerAspect.aspectOf().printTrace();
        //servProceed = true;
    }

/*

    pointcut includeServer():
            execution(* *..Server.main(..)) ;


    after():includeServer() {


        try{
            //sleep to make sure client is finished
            while(!servProceed)
                Thread.currentThread().sleep(100);
            servProceed = false;
        }
        catch(java.lang.InterruptedException e){
            e.printStackTrace();
        }

        edu.uci.ece.zen.util.TracerAspect.aspectOf().printTrace();
    }

    private boolean servProceed = false;
    */
}
