package clearcontrol.microscope.lightsheet.spatialphasemodulation.optimizer.geneticalgorithm;

import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.rank.Median;

/**
 * The Population represents a list of solutions undergoing runEpoch,
 * recombination (crossover) and mutation to make a new Population.
 *
 * https://en.wikipedia.org/wiki/Genetic_algorithm
 *
 * Author: @haesleinhuepf 04 2018
 */
public class Population<S extends SolutionInterface>
{

  private final SolutionFactory<S> mFactory;
  private final int mNumberOfMutations;
  ArrayList<S> mSolutionList;
  Random mRandom = new Random();

  private final double similarityTolerance = 0.001;

  public Population(SolutionFactory<S> pFactory,
                    int pPopulationSize,
                    int pNumberOfMutations)
  {
    mFactory = pFactory;
    mSolutionList = new ArrayList<S>();
    for (int i = 0; i < pPopulationSize; i++)
    {
      mSolutionList.add(pFactory.random());
    }
    mNumberOfMutations = pNumberOfMutations;
  }

  private Population(SolutionFactory<S> pFactory,
                     ArrayList<S> pSolutionList,
                     int pNumberOfMutations)
  {
    mFactory = pFactory;
    mSolutionList = pSolutionList;
    mNumberOfMutations = pNumberOfMutations;
  }

  public double[] fitnesses()
  {
    double[] lFitnesses = new double[mSolutionList.size()];
    for (int i = 0; i < lFitnesses.length; i++)
    {
      lFitnesses[i] = mSolutionList.get(i).fitness();
    }
    return lFitnesses;
  }

  public double fitness()
  {
    return new Mean().evaluate(fitnesses());
  }

  public S best()
  {
    double[] lFitnesses = fitnesses();

    double lMax = lFitnesses[0];
    int lMaxIndex = 0;

    for (int i = 1; i < lFitnesses.length; i++)
    {
      if (lFitnesses[i] > lMax)
      {
        lMax = lFitnesses[i];
        lMaxIndex = i;
      }
    }
    return mSolutionList.get(lMaxIndex);
  }

  public Population runEpoch()
  {
    // determine fitnesses for all solutions
    double[] lFitnesses = fitnesses();

    // select the better half
    ArrayList<S> lNewSolutionList = new ArrayList<S>();
    double median = new Median().evaluate(lFitnesses);
    for (int i = 0; i < lFitnesses.length; i++)
    {
      if (lFitnesses[i] > median)
      {
        lNewSolutionList.add(mSolutionList.get(i));
      }
    }

    // make new combinations for the other half
    int lNumberOfGoodSolutions = lNewSolutionList.size();
    while (lNewSolutionList.size() < mSolutionList.size())
    {
      int lRandomA = mRandom.nextInt(lNumberOfGoodSolutions);
      int lRandomB = mRandom.nextInt(lNumberOfGoodSolutions);
      while (lRandomA == lRandomB)
      {
        lRandomB = mRandom.nextInt(lNumberOfGoodSolutions);
      }

      S lSolution =
                  mFactory.crossover(lNewSolutionList.get(lRandomA),
                                     lNewSolutionList.get(lRandomB));
      for (int m = 0; m < mNumberOfMutations; m++)
      {
        lSolution.mutate();
      }
      lNewSolutionList.add(lSolution);
    }

    return new Population<S>(mFactory,
                             lNewSolutionList,
                             mNumberOfMutations);
  }

  public S getSolution(int index)
  {
    return mSolutionList.get(index);
  }

  public void removeDuplicates()
  {
    int populationSize = mSolutionList.size();

    boolean containedDuplicates = true;

    while (containedDuplicates)
    {
      containedDuplicates = false;
      for (int i = 0; i < populationSize; i++)
      {
        for (int j = 0; j < populationSize; j++)
        {
          if (i != j)
          {
            if (mSolutionList.get(i).isSimilar(mSolutionList.get(j),
                                               similarityTolerance))
            {
              containedDuplicates = true;
              mSolutionList.remove(j);
              break;
            }
          }
        }
        if (containedDuplicates)
        {
          break;
        }
      }
      if (containedDuplicates)
      {
        // add a new individuum
        S solution =
                   mFactory.crossover(mSolutionList.get(mRandom.nextInt(mSolutionList.size())),
                                      mSolutionList.get(mRandom.nextInt(mSolutionList.size())));
        solution.mutate();
        mSolutionList.add(solution);
      }

      System.out.println("Removing S duplicates");
      for (S s : mSolutionList)
      {
        System.out.println("S: " + s.toString());
      }
    }

  }
}
