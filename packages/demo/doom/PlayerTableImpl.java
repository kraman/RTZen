package demo.doom;

import java.util.*;

class PlayerTableImpl extends PlayerTablePOA
{
	private final static int MAX_PLAYERS = 16;

	private Vector players;
	
	public PlayerTableImpl()
	{
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

	public boolean addPlayer(String name, int xpos, int ypos)
	{
		if (players.size() == MAX_PLAYERS)
			return false;
		
		if ( playerExists(name) )
			return false;
		
		players.add( new Player(name, xpos, ypos) );

		return true;
	}

	public boolean deletePlayer(String name)
	{
		if ( playerExists(name) )
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
