package gunslinger.g7;

import java.util.*;

// An example player
// Extends gunslinger.sim.Player to start with your player
//
public class Player extends gunslinger.sim.Player
{
	
	private int nplayers;
    private int[] friends;
    private int[] enemies;
	private int[] priority;
	private int[] timesshot;
	// total versions of the same player
    private static int versions = 0;
    // my version no
    private int version = versions++;
	
	private boolean roundone,once=false;
    
    // A simple fixed shoot rate strategy used by the dumb player
    //private static double ShootRate = 0.8;
	
    // name of the team
    //
    public String name()
    {
        return "Player g7" + (versions > 1 ? " v" + version : "");
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
		//  gen = new Random(System.currentTimeMillis());
        // long seed = 12345;
        // gen = new Random(seed);
		
        this.nplayers = nplayers;
        this.friends = friends.clone();
        this.enemies = enemies.clone();
		roundone=true;
		once=true;
		
				
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
		/* Strategy used by the dumb player:
		 Decide whether to shoot or not with a fixed shoot rate
		 If decided to shoot, randomly pick one alive that is not your friend */
		
        // Shoot or not in this round?
		if(once)
		{
			priority=new int[nplayers];
			timesshot=new int[nplayers];
			roundone=true;
			for(int i=0;i<nplayers;i++)
			{
				for(int j=0;j<friends.length;j++)
				   if(friends[j]==i)
					  priority[i]=0;
				for(int j=0;j<enemies.length;j++)
					if(enemies[j]==i)
						priority[i]=2;
				else { 
					priority[i]=1;
					
				}
			}
			
			priority[id]=0;
			
			for(int i=0;i<nplayers;i++)
				timesshot[i]=0;
			
			once=false;
			
		}
		
		int target=-1;
		
				
		
		//Update priority[] and timesshot[]..who is dead and alive?
		
		//System.out.println(priority.length);
		for(int i=0;i<nplayers;i++)
			if(!alive[i])
			{
				
				priority[i]=-1;
				timesshot[i]=-1;
			}
		
		if(roundone)
		{
			Random r=new Random();
			int R = r.nextInt(3-1) + 1;
			if(R==1)
				target=-1;
			
		    else 
			{
				//Pick an enemy at random to shoot
				boolean stop=false;
				do
				{
					int enemy_to_kill_index=new Random().nextInt(enemies.length);
					if(enemy_to_kill_index != id && alive[enemy_to_kill_index])
					{
						target=enemy_to_kill_index;
						stop=true;
					}
					
				}
				while(stop!=true);
			}
			
			roundone=false;
		}
		
		else{
			/*boolean shoot = gen.nextDouble() < ShootRate;
			 
			 if (!shoot)
			 return -1;
			 
			 ArrayList<Integer> targets = new ArrayList<Integer>();
			 for (int i = 0; i != nplayers; ++i)
			 if (i != id && alive[i] && !Arrays.asList(friends).contains(i))
			 targets.add(i);
			 
			 //int target = targets.get(gen.nextInt(targets.size()));*/
			
			for(int i=0;i<nplayers;i++)
				if(prevRound[i]!=-1 && timesshot[prevRound[i]]!=-1)
					timesshot[prevRound[i]]++;
			
			
			
			for(int i=0;i<nplayers;i++)
			{
				if(prevRound[i]==id)
					priority[i]+=2;
				if(Arrays.asList(friends).contains(prevRound[i]))
				    priority[i]+=1;
				if(Arrays.asList(enemies).contains(prevRound[i]))
				    priority[i]-=1;
			}
			
			int get_prev_shot=prevRound[id];
			if(get_prev_shot!=-1 && alive[get_prev_shot])
				priority[get_prev_shot]+=1;
			
			
			
			
			int max=0;
			
			for(int i=0;i<nplayers;i++)
			{
				//max=0;
				if(priority[i]>max && i!=id)
					max=i;
				
			}
			
			target=max;
			
			
			int count=0;
			for(int i=0;i<nplayers;i++)
				if(priority[i]==priority[max] && alive[i] && i!=id)
					count++;
			
			if(count==1)
				target=max;
			else if(count > 1){
				
				int same_priority[]=new int[count];
				
				//Choosing the players with the same max priority
				for(int i=0,j=0;i<nplayers;i++)
					if(priority[i]==priority[max] && alive[i] && i!=id)
					{
						same_priority[j]=i;
						j++;
					}
				
				
					int t = 0 + (int)(Math.random() * ((count - 0)));
					//System.out.println("cnt: " + count + " t: " + t);
				    target = same_priority[t];	
			}
			
			/* //To resolve ambiguity of same priority. Calculate based on number of times person was shot.
			int count=0;
			for(int i=0;i<nplayers;i++)
				if(priority[i]==priority[max])
				   count++;
				 				
			if(count==1)
			   target=max;
			else if(count > 1){
				
				int same_priority[]=new int[count];
				for(int i=0,j=0;i<nplayers;i++)
					if(priority[i]==priority[max])
					{
						same_priority[j]=i;
						j++;
					}
				
				int max1=-1;
				
				for(int i=0;i<count;i++)
				{
					//max=0;
					if(timesshot[i]>max1 && i!=id && alive[i]) {
						max1=i;
						//System.out.println("Hi");
					}
					
				}
				
				if(max1 == -1)
					target = max;
				else
					target=max1;
						
			}
			 */
			
		}
		return target;
	}
	
	    //private Random gen;
    
}
