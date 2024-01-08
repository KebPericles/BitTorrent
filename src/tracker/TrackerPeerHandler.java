package tracker;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

// Clase interna que maneja las conexiones de clientes
public class TrackerPeerHandler extends TrackerHandler {

        // Constructor que recibe el socket del cliente
        public TrackerPeerHandler(Socket socket) {
                super(socket);
        }

        // Método principal del hilo que procesa las solicitudes del cliente
        @Override
        public void run() {
                // TODO PROCESAR SOLICITUD

                

                close();
        }
}
