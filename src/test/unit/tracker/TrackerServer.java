package test.unit.tracker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

import tracker.Tracker;
import tracker.TrackerServerHandler;

public class TrackerServer {
        
        public static void main(String[] args) throws NoSuchMethodException, SecurityException, IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                Tracker<TrackerServerHandler> tracker = new Tracker<>(4000, TrackerServerHandler.class.getDeclaredConstructor(Socket.class));

                tracker.run();
        }
}
