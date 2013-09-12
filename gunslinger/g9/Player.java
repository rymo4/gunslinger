package gunslinger.g9;

import java.util.*;

// An example player
// Extends gunslinger.sim.Player to start with your player
//
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
        return "g9" + (versions > 1 ? " v" + version : "");
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
        
        this.roundNumber = 0;
        this.nplayers = nplayers;
        this.friends = friends.clone();
        this.enemies = enemies.clone();

        priority = new double[nplayers];
        roundHistory = new int[100][nplayers];
        
        for (int i = 0; i < nplayers; i++) {
            this.priority[i] = 0.0;
        }
    }

    boolean is_friend(int p) {
        for (int i=0; i<friends.length; i++) {
            if (friends[i] == p) {
                return true;
            }
        }
        return false;
    }

    boolean is_enemy(int p) {
        for (int i=0; i<enemies.length; i++) {
            if (enemies[i] == p) {
                return true;
            }
        }
        return false;
    }
    
    // Pick a target to shoot
    // Parameters:
    //  prevRound - an array of previous shoots, prevRound[i] is the player that player i shot
    //              -1 if player i did not shoot
    //  alive - an array of player's status, true if the player is still alive in this round
    // Return:
    //  int - the player id to shoot, return -1 if do not shoot anyone
    //
    public int shoot(int[] prevRound, boolean[] alive) {
        // Store the round history
        int target = -1;


        
        /* Strategy used by the dumb player:
         Decide whether to shoot or not with a fixed shoot rate
         If decided to shoot, randomly pick one alive that is not your friend */
        
        if (roundNumber == 0) {
            ArrayList<Integer> targets = new ArrayList<Integer>();
            for (int i = 0; i < nplayers; ++i)
                if (i != id && alive[i] && is_enemy(i)) {
                    target = i;
                    break;
                }

        } else {
            roundHistory[roundNumber] = prevRound;
            this.calculatePriority();

            System.out.println("prevRound");
            for (int i=0; i<prevRound.length; i++) 
                System.out.print(prevRound[i] + " ");
            System.out.println();

            // calculate who to shoot by max priority
            target = -1;
            double max=0.0;
            for (int i=0; i< priority.length; i++) {
                if (alive[i] && is_enemy(i))
                    target = i;
                max = priority[i];
            }

            
            for (int i = 0; i < priority.length; i++) {
                
                if (priority[i] > max && alive[i]) {
                    target = i;
                    max=priority[i];
                }
            }
            print_priority();
        }
        
        roundNumber++;
        return target;
    }

    public void print_priority() {
        System.out.println("Priority for round " + roundNumber);

        for (int i=0; i<nplayers; i++) {
            System.out.print(priority[i] + " ");
        }

        System.out.println();
        System.out.println();
    }
    
    public void calculatePriority() {
        boolean[] hasPriorityAssigned = new boolean[nplayers];
        
        // Someone is shooting you
        for (int i = 0; i < nplayers; i++) {
            if (roundHistory[roundNumber][i] == id) {
                if (is_enemy(i)) {
                    priority[i] += ENEMY_SHOOTING_YOU;
                } else if (is_friend(i)) {
                    priority[i] += FRIEND_SHOOTING_YOU;
                } else {
                    priority[i] += NEUTRAL_SHOOTING_YOU;
                }
                hasPriorityAssigned[i] = true;
            }
        }

        print_priority();
        // Anyone is shooting your friend
        for (int i = 0; i < nplayers; i++) {
            int player_being_shot = roundHistory[roundNumber][i];
            System.out.println("player_being_shot " + player_being_shot);
            if (is_friend(player_being_shot)) {

                if (!is_friend(i)) {
                    if (is_enemy(i)) {
                        priority[i] += ENEMY_SHOOTING_FRIEND;
                    } else {
                        priority[i] += NEUTRAL_SHOOTING_FRIEND;
                    }
                    hasPriorityAssigned[i] = true;
                }

            }
        }
        print_priority();
        
        // Shooting an enemy who is consistently shot
        if (roundNumber > 1) {
            int currentRound = roundNumber;
            int prevRound = roundNumber - 1;
            
            for (int i = 0; i < nplayers; i++) {
                int currentShot = roundHistory[currentRound][i];
                int prevShot = roundHistory[prevRound][i];
                if (currentShot == prevShot) {
                    if (is_enemy(prevShot)) {
                        priority[currentShot] += ENEMY_CONSISTENTLY_SHOT;
                        hasPriorityAssigned[currentShot] = true;
                    }
                }
            }
        }
        
        // Shooting an enemy who doesn't fit the above cases
        for (int i = 0; i < enemies.length; i++) {
            if (!hasPriorityAssigned[enemies[i]]) {
                priority[enemies[i]] += ENEMY;
            }
        }

    }
    
    private Random gen;
    private int nplayers;
    private int roundNumber;
    private int[] friends;
    private int[] enemies;
    private int[][] roundHistory;
    
    private double[] priority;
    
    private static final double ENEMY_SHOOTING_YOU = .5;
    private static final double NEUTRAL_SHOOTING_YOU = .4;
    private static final double FRIEND_SHOOTING_YOU = .35;
    private static final double ENEMY_SHOOTING_FRIEND = .3;
    private static final double NEUTRAL_SHOOTING_FRIEND = .25;
    private static final double ENEMY_CONSISTENTLY_SHOT = .2;
    private static final double ENEMY = .1;
}
