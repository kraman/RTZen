package demo.doom;

import java.util.*;
import javax.realtime.RealtimeThread;
import java.lang.reflect.Array;

class PlayerTableImpl extends PlayerTablePOA
{
	private final static int MAX_PLAYERS = 16;

	private Vector players;
	
	public PlayerTableImpl()
	{
	    // Allocated at the construction time, which means in the servant's scope
        players = new Vector(MAX_PLAYERS);
	}

	public boolean playerExists(String name)
	{
		return getPlayer(name) != null;
	}	
	
	private Player getPlayer(String name)
	{
		for (Iterator i = players.iterator(); i.hasNext(); )
		{
			Player player = (Player) i.next();

			if (player.name.equals(name))
				return player;
		}
		
		return null;
	}

	public Player getPlayer(int index)
	{
		return (Player) players.get(index);
	}
    
	public boolean addPlayer(String _name, int _xpos, int _ypos)
	{  
	    // Copy name to the servant's scope
        String name = (String) MAUtils.buildInMA(
                DoomServer.scope,
                String.class,
                new Class[] {(_name.getBytes()).getClass()},
                new Object[] {_name.getBytes()});

        for (int i=0; i < RealtimeThread.getMemoryAreaStackDepth(); i++)
            System.out.println("MAstack[" + i + "] = " + RealtimeThread.getOuterMemoryArea(i));
        System.out.println("getInitialMemoryAreaIndex = " + RealtimeThread.getInitialMemoryAreaIndex());
        System.out.println("PlayerTableImpl.addPlayer()");
        System.out.println("Current MA = " + RealtimeThread.getCurrentMemoryArea());

        if (players.size() == MAX_PLAYERS)
            return false;

        if ( playerExists(name) )
            return false;
        Util.pln("Build in MA (Player), name = " + name);
        // Allocate player in servant's scope
        Player player = (Player) MAUtils.buildInMA(
                DoomServer.scope,
                Player.class,
                new Class[] {String.class, Integer.TYPE, Integer.TYPE},
                new Object[] {name, new Integer(_xpos), new Integer(_ypos)});
        
        // Add player
        players.add( player );
        return true;
    }

	public boolean deletePlayer(String name)
	{
		if ( !playerExists(name) )
			return false;

		for (Iterator i = players.iterator(); i.hasNext(); )
		{
			Player player = (Player) i.next();

			if (player.name.equals(name))
				i.remove();
		}

		return true;
	}

	public int getPlayerCount()
	{
		return players.size();
	}

	public boolean isEmpty()
	{
		return getPlayerCount() == 0;
	}

	public boolean setXYpos(String name, int xpos, int ypos)
	{ 
		Player player = getPlayer(name);
		
		if (player == null)
			return false;

		player.xpos = xpos;
		player.ypos = ypos;
		return true;
	}
}
