package gunslinger.g2;

import java.util.*;

// An example player
// Extends gunslinger.sim.Player to start with your player
//
public class Player extends gunslinger.sim.Player
{
    // total versions of the same player
    private static int versions = 0;
    private int playerNumber = versions++;

    public static final String VERSION = "0.0.2";

    // name of the team
    //
    public String name()
    {
        return "g2(" + VERSION + ")" + (versions > 1 ? " v" + playerNumber : "");
    }

    // Initialize the player
    //
    public void init(int nplayers, int[] friends, int enemies[])
    {
        // Note:
        //  Seed your random generator carefully
        //  if you want to repeat the same random number sequence
        //  pick your favourate seed number as the seed
        //  Or you can simply use the clock time as your seed
        //
        gen = new Random(System.currentTimeMillis());
        // long seed = 12345;
        // gen = new Random(seed);

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
    public int shoot(int[] prevRound, boolean[] alive)
    {
        updateLists(alive);

        ArrayList<Integer> targets = new ArrayList<Integer>();
        for (int i = 0; i != nplayers; i++) {
            if (validTarget(i, alive)) {
                // If you have living enemies, shoot one
                if (livingEnemies.size() > 0) {
                  if (livingEnemies.contains(i)) {
                      targets.add(i);
                  }
                }
                // If you have no living enemies, pick a non friend to shoot
                else if (!livingFriends.contains(i)) {
                    targets.add(i);
                }
            }
        }

        // If only friends, don't shoot
        if (targets.size() == 0) return -1;

        return targets.get(gen.nextInt(targets.size()));
    }

    // helper to fill lists with living friends and enemies
    private void updateLists(boolean[] alive)
    {
        livingFriends = new ArrayList<Integer>();
        livingEnemies = new ArrayList<Integer>();

        for (int i = 0; i < nplayers; i++){
            if (alive[i]) {
                boolean contains = false;
                for (int friend : friends){
                    if (friend == i){
                        contains = true;
                        break;
                    }
                }
                if (contains)
                    livingFriends.add(i);

                contains = false;
                for (int enemy : enemies){
                    if (enemy == i){
                        contains = true;
                        break;
                    }
                }
                if (contains)
                    livingEnemies.add(i);
            }
        }
    }

    private boolean validTarget(int player, boolean[] alive)
    {
        return player != id && alive[player];
    }

    private Random gen;

    // Should never change. The initial info about the world
    private int nplayers;
    private int[] friends;
    private int[] enemies;

    private ArrayList<Integer> livingFriends;
    private ArrayList<Integer> livingEnemies;
}