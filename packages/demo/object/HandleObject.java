package demo.object;

class HandleObjectImpl extends HandleObjectPOA{
    public org.omg.CORBA.Object echoObject(org.omg.CORBA.Object inVal, org.omg.CORBA.ObjectHolder outVal){
        org.omg.CORBA.Object retVal = inVal;
        outVal.value = inVal;
        return retVal;
    }
}
                                
