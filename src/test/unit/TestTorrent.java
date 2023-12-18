package test.unit;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import util.lib.Torrent;

public class TestTorrent {

        public static void main(String[] args) {
                try {
                        File f = new File("test/unit", "mytest.torrent");
                        f.createNewFile();
                        Torrent.createTorrent(f, new File("test/unit", "mytest.jpg"), "mitrackerXD");
                } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
        }
}
