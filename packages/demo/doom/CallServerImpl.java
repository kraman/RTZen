package demo.doom;

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
		synchronized(playerTable)
		{
			playerTable.setXYpos(name, xpos, ypos);
			return playerTable;
		}
	}

	public DoomMap join(String name)
	{
		synchronized(playerTable)
		{
			if (playerTable.addPlayer(name, 8421376, 8421376))
			{
				doomServer.curMap.success = true;
				doomServer.showPlayer();
				return doomServer.curMap;
			}

			doomServer.curMap.success = false;
			doomServer.showPlayer();
			return doomServer.curMap;
		}
	}

	private DoomServer doomServer;
	private PlayerTable playerTable;
}
