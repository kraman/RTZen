package demo.serial_transport;

public class AirplaneImpl extends AirplanePOA
{
    public void sendWayPoint(short id, WayPoint waypoint)
    {
        System.out.println("sending waypoint");
    }
}

