package demo.serial_transport;

public class AirplaneImpl extends AirplanePOA
{
    public void SendWayPoint(short id, WayPoint waypoint)
    {
        System.out.println("sending waypoint");
    }
}
