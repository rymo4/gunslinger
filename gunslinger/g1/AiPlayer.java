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
  public int RETALIATION  = 6;
  public int NUM_ENEMIES  = 7;


  public int max_e;
  public int max_f;

  public boolean[] enemies, friends;

  public float[] attrs;
  public float[] coeffs;

  public AiPlayer(int n, int e, int f, float[] cs){
      me = false;
      dead = false;

      max_e = e;
      max_f = f;

      attrs  = new float[]{ 0, 0, 0, 0, 0, 0, 0, 0};
      coeffs = cs;

      enemies = new boolean[n];
      friends = new boolean[n];
  }

  public AiPlayer(int n, int e, int f)
  {
      me = false;
      dead = false;

      max_e = e;
      max_f = f;

      attrs  = new float[]{ 0, 0, 0, 0, 0, 0, 0, 0};

      coeffs = new float[]{-10f, 5.6f, 1.2f, 1.2f, 5.6f, -2.8f, 10.0f, 0.5f};

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

}
