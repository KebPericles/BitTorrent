package tracker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import peer.Config;
import util.lib.Torrent;
import v2.ConnectionEndException;
import v2.Message;
import v2.MessageType;
import v2.PeerInfo;
import v2.Piece;
import v2.TorrentInfo;

// Clase interna que maneja las conexiones de clientes
public class TrackerPeerHandler extends TrackerHandler {
        private PeerInfo peer;

        // Constructor que recibe el socket del cliente
        public TrackerPeerHandler(Socket socket, Tracker<TrackerPeerHandler> tracker, PeerInfo peer) {
                super(socket, tracker);
                this.peer = peer;
        }

        // Método principal del hilo que procesa las solicitudes del cliente
        @Override
        public void run() {
                // TODO PROCESAR COSAS DE PEER

                System.out.println("Conectado");

                try {
                        Message m = Message.readMessage(inputStream);

                        if (m.getType() == MessageType.REQUEST_PEER_STATUS) {
                                System.out.println("Petición de estado de peer recibida");
                                // TODO Enviar estado de peer
                        } else if (m.getType() == MessageType.PEER_LIST) {
                                System.out.println("Lista de peers recibida");
                                // TODO procesar lista de peers
                        } else if (m.getType() == MessageType.REQUEST_PIECE) {
                                // ? iniciar transferencia de pieza
                                // Buscar pieza
                                Piece piece = findPiece(m.getPiece().getTorrentInfo(), m.getPiece());

                                // Enviar mensaje de inicio de transferencia de pieza

                        } else if (m.getType() == MessageType.REPLY_START_PIECE) {
                                // TODO procesar pieza recibida

                        }
                } catch (ConnectionEndException e) {
                        close();

                }

                System.out.println("Conexión cerrada");

                close();
        }

        /**
         * Busca una pieza en el peer
         */
        private byte[] findPieceData(Piece piece) {
                TorrentInfo torrentInfo = piece.getTorrentInfo();
                int pieceIndex = piece.getIndex();
                int pieceBegin = piece.getBegin();
                int pieceLength; // Calculado a partir de la longitud de la pieza y el
                                 // desplazamiento de la pieza en el archivo
                File file = new File(Config.DIR_COMPARTIDOS() + torrentInfo.getName());
                if (!file.exists()) {
                        return null;
                }
                long fileLength = file.length();
                if (pieceIndex == torrentInfo.getPieces().length - 1) {
                        pieceLength = (int) (fileLength - pieceIndex * torrentInfo.getPieceLength());
                } else {
                        pieceLength = torrentInfo.getPieceLength();
                }

                // TODO Leer pieza del archivo
                byte[] pieceData = new byte[pieceLength];

                return null;
        }
}
