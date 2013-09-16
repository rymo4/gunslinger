gunslinger
==========

COMS 4444 First Project

9/10/13

- Implemented basic ranking system that gave weights based on previous actions. Specifically, 2 points were given to players that shot at us, 1 point was given to enemies, 1 point was given to players that shot at friends, and 1 point was deducted from players that shot at enemies. A higher rank signified a higher chance to be attacked.
- Also counted the number of times a player was targeted to estimate the probablity that a certain player would be shot (greater number of times meant higher probability). This was used as a tiebreaker between targets with equal ranks.
