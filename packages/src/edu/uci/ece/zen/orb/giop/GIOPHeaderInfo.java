package edu.uci.ece.zen.orb.giop;

import javax.realtime.ImmortalMemory;


/** Inner class used to represent the information parsend from a
 * GIOP message header.
 *
 * @author Bruce Miller
 */
public class GIOPHeaderInfo {
    boolean nextMessageIsFragment = false;
    int messageSize = 0;
    boolean isLittleEndian;
    byte giopMajorVersion;
    byte giopMinorVersion;
    byte messageType;
	

}

class GIOPHeaderInfoHelper 
{
	private static GIOPHeaderInfo ghi;

	public static GIOPHeaderInfo instance()
	{
        try
        {
		    if (ghi == null)
			    ghi = (GIOPHeaderInfo) ImmortalMemory.instance().newInstance(GIOPHeaderInfo.class);
		    return ghi;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
	}
}
