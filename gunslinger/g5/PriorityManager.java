package gunslinger.g5;

import java.util.*;

public class PriorityManager {

	private static final int ENEMY_DEFAULT = 5;
	private static final int ENEMY_SHOOTS_ME = 5;
	private static final int NEUTRAL_SHOOTS_ME = 3;
	private static final int ENEMY_SHOOTS_ME_LOTS = 5;
	private static final int NEUTRAL_SHOOTS_ME_LOTS = 5;
	private static final int ENEMY_SHOOTS_NONFRIEND = 5;
	private static final int NONENEMY_SHOOTS_ENEMY = 3;
	private static final int ENEMY_SHOOTS_FRIEND = 3;
	private static final int NEUTRAL_SHOOTS_FRIEND = 3;
	private static final int ENEMY_SHOOTS_FRIEND_LOTS = 3;
	private static final int NEUTRAL_SHOOTS_FRIEND_LOTS = 1;
	
//	private static final int ENEMY_DEFAULT = 2;
//	private static final int ENEMY_SHOOTS_ME = 3;
//	private static final int NEUTRAL_SHOOTS_ME = 2;
//	private static final int ENEMY_SHOOTS_ME_LOTS = 3;
//	private static final int NEUTRAL_SHOOTS_ME_LOTS = 3;
//	private static final int ENEMY_SHOOTS_NONFRIEND = 3;
//	private static final int NONENEMY_SHOOTS_ENEMY = 2;
//	private static final int ENEMY_SHOOTS_FRIEND = 3;
//	private static final int NEUTRAL_SHOOTS_FRIEND = 2;
//	private static final int ENEMY_SHOOTS_FRIEND_LOTS = 3;
//	private static final int NEUTRAL_SHOOTS_FRIEND_LOTS = 2;
	
	private static final int TB_RANDOM = 0, TB_SMART = 1;


	private int nplayers, id;
	private GameHistory history;
	private ArrayList<Integer> friends, enemies;
	private boolean[] alive;
	
	private int[] weights;

	private Random gen;

	public void setParams(GameHistory h, boolean[] a) {
		history = h;
		alive = a;
	}

