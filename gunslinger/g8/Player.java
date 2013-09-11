package gunslinger.g8;

import java.util.*;

// Extends gunslinger.sim.Player to start with your player
public class Player extends gunslinger.sim.Player
{
    // total versions of the same player
    private static int versions = 0;
    // my version no
    private int version = versions++;
    
    // A simple fixed shoot rate strategy used by the dumb player
    private static double ShootRate = 0.8;

    // name of the team
    //
    public String name()
    {
        return "g8";
    }
 
    // Initialize the player
    //
    public void init(int nplayers, int[] friends, int enemies[])
    {
        this.nplayers = nplayers;
        this.friends = new Integer[friends.length];
        for (int i = 0; i < friends.length; i++) {
            this.friends[i] = friends[i];
        }
        this.enemies = new Integer[enemies.length];
        for (int i = 0; i < enemies.length; i++) {
            this.enemies[i] = enemies[i];
        }
    	this.history = new List[nplayers];
        this.timesTargeted = new Integer[nplayers];
    	for (int i = 0; i < nplayers; i++) {
    	    history[i] = new ArrayList<Integer>();
            timesTargeted[i] = 0;
    	}

    }

    // Pick a target to shoot
    // Parameters:
    //  prevRound - an array of previous shoots, prevRound[i] is the player that player i shot
    //              -1 if player i did not shoot
    //  alive - an array of player's status, true if the player is still alive in this round
    // Return:
    //  int - the player id to shoot, return -1 if do not shoot anyone
    //
    public int shoot(int[] prevRound, boolean[] alive)
    {
    	if (prevRound != null) {
      	    for (int i = 0; i < nplayers; i++) {    		
        	history[i].add(prevRound[i]);
                if (prevRound[i] != -1) {
                    timesTargeted[prevRound[i]]++;
                }
    	    }
    	}
        int[] ranks = new int[nplayers];
        for (int i = 0; i != nplayers; ++i) {
            if (i != id && alive[i] && !Arrays.asList(friends).contains(i)) {
                ranks[i] = rank(alive, i);
	    }
	}
        int target = -1;
    	int maxRank = 0;
    	for (int i = 0; i < nplayers; i++) {
    	    if (ranks[i] > maxRank || 
                target != -1 && ranks[i] == maxRank && 
                timesTargeted[i] > timesTargeted[target]) {
        		target = i;
        		maxRank = ranks[i];
            }
    	}
        return target;
    }

    // Assigns a ranking to a player. Higher ranking means a higher priority to shoot
    // Parameters:
    //  alive - an array of player's status, true if the player is still alive in this round
    //  target - the id of the person we are trying to assign a ranking to
    // Rerturn:
    // int - the ranking for the target
    public int rank(boolean[] alive, int target) {
    	int rank = Arrays.asList(enemies).contains(target) ? 1 : 0;        
    	List<Integer> playerHistory = history[target];
    	for (int otherTarget : playerHistory) {
    	    if (id == otherTarget) {
    		  rank += 2;
    	    } else if (Arrays.asList(friends).contains(otherTarget) && alive[otherTarget]) {
    		  rank++;
    	    } else if (Arrays.asList(enemies).contains(otherTarget) && alive[otherTarget]) {
    		  rank--;
    	    }
    	}
    	return rank;
    }

    // number of players in the game
    private int nplayers;
    // list of ids that correspond to friends
    private Integer[] friends;
    // list of ids that correspond to enemies
    private Integer[] enemies;
    // array of Lists that represent the previous moves of the other players
    private List<Integer>[] history;
    // list that represents the number of times each player has been targeted
    private Integer[] timesTargeted;
}
