/***** Copyright (c) 1999 Object Management Group. Unlimited rights to
     duplicate and use this code are hereby granted provided that this 
     copyright notice is included.
*****/

package org.omg.CORBA.TypeCodePackage;

public final class BadKind extends org.omg.CORBA.UserException
{

    public BadKind()
    {
        super( "IDL:omg.org/CORBA/TypeCode/BadKind:1.0" );
    }

    public BadKind( String reason )
    { // full constructor
        super( "IDL:omg.org/CORBA/TypeCode/BadKind:1.0 " + reason );
    }
}
