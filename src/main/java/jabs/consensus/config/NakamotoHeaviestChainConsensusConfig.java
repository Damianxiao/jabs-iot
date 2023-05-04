package jabs.consensus.config;

import jabs.ledgerdata.SingleParentBlock;

public class NakamotoHeaviestChainConsensusConfig <B extends SingleParentBlock<B>> extends ChainBasedConsensusConfig<B> {
    public NakamotoHeaviestChainConsensusConfig(B genesisBlock, double averageBlockMiningInterval) {
        super(genesisBlock, averageBlockMiningInterval);
    }

}
