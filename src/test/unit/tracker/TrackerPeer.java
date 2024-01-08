package test.unit.tracker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

import tracker.Tracker;
import tracker.TrackerPeerHandler;

public class TrackerPeer {
        public static void main(String[] args) throws NoSuchMethodException, SecurityException, IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                Tracker<TrackerPeerHandler> tracker = new Tracker<>(4001, TrackerPeerHandler.class.getDeclaredConstructor(Socket.class));

                tracker.run();
        }
}
