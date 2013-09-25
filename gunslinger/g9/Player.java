package gunslinger.g9;

import java.lang.*;
import java.util.*;
import java.util.Vector;

// An example player
// Extends gunslinger.sim.Player to start with your player
//
public class Player extends gunslinger.sim.Player
{
    // total versions of the same player
    private static int versions = 0;
    // my version no
    private int version = versions++;

    public String name()
    {
        return "g9" + (versions > 1 ? " v" + version : "");
    }

    // Initialize the player
    public void init(int nplayers, int[] friends, int enemies[])
    {
        this.roundNumber = 0;
        this.nplayers = nplayers;
        this.friends = friends.clone();
        this.enemies = enemies.clone();

        this.priority = new int[nplayers];
        this.roundHistory = new int[100][nplayers];
        this.shootRecord = new int[nplayers][nplayers];
        this.constShoot = new boolean[nplayers][nplayers];
    }


    public int shoot(int[] prevRound, boolean[] alive) {
        // Don't shoot the first round
        if (roundNumber == 0) {
            roundNumber++;
            return -1;
        }

        updateStatistics(prevRound, alive);
        this.prevRound = prevRound;
        this.alive = alive;

        // When there are only 3 players left, we analyze each case and choose the correct target
        if (numAlive() <= 3) {
            roundNumber++;
            return chooseEndgameTarget();
        }

        // Set the configuration based on the state of the game
        if (numAliveFriends() > numAliveEnemies()) {
            this.configuration = this.MoreFriends;
        } else if (numAliveFriends() < numAliveEnemies()) {
            this.configuration = this.MoreEnemies;
        } else {
            this.configuration = this.Standard;
        }

        this.calculatePriority(prevRound, alive);

        // calculate who to shoot by max priority
        int target = -1;
        int max = 0;

        for (int i = 0; i < priority.length; i++) {
            if (priority[i] > max && alive[i]) {
                target = i;
                max = priority[i];
            }
        }

        roundNumber++;
        return target;
    }

    public void calculatePriority(int prevRound[], boolean[] alive) {
        // Resetting priority each round
        for (int i = 0; i < nplayers; i++) {
            priority[i] = 0;
        }

        for (int i=0; i < nplayers; i++) {
            int shooter = i;
            int victim = prevRound[i];

            // If the victim and shooter are both alive
            if (!validTarget(shooter) || !validTarget(victim)) {
                continue;
            }

            // Shooting an enemy who shot someone, we are banking on the victim retaliating on that enemy
            if (isEnemy(shooter)) {
                priority[shooter] += configuration.get("SHOOTING_ENEMY_SHOT_SOMEONE");
            }

            // Shooting an enemy that has constantly been shot, and the shooter has not been shot in the previous round
            if (whoHasShot(shooter) != -1 && constShoot[shooter][victim] && isEnemy(victim)) {
                priority[victim] += configuration.get("SHOOTING_ENEMY_WHO_IS_CONSTANTLY_SHOT");
            }

        }

        if (whoHasShot(id) != -1) {
            int whoShotMe = whoHasShot(id);
            if (whoHasShot(whoShotMe) != -1) {
                int whoShotWhoShotMe = whoHasShot(whoShotMe);
                if (validTarget(whoShotWhoShotMe) && whoHasShot(whoShotWhoShotMe) == -1) {
                    if (isEnemy(whoShotMe)) {
                        priority[whoShotMe] += configuration.get("RETALIATING_AGAINST_ENEMY_SHOOTER");
                    } else if (isNeutral(whoShotMe)) {
                        priority[whoShotMe] += configuration.get("RETALIATING_AGAINST_NEUTRAL_SHOOTER");
                    }
                }
            }
        }

        // If any of our friends have been shot, then retaliate
        for (int j = 0; j < nplayers; j++) {
            if (alive[j] && isFriend(j) && whoHasShot(j) != -1) {
                int whoShotFriend = whoHasShot(j);
                if (alive[whoShotFriend]) {
                    if (isEnemy(whoShotFriend)) {
                        priority[whoShotFriend] += configuration.get("HELP_FRIEND_RETALIATE_AGAINST_ENEMY");
                    } else if (isNeutral(whoShotFriend)) {
                        priority[whoShotFriend] += configuration.get("HELP_FRIEND_RETALIATE_AGAINST_NEUTRAL");
                    }
                }
            }
        }

        // Helping our friend shoot someone they are constantly shooting
        for (int i = 0; i < nplayers; i++) {
            for (int j = 0; j < nplayers; j++) {
                if (alive[i] && isFriend(i) && constShoot[i][j] && !isFriend(j)) {
                    priority[j] += configuration.get("HELP_FRIEND_CONSTANT_SHOOT");
                }
            }
        }

        for (int i = 0 ; i < nplayers ; i++) {
            for (int j = 0; j < nplayers ; j++) {
                // friend is shooting our enemy that our friend has shot last round
                if (alive[i] && alive[j] && isEnemy(j) && prevRound[i] == j) {
                    // Friend shoots enemy
                    if (isFriend(i)) {
                        // see how many friends are alive, and whether they have been shot
                        priority[j] += configuration.get("FRIEND_HELPING_US_SHOOT_ENEMY");
                    }
                }
            }
        }
    }

