package gunslinger.g1;

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

    // Attributes to use in the feature vector
    private final int NUM_FEATURES = 5;

    private final int ENEMY        = 0;
    private final int FRIEND       = 1;
    private final int NONE         = 2;
    private final int FOE          = 3;
    private final int FRIENDS_FOE  = 4;

    private final float[] coeffs = new float[]{5.0f,-5.0f, 0.0f, 20.0f, 5.0f};

    // name of the team
    public String name()
    {
        return "g1(" + VERSION + ")" + (versions > 1 ? " v" + playerNumber : "");
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

        this.featureVectors = new int[nplayers][NUM_FEATURES];
        initFeatureVectors();
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
        updateFeatureVectors(prevRound, alive);

        /*
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
        */

        // If only friends, don't shoot
        // if (targets.size() == 0) return -1;

        int[] playersScores = new int[nplayers];
        for (int i = 0; i < nplayers; i++){
            // featureVectors[i] dot coeffs
            for (int j = 0; j < NUM_FEATURES; j++){
                playersScores[i] += coeffs[j] * featureVectors[i][j];
            }
        }

        // find biggest score, -1 if score is negative
        int playerToShoot = -1;
        float biggestScore = 0.0f;
        for (int i = 0; i < nplayers; i++) {
          if (playersScores[i] > biggestScore) {
              biggestScore = playersScores[i];
              playerToShoot = i;
          }
        }

        return playerToShoot;
    }

    // TODO: fix dumb logic
    private void initFeatureVectors(){
        for (int i : friends){
            featureVectors[i][FRIEND] = 1;
        }
        for (int i : enemies){
            featureVectors[i][ENEMY] = 1;
        }
        for (int i = 0; i < nplayers; i++){
            if (!contains(i, enemies) && !contains(i, friends))
                featureVectors[i][NONE] = 1;
        }
    }

    private void updateFeatureVectors(int[] prevRound, boolean[] alive)
    {
        if (prevRound == null) return;

        for (int i = 0; i < nplayers; i++){
            // player i last shot lastShot
            int lastShot = prevRound[i];
            if (contains(lastShot, friends))
                featureVectors[i][FRIENDS_FOE]++;
            if (lastShot == id)
                // TODO: make this number of ur friends he has shot,
                // not number of times he has shot ur friends
                featureVectors[i][FOE]++;
        }
    }

    // helper to fill lists with living friends and enemies
    private void updateLists(boolean[] alive)
    {
        livingFriends = new ArrayList<Integer>();
        livingEnemies = new ArrayList<Integer>();

        for (int i = 0; i < nplayers; i++){
            if (alive[i]) {
                if (contains(i, enemies))
                    livingEnemies.add(i);
                if (contains(i, friends))
                    livingFriends.add(i);
            }
        }

    }

    public boolean contains(int player, int[] lst){
        for (int i : lst)
            if (i == player) return true;
        return false;
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

    private int[][] featureVectors;
}
