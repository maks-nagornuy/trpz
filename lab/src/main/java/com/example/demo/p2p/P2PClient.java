package com.example.demo.p2p;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
public class P2PClient {
    public static void main(String[] args) throws Exception {
        MulticastSocket socket = new MulticastSocket(4446);
        InetAddress group = InetAddress.getByName("230.0.0.0");
        socket.joinGroup(group);
        System.out.println("Start");
        while (true) {
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            String msg = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received P2P: " + msg);
        }
    }
}