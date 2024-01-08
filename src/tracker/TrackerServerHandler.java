package tracker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import v2.ConnectionEndException;
import v2.Message;

public class TrackerServerHandler extends TrackerHandler {
        public TrackerServerHandler(Socket socket) {
                super(socket);
        }

        @Override
        public void run() {
                // TODO

                System.out.println("Conectado");

                while (true) {
                        try {
                                Message.readMessage(inputStream);
                        } catch (ConnectionEndException e) {
                                close();
                                
                        }
                }

                System.out.println("Conexión cerrada");

                close();
        }
}
