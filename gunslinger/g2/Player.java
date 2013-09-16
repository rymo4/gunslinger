package gunslinger.g2;

import java.util.*;


public class Player extends gunslinger.sim.Player
{
    // total versions of the same player
    private static int versions = 0;
    // my version no
    private int version = versions++;
    
    //Local copies of necessary variables
    private int nplayers;
    private int[] friends;
    private int[] enemies;
    int next_round[][];
    //Book-keeping variables
    PriorityQueue<PriorityTuple> priorityList;
    //bingyi-build the dynamic 2D table to record history
    List<List<Integer>> record = new ArrayList<List<Integer>>();
    //bingyi 
    //yash
    int prediction[][];
    //May not use these variables later
    //boolean shootable[];
    
    // name of the team
    public String name()
    {
        return "g2" + (versions > 1 ? " v" + version : "");
    }
 
    // Initialize the player
    //
    public void init(int nplayers, int[] friends, int enemies[])
    {
    	 this.nplayers = nplayers;
         this.friends = friends.clone();
         this.enemies = enemies.clone();
         
         for(int i=0;i<nplayers;i++)
         {
         	record.add(new ArrayList<Integer>());
         }
         
         priorityList=new PriorityQueue<PriorityTuple>(10, new PriorityTupleComparator());
         //yash
         prediction=new int[this.nplayers][this.nplayers];
         next_round=new int[this.nplayers][this.nplayers];
         
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
    	
        if(prevRound==null)
        {
        	return -1; //Make love, not war
        }
        
        System.out.println(Arrays.toString(friends));
        //printPrevRound(prevRound);
        int[] shotAt=new int[prevRound.length];
        
        for(int player=0;player<prevRound.length;player++)
        {
        	if(prevRound[player]!=-1)
        		shotAt[prevRound[player]]++;
        	gaugeSeverity(player,prevRound[player],alive);
        	
        	//bingyi
        	record.get(player).add(prevRound[player]);
		
        }
        
        //yash
        //call prediction function here
        next_round=new int[this.nplayers][this.nplayers];
        predictNextRound(next_round,record, alive);
        prediction=next_round.clone();
        
        //Printing the prediction array returned
        for(int i=0;i<prediction.length;i++)
        {
        	for(int j=0;j<prediction.length;j++)
        	{
        		System.out.print(prediction[i][j]);
        	}
        	System.out.println();
        }
        
        int popular_target[]=new int[prevRound.length];
        int players[]=new int[prevRound.length];
        
        for(int i=0;i<players.length;i++)
        {
        	players[i]=i;
        }
        
        //add the columns of prediction matrix to get player most likely to be shot at
        for(int i=0;i<prediction.length;i++)
        {
        	for(int j=0;j<prediction.length;j++)
        	{
        		popular_target[i]+=prediction[j][i];
        	}
        }
        
        //find out who tried to shoot you in the previous round and add more likeliness to them
        for(int i=0;i<prevRound.length;i++)
        {
        	//add more weight if an enemy shot you in the previous round
        	if(prevRound[i]==this.id && isEnemy(i) && alive[i])
        		popular_target[i]+=7;
        	//add less weight if a neutral tried to shoot you
        	else if(prevRound[i]==this.id && isNeutral(i) && alive[i])
        		popular_target[i]+=4; 
        	//add more weight to an enemy who shot your friend
        	else if(isFriend(prevRound[i]) && isEnemy(i) && alive[i] && alive[prevRound[i]])
        		popular_target[i]+=3;
        	//neutral who shot a friend
        	else if(isFriend(prevRound[i]) && isNeutral(i) && alive[i] && alive[prevRound[i]])
        		popular_target[i]+=2; 
        	//add weight to an enemy previously shot by a friend
        	else if(isEnemy(prevRound[i]) && isFriend(i) && alive[i] && alive[prevRound[i]])
        		popular_target[i]+=5;
        	//above conditions assume that friends don't shoot you
        }
        
        //sort player and popular_target arrays to get sorted list of targets
        for(int i=0;i<popular_target.length;i++)
        {
        	for(int j=0;j<popular_target.length-1;j++)
        	{
        		if(popular_target[j]<popular_target[j+1])
        		{
        			int temp=popular_target[j];
        			popular_target[j]=popular_target[j+1];
        			popular_target[j+1]=temp;
        			temp=players[j];
        			players[j]=players[j+1];
        			players[j+1]=temp;
        		}
        	}
        }
        //We only need the most likely target, but I'm sorting the list just to get an idea of what our targets are like
        
        //printing players and their likeliness to be shot
        for(int j=0;j<prediction.length;j++)
    	{
    		System.out.print("("+players[j]+","+popular_target[j]+") ");
    	}
    	System.out.println();
        
        
        //shoot the most likely player if he is not a friend
        for(int i=0;i<popular_target.length;i++)
        {
        	if((isEnemy(players[i]) || isNeutral(players[i])) && players[i]!=this.id && alive[players[i]])
        	{
        		return players[i];
        	}
        }
        
        //if no enemies or neutrals are to be shot at
        return -1;
        //yash
        
        
        
//        System.out.println("this is our hate most list\n");
//        printListofArray(hate_most); 
//        if(priorityList.size()==0)
//        	return -1;
//        
//        attentionSeekingPrint(priorityList.toString());
        
//        int myTarget=getMyTarget(shotAt);
//        priorityList.clear();
//    	//bingyi
//        printMatrix(record);
//        //bingyi		
//        return myTarget;
    }
    
