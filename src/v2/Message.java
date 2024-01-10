package v2;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Message implements java.io.Serializable {
        private MessageType type;
        private List<PeerInfo> peers = null;
        private Piece onePiece = null;

        /**
         * Constructor para un mensaje REQUEST_PEER_STATUS o ANNOUNCE_STATUS
         * 
         * @param type tipo de mensaje (REQUEST_PEER_STATUS o ANNOUNCE_STATUS)
         */
        public Message(MessageType type, PeerInfo peer) {
                this.type = MessageType.REQUEST_PEER_STATUS;
                this.peers = new ArrayList<>();
                this.peers.add(peer);
        }

        /**
         * Constructor para un mensaje PEER_LIST
         * 
         * @param type  tipo de mensaje (PEER_LIST)
         * @param peers lista de peers
         */
        public Message(MessageType type, List<PeerInfo> peers) {
                this.type = type;
                this.peers = peers;
        }

        /**
         * Constructor para un mensaje REQUEST_PIECE o REPLY_PIECE (tanto para empezar
         * como para terminar)
         * 
         * @param type  tipo de mensaje (REQUEST_PIECE o REPLY_PIECE)
         * @param piece pieza
         */
        public Message(MessageType type, Piece piece) {
                this.type = type;
                this.onePiece = piece;
        }

        
        public static Message readMessage(InputStream inputStream) throws ConnectionEndException {
                // TODO

                int i;
                do {
                        try {
                                i = inputStream.read();
                                if (i == -1) {
                                        throw new ConnectionEndException();
                                }
                        } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                        }
                } while (i != MessageType.START_MSG.value);

                try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                        return (Message) objectInputStream.readObject();
                } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                        return null;
                }
        }
 
        public static void sendMessage(OutputStream outputStream, Message message) {
                // TODO
        }

        public MessageType getType() {
                return type;
        }

        public void setType(MessageType type) {
                this.type = type;
        }

        public List<PeerInfo> getPeers() {
                return peers;
        }

        public void setPeers(List<PeerInfo> peers) {
                this.peers = peers;
        }

        public Piece getPiece() {
                return onePiece;
        }

        public void setPiece(Piece onePiece) {
                this.onePiece = onePiece;
        }
}
