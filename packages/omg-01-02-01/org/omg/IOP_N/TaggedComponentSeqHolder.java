package org.omg.IOP_N;


/**
 *	Generated from IDL definition of alias "TaggedComponentSeq"
 *	@author IDL compiler 
 */

final public class TaggedComponentSeqHolder
    implements org.omg.CORBA.portable.Streamable {
    public org.omg.IOP.TaggedComponent[] value;

    public TaggedComponentSeqHolder() {}

    public TaggedComponentSeqHolder(org.omg.IOP.TaggedComponent[] initial) {
        value = initial;
    }

    public org.omg.CORBA.TypeCode _type() {
        return TaggedComponentSeqHelper.type();
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = TaggedComponentSeqHelper.read(in);
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        TaggedComponentSeqHelper.write(out, value);
    }
}
