import org.apache.bcel.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class IsoLeak {
    static int cntr=0;
    public static void main(String args[]) throws Exception{

        ZipInputStream zip = new ZipInputStream(new FileInputStream(args[0]));
        ZipEntry ze = null;

        while((ze = zip.getNextEntry()) != null){
            String name = ze.getName();
            int ind = -1;
            if((ind = name.indexOf(".class")) > -1){
                String path = name;
                name = name.replace('/','.');
                name = name.substring(0,ind);
                //System.out.println(name);
                JavaClass cls= Repository.lookupClass(name);
                changeMethods( cls , path );
            }
        }
    }

    static void changeMethods( JavaClass cls , String name ){
        if( name.indexOf("uci") > -1 && name.indexOf("Logger") == -1 && !cls.isNative() ){
            Type[] parameters = new Type[]{Type.INT};
            String methodSignature = Type.getMethodSignature(Type.VOID, parameters);
            ConstantPoolGen pool = new ConstantPoolGen( cls.getConstantPool() );
            int result = pool.lookupMethodref("edu.uci.ece.zen.utils.Logger", "printMemStatsImm", methodSignature);


            Method[] methods = cls.getMethods();
            for( int i=0;i<methods.length;i++ ){
                cntr++;
                MethodGen method = new MethodGen( methods[i] , cls.getClassName() , pool );
                if (!(method.isAbstract() || method.isNative()  ) ) {
                    InstructionList oldList = method.getInstructionList();
                    String className = method.getClassName();
                    String methodName = method.getName();
                    String reportClass = "edu.uci.ece.zen.utils.Logger";
                    System.out.println( className + "." + methodName + "            " + cntr );

                    // create newList to add to oldList
                    Type[] types = method.getArgumentTypes();
                    InstructionFactory factory = new InstructionFactory(pool);
                    InstructionList newList1 = new InstructionList();
                    InstructionList newList2 = new InstructionList();

                    Type[] invokeMethodParams = new Type[]{Type.INT};
                    newList1.append(new PUSH(pool , cntr ));
                    newList1.append(factory.createInvoke(reportClass, "printMemStatsImm", Type.VOID, invokeMethodParams, Constants.INVOKESTATIC));

                    // insert new list before old list
                    oldList.append(oldList.getStart(), newList1);

                    newList2.append( oldList );
                    newList2.append(new PUSH(pool , cntr ));
                    newList2.append(factory.createInvoke(reportClass, "printMemStatsImm", Type.VOID, invokeMethodParams, Constants.INVOKESTATIC));

                    method.setInstructionList(newList2);
                    method.setMaxStack();
                    Method m = method.getMethod();
                    //REMINDER: must do method.getMethod() before can free memory
                    oldList.dispose();
                    newList1.dispose();
                    newList2.dispose();
                    methods[i] = m;
                }
            }
            cls.setConstantPool(pool.getFinalConstantPool());
        }else{
            //System.out.print(".");
        }

        try {
            cls.dump( name );
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
