package jabs.network.networks;

import jabs.consensus.config.ChainBasedConsensusConfig;
import jabs.consensus.config.ConsensusAlgorithmConfig;
import jabs.ledgerdata.Block;
import jabs.ledgerdata.ProofOfWorkBlock;
import jabs.network.stats.MinerGlobalRegionDistribution;
import jabs.network.stats.ProofOfWorkGlobalNetworkStats;
import jabs.network.node.nodes.MinerNode;
import jabs.network.node.nodes.Node;
import jabs.simulator.Simulator;
import jabs.simulator.randengine.RandomnessEngine;

import java.util.ArrayList;
import java.util.List;

//POW网络类 继承自GlobalNetwork 其中N、M、B、R分别代表不同的泛型参数
public abstract class GlobalProofOfWorkNetwork<N extends Node, M extends MinerNode, B extends Block<B>, R extends Enum<R>> extends GlobalNetwork<N, R> {
    // 矿工类
    protected final List<MinerNode> miners = new ArrayList<>();
    // 矿工全局分布 ?
    protected final MinerGlobalRegionDistribution<R> minerDistribution;

    protected GlobalProofOfWorkNetwork(RandomnessEngine randomnessEngine, ProofOfWorkGlobalNetworkStats<R> networkStats) {
        super(randomnessEngine, networkStats);
        this.minerDistribution = networkStats;
    }

    // 启动所有矿工挖矿过程
    public void startAllMiningProcesses() {
        List<MinerNode> allMiners = this.getAllMiners();
        for (MinerNode miner: allMiners) {
            miner.startMining();
        }
    }

    //获得所有矿工
    public List<MinerNode> getAllMiners() {
        return miners;
    }
    //随机采样矿工的区域；
    public R sampleMinerRegion() {
        return minerDistribution.sampleMinerRegion();
    }
    // 获得i矿工
    public MinerNode getMiner(int i) {
        return miners.get(i);
    }
    // 新增miner
    public void addMiner(M node) {
        nodes.add((N) node);
        miners.add(node);
        nodeTypes.put((N) node, sampleMinerRegion());
    }
    // 随机采样一个矿工的hash算力？ 怎么实现的
    protected double sampleHashPower() {
        return minerDistribution.sampleMinerHashPower();
    }

    // 生成创世块
    public abstract B genesisBlock(double difficulty);

    // 创建一个节点 ？
    public abstract N createSampleNode(Simulator simulator, int nodeID, B genesisBlock,
                                       ChainBasedConsensusConfig chainBasedConsensusConfig);
    // 创建矿工
    public abstract M createSampleMiner(Simulator simulator, int nodeID, double hashPower, B genesisBlock,
                                        ChainBasedConsensusConfig chainBasedConsensusConfig);

    // 用默认数量的节点和矿工填充网络 默认数量
    public void populateNetwork(Simulator simulator, ConsensusAlgorithmConfig consensusAlgorithmConfig) {
        this.populateNetwork(simulator, minerDistribution.totalNumberOfMiners(), nodeDistribution.totalNumberOfNodes(),
                consensusAlgorithmConfig);
    }
    //  指定数量填充网络
    public void populateNetwork(Simulator simulator, int numNodes, ConsensusAlgorithmConfig consensusAlgorithmConfig) {
        int numMiners = (int) Math.floor(minerDistribution.shareOfMinersToAllNodes() * numNodes) + 1;
        this.populateNetwork(simulator, numMiners, numNodes-numMiners, consensusAlgorithmConfig);
    }

    /**
     *
     *  实验测试需要用
     * @param simulator
     * @param numMiners
     * @param numNonMiners
     * @param consensusAlgorithmConfig
     */
    //指定节点、指定矿工 并且按比例分配hash算力 simulator 、 导入共识协议配置consensusAlgorithm
    public void populateNetwork(Simulator simulator, int numMiners, int numNonMiners, ConsensusAlgorithmConfig consensusAlgorithmConfig) {
        long totalHashPower = 0;
        List<Double> hashPowers = new ArrayList<>(); // 算力List
        for (int i = 0; i < numMiners; i++) {
            double hashPower = sampleHashPower(); // 随机取样算力
            hashPowers.add(hashPower);
            totalHashPower += hashPower; // 总算力
        }
        // 设置共识协议细节
        ChainBasedConsensusConfig chainBasedConsensusConfig = (ChainBasedConsensusConfig) consensusAlgorithmConfig;
        double miningInterval = chainBasedConsensusConfig.averageBlockMiningInterval(); // 拿到出块间隔
        B genesisBlock = (B) chainBasedConsensusConfig.getGenesisBlock(); // 拿到创世块
        double genesisDifficulty = ((ProofOfWorkBlock) genesisBlock).getDifficulty(); // 创世块难度
        //创世块难度调整到网络总哈希率的比例因子 这个因子用于调整块生成时间与预期的块挖掘时间保持一致
        double hashPowerScale = genesisDifficulty / (totalHashPower * miningInterval);

        for (int i = 0; i < numMiners; i++) {
            this.addMiner(createSampleMiner(simulator, i, hashPowerScale * hashPowers.get(i), genesisBlock,
                    chainBasedConsensusConfig));
        }

        for (int i = 0; i < numNonMiners; i++) {
            this.addNode(createSampleNode(simulator, numMiners + i, genesisBlock, chainBasedConsensusConfig));
        }

        for (Node node:this.getAllNodes()) {
            node.getP2pConnections().connectToNetwork(this); // 使节点加入P2P网络
        }
    }
}
