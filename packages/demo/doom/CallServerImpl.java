package demo.doom;
import javax.realtime.RealtimeThread;
import javax.realtime.MemoryArea;

public class CallServerImpl extends CallServerPOA
{
    public CallServerImpl(DoomServer doomserver)
    {
        doomServer = doomserver;
        playerTable = doomserver.playerTable;
    }

    public boolean exit(String name)
    {
        synchronized(playerTable)
        {
            playerTable.deletePlayer(name);
            doomServer.showPlayer();
            return true;
        }
    }

    public PlayerTable inform(String name, int xpos, int ypos)
    {
        //System.out.println("CallServerImpl.inform()");
        synchronized(playerTable)
        {
            //System.out.println("calling setXYpos");
            playerTable.setXYpos(name, xpos, ypos);
            return playerTable;
        }
    }

    public DoomMap join(String name)
    {
        System.out.println("CallServerImpl.join()" + playerTable);
//      System.out.println("Current MA = " + RealtimeThread.getCurrentMemoryArea());
        synchronized(playerTable)
        {
            System.out.println("))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))CallServerImpl.join()1");
            if (playerTable.addPlayer(name, 8421376, 8421376))
            {
                doomServer.curMap.success = true;
                System.out.println("))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))CallServerImpl.join()2");
                doomServer.showPlayer();
                System.out.println("))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))CallServerImpl.join()3");
//                 Util.pln("Going to return curMap");
//                 System.out.println("MA(curMap) = " + MemoryArea.getMemoryArea(doomServer.curMap));
                return doomServer.curMap;
            }
System.out.println("))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))CallServerImpl.join()4");
            doomServer.curMap.success = false;
            doomServer.showPlayer();
//            Util.pln("Going to return curMap");
//            System.out.println("MA(curMap) = " + MemoryArea.getMemoryArea(doomServer.curMap));

            return doomServer.curMap;
        }
    }

    private DoomServer doomServer;
    private PlayerTable playerTable;
}

