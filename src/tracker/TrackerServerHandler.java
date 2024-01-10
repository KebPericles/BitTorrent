package tracker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import v2.ConnectionEndException;
import v2.Message;
import v2.MessageType;
import v2.PeerInfo;

public class TrackerServerHandler extends TrackerHandler {
        public TrackerServerHandler(Socket socket, Tracker<TrackerServerHandler> tracker) {
                super(socket, tracker);
        }

        @Override
        public void run() {
                // TODO PROCESAR COSAS DEL TRACKER

                System.out.println("Conectado");

                try {
                        Message m = Message.readMessage(inputStream);
                        
                        if (m.getType() == MessageType.ANNOUNCE_STATUS) {
                                /*
                                 * m = new Message(MessageType.ANNOUNCE_STATUS, peer);
                                 * m tiene PeerInfo del peer que se acaba de conectar
                                 */
                                System.out.println("Anuncio de estado de peer recibido");
                                tracker.addPeer(m.getPeers().get(0));
                                // TODO devolver lista de peers @AlexisTepex

                                List<PeerInfo> mensajeListaPeers = tracker.getPeers();

                                Message.sendMessage(outputStream, mensajeListaPeers);
                        } else if (m.getType() == MessageType.UPDATE_STATUS) {
                                System.out.println("Actualización de estado de peer recibida");
                                // TODO @AlexisTepex 
                                
                                
                        }else if (m.getType() == MessageType.PEER_LIST) {
                                System.out.println("Lista de peers recibida");
                                // TODO @AlexisTepex
                        }
                } catch (ConnectionEndException e) {
                        close();

                }

                System.out.println("Conexión cerrada");

                close();
        }
}
