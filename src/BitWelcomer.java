/* BitWelcomer.java: clase ejecutable para dar la bienvenida a los pares entrantes de BitTorrent */
/* El objetivo principal es proporcionar la aceptación sin bloqueo de nuevos compañeros */


import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.io.IOException;
import java.util.LinkedList;

public class BitWelcomer extends Thread {
    private ServerSocket welcomeSocket = null;  // Bienvenida a nuevos peers
    private LinkedList<Socket> welcomeQ = null; // Nuevos peers pendientes
    private volatile boolean isStopped = false;

    public BitWelcomer(int welcomePort, final LinkedList<Socket> welcomeQ) {
        this.welcomeQ = welcomeQ;

        try {
            welcomeSocket = new ServerSocket(welcomePort);
            welcomeSocket.setSoTimeout(5000);
            System.out.println("Client listening on port " + welcomePort);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void stopThread() {
        isStopped = true;
    }

    /* ejecutar: bucle continuamente para aceptar nuevos pares de BitTorrent */
    public void run() {
        while (!isStopped) {
            Socket peerSocket = null;
            try {
                peerSocket = welcomeSocket.accept();
            } catch (Exception ex) {
                continue;
            }
            if (peerSocket != null) {
                synchronized (welcomeQ) {
                    welcomeQ.offer(peerSocket);
                    welcomeQ.notifyAll();
                }
            }
        }
    }
}
