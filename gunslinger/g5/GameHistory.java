package gunslinger.g5;

import java.util.ArrayList;
import java.util.Arrays;

public class GameHistory {
	
	private ArrayList<int[]> history;
	
	public GameHistory() {
		history = new ArrayList<int[]>();
	}
	
	public void add(int[] roundInfo) {
		history.add(roundInfo.clone());
	}
	
	// returns an array of players who have ever shot the victim
	public int[] everShot(int victimID) {
		ArrayList<Integer> templist = new ArrayList<Integer>();
		for (int i = 0; i < history.size(); i++) {
			int[] info = history.get(i);
			for (int j = 0; j < info.length; j++) {
				if (info[j] == victimID && !templist.contains(j)) {
					templist.add(j);
				}
			}
		}
		if (templist.isEmpty()) {
			return null;
		}
		int[] shooters = new int[templist.size()];
		for (int i = 0; i < shooters.length; i++) {
			shooters[i] = templist.get(i).intValue();
		}
		return shooters;
	}
	
	// returns the number of shots fired in a given round
	public int shotsFiredInRound(int round) {
		int amount = 0;
		int[] info = history.get(round);
		for (int i = 0; i < info.length; i++) {
			if (info[i] != -1) {
				amount++;
			}
		}
		return amount;
	}
	
	public int shotsFiredOnPlayer(int victimID) {
		int numShots = 0;
		for (int i = 0; i < history.size(); i++) {
			int[] info = history.get(i);
			for (int j = 0; j < info.length; j++) {
				if (info[j] == victimID) {
					numShots++;
				}
			}
		}
		return numShots;
	}
	
	public int consecutiveShots(int shooterID, int victimID) {
		int numShots = 0;
		for (int i = history.size() - 1; i >= 0; i--) {
			if (history.get(i)[shooterID] == victimID) {
				numShots++;
			}
			else {
				break;
			}
		}
		return numShots;
	}
	
	public int shotInRound(int victimID, int round) {
		int[] roundInfo = history.get(round);
		for (int i = 0; i < roundInfo.length; i++) {
			if (roundInfo[i] == victimID) {
				return i;
			}
		}
		return -1;
	}
	
	public int getRetaliationFactor(int playerID) {
		int amount = 0;
		for (int i = 0; i < history.size() - 1; i++) {
			int shooter = shotInRound(playerID, i);
			if (shooter > -1) {
				if (shotInRound(shooter, i+1) == playerID) {
					amount++;
				}
			}
		}
		return amount;
	}
	
	public int getReinforcementFactor(int playerID, boolean[] alive) {
		int amount = 0;
		for (int i = 0; i < history.size() - 1; i++) {
			int shooter = shotInRound(playerID, i);
			if (shooter > -1) {
				int[] nextRound = history.get(i+1);
				for (int j = 0; j < nextRound.length; j++) {
					if (nextRound[j] == shooter && alive[j]) {
						amount++;
					}
				}
				
			}
		}
		return amount;
	}
	
	
	public String toString() {
		String s = "g5 HISTORY:";
		for (int i = 0; i < history.size(); i++) {
			s += Arrays.toString(history.get(i));
		}
		return s;
	}
	
	
}
