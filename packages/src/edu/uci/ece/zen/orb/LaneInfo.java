package edu.uci.ece.zen.orb;

import javax.realtime.*;
import edu.uci.ece.zen.utils.*;

class LaneInfo{
    public LaneInfo(){
        try{
            objectKey = new FString();
            objectKey.init(1024);
        }catch( Exception e2 ){
             ZenProperties.logger.log(
                Logger.SEVERE,
                "edu.uci.ece.zen.orb.LaneInfo",
                "<init>",
                "Could not initialize Lane due to exception: " + e2.toString()
                );
        }
    }

    public int minPri;
    public int maxPri;
    public ScopedMemory transpScope;
    private FString objectKey;

    public void init( int minPri , int maxPri , ScopedMemory transpScope , byte[] objKey ){
        this.minPri = minPri;
        this.maxPri = maxPri;
        this.transpScope = transpScope;
        objectKey.append( objKey , 0 , objKey.length );
    }
/*
    public byte[] getObjectKey(){
        return objectKey.getTrimData();
    }*/
    public FString getObjectKey(){
        return objectKey;
    }

}
