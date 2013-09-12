package gunslinger.g5;

import java.util.*;

// An example player
// Extends gunslinger.sim.Player to start with your player
//
public class Player extends gunslinger.sim.Player {
	// total versions of the same player
	private static int versions = 0;
	// my version no
	private int version = versions++;

	// A simple fixed shoot rate strategy used by the dumb player
	private static double ShootRate = 0.8;

	// name of the team
	//
	public String name() {
		return "dumb" + (versions > 1 ? " v" + version : "");
	}

	// Initialize the player
	//
	public void init(int nplayers, int[] friends, int enemies[]) {
		// Note:
		// Seed your random generator carefully
		// if you want to repeat the same random number sequence
		// pick your favourate seed number as the seed
		// Or you can simply use the clock time as your seed
		//       
		gen = new Random(System.currentTimeMillis());
		// long seed = 12345;
		// gen = new Random(seed);

		this.nplayers = nplayers;
		// creating the weight array
		this.weight = new int[nplayers];

		// Length of the neutrals
		numNeutrals = nplayers - friends.length - enemies.length - 1;

		for (int i = 0; i != friends.length; i++)
			this.friends.add(friends[i]);

		for (int i = 0; i != enemies.length; i++)
			this.enemies.add(enemies[i]);

	}

	// Pick a target to shoot
	// Parameters:
	// prevRound - an array of previous shoots, prevRound[i] is the player that
	// player i shot
	// -1 if player i did not shoot
	// alive - an array of player's status, true if the player is still alive in
	// this round
	// Return:
	// int - the player id to shoot, return -1 if do not shoot anyone
	//
	public int shoot(int[] prevRound, boolean[] alive) {
		/*
		 * Strategy used by the dumb player: Decide whether to shoot or not with
		 * a fixed shoot rate If decided to shoot, randomly pick one alive that
		 * is not your friend
		 */

		// Shoot or not in this round?

		//
		// if (!shoot)
		// return -1;
		System.out.println("Friends: " + friends);
		System.out.println("Enemies: " + enemies);
		ArrayList<Integer> targets = new ArrayList<Integer>();
		if (prevRound == null) {// First Round
			if ((numNeutrals + enemies.size()) > friends.size()) {
				for (int i = 0; i != nplayers; ++i) {
					if (i != id && alive[i] && !friends.contains(i))
						targets.add(i);
				}
			} else if (numNeutrals > friends.size()
					&& numNeutrals > enemies.size()) {
				for (int i = 0; i != nplayers; ++i)
					if (i != id && alive[i] && !friends.contains(i)
							&& !enemies.contains(i))
						targets.add(i);
			} else if (friends.size() > (numNeutrals + enemies.size())) {
				return -1;
			}
			int target = targets.get(gen.nextInt(targets.size()));
			return target;
		} else {// Further Rounds
			Arrays.fill(weight, 0);
			for (int i = 0; i < nplayers; i++) {
				if (!friends.contains(i))
					weight[i]++;
			}
			int killer = -1;
			for (int i = 0; i < enemies.size(); i++) {
				killer = findShooter(enemies.get(i), prevRound);
				// System.out.println(killer == id);
				if (killer > -1 && friends.contains(killer)
						&& alive[enemies.get(i)])
					weight[enemies.get(i)] += 2;
			}

			for (int i = 0; i < friends.size(); i++) {
				killer = findShooter(friends.get(i), prevRound);
				if (killer > -1 && alive[killer])
					weight[killer] += 3;
			}

			killer = findShooter(id, prevRound);
			if (killer > -1 && alive[killer]) {
				weight[killer] += 4;
			}
			int target = maxIndex(weight);
			boolean shoot = gen.nextDouble() < ShootRate;
			if (shoot && alive[target] && target != id) // REFINE THIS
				return target;
			else
				return -1;
		}

	}

	private int findShooter(int n, int[] arr) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == n)
				return i;
		}
		return -1;
	}

	private int maxIndex(int[] arr) {
		int max = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > max)
				max = arr[i];
		}
		return findShooter(max, arr);
	}

	private Random gen;
	private int nplayers;
	private ArrayList<Integer> friends = new ArrayList<Integer>();
	private ArrayList<Integer> enemies = new ArrayList<Integer>();
	private int[] weight;
	private int numNeutrals;
}
