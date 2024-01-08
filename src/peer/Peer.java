package peer;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class Peer {

        public static void main(String[] args) {
                try {
                        // Set the console output encoding to UTF-8
                        System.setOut(new PrintStream(System.out, true, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                }

                System.out.println("--------Peer--------");
                // CARGAR CONFIGURACION
                // CARGAR ARCHIVO DE CONFIGURACION

                Config.load();
                Config.save();

                // TODO HILO PARA EL TRACKER
                // CONECTAR AL TRACKER
                // ENVIAR MENSAJE ANNOUNCE_STATUS
                // RECIBIR MENSAJE REPLY_PEERS

                // TODO HILO PARA CADA PEER
                // CONECTAR A LOS PEERS
                // ENVIAR MENSAJE REQUEST_PEER_INFO
                // RECIBIR MENSAJE REPLY_PEER_INFO
                // ENVIAR MENSAJE REQUEST_PIECE
                // RECIBIR MENSAJE REPLY_PIECE

                // TODO MENÚ PARA EL USUARIO
                System.out.flush();
                Menu.loop();
        }
}
