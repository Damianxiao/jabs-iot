package jabs.simulator.randengine;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.ParetoDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.util.FastMath;

import java.util.Collections;
import java.util.List;
// 继承的apcache 的随机数生成器
public class RandomnessEngine extends MersenneTwister {
    public RandomnessEngine(long seed) {
        super(seed);
    }

    // sampleSubset：从给定列表中随机选择n个元素并返回。如果列表长度小于n，则返回null。
    public <E> List<E> sampleSubset(List<E> list, int n) {
        int length = list.size();
        if (length < n) return null;
        for (int i = length - 1; i >= length - n; --i) {
            Collections.swap(list, i , this.nextInt(i + 1));
        }
        return list.subList(length - n, length);
    }

    // sampleFromList：从给定列表中随机选择一个元素并返回。
    public <E> E sampleFromList(List<E> list) {
        return list.get(this.nextInt(list.size()));
    }

    public long sampleDistributionWithBins(double[] dist, long[] bins) {
        double rand = this.nextDouble();
        for (int i = 0; i < dist.length-1; i++) {
            if (rand < dist[i]) {
                double diff = rand / dist[i];
                return (bins[i] + (long)(diff * (bins[i+1]-bins[i])));
            } else {
                rand -= dist[i];
            }
        }
        return bins[bins.length-1];
    }

    // sampleDistributionWithBins：使用给定的分布和区间，生成一个随机数并返回。该方法通过比较随机数和分布的累积值来确定所属区间，并在该区间内计算随机数的值。
    public long sampleDistributionWithBins(List<Double> dist, long[] bins) {
        double rand = this.nextDouble();
        for (int i = 0; i < dist.size()-1; i++) {
            if (rand < dist.get(i)) {
                double diff = rand / dist.get(i);
                return (bins[i] + (long)(diff * (bins[i+1]-bins[i])));
            } else {
                rand -= dist.get(i);
            }
        }
        return bins[bins.length-1];
    }

    //sampleFromDistribution：使用给定的概率分布生成一个随机整数。
    public int sampleFromDistribution(double[] dist) {
        double rand = this.nextDouble();
        for (int i = 0; i < dist.length-1; i++) {
            if (rand < dist[i]) {
                double diff = rand / dist[i];
                return i;
            } else {
                rand -= dist[i];
            }
        }
        return dist.length-1;
    }

    // sampleInt：生成一个0到max（不包括max）之间的随机整数。
    public int sampleInt(int max) {
        return this.nextInt(max);
    }

    // sampledouble：生成一个0到max（不包括max）之间的随机浮点数。
    public double sampleDouble(double max) {
        return this.nextDouble() * max;
    }

    //sampleExponentialDistribution：生成一个指数分布的随机数，其中平均值由mean参数指定。
    public double sampleExponentialDistribution(double mean) {
        ExponentialDistribution expDist = new ExponentialDistribution(this, mean);
        return expDist.sample();
    }

    //sampleLogNormalDistribution：生成一个对数正态分布的随机数，其中中位数和标准差由median和stdDev参数指定。
    public double sampleLogNormalDistribution(double median, double stdDev) {
        LogNormalDistribution expDist = new LogNormalDistribution(this, FastMath.log(median), stdDev);
        return expDist.sample();
    }

    public double sampleParetoDistribution(double scale, double shape) {
        ParetoDistribution pareto = new ParetoDistribution(this, scale, shape);
        return pareto.sample();
    }
}
