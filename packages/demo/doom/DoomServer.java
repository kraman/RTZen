package demo.doom;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import javax.realtime.ScopedMemory;
import javax.realtime.LTMemory;
import javax.realtime.RealtimeThread;

public class DoomServer extends RealtimeThread
{
	private static final int MAP_ARRAY_SIZE = 0x10000;
    private static final ScopedMemory scope = new LTMemory(1024 * 1024,1024 * 1024 * 10);
    static DoomMap curMap = generateMap();
    PlayerTable playerTable;

    public static void main(String args[]) throws Exception {
        (new RealtimeThread(null, null, null, scope, null, new Starter())).start();
    }

    public static class Starter extends RealtimeThread {
        
        public void run() {
            try {
                System.out.println( "=====================Creating RT Thread in server==========================" );
                RealtimeThread rt = new DoomServer();
                System.out.println( "=====================Starting RT Thread in server==========================" );
                rt.start();
            } catch (Exception e) {
                System.out.println("Exception creating RT thread " + e);
            }
        }
    }
    
    public DoomServer() {
        super(null, null, null, scope, null, null);
    }

    public void run() {
        System.out.println("Current MA = " + RealtimeThread.getCurrentMemoryArea());
        try
        {
            System.out.println( "=====================Calling ORB Init in server============================" );
            ORB zen = ORB.init((String[])null, null);
            System.out.println( "=====================ORB Init complete in server===========================" );

            POA rootPOA = POAHelper.narrow(zen.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();
            System.out.println( "=================== RootPOA resolved, starting servant_to_ref ==============" );

            // Register 
            // - with the CORBA Naming Service
            // NamingContextExt nameService = NamingContextExtHelper.narrow(zen.resolve_initial_references("NameService"));

            CallServerImpl callServerImpl = new CallServerImpl(this);
            org.omg.CORBA.Object callServerObj = rootPOA.servant_to_reference(callServerImpl);
            System.out.println( "=================== CallServer registered ========================" );
            PlayerTableImpl playerTableImpl = new PlayerTableImpl();
            System.out.println("rootPOA.servant_to_reference(playerTableImpl)");
            org.omg.CORBA.Object playerTableObj = rootPOA.servant_to_reference(playerTableImpl);
	    //            System.out.println("PlayerTableHelper.narrow(" + playerTableObj + ")");
            playerTable = PlayerTableHelper.unchecked_narrow(playerTableObj);
            System.out.println( "=================== PlayerTable registered ========================" );


            // Register
            // - with the CORBA Naming Service
            // nameService.rebind( nameService.to_name("CallServer"), callServerObj );

	    // - or using files
            System.out.println( "=================== Getting IOR and writing it to ior.txt ========================" );
            String ior = zen.object_to_string(callServerObj);
            BufferedWriter bw = new BufferedWriter( new FileWriter("CallServer_IOR.txt") );
            System.out.println("bw.write(ior)");
	    System.out.println("ior = " + ior);
            bw.write(ior);
            System.out.println("bw.close()");
            bw.close();
//            System.out.println("zen.object_to_string(playerTableObj)");
//            ior = zen.object_to_string(playerTableObj);
//            bw = new BufferedWriter( new FileWriter("PlayerTable_IOR.txt") );
//            bw.write(ior);
//            bw.close();

            System.out.println( "============================ ZEN.run() ====================================" );
            zen.run();
        }
        catch (Exception e)
        {
            System.out.println("Exception in DoomServer.run(): ");
            e.printStackTrace();
            System.exit(-1);
        }
    }

	static int[] createLightMap() {
		// The lightmap generation code broke at some point during the conversion from
		// RMI to CORBA. It has been commented out and replaced with a simple hard-coded
		// version that does pretty much the same thing.
		/*
		try
		{
			int[] imageMap = new int[MAP_ARRAY_SIZE];
			int[] lightMap = new int[MAP_ARRAY_SIZE];
			java.awt.Image image = Toolkit.getDefaultToolkit().getImage("images/lights.gif");
			MediaTracker mediatracker = new MediaTracker(this);
			mediatracker.addImage(image, 1);
			mediatracker.waitForAll();
			PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, 256, 256, imageMap, 0, 256);
			pixelgrabber.grabPixels();

			for(int i = 0; i < MAP_ARRAY_SIZE; i++)
			{
				lightMap[i] = (imageMap[i] & 0xff) + 64;
				if (lightMap[i] > 255)
					lightMap[i] = 255;
			}
			
			return lightMap;
		}
		catch(InterruptedException _ex)
		{
			System.err.println("Image loading interrupted");
			return null;
		}
		*/
		int[] lightMap = new int[MAP_ARRAY_SIZE];
		java.util.Arrays.fill(lightMap, 255);
		return lightMap;
	}

	static int[] createMap() {
		int[] map = new int[MAP_ARRAY_SIZE];

		for(int l2 = 0; l2 < 256; l2++) {
			for(int j2 = 0; j2 < 256; j2++)
				map[(j2 << 8) + l2] = -1;
		}

		for(int l3 = 0; l3 < 300; l3++) {
			int i = (int)(Math.random() * 200D);
			int k = (int)(Math.random() * 200D);
			int i1 = (int)(Math.random() * 50D);
			int j1 = (int)(Math.random() * 50D);
			int i2 = (int)(Math.random() * 22D);
			for(int k2 = i; k2 < i + i1; k2++) {
				map[(k << 8) + k2] = i2;
				map[(k + j1 << 8) + k2] = i2;
			}

			for(int i3 = k; i3 < k + j1; i3++) {
				map[(i3 << 8) + i] = i2;
				map[(i3 << 8) + i + i1] = i2;
			}

		}

		for(int i4 = 0; i4 < 1500; i4++) {
			int j = (int)(Math.random() * 255D);
			int k1 = (int)(Math.random() * 255D);
			int l1;
			do
				l1 = (int)(Math.random() * 255D);
			while(k1 > l1 || l1 - k1 > 18);
			for(int j3 = k1; j3 < l1; j3++)
				map[(j3 << 8) + j] = -1;

			j = (int)(Math.random() * 255D);
			k1 = (int)(Math.random() * 255D);
			do
				l1 = (int)(Math.random() * 255D);
			while(k1 > l1 || l1 - k1 > 15);
			for(int k3 = k1; k3 < l1; k3++)
				map[(j << 8) + k3] = -1;

		}

		for(int j4 = 0; j4 < 256; j4++) {
			map[j4 << 8] = 1;
			map[(j4 << 8) + 255] = 1;
			map[j4] = 1;
			map[65280 + j4] = 1;
		}
		return map;
	}
	
	static byte[] createFloorMap() {
		byte[] floorMap = new byte[MAP_ARRAY_SIZE];

		for(int k4 = 0; k4 < 5000; k4++) {
			byte byte0 = (byte)(int)(Math.random() * 22D);
			int i5 = (int)(Math.random() * 230D);
			int k5 = (int)(Math.random() * 230D);
			int i6 = i5 + (int)(Math.random() * 12D);
			int k6 = k5 + (int)(Math.random() * 12D);
			for(int k7 = k5; k7 < k6; k7++) {
				for(int i7 = i5; i7 < i6; i7++)
					floorMap[k7 * 256 + i7] = byte0;
			}
		}
		
		return floorMap;
	}

	static byte[] createCeilingMap() {
		byte[] ceilingMap = new byte[MAP_ARRAY_SIZE];
		for(int l4 = 0; l4 < 5000; l4++) {
			byte byte1 = (byte)(int)(Math.random() * 22D);
			int j5 = (int)(Math.random() * 230D);
			int l5 = (int)(Math.random() * 230D);
			int j6 = j5 + (int)(Math.random() * 12D);
			int l6 = l5 + (int)(Math.random() * 12D);
			for(int l7 = l5; l7 < l6; l7++)
			{
				for(int j7 = j5; j7 < j6; j7++)
					ceilingMap[l7 * 256 + j7] = byte1;
			}
		}
		return ceilingMap;
	}

	public static DoomMap generateMap() {
	    System.out.println("Generating map");
	    System.out.println("new DoomMap");
		DoomMap curMap = new DoomMap();
            System.out.println("createMap");
		curMap.map = createMap();
            System.out.println("createLightMap");
		curMap.lightMap = createLightMap();
            System.out.println("createFloorMap");
		curMap.floorMap = createFloorMap();
            System.out.println("createCeilingMap");
		curMap.ceilingMap = createCeilingMap();
        return curMap;
	}

	public void showPlayer() {
		String s = "";
		for(int i = 0; i < playerTable.getPlayerCount(); i++) {
			Player player = playerTable.getPlayer(i);
			s += player.name + "\n";
		}
		System.out.println("### A new player added/removed! List of all current players: \n" + s);
	}
}
