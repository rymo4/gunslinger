package gunslinger.g5;

import java.util.*;


/**
 * Group 5's proprietary Player implementation.
 * @author Andrew Goldin
 * @author Priyanka Singh
 * @author Neha Aggarwal
 * 
 * TODO: Decide if the tiebreaker is a good idea or if it is even necessary.
 * TODO: Decide whether to take into account e/f/n ratios in any circumstance.
 * 		if (f>e+n), friend heavy. if (e>f+n), enemy heavy. if (n>e+f), neutral heavy
 * TODO: Modify weighting system to incorporate retaliation/reinforcement factors.
 * TODO: Do well in games where f<2 or e>>f, not so well when f>e. Maybe devise a smarter way to help/protect friends in this case.
 * TODO: Continue compartmentalizaion. Weight manager class and static constants for weight values.
 */
public class Player extends gunslinger.sim.Player {

	// total versions of the same player
	private static int versions = 0;
	// my version no
	private int version = versions++;

	// A simple fixed shoot rate strategy used by the dumb player
	private static double ShootRate = 0.8;

	private Random gen;
	private int nplayers;
	private ArrayList<Integer> friends;
	private ArrayList<Integer> enemies;
	private int[] weights;
	private int numNeutrals;

	private int[] prevRound;
	private boolean[] alive;
	private GameHistory history;
	private PriorityManager manager;


	/**
	 * Returns the player/team name.
	 */
	public String name() {
		return "g5" + (versions > 1 ? " v" + version : "");
	}


	/**
	 * Initializes the player.
	 * @param nplayers the number of players in the game
	 * @param friends a list of the player's friends
	 * @param enemies a list of the player's enemies
	 */
	public void init(int nplayers, int[] friends, int[] enemies) {

		// seed the generator
		gen = new Random(System.currentTimeMillis());
		// long seed = 12345;
		// gen = new Random(seed);

		this.nplayers = nplayers;

		// creating the weight array
		this.weights = new int[nplayers];

		// number of neutral players
		numNeutrals = nplayers - friends.length - enemies.length - 1;

		// initialize friends/enemies instance variables
		this.friends = new ArrayList<Integer>();
		this.enemies = new ArrayList<Integer>();

		// populate friend and enemy lists
		for (int i = 0; i != friends.length; i++) {
			this.friends.add(friends[i]);
		}
		for (int i = 0; i != enemies.length; i++) {
			this.enemies.add(enemies[i]);
		}

//		System.out.println("g5 FRIENDS: " + this.friends);
//		System.out.println("g5 ENEMIES: " + this.enemies);

		// initialize the game history
		history = new GameHistory();

		manager = new PriorityManager(nplayers, id, this.friends, this.enemies);

	}


	/**
	 * The meat of the Player class, decides whether or not to shoot another player, via a
	 * priority targeting system.
	 * @param prevRound the previous round information
	 * @param alive the players who are still alive
	 * @return the ID of a player who has been targeted, -1 if player decides not to shoot
	 */
	public int shoot(int[] prevRound, boolean[] alive) {

		this.prevRound = prevRound;
		this.alive = alive;

		// Don't shoot first round
		// perhaps a smarter choice can be made based on number of friends/enemies as to who our friends might target
		if (prevRound == null) {
			return -1;
		}

		// Add the previous round information to the game history
		history.add(prevRound);
		manager.setParams(history, this.alive);
		int target = manager.getBestTarget();

		// print all relevant round info
//		System.out.println("g5 ID: " + id);
//		System.out.println(history);
//		System.out.println("g5 ENEMIES ALIVE: " + numEnemiesAlive());
//		System.out.println("g5 FRIENDS ALIVE: " + numFriendsAlive());
//		System.out.println("g5 WEIGHTS: " + Arrays.toString(manager.getWeights()));
//		System.out.println("g5 TARGET: " + target);

		return target;
	}


	/**
	 * Computes the number of enemies still alive.
	 * @return the number of enemies alive
	 */
	private int numEnemiesAlive() {
		int amount = 0;
		for (int i = 0; i < enemies.size(); i++) {
			if (alive[enemies.get(i)]) {
				amount++;
			}
		}
		return amount;
	}


	/**
	 * Computes the number of friends still alive.
	 * @return the number of friends alive
	 */
	private int numFriendsAlive() {
		int amount = 0;
		for (int i = 0; i < friends.size(); i++) {
			if (alive[friends.get(i)]) {
				amount++;
			}
		}
		return amount;
	}

}