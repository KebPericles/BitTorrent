package v2;

public enum MessageType {
        START_MSG(0),

        // -----------------------------//
        // TRACKER
        // -----------------------------//
        PEER_LIST(4),
        REQUEST_PEER_STATUS(5),

        // -----------------------------//
        // PEER
        // -----------------------------//
        ANNOUNCE_STATUS(1),
        REQUEST_PIECE(6),
        REPLY_START_PIECE(7),
        REPLY_END_PIECE(8);

        public final int value;

        private MessageType(int value) {
                this.value = value;
        }
}