    public int chooseEndgameTarget() {
        Vector<Integer> aliveEnemies = new Vector<Integer>();
        Vector<Integer> aliveFriends = new Vector<Integer>();
        Vector<Integer> aliveNeutrals = new Vector<Integer>();

        for (int i=0; i<alive.length; i++) {
            if (prevRound[i] == id)
                return i;
            if (alive[i] && i != id) {
                if (isEnemy(i)) {
                    aliveEnemies.add(new Integer(i));
                } else {
                    if (isFriend(i)) 
                        aliveFriends.add(new Integer(i));
                    else
                        aliveNeutrals.add(new Integer(i));
                }
            }
        }

        if (aliveFriends.size() == 2)
            return -1;

        if (aliveFriends.size() == 1 && aliveNeutrals.size() == 1)
            return aliveNeutrals.get(0);

        if (aliveFriends.size() == 1 && aliveEnemies.size() == 1) {
            int e = aliveEnemies.get(0);
            return e;
        }

        if (aliveNeutrals.size() == 2) {
            int n1 = aliveNeutrals.get(0);
            int n2 = aliveNeutrals.get(1);

            if (prevRound[n1] == n2) return n1;
            if (prevRound[n2] == n1) return n2;
            return -1;
        }

        if (aliveEnemies.size() == 2 ) {
            int e1 = aliveEnemies.get(0);
            int e2 = aliveEnemies.get(1);

            if (prevRound[e1] == e2) return e1;
            if (prevRound[e2] == e1) return e2;
            return -1;
        }

        if (aliveNeutrals.size() == 1 && aliveEnemies.size() == 1) {
            int n = aliveNeutrals.get(0);
            int e = aliveEnemies.get(0);

            if (prevRound[n] == e) return e;
            if (prevRound[e] == n) return e;
            return -1;
        }

        return -1;
    }

    public void updateStatistics(int[] prevRound, boolean[] alive) {

        roundHistory[roundNumber] = prevRound;

        // update constShoot and shootRecord matrices
        // constShoot[i][j] is whether player i is constantly shooting player j
        // shootRecord[i][j] is how many times player i has shot player j
        if (roundNumber > 1) {
            for (int i=0; i < nplayers; i++) {
                for (int j=0; j < nplayers; j++) {
                    if (prevRound[i] == j && roundHistory[roundNumber - 1][i] == j) {
                        constShoot[i][j] = true;
                    } else {
                        constShoot[i][j] = false;
                    }
                }
                if (prevRound[i] != -1) {
                    shootRecord[i][prevRound[i]]++;
                }
            }
        }
    }


    // Returns whether a player is a valid target to shoot
    public boolean validTarget(int player) {
        if (player != -1 && this.alive[player]) {
            return true;
        }
        return false;
    }

    boolean isFriend(int player) {
        for (int i=0; i<friends.length; i++) {
            if (friends[i] == player) {
                return true;
            }
        }
        return false;
    }

    boolean isEnemy(int player) {
        for (int i=0; i<enemies.length; i++) {
            if (enemies[i] == player) {
                return true;
            }
        }
        return false;
    }

