/* BitPeer.java: clase para la conexión a un par en BitTorrent */

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Queue;
import java.util.Random;

import util.lib.BitLibrary;

public class BitPeer {
    private static final int HANDSHAKE_SIZE = 68;
    private InetSocketAddress peerAddrPort;
    private String peerIDstring;    // hexadecimal string de SHA1
    private byte[] peerID;
    private long lastUsed;
    private boolean[] remoteBitfield = null;
    private Socket peerSocket = null;
    private BufferedOutputStream outToPeer = null;
    private BufferedInputStream inFromPeer = null;
    private BitReader reader = null;
    private Queue<BitMessage> messageQ = null;

    public HashSet<Integer> outstandingRequests = null;
    public boolean localIsChoked;       // peer esta chocando con este cliente
    public boolean remoteIsChoked;      // Este cliente esta chocando con el peer
    public boolean localIsInterested;   // Este cliente esta interesado
    public boolean remoteIsInterested;  // El peer esta interesado

    /* BitPeer(InetAddress, int): contructor para el comando del peer line/tracker */
    public BitPeer(InetAddress peerAddr, int peerPort) {
        this.peerAddrPort = new InetSocketAddress(peerAddr, peerPort);
        String stringToHash = getIP().toString() + String.valueOf(getPort());
        peerID = BitLibrary.getSHA1(stringToHash);
        peerIDstring = BitLibrary.bytesToHex(peerID);
        this.lastUsed = System.currentTimeMillis();
        this.outstandingRequests = new HashSet<Integer>();

        // los compañeros empiezan chokend y interested
        this.localIsChoked = true;
        this.localIsInterested = false;
        this.remoteIsChoked = true;
        this.remoteIsInterested = false;
    }

