public class GroundStationImpl extends GroundStationPOA
{
    public void sendWayPoint(short id, WayPoint[] noFlyZone)
    {
        System.out.println("GroundStation: no-fly-zone data received, id=" + id);

        for (int i = 0; i < noFlyZone.length; i++)
        {
            System.out.println("GroundStation: waypoint " + i + ": altitude=" + noFlyZone[i].altitude + " latitude=" + noFlyZone[i].latitude + " longitude=" + noFlyZone[i].longitude);
        }
    }
}

