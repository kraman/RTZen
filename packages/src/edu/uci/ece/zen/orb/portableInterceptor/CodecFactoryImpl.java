package edu.uci.ece.zen.orb.portableInterceptor;

import org.omg.IOP.*;
import org.omg.CORBA.ORB;

import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.*;
import org.omg.IOP.CodecFactoryPackage.*;

public class CodecFactoryImpl extends org.omg.CORBA.LocalObject
              implements CodecFactory
{
    private ORB orb = null;

    public CodecFactoryImpl(ORB orb)
    {
        this.orb = orb;
    } 

    /**
     */

    public Codec create_codec( Encoding e ) throws UnknownEncoding
    {
        if(e.format == ENCODING_CDR_ENCAPS.value){
            if( e.major_version == 1 ){
                if( e.minor_version == 0 ) return new Codec_1_0_Impl(orb);
                /*else
                    
                    if(e.minor_version == 1 ) return Codec_1_1_Impl(orb);
                */
            }
            else throw new UnknownEncoding("Cannot Create a Codec");
        }
        return null;
    }
}