    boolean isNeutral(int player) {
        return (!isFriend(player)) && (!isEnemy(player));
    }


    // Returns who shot player in the previous round
    public int whoHasShot(int player) {
        for (int i = 0; i < nplayers; i++) {
            if (this.prevRound[i] == player) {
                return i;
            }
        }
        return -1;
    }

    public int numAlive() {
        int num = 0;
        for (int i = 0; i < nplayers; i++) {
            if (this.alive[i]) {
                num++;
            }
        } 
        return num;
    }

    public int numAliveEnemies() {
        int num = 0;
        for (int i = 0; i < nplayers; i++) {
            if (this.alive[i] && isEnemy(i)) {
                num++;
            }
        } 
        return num;
    }

    public int numAliveFriends() {
        int num = 0;
        for (int i = 0; i < nplayers; i++) {
            if (this.alive[i] && isFriend(i)) {
                num++;
            }
        } 
        return num;
    }

    private int nplayers;
    private int roundNumber;
    private int[] friends;
    private int[] enemies;

    private int[][] roundHistory;

    private boolean[][] constShoot;
    private int[][] shootRecord;

    private int[] prevRound;
    private boolean[] alive;

    // an array to keep the priority of each player
    private int[] priority;

    private Map<String, Integer> configuration;
    private static final Map<String, Integer> MoreFriends;
    static
    {
        MoreFriends = new HashMap<String, Integer>();
        MoreFriends.put("SHOOTING_ENEMY_SHOT_SOMEONE", 3);
        MoreFriends.put("SHOOTING_ENEMY_WHO_IS_CONSTANTLY_SHOT", 3);
        MoreFriends.put("RETALIATING_AGAINST_ENEMY_SHOOTER", 8);
        MoreFriends.put("RETALIATING_AGAINST_NEUTRAL_SHOOTER", 7);
        MoreFriends.put("HELP_FRIEND_RETALIATE_AGAINST_ENEMY", 20);
        MoreFriends.put("HELP_FRIEND_RETALIATE_AGAINST_NEUTRAL", 5);
        MoreFriends.put("HELP_FRIEND_CONSTANT_SHOOT", 6);
        MoreFriends.put("FRIEND_HELPING_US_SHOOT_ENEMY", 20);
    }

    private static final Map<String, Integer> MoreEnemies;
    static
    {
        MoreEnemies = new HashMap<String, Integer>();
        MoreEnemies.put("SHOOTING_ENEMY_SHOT_SOMEONE", 10);
        MoreEnemies.put("SHOOTING_ENEMY_WHO_IS_CONSTANTLY_SHOT", 6);
        MoreEnemies.put("RETALIATING_AGAINST_ENEMY_SHOOTER", 9);
        MoreEnemies.put("RETALIATING_AGAINST_NEUTRAL_SHOOTER", 8);
        MoreEnemies.put("HELP_FRIEND_RETALIATE_AGAINST_ENEMY", 9);
        MoreEnemies.put("HELP_FRIEND_RETALIATE_AGAINST_NEUTRAL", 5);
        MoreEnemies.put("HELP_FRIEND_CONSTANT_SHOOT", 5);
        MoreEnemies.put("FRIEND_HELPING_US_SHOOT_ENEMY", 9);
    }

    private static final Map<String, Integer> Standard;
    static
    {
        Standard = new HashMap<String, Integer>();
        Standard.put("SHOOTING_ENEMY_SHOT_SOMEONE", 10);
        Standard.put("SHOOTING_ENEMY_WHO_IS_CONSTANTLY_SHOT", 10);
        Standard.put("RETALIATING_AGAINST_ENEMY_SHOOTER", 9);
        Standard.put("RETALIATING_AGAINST_NEUTRAL_SHOOTER", 7);
        Standard.put("HELP_FRIEND_RETALIATE_AGAINST_ENEMY", 8);
        Standard.put("HELP_FRIEND_RETALIATE_AGAINST_NEUTRAL", 5);
        Standard.put("HELP_FRIEND_CONSTANT_SHOOT", 6);
        Standard.put("FRIEND_HELPING_US_SHOOT_ENEMY", 10);
    }
}