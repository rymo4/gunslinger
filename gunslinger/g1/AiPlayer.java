package gunslinger.g1;
import java.util.*;

/*
  A class representing all players in the game who are not controlled by the g1.Player class.
  This encapsulates the ML used to guess what the other players will do next, meaning we
  can optimize our own actions to take advantage of their actions.

  AI learning from AI, so meta.
*/

public class AiPlayer {

  public boolean me, dead;

  public int FRIEND       = 0;
  public int SHOT         = 1;
  public int FOE          = 2;
  public int FRIENDS_FOE  = 3;
  public int ENEMY        = 4;
  public int NONE         = 5;

  public int max_e;
  public int max_f;

  public boolean[] enemies, friends;

  public int[] attrs;
  public float[] coeffs;

  public AiPlayer(int n, int e, int f)
  {
      me = false;
      dead = false;

      max_e = e;
      max_f = f;

      attrs  = new int[]{ 0, 0, 0, 0, 0, 0 };
      coeffs = new float[]{ -10f, 2.5f, 9f, 4f, 5f, -2.5f };

      enemies = new boolean[n];
      friends = new boolean[n];
  }

  public float badness_level()
  {
      float sum = 0.0f;

      for (int i = 0; i < attrs.length; i++){
          sum += (attrs[i] * coeffs[i]);
      }
      return sum;
  }

  // coeffs
  private float ENEMY_VAL = 5.0f;
  private float FRIEND_VAL = -10.0f;
  private float NONE_VAL = 0.0f;
  private float FOE_VAL = 9.0f;
  private float FRIENDS_FOE_VAL = 4.0f;
  private float PREV_SHOT_VAL = 5.0f;
}
