import org.apache.bcel.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class IsoLeak {
    static PrintWriter map = null;
    public static void main(String args[]) throws Exception{
        ZipInputStream zip = new ZipInputStream(new FileInputStream(args[0]));
        ZipEntry ze = null;
        map = new PrintWriter( new FileOutputStream(args[1]));

        while((ze = zip.getNextEntry()) != null){
            String name = ze.getName();
            int ind = -1;
            if((ind = name.indexOf(".class")) > -1){
                String path = name;
                name = name.replace('/','.');
                name = name.substring(0,ind);
                JavaClass cls= Repository.lookupClass(name);
                changeMethods( cls , path );
            }
        }
        map.flush();
        map.close();
    }

    static long methodId = 0;
    static long classId = 0;
    static void changeMethods( JavaClass cls , String name ){
        if( name.indexOf("uci") > -1 && !cls.isNative() ){
            Type[] parameters = new Type[]{Type.INT};
            String methodSignature = Type.getMethodSignature(Type.VOID, parameters);
            ConstantPoolGen pool = new ConstantPoolGen( cls.getConstantPool() );

            //add hooks
            Method[] methods = cls.getMethods();
            for( int i=0;i<methods.length;i++ ){
                MethodGen method = new MethodGen( methods[i] , cls.getClassName() , pool );
                if (!(method.isAbstract() || method.isNative()  ) ) {
                    System.out.println( cls.getClassName() + "." + method.getName() + "," + methodId );
                    map.println( "method" + "," + cls.getClassName() + "." + method.getName() + "," + methodId );
                    methods[i] = addCodetoMethod( method , pool , methodId );
                    ++methodId;
                }
            }
            cls.setConstantPool(pool.getFinalConstantPool());

            //adding static final identifier field to every class
            if( !cls.isAbstract() ){
                ClassGen thisClass = new ClassGen( cls );
                pool = thisClass.getConstantPool();
                thisClass.addInterface( (Repository.lookupClass("IsoLeakAnotated")).getClassName() );
                InstructionFactory factory = new InstructionFactory(pool);
                InstructionList classIdInstrutions = new InstructionList();
                MethodGen classIdMethod = new MethodGen( Constants.ACC_PUBLIC, Type.LONG, Type.NO_ARGS, new String[] {  }, "__isoLeak_classId",
                     cls.getClassName(), classIdInstrutions, pool);
                map.println( "class" + "," + cls.getClassName() + "," + classId );
                classIdInstrutions.append( new PUSH( pool , ((long)classId++) ) );
                classIdInstrutions.append( factory.createReturn( Type.LONG ) );
                classIdMethod.setMaxLocals();
                classIdMethod.setMaxStack();
                thisClass.addMethod( classIdMethod.getMethod() );
                classIdInstrutions.dispose();
                cls = thisClass.getJavaClass();
            }
        }

        try {
            cls.dump( name );
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Method addCodetoMethod( MethodGen method , ConstantPoolGen pool , long methodId ){
        InstructionList oldList = method.getInstructionList();
        String className = method.getClassName();
        String methodName = method.getName();
        String reportClass = "Test";

        InstructionFactory factory = new InstructionFactory(pool);
        InstructionHandle instructions[] = oldList.getInstructionHandles();

        InstructionHandle startInstructionHandle = null;
        if( method.getName().equals( "<init>" ) ){
            for( int i=0;i<instructions.length;i++ )
                if( instructions[i].getInstruction() instanceof INVOKESPECIAL ){
                    startInstructionHandle = instructions[i];
                    break;
                }
        }else{
        }
        
        Vector finallyJumpHandlers = new Vector();
        for( int i=0;i<instructions.length;i++ ){
            if( instructions[i].getInstruction() instanceof ReturnInstruction )
            {
                BranchInstruction finallyJump = new JSR( null );
                finallyJumpHandlers.add( finallyJump );
                InstructionHandle ih;
                if( instructions[i].getInstruction() instanceof RETURN ){
                    ih = oldList.insert( instructions[i] , finallyJump );
                }else{
                    LocalVariableGen retVar = method.addLocalVariable( "__isoLeak_retStore" , method.getReturnType() , null , null );
                    InstructionList retIl = new InstructionList();
                    retIl.append( factory.createStore( method.getReturnType() , retVar.getIndex()  ));
                    retIl.append( finallyJump );
                    retIl.append( factory.createLoad( method.getReturnType() , retVar.getIndex() ));
                    ih = oldList.insert( instructions[i] , retIl );
                }
                //look for any goto's that jump directly to the return
                for( int j=0;j<instructions.length;j++ ){
                    if( instructions[j].getInstruction() instanceof BranchInstruction && ((BranchInstruction)instructions[j].getInstruction()).getTarget().equals( instructions[i] ) )
                        ((BranchInstruction)instructions[j].getInstruction()).setTarget( ih );
                }
            }
        }
        
        LocalVariableGen varGen = method.addLocalVariable( "__isoLeak_memAlloc" , Type.LONG , null , null );
        LocalVariableGen varGenImm = method.addLocalVariable( "__isoLeak_immMemAlloc" , Type.LONG , null , null );
        Type[] invokeMethodParams = new Type[]{};
        InstructionList newList = new InstructionList();
        newList.append( factory.createInvoke( "IsoLeakHelper" , "__isoLeak_recordScopedSize" , Type.LONG , invokeMethodParams , Constants.INVOKESTATIC ) );
        newList.append( factory.createStore( Type.LONG , varGen.getIndex() ) );
        newList.append( factory.createInvoke( "IsoLeakHelper" , "__isoLeak_recordImmortal" , Type.LONG , invokeMethodParams , Constants.INVOKESTATIC ) );
        newList.append( factory.createStore( Type.LONG , varGenImm.getIndex() ) );
        InstructionHandle IStartTry = newList.append( new NOP() );
        if( startInstructionHandle == null ){
            newList.append( oldList );
            oldList = newList;
        }else
            oldList.append( startInstructionHandle , newList );
        InstructionHandle IEndTry       = instructions[instructions.length-1];
        //start catch anything routing
        BranchInstruction IGotoAfterTCF = new GOTO( null );
        oldList.append( IGotoAfterTCF );
        LocalVariableGen varGenTmp2 = method.addLocalVariable( "__Tmp2" , Type.THROWABLE , null , null );
        InstructionHandle ICatchFinally = oldList.append( factory.createStore( Type.THROWABLE , varGenTmp2.getIndex() ) );
        //InstructionHandle ICatchFinally = oldList.append( new NOP() );
        BranchInstruction IStartCatch   = new JSR( null );
        oldList.append( IStartCatch );      //call finally
        oldList.append( factory.createLoad( Type.OBJECT , varGenTmp2.getIndex() ) );
        oldList.append( InstructionConstants.ATHROW );  //rethrow exception
        //start finally routine
        LocalVariableGen varGenTmp = method.addLocalVariable( "__Tmp" , Type.OBJECT , null , null );
        InstructionHandle IStartFinally = oldList.append( factory.createStore( Type.THROWABLE , varGenTmp.getIndex() ) );

        invokeMethodParams = new Type[]{ Type.LONG , Type.LONG , Type.LONG };
        oldList.append(new PUSH(pool , (long)methodId ));
        oldList.append( factory.createLoad( Type.LONG , varGen.getIndex() ) );
        oldList.append( factory.createLoad( Type.LONG , varGenImm.getIndex() ) );
        oldList.append( factory.createInvoke( "IsoLeakHelper" , "__isoLeak_checkMemStats" , Type.VOID , invokeMethodParams , Constants.INVOKESTATIC ) );
        //oldList.append( factory.createStore( Type.LONG , varGen.getIndex() ) );

        oldList.append( new RET(varGenTmp.getIndex()) );
        InstructionHandle IAfterTCF = oldList.append( new NOP() );
        IStartCatch.setTarget( IStartFinally );
        for( int i=0;i<finallyJumpHandlers.size();i++ ){
            ((BranchInstruction) finallyJumpHandlers.elementAt(i)).setTarget( IStartFinally );
        }
        IGotoAfterTCF.setTarget( IAfterTCF );
        method.addExceptionHandler( IStartTry , IEndTry , ICatchFinally , null );
        
        
        oldList.setPositions(true);
        method.setInstructionList(oldList);
        method.setMaxStack();
        method.setMaxLocals();
        Method m = method.getMethod();
        //REMINDER: must do method.getMethod() before can free memory
        oldList.dispose();
        //newList.dispose();
        return m;
    }
}
