#!/bin/bash
javac ConstantsGenerator.java
for i in {0..1}
do
  config=$(java ConstantsGenerator $i)
  (cd ../.. && java gunslinger.sim.Gunslinger gunslinger/players.list 1 2 false false false false 100 $config) | tail -32 > $i.txt
done