require 'rubygems'
require 'java'
include_class 'gunslinger.sim.Gunslinger'

class Chromosome
  attr_accessor :genes

  MUTATION_P = 0.8

  def initialize genes, features, eval_f, res, step_sizes
    @features = features
    @eval_f = eval_f
    @genes = genes
    @res = res
    @fitness = nil
    @genes = genes
    @step_sizes = step_sizes
  end

  def clone_with_genes genes
    Chromosome.new(genes, @features, @eval_f, @res, @step_sizes)
  end

  # { name: "..." min: 0 max: 3}
  def self.random features, eval_f, res
    genes = []
    step_sizes = []

    features.each do |f|
      step_size = (f[:max] - f[:min]).to_f / res
      step_sizes << step_size
      genes << f[:min] + ((rand * res).to_i * step_size)
    end

    Chromosome.new(genes, features, eval_f, res, step_sizes)
  end

  def self.mate dad, mom
    split_point = rand(dad.genes.size - 2) + 1

    son_genes = dad.genes.clone
    daughter_genes = mom.genes.clone

    son_genes[0..split_point], daughter_genes[0..split_point] = mom.genes[0..split_point], dad.genes[0..split_point]

    return dad.clone_with_genes(son_genes), mom.clone_with_genes(daughter_genes)
  end

  def fitness
    @fitness ||= @eval_f.call(@genes)
  end

  def mutate!
    @genes.each_with_index do |g, i|
      if rand < MUTATION_P
        if rand < 0.5
          @genes[i] = g + @step_sizes[i]
        else
          @genes[i] = g - @step_sizes[i]
        end
        if @genes[i] < @features[i][:min]
          @genes[i] = @features[i][:min]
        elsif @genes[i] > @features[i][:max]
          @genes[i] = @features[i][:max]
        end
      end
    end
  end

end

POPULATION_SIZE = 10
RESOLUTION = 5
NUM_ITERATIONS = 3
NUM_ELITES = 4

features = [
  {name: "friend", min: -10, max: 1},
  {name: "shot", min: -10, max: 10},
  {name: "foe", min: -10, max: 10},
  {name: "friends_foe", min: -10, max: 10},
  {name: "enemy", min: -10, max: 10},
  {name: "none", min: -10, max: 10},
  {name: "retaliation", min: -10, max: 10}
]

eval_f = lambda { |gene_values|
  Gunslinger.avgScoreWithCoeffs(gene_values.to_java :float)
}

puts eval_f.call([-1.1999999999999993, 2.0, -6.0, 6.0, 2.0, 2.0, 2.0])

exit

# Make base population
pop = []
POPULATION_SIZE.times do |n|
  pop << Chromosome.random(features, eval_f, RESOLUTION)
end

NUM_ITERATIONS.times do |n|
  puts "Iteration #{n+1}/#{NUM_ITERATIONS}"

  # compute across many threads and cache result
  #pop.threach(4) { |p| p.fitness }

  pop.sort! { |a,b| -a.fitness <=> -b.fitness }
  # print top three solutions
  puts pop.map(&:genes).inspect
  puts pop.map(&:fitness).inspect
  # Take NUM_ELITES best from pop
  pop_1 = pop[0..NUM_ELITES-1]

  pop_2 = []
  num_crossovers = (POPULATION_SIZE - NUM_ELITES) / 2
  num_crossovers.times do
    dad = pop.sample
    mom = pop.sample
    son, daughter = Chromosome.mate dad, mom
    pop_2 << son
    pop_2 << daughter
  end

  num_crossovers.times do
    # mutate a random sample in children
    pop_2[(rand * pop_2.size).to_i].mutate!
  end

  pop = pop_1 + pop_2
end
