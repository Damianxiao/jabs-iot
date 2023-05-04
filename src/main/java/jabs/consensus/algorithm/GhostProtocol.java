package jabs.consensus.algorithm;

import jabs.consensus.blockchain.LocalBlockTree;
import jabs.consensus.config.GhostProtocolConfig;
import jabs.ledgerdata.SingleParentBlock;
import jabs.ledgerdata.Tx;

import java.util.HashMap;
import java.util.HashSet;

public class GhostProtocol<B extends SingleParentBlock<B>, T extends Tx<T>>
        extends AbstractChainBasedConsensus<B, T> {
    private final HashMap<B, Integer> totalWeights = new HashMap<>();
    public static int DEFAULT_GHOST_WEIGHT = 1;
    protected B originOfGhost;
    private final double averageBlockMiningInterval;

    public GhostProtocol(LocalBlockTree<B> localBlockTree, GhostProtocolConfig ghostProtocolConfig) {
        super(localBlockTree);
        this.originOfGhost = localBlockTree.getGenesisBlock();
        this.newIncomingBlock(localBlockTree.getGenesisBlock());
        this.averageBlockMiningInterval = ghostProtocolConfig.averageBlockMiningInterval();
    }

    @Override
    public void newIncomingBlock(B block) {
        //totalWeights 是一个hashmap （区块，权重） 这里的权重是默认值：1
        /*这里要给区块一个别的权重，避免balance attack*/
        totalWeights.put(block, DEFAULT_GHOST_WEIGHT);
        // 判断是否连接到genesisblock 也就是判断区块是否是正确的
        if (this.localBlockTree.getLocalBlock(block).isConnectedToGenesis) {
            // 遍历所有父块
            for (B ancestor:this.localBlockTree.getAllAncestors(block)) {
                // 如果存在有父块不存在于主链中，那么将他加入进去 但权重为初始权重,也就是说越靠近末端的区块权重越低
                if (!totalWeights.containsKey(ancestor)) {
                    totalWeights.put(ancestor, DEFAULT_GHOST_WEIGHT);
                }
                totalWeights.put(ancestor, totalWeights.get(ancestor) + DEFAULT_GHOST_WEIGHT);
            }
        }
        //用ghost() 确认主链
        B ghostMainChainHead = this.ghost();
        if (this.currentMainChainHead != ghostMainChainHead) {
            this.currentMainChainHead = ghostMainChainHead;
            updateChain();
        }
    }

    public B ghost() {
        B block = this.originOfGhost; //指向genesisblock

        while (true) {
            if (totalWeights.get(block) == 1) { // 检查originOfGhost节点是否为叶子节点
                return block;
            }

            int maxWeight = 0;
            // getChildren 拿到区块链上末尾的所有区块 选择权重最大的那一个作为主链
            HashSet<B> children = this.localBlockTree.getChildren(block);
            for (B child: children) {
                if (localBlockTree.getLocalBlock(child).isConnectedToGenesis) {
                    if (totalWeights.get(child) > maxWeight) {
                        maxWeight = totalWeights.get(child);
                        block = child;
                    }
                }
            }
        }
    }

    @Override
    protected void updateChain() {
        this.confirmedBlocks = this.localBlockTree.getAllAncestors(this.currentMainChainHead);
    }
}
