package org.omg.IOP_N;


/**
 *	Generated from IDL definition of interface "CodecFactory"
 *	@author IDL compiler 
 */

public class _CodecFactoryStub extends org.omg.CORBA.portable.ObjectImpl
    implements org.omg.IOP_N.CodecFactory {
    private String[] ids = {"IDL:omg.org/IOP_N/CodecFactory:1.0",
        "IDL:omg.org/CORBA/Object:1.0"};
    public String[] _ids() {
        return ids;
    }

    public final static java.lang.Class _opsClass = org.omg.IOP_N.CodecFactoryOperations.class;
    public org.omg.IOP_N.Codec create_codec(org.omg.IOP_N.Encoding enc) throws org.omg.IOP_N.CodecFactoryPackage.UnknownEncoding {
        while (true) {
            if (!this._is_local()) {
                org.omg.CORBA.portable.InputStream _is = null;

                try {
                    org.omg.CORBA.portable.OutputStream _os = _request("create_codec",
                            true);

                    org.omg.IOP_N.EncodingHelper.write(_os, enc);
                    _is = _invoke(_os);
                    org.omg.IOP_N.Codec _result = org.omg.IOP_N.CodecHelper.read(_is);

                    return _result;
                } catch (org.omg.CORBA.portable.RemarshalException _rx) {} catch (org.omg.CORBA.portable.ApplicationException _ax) {
                    String _id = _ax.getId();

                    if (_id.equals("IDL:omg.org/IOP_N/CodecFactory/UnknownEncoding:1.0")) {
                        throw org.omg.IOP_N.CodecFactoryPackage.UnknownEncodingHelper.read(_ax.getInputStream());
                    } else { 
                        throw new RuntimeException("Unexpected exception " + _id);
                    }
                }
                finally {
                    this._releaseReply(_is);
                }
            } else {
                org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("create_codec",
                        _opsClass);

                if (_so == null) {
                    throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
                }
                CodecFactoryOperations _localServant = (CodecFactoryOperations) _so.servant;
                org.omg.IOP_N.Codec _result;

                try {
                    _result = _localServant.create_codec(enc);
                }
                finally {
                    _servant_postinvoke(_so);
                }
                return _result;
            }

        }

    }

}
