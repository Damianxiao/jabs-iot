package main.java.event;

import main.java.message.Packet;
import main.java.network.Network;
import main.java.node.nodes.Node;

import static main.java.config.SimulationConfig.PACKET_PROCESSING_TIME;

public class PacketReceivingProcess extends AbstractPacketProcessor {
    public PacketReceivingProcess(Network network, Node node) {
        super(network, node);
    }

    protected void sendPacketToNextProcess(Packet packet) {
        this.node.processIncomingPacket(packet);
    }

    public long processingTime(Packet packet) {
        return ((packet.getSize()*8) / (node.getNodeNetworkInterface().downloadBandwidth/1000)) + PACKET_PROCESSING_TIME;
    }
}

