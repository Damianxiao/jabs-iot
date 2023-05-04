package jabs.utils;

import jabs.ledgerdata.ethereum.EthereumBlock;

import java.util.Comparator;

public class hashComparator implements Comparator<EthereumBlock> {
    public int compare(EthereumBlock e1, EthereumBlock e2) {
        return e1.compareTo(e2);
    }
}
