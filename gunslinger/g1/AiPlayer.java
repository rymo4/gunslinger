import java.util.*;

// An example player
// Extends gunslinger.sim.Player to start with your player
//
public class AiPlayer 
{

  private float ENEMY_VAL = 5.0f
  private float FRIEND_VAL = -5.0f
  private float NONE_VAL = 0.0f
  private float FOE_VAL = 20.0f
  private float FRIENDS_FOE_VAL = 5.0f

  public AiPlayer()
  {
    int friend = 0;
    int foe = 0;
    int friends_foe = 0;
    int enemy = 0;
    int none = 0;
  }

  public float badness_level()
  {
    return friend*FRIEND_VAL + foe*FOE_VAL + friends_foe*FRIENDS_FOE_VAL + enemy*ENEMY_VAL + none*NONE_VAL
  }

}