package gunslinger.g4;

import java.util.*;

// The base class of a player
// Extends the base class to start your player
// See dumb/Player.java for an example
//
public class Player extends gunslinger.sim.Player
{

    private int nplayers;
    private int[] friends;
    private int[] enemies;
    
    // total versions of the same player
    private static int versions = 0;
    // my version no
    private int version = versions++;
    
    // name of the team
    //
    public String name()
    {
        return "Group3Player" + (versions > 1 ? " v" + version : "");
    }
    
    // Initialize the player
    //
    public void init(int nplayers, int[] friends, int enemies[])
    {
        this.nplayers = nplayers;
        this.friends = friends.clone();
        this.enemies = enemies.clone();  	
    }

    // Pick a target to shoot
    // Parameters:
    //  prevRound - an array of previous shoots, prevRound[i] is the player that player i shot
    //              -1 if player i did not shoot
    //  alive - an array of player's status, true if the player is still alive in this round
    // Return:
    //  int - the player id to shoot, return -1 if do not shoot anyone
    //
    public int shoot(int[] prevRound, boolean[] alive){
    	    	
    	if (prevRound == null)
    	{
    		//First Round Strategy -> wait do nothing
    	}
    	else
    	{
    		//Priority 1: Shoot person you shot at before if they are not dead
    		int lastPersonShotAt = prevRound[id];
	    		
	    	if( lastPersonShotAt != -1 && alive[lastPersonShotAt] )
	    	{
	    		return lastPersonShotAt;
	    	}

	    	//Priority 2: Shoot the person who shot you last round
	    	for(int i = 0;i < prevRound.length; i++)
	    	{
	    		if( (prevRound[i] == id) && alive[i] )
	    		{
	    			return i;
	    		}
	       	}
    		
			//Priority 3: Shoot at enemies that shot at friends
			for(int i = 0;i < prevRound.length; i++)
			{
				for(int j = 0;j < friends.length; j++)
				{
					// Did the player shoot a friend?
					if ( (friends[j] == prevRound[i]) && alive[i])
					{
						// Is the player an enemy
						for(int k = 0;k < enemies.length; k++)
						{
							if (enemies[k] == i)
							{
								return i;
							}
							//else keep a low profile by not killing neutral players
						}
					}
				}
			}		
    	}
    	    	
    	return -1;
    	
    }
    
}
