package tracker;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import serverTracker.Config;
import v2.PeerInfo;

public class Tracker<T extends TrackerHandler> {
        // Servidor Socket para escuchar conexiones entrantes
        private ServerSocket serverSocket;

        // Listas para almacenar los peers
        private List<PeerInfo> peers;
        private Constructor<T> handlerConstructor;
        private AtomicInteger connectionNumber = new AtomicInteger(0);
        private ExecutorService executor;

        // Constructor del tracker que inicializa el servidor socket en el puerto
        // especificado
        public Tracker(int port, Constructor<T> handlerConstructor) throws IOException {
                System.out.println("Iniciando tracker en puerto " + port);
                this.serverSocket = new ServerSocket(port);
                this.peers = new ArrayList<>();
                this.handlerConstructor = handlerConstructor;
                this.executor = Executors.newFixedThreadPool(Config.TRACKER_MAX_CONNECTIONS());
        }

        // Método principal del tracker que inicia el bucle de escucha
        public void run() throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException,
                        InvocationTargetException {
                System.out.println("Escuchando conexiones entrantes");

                while (true) {
                        // Acepta una nueva conexión entrante
                        connectionNumber.incrementAndGet();
                        Socket socket = serverSocket.accept();
                        // Crea un nuevo hilo para manejar la conexión de forma independiente
                        TrackerHandler handlerInstance = this.handlerConstructor.newInstance(socket);
                        executor.execute(handlerInstance);
                }
        }

        // Método para agregar un peer a la lista de peers
        public void addPeer(PeerInfo peer) {
                peers.add(peer);
        }

        // Método para obtener la lista de peers
        public List<PeerInfo> getPeers() {
                return peers;
        }

        public void updatePeer(PeerInfo peer) {
                // TODO
        }

        public static void main(String[] args) throws InstantiationException, IllegalAccessException,
                        IllegalArgumentException, InvocationTargetException, IOException {

                // TODO INICIAR SERVIDOR
                // TODO ESCUCHAR MENSAJE

                // TODO HILO PARA CADA PEER
                // TODO ENVIAR MENSAJE REPLY_PEERS
                // Acá el peer debe checar los peers que están conectados al tracker
                // y evaluar si debe establecer conexión con alguno de ellos

                // TODO ENVIAR MENSAJE NOTIFY_PEER_ONLINE A TODOS LOS PEERS

                // TODO

                // TODO ENVIAR MENSAJE NOTIFY_PEER_OFFLINE A TODOS LOS PEERS

        }
}
