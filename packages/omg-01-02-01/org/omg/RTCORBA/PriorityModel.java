package org.omg.RTCORBA;


/**
 *	Generated from IDL definition of enum "PriorityModel"
 */

public final class PriorityModel
    implements org.omg.CORBA.portable.IDLEntity {
    private int value = -1;
    public static final int _CLIENT_PROPAGATED = 0;
    public static final PriorityModel CLIENT_PROPAGATED = new PriorityModel(_CLIENT_PROPAGATED);
    public static final int _SERVER_DECLARED = 1;
    public static final PriorityModel SERVER_DECLARED = new PriorityModel(_SERVER_DECLARED);
    public int value() {
        return value;
    }

    public static PriorityModel from_int(int value) {
        switch (value) {
        case _CLIENT_PROPAGATED:
            return CLIENT_PROPAGATED;

        case _SERVER_DECLARED:
            return SERVER_DECLARED;

        default:
            throw new org.omg.CORBA.BAD_PARAM();
        }
    }

    protected PriorityModel(int i) {
        value = i;
    }
	
}
