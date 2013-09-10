package gunslinger.g1;

import java.util.*;
import gunslinger.g1.Player;

public class Test
{
    private static gunslinger.g1.Player player;
    public static void main(String[] args){
        player = new gunslinger.g1.Player();
        player.init(5, new int[]{2,3}, new int[]{4});

        System.out.println("Testing player " + player.name());

        boolean fail = false;

        String out = shootsEnemyOnFirstRound();
        if (out != "") {
          fail = true;
          System.out.println(out);
        }

        if (fail) {
            System.out.println("FAIL");
            return;
        }

        System.out.println("Passes");
    }

    private static String shootsEnemyOnFirstRound(){
        int target = player.shoot(new int[]{}, new boolean[]{true, true, true, true, true});
        if (target != 4)
            return "Shot player " + target + " instead of player 4";
        return "";
    }
}
