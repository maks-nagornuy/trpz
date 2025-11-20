package com.example.demo.p2p;
import org.springframework.stereotype.Service;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
@Service
public class P2PService {
    private static final String GROUP = "230.0.0.0";
    private static final int PORT = 4446;
    public void sendMessage(String text) {
        try {
            InetAddress group = InetAddress.getByName(GROUP);
            byte[] msg = text.getBytes();

            DatagramPacket packet = new DatagramPacket(msg, msg.length, group, PORT);
            MulticastSocket socket = new MulticastSocket();
            socket.send(packet);
            socket.close();
            System.out.println("P2P sent: " + text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}