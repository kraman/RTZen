package demo.object;

class ExchangeObjectImpl extends ExchangeObjectPOA{
    public org.omg.CORBA.Object exchange(org.omg.CORBA.Object inVal, org.omg.CORBA.ObjectHolder outVal){
        org.omg.CORBA.Object retVal = inVal;
        outVal.value = inVal;
        return retVal;
    }
}
                                
