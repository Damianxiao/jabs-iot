package jabs.scenario;

import jabs.consensus.algorithm.NakamotoHeaviestChainConsensus;
import jabs.consensus.blockchain.LocalBlockTree;
import jabs.consensus.config.GhostProtocolConfig;
import jabs.consensus.config.NakamotoConsensusConfig;
import jabs.consensus.config.NakamotoHeaviestChainConsensusConfig;
import jabs.ledgerdata.Block;
import jabs.ledgerdata.BlockWithTx;
import jabs.ledgerdata.ethereum.EthereumBlock;
import jabs.ledgerdata.ethereum.EthereumBlockWithTx;
import jabs.network.networks.ethereum.EthereumGlobalProofOfWorkNetwork;
import jabs.network.stats.sixglobalregions.ethereum.EthereumProofOfWorkGlobalNetworkStats6Regions;

import static jabs.network.stats.eightysixcountries.ethereum.EthereumProofOfWorkGlobalNetworkStats86Countries.ETHEREUM_DIFFICULTY_2022;

public class NormalEthereumNetworkScenario extends AbstractScenario {
    private final double simulationStopTime;
    private final double averageBlockInterval;

    /**
     * @param name
     * @param seed
     * @param simulationStopTime
     * @param averageBlockInterval
     */
    public NormalEthereumNetworkScenario(String name, long seed,
                                         double simulationStopTime, double averageBlockInterval) {
        super(name, seed);
        this.simulationStopTime = simulationStopTime;
        this.averageBlockInterval = averageBlockInterval;
    }

    // from Main.createNetwork
    // populateNetwork
    @Override
    public void createNetwork() {

        EthereumGlobalProofOfWorkNetwork<?> ethereumNetwork =
                new EthereumGlobalProofOfWorkNetwork<>
                (// new 一个以太坊POW网络
                        randomnessEngine, // 随机数生成引擎
                        new EthereumProofOfWorkGlobalNetworkStats6Regions(randomnessEngine)  // 模拟以太坊网络的算力分布
                );
        this.network = ethereumNetwork;
        // 填充网络节点：普通节点和矿工节点 这里没有指定数量 调用方法给了默认的节点数量， 并且，这里设置的共识协议是ghost协议
//        ethereumNetwork.populateNetwork(simulator, new GhostProtocolConfig(EthereumBlockWithTx.generateGenesisBlock(ETHEREUM_DIFFICULTY_2022), this.averageBlockInterval));
        ethereumNetwork.populateNetwork(simulator,new NakamotoConsensusConfig<>(EthereumBlockWithTx.generateGenesisBlock(ETHEREUM_DIFFICULTY_2022),this.averageBlockInterval,6));
//        ethereumNetwork.populateNetwork(simulator,
//                        new NakamotoConsensusConfig(EthereumBlockWithTx.generateGenesisBlock(ETHEREUM_DIFFICULTY_2022),this.averageBlockInterval,6))
//                , this.averageBlockInterval));
    }

    @Override
    protected void insertInitialEvents() {
        ((EthereumGlobalProofOfWorkNetwork<?>) network).startAllMiningProcesses();  // run 方法 insertInitialEvents 初始事件： 启动所有挖矿过程
    }

    @Override
    public boolean simulationStopCondition() {
        return (simulator.getSimulationTime() > simulationStopTime);
    }
}
