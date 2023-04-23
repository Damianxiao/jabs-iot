package jabs.scenario;

import jabs.consensus.config.GhostProtocolConfig;
import jabs.ledgerdata.ethereum.EthereumBlock;
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
        ethereumNetwork.populateNetwork(simulator,
                                                                    new GhostProtocolConfig(EthereumBlock.generateGenesisBlock(ETHEREUM_DIFFICULTY_2022),
                                                                    this.averageBlockInterval)
        );
    }

    @Override
    protected void insertInitialEvents() {
        ((EthereumGlobalProofOfWorkNetwork<?>) network).startAllMiningProcesses();
    }

    @Override
    public boolean simulationStopCondition() {
        return (simulator.getSimulationTime() > simulationStopTime);
    }
}
