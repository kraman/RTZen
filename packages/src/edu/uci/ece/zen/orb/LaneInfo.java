package edu.uci.ece.zen.orb;

import javax.realtime.*;

class LaneInfo{
    public LaneInfo(){
        objectKey = new byte[1024];
    }

    public int minPri;
    public int maxPri;
    public ScopedMemory transpScope;

    public void init( int minPri , int maxPri , ScopedMemory transpScope , byte[] objKey ){
        this.minPri = minPri;
        this.maxPri = maxPri;
        this.transpScope = transpScope;
        objectKeyLength = objKey.length;
        System.arraycopy( objKey , 0 , objectKey , 0 , objectKeyLength );
    }

    public byte[] getObjectKey(){
        byte[] ret = new byte[objectKeyLength];
        System.arraycopy( objectKey , 0 , ret , 0 , objectKeyLength );
        return ret;
    }

    private byte[] objectKey;
    private int objectKeyLength;
}