    //bingyi
	public void predictNextRound(int[][] next_round, List<List<Integer>> m, boolean[] alive)	
	{
		int columns = m.get(0).size();
		int rows = m.size();
//		int[][] next_round = new int[rows][rows];
		//if someone shoot the other last round, most likely the other will shot back next_round
		for (int i=0; i<rows;i++){
			if (m.get(i).get(columns-1) != -1) {
				if (alive[i] && alive[m.get(i).get(columns-1)]){
					next_round[m.get(i).get(columns-1)][i]=5;
				}
			}
		}
		//if someone shot someone before, and they hasn't died yet, high chance they will shot that person
		for (int i=0; i< rows; i++) {
			for (int j=0; j<columns;j++) {
				if (m.get(i).get(j)!=-1 && next_round[i][m.get(i).get(j)]==0 && alive[i] && alive[m.get(i).get(j)]) {
					next_round[i][m.get(i).get(j)]= 3;
				}
			}
		}
		//if someone shot them before, and that person hasn't diet yet, high chance they will shot back
		for (int i=0;i<rows;i++){
			for (int j=0;j<columns;j++){
				if (m.get(i).get(j)!=-1 && alive[i] && alive[m.get(i).get(j)]){
					if (next_round [m.get(i).get(j)][i]==0){
						next_round[m.get(i).get(j)][i] =4;
					}
					
				}
			}
		}
//		return next_round;
	}
	
	public void printpredict(int[][] m){
	for (int i=0; i<m[0].length; i++) { 
		for (int j=0;j<m[0].length;j++) {
    			System.out.println( m[i][j]+" ");
  		} 
  		System.out.println();
  		}
	}
    //bingyi 
    public void attentionSeekingPrint(String s)
    {
    	System.out.println("----------------\n"+s+"\n-----------------");
    }
    
    private int getMyTarget(int[] shotAt) 
    {
		
    	/*PriorityTuple firstTuple=priorityList.remove();
    	if(shotAt[firstTuple.getPlayerId()]>0)
    		return firstTuple.getPlayerId();
    	
    	PriorityTuple nextTuple;
    	if(priorityList.size()!=0)
    		nextTuple=priorityList.remove();
    	
    	while(nextTuple.getPriority()==firstTuple.getPriority())
    	{
    		if(shotAt[firstTuple.getPlayerId()]<shotAt[nextTuple.getPlayerId()])
    			return nextTuple.getPlayerId();
    		
    		if(priorityList.size()!=0)
        		nextTuple=priorityList.remove();
    		else
    			break;
    	}
    	
    	return firstTuple.getPlayerId();*/
    	
    	return priorityList.remove().getPlayerId();
    	
	}

	private void gaugeSeverity(int shooter,int target, boolean[] alive) {
		
		System.out.println(shooter+" "+target);
		
		if(target==-1 || shooter==this.id)
			return;
		
    	//If x -> y, p1
    	if(target==this.id && !isFriend(shooter))
    	{
    		priorityList.add(new PriorityTuple(shooter, 1));
    	}
    	
    	//If f -> e, p2
    	else if(isFriend(shooter)&&isEnemy(target)&&alive[target])
    	{
    		priorityList.add(new PriorityTuple(target, 2));
    	}
    	
    	//If e -> f, p2
    	else if(isFriend(target)&&isEnemy(shooter)&& alive[target])
    	{
    		if(alive[shooter])
    			priorityList.add(new PriorityTuple(shooter,2));
    	}
    	
    	//If n -> e, p4
    	else if(isNeutral(shooter)&&isEnemy(target)&&alive[target])
    	{
    		priorityList.add(new PriorityTuple(target,7));
    	}
    	
    	//If n -> f, p5
    	else if(isNeutral(shooter)&&isFriend(target)&&alive[target])
    	{
    		if(alive[shooter])
    			priorityList.add(new PriorityTuple(shooter,6));
    	}
    	
    	//If e -> n, 
    	else if(isNeutral(target)&&isEnemy(shooter))
    	{
    		if(alive[shooter])
    			priorityList.add(new PriorityTuple(shooter, 4));
    	}
    	
    	//If f -> n, 
    	else if(isFriend(shooter)&&isNeutral(target))
    	{
    		if(alive[target])
    			priorityList.add(new PriorityTuple(target, 5));
    	}	
	}

	public boolean isFriend(int pid)
    {
    	for(int friend:friends)
    	{
    		if(friend==pid)
    			return true;
    	}
    	return false;
    }
    
    public boolean isEnemy(int pid)
    {
    	for(int enemy:enemies)
    	{
    		if(enemy==pid)
    			return true;
    	}
    	return false;
    }
    
    //yash
    public boolean isNeutral(int pid)
    {
    	return !isEnemy(pid) && !isFriend(pid);
    }
    //yash
    
    public void printPrevRound(int[] a)
    {
    	String s="";
    	for(int i=0;i<a.length;i++)
    	{
    		s+="["+i+","+a[i]+"]";
    	}
    	
    	attentionSeekingPrint(s);
    }
    
    public void printMatrix(List<List<Integer>> m)
    {
    //try{
        int rows = m.size();
        int columns = m.get(0).size();
        String str = "|\t";

    }
    
}
