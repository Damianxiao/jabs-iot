package jabs.consensus.config;

import jabs.ledgerdata.SingleParentBlock;

/**
 */
public class GhostProtocolConfig<B extends SingleParentBlock<B>> extends ChainBasedConsensusConfig<B> {
    /**
     * @param averageBlockMiningInterval
     *  出块间隔
     */
    public GhostProtocolConfig(B genesisBlock, double averageBlockMiningInterval) {
        super(genesisBlock, averageBlockMiningInterval);
    }
}
