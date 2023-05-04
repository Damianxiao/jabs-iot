package jabs.simulator.event;

import jabs.network.node.nodes.MinerNode;
import jabs.simulator.randengine.RandomnessEngine;
import jabs.simulator.Simulator;

public class BlockMiningProcess extends AbstractPoissonProcess {
    protected final MinerNode miner;

    // 变量：1 Simulator 2 随机数生成器 3 出块间隔时间 4 矿工节点
    public BlockMiningProcess(Simulator simulator, RandomnessEngine randomnessEngine, double averageTimeBetweenBlocks, MinerNode miner) {
        super(simulator, randomnessEngine, averageTimeBetweenBlocks);
        this.miner = miner;
    }

    @Override
    public void generate() {
        miner.generateNewBlock();
    }

    public MinerNode getMiner() {
        return this.miner;
    }
}
