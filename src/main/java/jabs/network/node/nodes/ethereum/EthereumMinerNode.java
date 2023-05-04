package jabs.network.node.nodes.ethereum;

import jabs.consensus.algorithm.AbstractChainBasedConsensus;
import jabs.consensus.config.GhostProtocolConfig;
import jabs.consensus.config.NakamotoConsensusConfig;
import jabs.consensus.config.NakamotoHeaviestChainConsensusConfig;
import jabs.ledgerdata.ethereum.EthereumBlock;
import jabs.ledgerdata.ethereum.EthereumBlockWithTx;
import jabs.ledgerdata.ethereum.EthereumTx;
import jabs.network.message.DataMessage;
import jabs.network.message.Packet;
import jabs.network.networks.Network;
import jabs.network.networks.ethereum.EthereumGlobalProofOfWorkNetwork;
import jabs.network.node.nodes.MinerNode;
import jabs.network.node.nodes.Node;
import jabs.network.stats.MinerGlobalRegionDistribution;
import jabs.network.stats.eightysixcountries.ethereum.EthereumProofOfWorkGlobalNetworkStats86Countries;
import jabs.simulator.Simulator;
import jabs.simulator.event.BlockMiningProcess;
import jabs.simulator.randengine.RandomnessEngine;
import jabs.utils.hashComparator;

import javax.swing.plaf.synth.Region;
import java.util.*;

import static jabs.ledgerdata.BlockFactory.ETHEREUM_MIN_DIFFICULTY;


/*以太坊矿工节点、 继承自MineNode*/
public class EthereumMinerNode extends EthereumNode implements MinerNode {
    protected Set<EthereumTx> memPool = new HashSet<>();// 存储交易池中的未处理交易
    protected Set<EthereumBlock> alreadyUncledBlocks = new HashSet<>();  // ?
    protected final double hashPower;//节点算力
    protected Simulator.ScheduledEvent miningProcess; //创建一个挖矿进程
    static final long MAXIMUM_BLOCK_GAS = 12500000; // 区块 gas limit 2023.4： 15.000.000

//    protected final EthereumProofOfWorkGlobalNetworkStats86Countries;


