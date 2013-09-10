# Gunslinger

It is the wild west, and you find yourself among a circle of N outlaws all
bearing grudges and loyalties from past encounters. In particular, each outlaw
has a list of exactly f friends and a list of e enemies (the two lists are
disjoint), where e + f < N. Each outlaw has a different list. The friend
relationship is symmetric: if A is a friend of B then B is a friend of A.
However, if A is an enemy of B, B is not necessarily an enemy of A; enmity
could have arisen secretly. Further, a friend of a friend could well be an
enemy!

There is a showdown, each of you ready to draw your guns at the slightest
provocation. Each gunslinger chooses another gunslinger and fires a shot;
alternatively, a gunslinger can elect not to shoot. All shots happen at once,
with the bullets in flight simultaneously. Each gunslinger has a shield that
can protect against one shot, but not against more than one. A player is
eliminated if, on a single turn, she is shot by more than one gunslinger.
Otherwise the player remains in the game for the next round.

Players have complete information about who tried to shoot whom on previous
rounds. From this information, they can try to infer friendship and enmity
relationships. Players do not have direct access to other players'
relationships, and do not even know which group's code corresponds to which
player.

The showdown ends when nobody has died for ten consecutive rounds. This might
happen if all remaining players consistently choose not to shoot. It will also
happen if all but one player consistently choose not to shoot, since it takes
two bullets to eliminate a player.

At the end of the showdown, players (including eliminated players) score a
point for each of the following:

* Surviving at the end.
* Having a friend that survived.
* Having an enemy that was eliminated.

So the maximum score on a round is e+f+1.

For the tournament, we will run many instances of the game with the same
collection of players, but with new randomized lists for each showdown. We'll
try different settings for e, f, and N and see how they influence strategy.

## Running

```bash
$ cd [this dir]
$ java gunslinger.sim.Gunslinger gunslinger/players.list [options given in
Gunslinger README]
```

## Changelog

### v0.0.1

* Added initial given player and sim
* Copied the player into a new base player class in `gunslinger/g1/Player.java`
to work from

### v0.0.2

* If you have enemies, shoot a random one. If you don't have any enemies, shoot
a non friend. If you dont have any non friends, don't shoot.
* Simplest actual strategy, with 1 g1 and 8 dumb players, the single g1 player
wins about 50% of the games.
