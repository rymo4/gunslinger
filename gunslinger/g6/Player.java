package gunslinger.g6;

import java.util.*;

// An example player
// Extends gunslinger.sim.Player to start with your player
public class Player extends gunslinger.sim.Player
{
	// keep track of rounds
	private int round = 0;

	// Conststency Matrix
	private double[][] consistency;

	// Current Neutral-turned enemies
	ArrayList<Integer> current_alive_turned_enemies = new ArrayList<Integer>();

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
		return "g6" + (versions > 1 ? " v" + version : "");
	}

	// Initialize the player
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

		for (int i = 0; i != friends.length; i++)
			this.friends.add(friends[i]);

		for (int i = 0; i != enemies.length; i++)
			this.enemies.add(enemies[i]);

		history = new int[1000][nplayers];
		for (int i=0 ; i<nplayers ; i++)
			history[0][i] = -1;

		consistency = new double[nplayers][nplayers];
		for (int i=0 ; i<nplayers ; i++)
		{
			for (int j=0 ; j<nplayers ; j++)
			{
				consistency[i][j] = 0.0;
			}
		}
	}

/*	public boolean ratio(int alive_fr, int alive_en)
	{
		double ratio = (1.0*alive_en - 1.0*alive_fr);
		if (ratio > 0.0)
			return true;
		else
			return false;
	}
*/
	public boolean is_friend(int friend_id)
	{
		return friends.contains(friend_id);
	}	

	public boolean is_enemy(int enemy_id)
	{
		return enemies.contains(enemy_id);
	}

	public boolean is_neutral(int neutral_id)
	{
		return (!friends.contains(neutral_id) && !enemies.contains(neutral_id));
	}	

	public boolean is_alive(int player_id, boolean[] alive)
	{
		return alive[player_id];
	}

	// Update consistency matrix after every round
	public void updateConsistency(int[] prevRound, int[][] history, int currentRound)
	{
		for (int i=0 ; i<nplayers ; i++)
		{
			ArrayList<Integer> trackRoundNumber = new ArrayList<Integer>();
			trackRoundNumber.add(0);
			int mostRecentRound = -1;
			for (int k=0 ; k<nplayers ; k++)
			{
				for (int j=1 ; j<=currentRound ; j++)
				{
					if (history[j][i] == k)
					{
						trackRoundNumber.add(j);
						mostRecentRound = j;
					}
				}

				// Average of differences in shooting
				double sum = 0.0;
				for (int j=1 ; j<trackRoundNumber.size() ; j++)
				{
					sum += trackRoundNumber.get(j) - trackRoundNumber.get(j-1);
				}
				sum /= (trackRoundNumber.size() - 1);

				double tempConsistency = ((((double)currentRound - sum)/(double)currentRound) * ((double)mostRecentRound/(double)currentRound));
				consistency[i][k] = tempConsistency;
			}
		}
	}

	// Print consistencies
	public void printConsistency()
	{
		for (int i=0 ; i<nplayers ; i++)
		{
			for (int j=0 ; j<nplayers ; j++)
			{
				System.out.print(consistency[i][j] + " ");
			}
			System.out.println();
		}
	}

	// Calculate average consistency 
	public double averageConsistency(int playerID)
	{
		double average = 0.0;
		for (int i=0 ; i<nplayers ; i++)
		{
			average += consistency[playerID][i];
		}
		average /= nplayers;
		return average;
	}

	// Pick a target to shoot
	// Parameters:
	//  prevRound - an array of previous shoots, prevRound[i] is the player that player i shot
	//              -1 if player i did not shoot
	//  alive - an array of player's status, true if the player is still alive in this round
	//  Return:
	//  int - the player id to shoot, return -1 if do not shoot anyone
	//
	public int shoot(int[] prevRound, boolean[] alive)
	{	
		int target;

		round++;	
		System.out.println("Players: " + nplayers);

		int alive_friends = 0;
		int alive_players = 0;
		int alive_enemies = 0;
		ArrayList<Integer> current_alive_friends = new ArrayList<Integer>();
		ArrayList<Integer> current_alive_enemies = new ArrayList<Integer>();

		try{
			// Remove DEAD enemies from my NEUTRAL-TURNED-ENEMY list
			for (int i = 0; i<nplayers; i++)
			{			
				if (!alive[i] && (current_alive_turned_enemies.contains(i)))
				{
					current_alive_turned_enemies.remove(new Integer(i));
				}
			}

			// Populating currently alive players, alive friends & alive enemies
			for (int i=0 ; i<nplayers ; i++)
			{
				if (alive[i])
				{
					alive_players++;
					if (friends.contains(i))
					{
						current_alive_friends.add(i);
						alive_friends++;					
					}

					else if (enemies.contains(i))
					{
						current_alive_enemies.add(i);
						alive_enemies++;
					}
				}
			}

			// Merge the two enemy lists
			current_alive_enemies.addAll(current_alive_turned_enemies);

			// Calculating initial probability to die
			double initial_prob = 1.0 - ((1.0*alive_friends)/(alive_players - 1));

			if (round == 1)
			{
				return -1;
				/*(			boolean shoot = true;

				  if (initial_prob > 0.5)
				  {
				  shoot = true;
				  target = enemies.get(0);
				  return target;
				  }
				  else
				  {
				  shoot = false;
				  return -1;
				  }
				 */		}

			else
			{	
				// Add previous round to History
				for (int i = 0; i < nplayers ; i++)
				{
					history[round-1][i] = prevRound[i];
				}

				updateConsistency(prevRound, history, round);

				// I AM SHOT
				// Make my shooter my prime target
				for (int i=0 ; i < nplayers ; i++)
				{
					// If someone shoots me
					if (i!= id && prevRound[i] == id)
					{
						// If I am in the middle of a directed flow A->"B"->C
						// Trying to be consistent and hoping friends (if any) will help
						if (alive[i] && prevRound[id]!=-1 && alive[prevRound[id]] && prevRound[id]!=i && alive_friends > 1)
						{
							target = prevRound[id];
							return target;
						}
					}
				}

				for (int i=0 ; i < nplayers ; i++)
				{
					// If someone shoots me
					if (i!=id && prevRound[i] == id)
					{
						// If shooter is an enemy and he is alive
						if (alive[i] && enemies.contains(i))
						{
							target = i;
							return target;
						}
					}
				}

				for (int i=0 ; i < nplayers ; i++)
				{
					// If someone shoots me
					if (i!=id && prevRound[i] == id)
					{	
						// If shooter is a neutral person and he is alive
						if (alive[i] && !friends.contains(i) && !enemies.contains(i))
						{
							target = i;
							// If not a turned enemy
							if(!current_alive_turned_enemies.contains(i))
							{
								// Add current hitter to my enemy list
								current_alive_turned_enemies.add(i);
							}
							return target;
						}
					}
				}

				for (int i=0 ; i < nplayers ; i++)
				{
					// If someone shoots me
					if (i!=id && prevRound[i] == id)
					{	
						// If shooter is a friend and he is alive
						if (alive[i] && friends.contains(i))
						{
							// Check consistency of the friend hitting me
							// If consistency very high, shoot the friend
							if (consistency[i][id] > FRIEND_HITTING_ME)
							{
								target = i;
								return target;
							}
						}
					}
				}

				// I WAS NOT SHOT
				// Can decide what to do next
				// If 3 Players or less are left
				if (alive_players <= 3)
				{
					for (int i=0 ; i<nplayers ; i++)
					{
						for (int j=0 ; j<nplayers ; j++)
						{
							if (i!=id && j!=id && alive[i] && alive[j] && prevRound[i] == j && initial_prob < INITIAL_PROB)
							{
								if (!friends.contains(i))
								{
									target = i;
									return target;
								}
								else if (!friends.contains(j))
								{
									target = j;
									return target;
								}

							}

							if (i!=id && j!=id && alive[i] && alive[j] && prevRound[j] == i && initial_prob < INITIAL_PROB)
							{
								if (!friends.contains(j))
								{
									target = j;
									return target;
								}
								else if (!friends.contains(i))
								{
									target = i;
									return target;
								}	
							}
						}
					}
				}

				// If more than 3 are left
				for (int i=0 ; i < nplayers ; i++)
				{
					for (int p = 0; p < nplayers ; p++)
					{
						// If enemy/neutral shoots a friend
						// HELPING THE FRIEND
						if (i!=id && p!=id && alive[i] && alive[p] && !friends.contains(i) && friends.contains(p) && prevRound[i] == p)
						{
							target = i;
							return target;
						}
					}
				}

				for (int i=0 ; i < nplayers ; i++)
				{
					for (int p = 0; p < nplayers ; p++)
					{
						// If someone shoots enemy
						// HELPING SOMEONE ELIMINATE OUR ENEMY
						if (i!=id && p!=id && alive[i] && alive[p] && enemies.contains(p) && prevRound[i] == p && initial_prob < INITIAL_PROB)
						{
							// Friend shoots enemy
							if (friends.contains(i))
							{
								for (int e = 0 ; e < nplayers ; e++)
								{
									if (prevRound[e] != i )//&& averageConsistency(i) > FRIEND_HITTING_ENEMY)
									{
										target = p;
										return target;
									}
								}
							}
						}
					}
				}

				for (int i=0 ; i < nplayers ; i++)
				{
					for (int p = 0; p < nplayers ; p++)
					{
						// Neutral shoots enemy
						if (i!=id && p!=id && alive[i] && !friends.contains(i) && !enemies.contains(i) && enemies.contains(p))
						{
							for (int e = 0 ; e < nplayers ; e++)
							{
								// Help the neutral in case the neural has not been shot
								if (e!=id && alive[p] && prevRound[e] != i && averageConsistency(i) > NEUTRAL_HITTING_ENEMY && initial_prob < INITIAL_PROB)
								{
									target = p;
									return target;
								}
							}
						}
					}
				}


				for (int i=0 ; i < nplayers ; i++)
				{
					for (int p = 0; p < nplayers ; p++)
					{	
						// If enemy shoots an enemy
						if (i!=id && p!=id && alive[i] && alive[p] && enemies.contains(i) && enemies.contains(p) && prevRound[i] == p && initial_prob < INITIAL_PROB)
						{
							target = i;
							return target;
						}
					}	
				}

				// Pick first target and shoot
//				if (current_alive_enemies.size()>0 && alive_friends>0 && initial_prob < INITIAL_PROB)
//				{
//					target = current_alive_enemies.get(0);
//					return target;
//				}
			}
			
			return -1;
		}
		catch(Exception e)
		{
			return -1;
		}		
	}

	private Random gen;
	private int nplayers;
	private ArrayList<Integer> friends = new ArrayList<Integer>();
	private ArrayList<Integer> enemies = new ArrayList<Integer>();

	// track the history of all the rounds
	private int[][] history;

	private double INITIAL_PROB = 0.5;
	private double FRIEND_HITTING_ME = 0.6;
	private double FRIEND_HITTING_ENEMY = 0.1;
	private double NEUTRAL_HITTING_ENEMY = 0.3;
	private double FRIEND_HITTING_NONFRIEND = 0.1;
	private double ENEMY_HITTING_ME = 0.1;
}