	public PriorityManager(int nplayers, int id, ArrayList<Integer> friends, ArrayList<Integer> enemies) {
		this.nplayers = nplayers;
		this.id = id;
		this.friends = friends;
		this.enemies = enemies;
		weights = new int[nplayers];
		gen = new Random(System.currentTimeMillis());
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
	 * Adds weight to players who are not friends, with additional weight to enemies.
	 */
	private void weightEnemies() {
		for (int i = 0; i < enemies.size(); i++) {
			if (alive[i]) weights[enemies.get(i)] += ENEMY_DEFAULT;
		}
	}


	/**
	 * Adds weight to those who have shot our player, with additional weight to enemies.
	 */
	private void weightShotMe() {
		// give weight to any enemies who ever shot us
		// give weight to neutrals who have shot us more than once in a row
		int[] shooters = history.everShot(id);
		if (shooters != null) {
			for (int i = 0; i < shooters.length; i++) {
				if (alive[shooters[i]]) {
					// give weight to an enemy who EVER shot us
					if (enemies.contains(shooters[i])) {
						weights[shooters[i]] += ENEMY_SHOOTS_ME;
					}

					// check for consecutive shots from other players
					int numConsShots = history.consecutiveShots(shooters[i], id);
					if (numConsShots == 1) {
						if (enemies.contains(shooters[i])) {
							weights[shooters[i]] += ENEMY_SHOOTS_ME;
						}
						else if (!friends.contains(shooters[i])) {
							weights[shooters[i]] += NEUTRAL_SHOOTS_ME;
						}
					}
					else if (numConsShots > 1) {
						if (enemies.contains(shooters[i])) {
							weights[shooters[i]] += ENEMY_SHOOTS_ME_LOTS * numConsShots;
						}
						else if (!friends.contains(shooters[i])) {
							weights[shooters[i]] += NEUTRAL_SHOOTS_ME_LOTS * numConsShots;
						}
					}
				}
			}
		}
	}


	/**
	 * Adds weight to those who have been shot by other players, with additional weight to enemies.
	 */
	private void weightShotByOthers() {
		for (int victim = 0; victim < nplayers; victim++) {
			if (alive[victim]) {
				int[] shooters = history.everShot(victim);
				if (shooters != null) {
					for (int i = 0; i < shooters.length; i++) {
						if (alive[shooters[i]]) {
							// enemy shooting enemy or neutral; target shooter
							if (enemies.contains(shooters[i]) && !friends.contains(victim)) {
								weights[shooters[i]] += ENEMY_SHOOTS_NONFRIEND;
							}
							// friend or neutral shooting enemy; target victim
							else if (!enemies.contains(shooters[i]) && enemies.contains(victim)) {
								weights[victim] += NONENEMY_SHOOTS_ENEMY;
							}

						}
					}
				}
			}
		}
	}


	/**
	 * Adds weight to players who have attacked our friends, with additional weight to enemies.
	 */
	private void weightShotFriends() {
		for (int i = 0; i < friends.size(); i++) {
			if (alive[friends.get(i)]) {
				int[] shooters = history.everShot(friends.get(i));
				if (shooters != null) {
					for (int j = 0; j < shooters.length; j++) {
						if (alive[shooters[j]]) {
							if (enemies.contains(shooters[j])) {
								weights[shooters[j]] += ENEMY_SHOOTS_FRIEND;
							}

							// check for consecutive shots from other players
							int numConsShots = history.consecutiveShots(shooters[j], friends.get(i));
							if (numConsShots == 1) {
								if (enemies.contains(shooters[j])) {
									weights[shooters[j]] += ENEMY_SHOOTS_FRIEND;
								}
								else if (!friends.contains(shooters[j])) {
									weights[shooters[j]] += NEUTRAL_SHOOTS_FRIEND;
								}
							}
							else if (numConsShots > 1) {
								if (enemies.contains(shooters[j])) {
									weights[shooters[j]] += ENEMY_SHOOTS_FRIEND_LOTS * numConsShots;
								}
								else if (!friends.contains(shooters[j])) {
									weights[shooters[j]] += NEUTRAL_SHOOTS_FRIEND_LOTS * numConsShots;
								}
							}
						}
					}
				}
			}
		}
	}


	/**
	 * Given an int array, returns an array of all indices which contain the max value.
	 * Used in the game for determining which players have the highest weights after evaluation.
	 * @param arr the array to be searched
	 * @return int array containing indices from arr which contain arr's max value
	 */
	private int[] indexOfMaxValue(int[] arr) {
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

	private int breakTie(int[] candidates, int method) {
		if (method == TB_SMART) {
			int[] secondaryWeights = new int[nplayers];

			// give weight to players who have been shot lots, they are likely to be vulnerable
			for (int i = 0; i < candidates.length; i++) {
				secondaryWeights[candidates[i]] += history.shotsFiredOnPlayer(candidates[i]);
				secondaryWeights[candidates[i]] -= history.getRetaliationFactor(candidates[i]);
				secondaryWeights[candidates[i]] -= history.getReinforcementFactor(candidates[i], alive);
			}
			System.out.println("g5 TIEBREAKER - weights: " + Arrays.toString(secondaryWeights));
			return indexOfMaxValue(secondaryWeights)[0];
		}
		else if (method == TB_RANDOM) {
			return candidates[gen.nextInt(candidates.length)];
		}
		else {
			return candidates[0];
		}
	}

	public int[] getWeights() {
		return weights;
	}

	
	/**
	 * Gets the best target based on players left alive and current game history
	 * @return Index of best target.
	 */
	public int getBestTarget() {

		// If there are only friends remaining, do not shoot
		System.out.println("g5 ONLY FRIENDS LEFT: " + onlyFriendsRemaining());
		if (onlyFriendsRemaining()) {
			return -1;
		}

		// begin computing weights
		Arrays.fill(weights, 0);
		weightEnemies();
		weightShotMe();
		weightShotByOthers();
		weightShotFriends();

		// select a random target from those with highest weights
		int[] candidates = indexOfMaxValue(weights);
		int target = -1;

		if (candidates.length > 1) {
			target = breakTie(candidates, TB_RANDOM);
		}
		else {
			target = candidates[0];
		}

		// this constraint works well when there is only one enemy
		if (weights[target] == 0 || weights[target] == ENEMY_DEFAULT) {
			return -1;
		}

		return target;
	}

}