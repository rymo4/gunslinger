package gunslinger.g1;
import java.util.*;

public class AiPlayer {

  public boolean me, dead;
  public int friend, foe, friends_foe, enemy, none, max_e, max_f;
  public boolean[] enemies, friends;

  public AiPlayer(int n, int e, int f)
  {
    me = false;
    dead = false;
    friend = 0;
    foe = 0;
    friends_foe = 0;
    enemy = 0;
    none = 0;
    max_e = e;
    max_f = f;
    enemies = new boolean[n];
    friends = new boolean[n];
  }

  public float badness_level()
  {
    return friend*FRIEND_VAL + foe*FOE_VAL + friends_foe*FRIENDS_FOE_VAL + enemy*ENEMY_VAL + none*NONE_VAL;
  }

  // coeffs
  private float ENEMY_VAL = 5.0f;
  private float FRIEND_VAL = -5.0f;
  private float NONE_VAL = 0.0f;
  private float FOE_VAL = 20.0f;
  private float FRIENDS_FOE_VAL = 5.0f;
}