    /* BitPeer(Socket):  constructor para recibir peer off socket de bienvenida*/
    public BitPeer(Socket peerSocket) {
        this.peerSocket = peerSocket;
        this.peerAddrPort = new InetSocketAddress(peerSocket.getInetAddress(), 
                                                  peerSocket.getPort());
        String stringToHash = getIP().toString() + String.valueOf(getPort());
        peerID = BitLibrary.getSHA1(stringToHash);
        peerIDstring = BitLibrary.bytesToHex(peerID);
        this.outstandingRequests = new HashSet<Integer>();

        // peers comienzan choked y uninterested
        this.localIsChoked = true;
        this.localIsInterested = false;
        this.remoteIsChoked = true;
        this.remoteIsInterested = false;
        try {
            this.inFromPeer = new BufferedInputStream(
                              new DataInputStream(peerSocket.getInputStream()));
            this.outToPeer = new BufferedOutputStream(
                             new DataOutputStream(peerSocket.getOutputStream()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // solo inicialice el lector una vez que se complete el protocolo de enlace

        this.lastUsed = System.currentTimeMillis();
    }

    /* connect:  conectar a un peer espesifico */
    public int connect() {
        // initializar input/output streams
        try {
            peerSocket = new Socket(getIP(), getPort());
            outToPeer = new BufferedOutputStream(
                        new DataOutputStream(peerSocket.getOutputStream()));
            inFromPeer = new BufferedInputStream(
                         new DataInputStream(peerSocket.getInputStream()));
        } catch (IOException ex) {
            System.err.println("error: failed to connect to peer at " + getIP());
            return -1;
        }

        this.lastUsed = System.currentTimeMillis();

        return 0;
    }

    /* updateLastUsed: actualizar la marca de tiempo para cuando se usó por última vez */
    public void updateLastUsed() {
        lastUsed = System.currentTimeMillis();
    }

    /* getLastUsed: indicar cuándo se usó por última vez*/
    public long getLastUsed() {
        return lastUsed;
    }

    /* close: apague el hilo del lector y cierra el socket */
    public void close() {
        if (reader != null) {
            reader.stopThread();
        }
        if (peerSocket != null) {
            try {
                peerSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean[] getBitfield() {
        return this.remoteBitfield;
    }

    public void setBitfield(boolean[] remoteBitfield) {
        this.remoteBitfield = remoteBitfield;
    }

    public void addToBitfield(int index) {
        remoteBitfield[index] = true;
    }

    public boolean hasPiece(int index) {
        return this.remoteBitfield[index];
    }

    /* getNextMessage: regresa el mensaje messageQ */
    public BitMessage getNextMessage() {
        BitMessage msg = null;
        if (messageQ == null) {
            return null;
        }
        synchronized (messageQ) {
            msg = messageQ.poll();
            messageQ.notifyAll();    // notificar al hilo del lector de un nuevo espacio
        }
        return msg;
    }

    /* getRarePiece: índice de retorno de una pieza que tuvo un compañero y no en */
    /*  remoteBitfield hace esto al azar y devuelve -1 si no existe tal pieza*/
    public int getRarePiece(boolean[] clientHas) {
        if (remoteBitfield == null || clientHas == null) {
            return -1;
        }

        int[] rarePieces = new int[clientHas.length];
        int j = 0;
        for (int i = 0; i < clientHas.length; ++i) {
            if (!clientHas[i] && remoteBitfield[i]) {
                rarePieces[j++] = i;
            }
        }
        if (j == 0) {
            return -1;
        }
        Random random = new Random(System.currentTimeMillis());
        return rarePieces[random.nextInt(j)];

    }

    /* write:  escribe el numero de bytes del socket */
    public int write(byte[] sendData, int offset, int len) {
        int numWritten = 0;
        if (outToPeer == null) {
            return 0;
        }

        try {
            outToPeer.write(sendData, offset, len);
            outToPeer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return numWritten;
    }

    /* sendHandshake: abre socket al peer y envia el mensaje handshake */
    /* devuelve 0 en caso de éxito, -1 en caso de fracaso */
    public int sendHandshake(String encoded) {
        // abrir conexion socket
        try {
            // envir handshake
            byte[] handshakeMsg = generateHandshake(encoded);
            outToPeer.write(handshakeMsg, 0, handshakeMsg.length);
            outToPeer.flush();
        } catch (IOException ex) {
            System.err.println("error: could not initiate connection");
            return -1;
        }

        return 0;
    }

    /* receiveHandshake: recibir, verificar, responder al patrón de apretón de manos */
    /* devuelve 0 en caso de éxito, -1 en caso de fracaso */
    public int receiveHandshake(String encoded) {
        if (inFromPeer == null || outToPeer == null) {
            System.err.println("error: receiveHandshake found null socket");
            return -1;
        }

        // lee mensaje peer handshake  (blocking), compara con el esperado
        byte[] peerHandshakeMsg = new byte[HANDSHAKE_SIZE];
        try {
            // continuar leyendo hasta que se lea el apretón de manos completo
            int numRead = 0;
            while (numRead < HANDSHAKE_SIZE) {
                numRead += inFromPeer.read(peerHandshakeMsg, numRead, 
                                           HANDSHAKE_SIZE - numRead);
            }
        } catch (IOException ex) {
            System.err.println("error: failed to read entire handshake");
            return -1;
        }

        byte[] myHandshakeMsg = generateHandshake(encoded);
        if (myHandshakeMsg.length != peerHandshakeMsg.length) {
            return -1;
        }
        for (int i = 0; i < myHandshakeMsg.length - 20; ++i) {
            // nota: peerID no se comprueba aquí (consulte "- 20" más arriba)
            if (peerHandshakeMsg[i] != myHandshakeMsg[i]) {
                System.err.println("error: peer at " + getIP() 
                                   + " has wrong .torrent file");
                return -1;
            }
        }

        // initializa el lector del socket
        this.messageQ = new LinkedList<BitMessage>();
        this.reader = new BitReader(inFromPeer, messageQ);
        Thread t = new Thread(reader);
        t.start();

        return 0;
    }

    public byte[] generateHandshake(String encoded) {
        ByteBuffer handshakeMsg = ByteBuffer.allocate(HANDSHAKE_SIZE);

        // construcye el mensaje de 48-byte handshake 
        // (i) byte=19 seguido por el protocolo "BitTorrent "
        byte b = 19;
        handshakeMsg.put(b);
        try {
            handshakeMsg.put("BitTorrent protocol".getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return null;
        }
        // (ii) 8-byte extencion
        byte[] pad = new byte[8];
        for (int i = 0; i < 8; ++i) {
            pad[i] = 0;
        }
        handshakeMsg.put(pad);
        // (iii) 20-byte SHA1 codificación de metainfo codificada (diferente del protocolo)
        handshakeMsg.put(BitLibrary.getSHA1(encoded));
        // (iv) 20-byte peer ID (Codificación SHA1 de IP y puerto)
        handshakeMsg.put(peerID);
        handshakeMsg.flip();    // prepárando para escribir

        return handshakeMsg.array();
    }

    public Socket getSocket() {
        return peerSocket;
    }

    public InetAddress getIP() {
        return peerAddrPort.getAddress();
    }

    public int getPort() {
        return peerAddrPort.getPort();
    }

    public String getHostName() {
        return peerAddrPort.getHostName();
    }
}
