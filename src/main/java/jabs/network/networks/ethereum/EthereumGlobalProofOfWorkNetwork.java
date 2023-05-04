package jabs.network.networks.ethereum;

import jabs.consensus.config.ChainBasedConsensusConfig;
import jabs.consensus.config.GhostProtocolConfig;
import jabs.consensus.config.NakamotoConsensusConfig;

import jabs.ledgerdata.ethereum.EthereumBlock;
import jabs.network.networks.GlobalProofOfWorkNetwork;
import jabs.network.stats.*;
import jabs.network.node.nodes.ethereum.EthereumMinerNode;
import jabs.network.node.nodes.ethereum.EthereumNode;
import jabs.simulator.randengine.RandomnessEngine;
import jabs.simulator.Simulator;

import java.util.HashSet;

// 继承自全局POW网络
public class EthereumGlobalProofOfWorkNetwork<R extends Enum<R>> extends GlobalProofOfWorkNetwork<EthereumNode, EthereumMinerNode, EthereumBlock, R> {


    public EthereumGlobalProofOfWorkNetwork(RandomnessEngine randomnessEngine, ProofOfWorkGlobalNetworkStats<R> networkStats) {
        super(randomnessEngine, networkStats);
    }

    /**
     * @param difficulty Difficulty of genesis block
     * @return Parent-less block that could be used for genesis block
     * 重写global POW network 三个方法
     *   genesisBlock
     *   createNode
     *   createMiner
     */

    @Override
    public EthereumBlock genesisBlock(double difficulty) {
        return new EthereumBlock(0, 0, 0, null, null, new HashSet<>(), difficulty,
                0);
    }

//    @Override
//    public EthereumNode createSampleNode(Simulator simulator, int nodeID, EthereumBlock genesisBlock, ChainBasedConsensusConfig chainBasedConsensusConfig) {
//        R region = this.sampleRegion(); // 随机选择region
//        return new EthereumNode(simulator, this, nodeID, this.sampleDownloadBandwidth(region), this.sampleUploadBandwidth(region), genesisBlock, (GhostProtocolConfig) chainBasedConsensusConfig);
//    }
    // 使用nakamoto
    @Override
    public EthereumNode createSampleNode(Simulator simulator, int nodeID, EthereumBlock genesisBlock, ChainBasedConsensusConfig chainBasedConsensusConfig) {
        R region = this.sampleRegion(); // 随机选择region
        return new EthereumNode(simulator, this, nodeID, this.sampleDownloadBandwidth(region), this.sampleUploadBandwidth(region), genesisBlock, (NakamotoConsensusConfig) chainBasedConsensusConfig);
    }

    // 矿工需要额外指定hash算力
//    @Override
//    public EthereumMinerNode createSampleMiner(Simulator simulator, int nodeID, double hashPower, EthereumBlock genesisBlock, ChainBasedConsensusConfig chainBasedConsensusConfig) {
//        R region = this.sampleMinerRegion();
//        return new EthereumMinerNode(simulator, this, nodeID, this.sampleDownloadBandwidth(region),
//                this.sampleUploadBandwidth(region), hashPower,
//                genesisBlock, (GhostProtocolConfig) chainBasedConsensusConfig);
//    }
    @Override
    public EthereumMinerNode createSampleMiner(Simulator simulator, int nodeID, double hashPower, EthereumBlock genesisBlock, ChainBasedConsensusConfig chainBasedConsensusConfig) {
        R region = this.sampleMinerRegion();
        return new EthereumMinerNode(simulator, this, nodeID, this.sampleDownloadBandwidth(region),
                this.sampleUploadBandwidth(region), hashPower,
                genesisBlock, (NakamotoConsensusConfig) chainBasedConsensusConfig);
    }

}
