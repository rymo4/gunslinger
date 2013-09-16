package gunslinger.g5;

import java.util.*;


/**
 * Group 5's proprietary Player implementation.
 * @author Andrew Goldin
 * @author Priyanka Singh
 * @author Neha Aggarwal
 * 
 * TODO: Perhaps figure out a way to hang back and not shoot as much.
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
	private ArrayList<int[]> history;

	
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

		System.out.println("g5 FRIENDS: " + this.friends);
		System.out.println("g5 ENEMIES: " + this.enemies);

		// initialize the game history
		history = new ArrayList<int[]>();

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

		// If it is the first round, just target a random enemy, assuming one exists
		// perhaps a smarter choice can be made based on number of friends/enemies as to who our friends might target
		if (prevRound == null) {
			if (enemies.size() > 0) {
				return enemies.get(gen.nextInt(enemies.size()));
			}
			return -1;
		}

		// Add the previous round information to the game history
		history.add(prevRound);

		// If there are only friends remaining, do not shoot
		System.out.println("g5 ONLY FRIENDS LEFT: " + onlyFriendsRemaining());
		if (onlyFriendsRemaining()) {
			return -1;
		}

		// begin computing weights
		Arrays.fill(weights, 0);
		weightNonFriends();
		weightShotPlayer();
		weightShotByOthers();
		weightShotFriends();

		// select a random target from those with highest weights
		int[] targets = maxIndex(weights);
		int target = targets[gen.nextInt(targets.length)];

		// print all relevant round info
		System.out.println("g5 ID: " + id);
		printHistory();
		System.out.println("g5 ENEMIES ALIVE: " + numEnemiesAlive());
		System.out.println("g5 FRIENDS ALIVE: " + numFriendsAlive());
		System.out.println("g5 WEIGHTS: " + Arrays.toString(weights));
		System.out.println("g5 TARGET: " + target);

		// ideally, target should not be alive or by ourselves
		if (alive[target] && target != id) {
			return target;
		}
		// still need to decide if there are cases other than above in which it would be wise to not shoot
		else {
			return -1;
		}

	}

	
	/**
	 * Determines if the only players left alive are our friends.
	 * @return true if only friends remain, false otherwise
	 */
	private boolean onlyFriendsRemaining() {
		for (int i = 0; i < nplayers; i++) {
			if (alive[i] && !friends.contains(i) && i != id) {
				return false;
			}
		}
		return true;
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

	
	/**
	 * Prints the entire round history of the game in a readable format.
	 */
	private void printHistory() {
		System.out.println("g5 HISTORY:");
		for (int i = 0; i < history.size(); i++) {
			System.out.println(Arrays.toString(history.get(i)));
		}
	}

	
	/**
	 * Adds weight to players who are not friends, with additional weight to enemies.
	 */
	private void weightNonFriends() {
		// weight for non-friends
		for (int i = 0; i < nplayers; i++) {
			if (alive[i]) {
				if (!friends.contains(i) && i != id) {
					weights[i] += 5;
					// extra weight for enemies
					if (enemies.contains(i)) {
						weights[i] += 5;
					}
				}
			}
		}
	}

	
	/**
	 * Adds weight to those who have shot our player, with additional weight to enemies.
	 */
	private void weightShotPlayer() {
		// one weight for enemies who have ever shot us
		// half weight for neutrals who have ever shot us
		for (int i = 0; i < nplayers; i++) {
			boolean[] weightAdded = new boolean[nplayers];
			if (alive[i]) {
				for (int j = 0; j < history.size(); j++) {
					if (history.get(j)[i] == id && !weightAdded[i]) {
						if (enemies.contains(i)) {
							weights[i] += 10;
						}
						else {
							if (!friends.contains(i)) {
								weights[i] += 5;
							}
						}
						//weightAdded[i] = true;
					}
				}
			}
		}
	}


	/**
	 * Adds weight to those who have been shot by other players, with additional weight to enemies.
	 */
	private void weightShotByOthers() {
		// loop through each player
		for (int i = 0; i < nplayers; i++) {
			boolean[] weightAdded = new boolean[nplayers];
			// if the player is alive...
			if (alive[i]) {
				// ...check that player's history
				for (int j = 0; j < history.size(); j++) {
					// determine if anyone has ever tried to shoot the player
					int shooter = findShooter(i, history.get(j));
					if (shooter > -1 && alive[i] && !weightAdded[i]) {
						// if the player is an enemy, add weight to said enemy
						if (enemies.contains(i)) {
							weights[i] += 5;
							// if the shooter was a friend, add even more weight to the enemy
							if (friends.contains(shooter)) {
								weights[i] += 5;
							}
						}
						// if the player was not an enemy, only add weight if the player has tried to kill us
						else {
							// at this point only neutrals who have tried to kill us can have weights greater than 5
							if (weights[i] > 5) {
								// if there are enemies alive, give neutral less weight
								if (numEnemiesAlive() > 0) {
									weights[i] += 2;
								}
								// if there are no enemies left, give neutral more weight
								else {
									weights[i] += 5;
								}
							}
						}
						// make sure weight isn't compounded
						weightAdded[i] = true;
					}
				}
			}
		}
	}


	/**
	 * Adds weight to players who have attacked our friends, with additional weight to enemies.
	 */
	private void weightShotFriends() {
		// check through each friend's history
		for (int i = 0; i < friends.size(); i++) {
			for (int j = 0; j < history.size(); j++) {
				// determine if anyone ever tried to shoot the friend
				int shooter = findShooter(friends.get(i), history.get(j));
				// if both friend and shooter are still alive, assign weight
				if (shooter > -1 && alive[shooter] && alive[friends.get(i)]) {
					// if the shooter is an enemy, add more weight
					if (enemies.contains(shooter)) {
						weights[shooter] += 8;
					}
					// else if the shooter is a neutral, add less weight
					else if (!friends.contains(shooter)) {
						weights[shooter] += 4;
					}
				}
			}
		}
	}

	
	/**
	 * Determines if a certain player has been shot in a given round.
	 * @param playerId the ID of the victim player
	 * @param roundInfo the shooting info for a round
	 * @return the ID of the shooter. If there is more than one shooter, chooses randomly. If no shooters, returns -1.
	 */
	private int findShooter(int playerId, int[] roundInfo) {
		ArrayList<Integer> candidates = new ArrayList<Integer>();
		for (int i = 0; i < roundInfo.length; i++) {
			if (roundInfo[i] == playerId) {
				candidates.add(i);
			}
		}
		if (!candidates.isEmpty()) {
			// THIS IS RANDOM, CAN BE IMPROVED
			return candidates.get(gen.nextInt(candidates.size())).intValue();
		}
		return -1;
	}

	
	/**
	 * Given an int array, returns an array of all indices which contain the max value.
	 * Used in the game for determining which players have the highest weights after evaluation.
	 * @param arr the array to be searched
	 * @return int array containing indices from arr which contain arr's max value
	 */
	private int[] maxIndex(int[] arr) {
		int max = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}
		ArrayList<Integer> temp = new ArrayList<Integer>();
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == max) {
				temp.add(i);
			}
		}
		int[] top = new int[temp.size()];
		for (int i = 0; i < top.length; i++) {
			top[i] = temp.get(i).intValue();
		}
		return top;
	}

}