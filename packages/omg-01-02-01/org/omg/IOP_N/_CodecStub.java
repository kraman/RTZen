package org.omg.IOP_N;


/**
 *  Generated from IDL definition of interface "Codec"
 *  @author IDL compiler
 */

public class _CodecStub extends org.omg.CORBA.portable.ObjectImpl
    implements org.omg.IOP_N.Codec {
    private String[] ids = {"IDL:omg.org/IOP_N/Codec:1.0",
        "IDL:omg.org/CORBA/Object:1.0"};
    public String[] _ids() {
        return ids;
    }

    public final static java.lang.Class _opsClass = org.omg.IOP_N.CodecOperations.class;
    public org.omg.CORBA.Any decode(byte[] data) throws org.omg.IOP_N.CodecPackage.FormatMismatch {
        while (true) {
            if (!this._is_local()) {
                org.omg.CORBA.portable.InputStream _is = null;

                try {
                    org.omg.CORBA.portable.OutputStream _os = _request("decode",
                            true);

                    org.omg.CORBA.OctetSeqHelper.write(_os, data);
                    _is = _invoke(_os);
                    org.omg.CORBA.Any _result = null;// ajc _is.read_any();

                    return _result;
                } catch (org.omg.CORBA.portable.RemarshalException _rx) {} catch (org.omg.CORBA.portable.ApplicationException _ax) {
                    String _id = _ax.getId();

                    if (_id.equals("IDL:omg.org/IOP_N/Codec/FormatMismatch:1.0")) {
                        throw org.omg.IOP_N.CodecPackage.FormatMismatchHelper.read(_ax.getInputStream());
                    } else {
                        throw new RuntimeException("Unexpected exception " + _id);
                    }
                }
                finally {
                    this._releaseReply(_is);
                }
            } else {
                org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("decode",
                        _opsClass);

                if (_so == null) {
                    throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
                }
                CodecOperations _localServant = (CodecOperations) _so.servant;
                org.omg.CORBA.Any _result;

                try {
                    _result = _localServant.decode(data);
                }
                finally {
                    _servant_postinvoke(_so);
                }
                return _result;
            }

        }

    }

    public org.omg.CORBA.Any decode_value(byte[] data, org.omg.CORBA.TypeCode tc) throws org.omg.IOP_N.CodecPackage.TypeMismatch, org.omg.IOP_N.CodecPackage.FormatMismatch {
        while (true) {
            if (!this._is_local()) {
                org.omg.CORBA.portable.InputStream _is = null;

                try {
                    org.omg.CORBA.portable.OutputStream _os = _request("decode_value",
                            true);

                    org.omg.CORBA.OctetSeqHelper.write(_os, data);
                    // @@@ ajc _os.write_TypeCode(tc);
                    _is = _invoke(_os);
                    org.omg.CORBA.Any _result = null; // ajc _is.read_any();

                    return _result;
                } catch (org.omg.CORBA.portable.RemarshalException _rx) {} catch (org.omg.CORBA.portable.ApplicationException _ax) {
                    String _id = _ax.getId();

                    if (_id.equals("IDL:omg.org/IOP_N/Codec/TypeMismatch:1.0")) {
                        throw org.omg.IOP_N.CodecPackage.TypeMismatchHelper.read(_ax.getInputStream());
                    } else if (_id.equals("IDL:omg.org/IOP_N/Codec/FormatMismatch:1.0")) {
                        throw org.omg.IOP_N.CodecPackage.FormatMismatchHelper.read(_ax.getInputStream());
                    } else {
                        throw new RuntimeException("Unexpected exception " + _id);
                    }
                }
                finally {
                    this._releaseReply(_is);
                }
            } else {
                org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("decode_value",
                        _opsClass);

                if (_so == null) {
                    throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
                }
                CodecOperations _localServant = (CodecOperations) _so.servant;
                org.omg.CORBA.Any _result;

                try {
                    _result = _localServant.decode_value(data, tc);
                }
                finally {
                    _servant_postinvoke(_so);
                }
                return _result;
            }

        }

    }

    public byte[] encode_value(org.omg.CORBA.Any data) throws org.omg.IOP_N.CodecPackage.InvalidTypeForEncoding {
        while (true) {
            if (!this._is_local()) {
                org.omg.CORBA.portable.InputStream _is = null;

                try {
                    org.omg.CORBA.portable.OutputStream _os = _request("encode_value",
                            true);

                    // @@@ ajc _os.write_any(data);
                    _is = _invoke(_os);
                    byte[] _result = org.omg.CORBA.OctetSeqHelper.read(_is);

                    return _result;
                } catch (org.omg.CORBA.portable.RemarshalException _rx) {} catch (org.omg.CORBA.portable.ApplicationException _ax) {
                    String _id = _ax.getId();

                    if (_id.equals("IDL:omg.org/IOP_N/Codec/InvalidTypeForEncoding:1.0")) {
                        throw org.omg.IOP_N.CodecPackage.InvalidTypeForEncodingHelper.read(_ax.getInputStream());
                    } else {
                        throw new RuntimeException("Unexpected exception " + _id);
                    }
                }
                finally {
                    this._releaseReply(_is);
                }
            } else {
                org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("encode_value",
                        _opsClass);

                if (_so == null) {
                    throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
                }
                CodecOperations _localServant = (CodecOperations) _so.servant;
                byte[] _result;

                try {
                    _result = _localServant.encode_value(data);
                }
                finally {
                    _servant_postinvoke(_so);
                }
                return _result;
            }

        }

    }

    public byte[] encode(org.omg.CORBA.Any data) throws org.omg.IOP_N.CodecPackage.InvalidTypeForEncoding {
        while (true) {
            if (!this._is_local()) {
                org.omg.CORBA.portable.InputStream _is = null;

                try {
                    org.omg.CORBA.portable.OutputStream _os = _request("encode",
                            true);

                    // @@@ ajc _os.write_any(data);
                    _is = _invoke(_os);
                    byte[] _result = org.omg.CORBA.OctetSeqHelper.read(_is);

                    return _result;
                } catch (org.omg.CORBA.portable.RemarshalException _rx) {} catch (org.omg.CORBA.portable.ApplicationException _ax) {
                    String _id = _ax.getId();

                    if (_id.equals("IDL:omg.org/IOP_N/Codec/InvalidTypeForEncoding:1.0")) {
                        throw org.omg.IOP_N.CodecPackage.InvalidTypeForEncodingHelper.read(_ax.getInputStream());
                    } else {
                        throw new RuntimeException("Unexpected exception " + _id);
                    }
                }
                finally {
                    this._releaseReply(_is);
                }
            } else {
                org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke("encode",
                        _opsClass);

                if (_so == null) {
                    throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
                }
                CodecOperations _localServant = (CodecOperations) _so.servant;
                byte[] _result;

                try {
                    _result = _localServant.encode(data);
                }
                finally {
                    _servant_postinvoke(_so);
                }
                return _result;
            }

        }

    }

}
