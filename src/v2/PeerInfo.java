package v2;

import java.io.Serializable;
import java.util.List;

public class PeerInfo implements Serializable {
        private String ip;
        private int port;
        private List<TorrentInfo> archivosCompartidos;

        public PeerInfo() {
        }

        public PeerInfo(String ip, int port, List<TorrentInfo> archivosCompartidos) {
                this.ip = ip;
                this.port = port;
                this.archivosCompartidos = archivosCompartidos;
        }

        public String getIp() {
                return ip;
        }

        public void setIp(String ip) {
                this.ip = ip;
        }

        public int getPort() {
                return port;
        }

        public void setPort(int port) {
                this.port = port;
        }

        public List<TorrentInfo> getArchivosCompartidos() {
                return archivosCompartidos;
        }

        public void setArchivosCompartidos(List<TorrentInfo> archivosCompartidos) {
                this.archivosCompartidos = archivosCompartidos;
        }

}
