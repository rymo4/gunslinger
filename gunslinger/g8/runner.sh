#!/bin/bash
max=0
best=-1
for i in {0..8}
do
  config=$(java ConstantsGenerator $i)
  scores=$(cd ../.. && java gunslinger.sim.Gunslinger gunslinger/players.list 1 2 false false false false 20 1000 $config 3>&1 1>&2- 2>&3- | tail -32)
  player=$(echo ${scores} | sed "s/.*g8: \([^ ]*\).*/\1/")
  if [ $(echo $player'>'$max | bc -l) -eq 1 ]
    then
      best=$config
      max=$player
  fi
done
echo 'Best Configuration:'
echo $best