package v2;

import java.io.Serializable;

public class Piece implements Serializable {

        /**
         * Información del torrent
         */
        private TorrentInfo torrentInfo;

        /**
         * Índice de la pieza
         */
        private int index;

        /**
         * Desplazamiento dentro de una pieza de un bloque solicitado
         */
        private int begin;

        /**
         * Constructor para un mensaje REQUEST_PIECE,
         * inicializa el offset y el bloque a cero
         */
        public Piece(TorrentInfo torrentInfo, int index) {
                this(torrentInfo, index, 0, null);
        }

        /**
         * Constructor para un mensaje REPLY_PIECE o REQUEST_PIECE (en caso de que se
         * haya interrumpido la transferencia)
         */
        public Piece(TorrentInfo torrentInfo, int index, int begin, byte[] block) {
                this.torrentInfo = torrentInfo;
                this.index = index;
                this.begin = begin;
        }

        public TorrentInfo getTorrentInfo() {
                return torrentInfo;
        }

        public void setTorrentInfo(TorrentInfo torrentInfo) {
                this.torrentInfo = torrentInfo;
        }

        public int getIndex() {
                return index;
        }

        public void setIndex(int index) {
                this.index = index;
        }

        public int getBegin() {
                return begin;
        }

        public void setBegin(int begin) {
                this.begin = begin;
        }
}
