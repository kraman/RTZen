package org.omg.RTPortableServer;


public interface POA extends org.omg.PortableServer.POA {
    Object create_reference_with_priority(
            String  intf,
            short priority
            ) throws org.omg.PortableServer.POAPackage.WrongPolicy;

    Object create_reference_with_id_and_priority(
            byte[] oid,
            String intf,
            short priority
            ) throws org.omg.PortableServer.POAPackage.WrongPolicy;

    byte[] activate_object_with_priority(
            org.omg.PortableServer.Servant p_servant,
            short priority
            )throws org.omg.PortableServer.POAPackage.ServantAlreadyActive,
                org.omg.PortableServer.POAPackage.WrongPolicy;

    void activate_object_with_id_and_priority(
            byte[] oid,
            org.omg.PortableServer.Servant p_servant,
            short priority
            ) throws org.omg.PortableServer.POAPackage.ServantAlreadyActive,
                org.omg.PortableServer.POAPackage.ObjectAlreadyActive,
                org.omg.PortableServer.POAPackage.WrongPolicy;
}