    // 构造方法1，接受模拟器、网络、节点ID、下载/上传带宽、哈希算力、创世区块和Ghost协议配置作为参数
    public EthereumMinerNode(Simulator simulator, Network network, int nodeID, long downloadBandwidth, long uploadBandwidth, double hashPower, EthereumBlock genesisBlock, GhostProtocolConfig ghostProtocolConfig) {
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, genesisBlock, ghostProtocolConfig);
        this.hashPower = hashPower;
    }

    //中本聪最长链共识
    public EthereumMinerNode(Simulator simulator, Network network, int nodeID, long downloadBandwidth, long uploadBandwidth, double hashPower, EthereumBlock genesisBlock, NakamotoConsensusConfig nakamotoConsensusConfig) {
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, genesisBlock, nakamotoConsensusConfig);
        this.hashPower = hashPower;
    }


    // 构造方法2，接受模拟器、网络、节点ID、下载/上传带宽、哈希算力和共识算法作为参数
    public EthereumMinerNode(Simulator simulator, Network network, int nodeID, long downloadBandwidth,
                             long uploadBandwidth, double hashPower,
                             AbstractChainBasedConsensus<EthereumBlock, EthereumTx> consensusAlgorithm) {
        super(simulator, network, nodeID, downloadBandwidth, uploadBandwidth, consensusAlgorithm);
        this.hashPower = hashPower;
    }




    // 生成新区块 这个操作应该在process之前 生成区块包含了 miner 的求hash操作
    public void generateNewBlock() {
        RandomnessEngine rand = new RandomnessEngine(this.getNodeID());
        boolean flag = false; // flag == true 即说明此miner通过了检验，为此它将进行一次enhanced ghost协议
        List<EthereumBlock> sameHeightTipBLocks = new ArrayList<>();

        // 以太坊节点性能
        EthereumProofOfWorkGlobalNetworkStats86Countries randomR = new EthereumProofOfWorkGlobalNetworkStats86Countries(rand);
        int sampNum = rand.sampleInt(randomR.totalNumberOfMiners()); // 矿工节点数量取样
        double thisHashPower = this.hashPower;
        // 先获取区块链的主链区块
        EthereumBlock canonicalChainHead = this.consensusAlgorithm.getCanonicalChainHead();
        // 如果这个miner节点的算力足够高，或者是概率达到标准，都将有机会将上一个高度的父块进行拓扑排序并加入总区块链。
        while(sampNum-->0){
            if(thisHashPower<randomR.sampleMinerHashPower()){
                flag = false;
                break;
            }
            flag = true;
        }

        // tipBLocks ：叔块。当矿工挖掘新块时，他们必须指定该块的父块，这样新块才能成为区块链上的有效块。
        //  获得相同高度的叔块：
        Set<EthereumBlock> tipBlocks = this.localBlockTree.getChildlessBlocks();
        // 这里移除了canonical head，
        // 移除CCH，确保新区块只包含未确认的交易？
        tipBlocks.remove(canonicalChainHead);
        // 移除已被确认的叔块 这些叔块在分叉链上，但已经被新来的区块包含并确认了
        tipBlocks.removeAll(alreadyUncledBlocks);
        // 在gas limit 内打包交易
        Set<EthereumTx> blockTxs = new HashSet<>();
        long totalGas = 0;
        for (EthereumTx ethereumTx:memPool) {
            if ((totalGas + ethereumTx.getGas()) > MAXIMUM_BLOCK_GAS) {
                break;
            }
            blockTxs.add(ethereumTx);
            totalGas += ethereumTx.getGas();
        }
        for(EthereumBlock ether:tipBlocks){
            // 若某节点完成了一个较高质量的hash结果,则他能够对分叉的tipblocks进行 一个同高度的 enhanced ghost protocol 拓扑排序
            if(ether.getHeight()==canonicalChainHead.getHeight()){
                sameHeightTipBLocks.add(ether);
            }
        }
            if(flag&&!tipBlocks.isEmpty()&&tipBlocks.size()>1&&!sameHeightTipBLocks.isEmpty()){
                Collections.sort(sameHeightTipBLocks,new hashComparator());
                // 如果是enhanced node 权重特殊处理
                double weight = this.network.getRandom().sampleExponentialDistribution(sameHeightTipBLocks.size());
                // 新区块高度在canonical上加一。creator 为当前mineNode
                EthereumBlockWithTx ethereumBlockWithTx = new EthereumBlockWithTx(
                        canonicalChainHead.getHeight()+1, simulator.getSimulationTime(),
                        this,
                        this.getConsensusAlgorithm().getCanonicalChainHead(), tipBlocks, blockTxs, ETHEREUM_MIN_DIFFICULTY,
                        weight); // TODO: Difficulty?
                this.processIncomingPacket(
                        new Packet(
                                this, this, new DataMessage(ethereumBlockWithTx)
                        )
                );
                // 把排序过后的区块进行 加入区块链的操作 processIncomingPacket
                for(EthereumBlock ether :sameHeightTipBLocks){
                    this.processIncomingPacket(
                            new Packet(
                                    this, this, new DataMessage(ether)
                            )
                    );
                }

            }else{
                /*权重：新区块将会被随机赋上一个权重
                 创新点3 一个区块将根据 POW难度、gas fee 、 交易数量 、时间戳产生一个加权值 权重越高，区块就越容易被接受*/
                double weight = this.network.getRandom().sampleExponentialDistribution(1);  // 待重写
                // 新区块高度在canonical上加一。creator 为当前mineNode
                EthereumBlockWithTx ethereumBlockWithTx = new EthereumBlockWithTx(
                        canonicalChainHead.getHeight()+1, simulator.getSimulationTime(),
                        this,
                        this.getConsensusAlgorithm().getCanonicalChainHead(), tipBlocks, blockTxs, ETHEREUM_MIN_DIFFICULTY,
                        weight); // TODO: Difficulty?

                // 将这新区块封装进一个DataMessage中,并发送给自己: 自己现在本地进行一次验证。之后方便发给其他节点
                // 这里是区块加入区块链的操作 processIncomingPacket
                this.processIncomingPacket(
                        new Packet(
                                this, this, new DataMessage(ethereumBlockWithTx)
                        )
                );
            }

    }

    /**
     * 挖矿
     */
    @Override
    public void startMining() {
        // 新建一个BlockMiningProcess 过程，1 Simulator 2 提供一个随机数，即挖矿中puzzle的nonce 挖矿难度是由区块头除去当前miner的hashpower得来。
        BlockMiningProcess blockMiningProcess = new BlockMiningProcess(this.simulator, this.network.getRandom(), this.consensusAlgorithm.getCanonicalChainHead().getDifficulty()/((double) this.hashPower), this);
        this.miningProcess = this.simulator.putEvent(blockMiningProcess, blockMiningProcess.timeToNextGeneration());
    }

    /**
     *
     */
    @Override
    public void stopMining() {
        simulator.removeEvent(this.miningProcess);
    }

    public double getHashPower() {
        return this.hashPower;
    }

    @Override
    protected void processNewTx(EthereumTx ethereumTx, Node from) {
        // add to memPool
        memPool.add((EthereumTx) ethereumTx);

        this.broadcastTransaction((EthereumTx) ethereumTx, from);
    }

    @Override
    protected void processNewBlock(EthereumBlock ethereumBlock) {
        // 这个方法用于更新区块链状态 在ghost协议下： 将 newInComing block 加入
        this.consensusAlgorithm.newIncomingBlock(ethereumBlock);

        alreadyUncledBlocks.addAll(ethereumBlock.getUncles());

        // remove from memPool
        if (ethereumBlock instanceof EthereumBlockWithTx) {
            for (EthereumTx ethereumTx: ((EthereumBlockWithTx) ethereumBlock).getTxs()) {
                memPool.remove(ethereumTx); // TODO: This should be changed. Ethereum reverts Txs from non canonical chain
            }
        }

        this.broadcastNewBlockAndBlockHashes(ethereumBlock);
    }
}
