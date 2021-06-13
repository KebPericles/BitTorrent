/* BitClient.java:  Cliente BitTorrent */

import java.nio.file.*;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.net.Socket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import util.bencode.*;        // interface para objetos bencoded
import util.lib.BitLibrary;   // Librerias con funciones del protocolo BitTorrent

/* BitClient: Maneja una sesion de conexion Bittorrent */
public class BitClient {
    private static final String TRNT_DIR = "./test/torrents/";
    private static final String DNLD_DIR = "./test/downloads/";
    private static final String UPLD_DIR = "./test/uploads/";
    private static final int MAX_UNCHOKED = 4;         // Solo unchoke 4 a lq vez
    private static final int SHA_LENGTH = 20;          // bytes en una SHA1 hash
    private static final int INT_LEN = 4;              // bytes en un entero
    private static boolean _DEBUG = false;             // Bandera debugging
    private static String encoded;                     // Bencoded .torrent file
    private static String infoBencoded;                // Bencoded info dict
    private static int fileLength = -1;                // Tamaño del archivo completo
    private static int pieceLength = -1;               // Tamaño de cada pieza
    private static int numPieces = -1;                 // num. de piezas del archivo
    private static Random random = null;               // Requerimiento por piezas random
    private static boolean[] localBitfield = null;     // Piesas que tiene el cliente
    private static String savePath = null;             // Locacion de guardado
    private static RandomAccessFile file = null;       // Archivo a transferir
    private static String[] pieces = null;             // SHA1 de piezas 
    private static String trackerURL = null;           // URL del tracker
    private static boolean isSeeder = false;           // El cliente tiene el archivo completo
    private static boolean runSlowly = false;          // Ejecucion lenta para testing (-z slow)
    private static int welcomePort = 6789;             // Puerto de bienvenida
    private static BitWelcomer welcomer = null;        // Bienvenida a nuevos peers
    private static LinkedList<Socket> welcomeQ = null; // Peers a la espera de conexion 
    private static ArrayList<BitPeer> peerList = null; // Peers conectados
    private static int numUnchoked = -1;

    public static void main(String[] args) {
        ByteBuffer lenBuf = ByteBuffer.allocate(INT_LEN);
        BitMessage unchoke = new BitMessage(BitMessage.MessageType.UNCHOKE);
        // obtener la configuración del cliente desde la línea de comandos, incluida peerList
        if (parseArgs(args) == -1) {
            return;
        }

        // analizar la metainformación del archivo .torrent
        if (initClient() == -1) {
            return;
        }
        // Inicialización: fileLength, pieceLength, file, pieces,
        // welcomer, infoBencoded
        logOutput(BitLibrary.getTimeString() + ": PARSED .TORRENT INFO");
        logOutput("\t   LOCATION OF FILE " + savePath);
        logOutput("\t   FILE OF LENGTH " + fileLength);
        logOutput("\t   PCS. OF LENGTH " + pieceLength);
        logOutput("\t   INIT BITFIELD  " + BitLibrary.getBitString(localBitfield));
        logOutput(BitLibrary.getTimeString() 
                  + ": LISTENING ON PORT " + welcomePort);

        // Abrir conexion y enviar handshakes a todos los peers
        Iterator<BitPeer> it = peerList.iterator();
        while (it.hasNext()) {
            BitPeer peer = it.next();
            if (peer.connect() == -1) {
                it.remove();
                continue;
            }
            peer.sendHandshake(infoBencoded);
            BitMessage bfmsg = new BitMessage(BitMessage.MessageType.BITFIELD,
                                       BitLibrary.booleanToBits(localBitfield));
            sendMessage(peer, bfmsg);
            logOutput(BitLibrary.getTimeString() + ": HANDSHAKE INITIALIZED");
            peer.receiveHandshake(infoBencoded);
            logOutput(BitLibrary.getTimeString() + ": HANDSHAKE COMPLETE");
        }

        // random unchoke a cuatro peers (todos los pares si <= 4 están conectados)
        if (peerList.size() <= MAX_UNCHOKED) {
            for (BitPeer peer : peerList) {
                sendMessage(peer, unchoke);
                peer.remoteIsChoked = false;
            }
            numUnchoked = peerList.size();
        } else {
            Set<Integer> toUnchoke 
                    = BitLibrary.getRandomSet(MAX_UNCHOKED, 0, peerList.size());
            for (Integer i : toUnchoke) {
                sendMessage(peerList.get(i), unchoke);
                peerList.get(i).remoteIsChoked = false;
            }
            numUnchoked = MAX_UNCHOKED;
        }

        while (true) {
            // qceptar nueva conexion de peers (solo si)
            synchronized (welcomeQ) {
                // Evitar ocupado sin peers
                while (welcomeQ.isEmpty() && peerList.isEmpty()) {
                    try {
                        logOutput(BitLibrary.getTimeString() 
                                  + ": WAITING FOR PEERS");
                        welcomeQ.wait();
                    } catch (InterruptedException ex) {
                    }
                }
                // limpiar la cola vacía aceptando nuevos peers
                while (!welcomeQ.isEmpty()) {
                    Socket peerSocket = welcomeQ.poll();
                    BitPeer peer = new BitPeer(peerSocket);
                    
                    if (peer.receiveHandshake(infoBencoded) == 0) {
                        // agregar a peerList
                        logOutput(BitLibrary.getTimeString() + ": ADDED PEER AT "
                                  + peer.getIP());
                        peerList.add(peer);
                        // completar handshake
                        peer.sendHandshake(infoBencoded);
                        logOutput(BitLibrary.getTimeString() 
                                  + ": COMPLETED HANDSHAKE WITH "+peer.getIP());
                        // enviar bitfield
                        BitMessage bitfieldMsg 
                               = new BitMessage(BitMessage.MessageType.BITFIELD,
                                       BitLibrary.booleanToBits(localBitfield));
                        sendMessage(peer, bitfieldMsg);
                        // unchoke si hay lugares disponibles
                        if (numUnchoked < MAX_UNCHOKED) {
                            peer.remoteIsChoked = false;
                            sendMessage(peer, unchoke);
                            numUnchoked++;
                        }
                    }
                    
                }
            }

            // procesar un mensaje para cada peer
            for (BitPeer peer : peerList) {
                BitMessage msg = peer.getNextMessage();
                if (msg == null) {
                    continue;
                }

                // analizar el tipo de mensaje y procesarlo en consecuencia
                logOutput(BitLibrary.getTimeString() + ": RECEIVED MESSAGE TYPE "
                                     + msg.getType() + " FROM " + peer.getIP());
                peer.updateLastUsed();
                if (msg.getType() == BitMessage.MessageType.KEEP_ALIVE) {
                    // ya actualizado por última vez
                } else if (msg.getType() == BitMessage.MessageType.CHOKE) {
                    logDebug("CHOKE Message");
                    peer.localIsChoked = true;
                } else if (msg.getType() == BitMessage.MessageType.UNCHOKE) {
                    logDebug("UNCHOKE Message");
                    peer.localIsChoked = false;
                } else if (msg.getType() == BitMessage.MessageType.INTERESTED) {
                    logDebug("INTERESTED Message");
                    peer.remoteIsInterested = true;
                } else if (msg.getType() == BitMessage.MessageType.UNINTERESTED) {
                    logDebug("UNINTERESTED Message");
                    peer.remoteIsInterested = false;
                } else if (msg.getType() == BitMessage.MessageType.HAVE) {
                    peer.addToBitfield(msg.getIndex());
                    logOutput(BitLibrary.getTimeString() 
                              + ": PEER " + peer.getIP()
                              + " HAS " 
                              + BitLibrary.getBitString(peer.getBitfield()));
                    // di interested si no tenemos esta pieza
                    if (localBitfield[msg.getIndex()] == false) {
                        sendMessage(peer,
                            new BitMessage(BitMessage.MessageType.INTERESTED));
                    } else if (BitLibrary.isAllTrue(peer.getBitfield())) {
                        // hacer espacio para otros si el peer ahora es seeder
                        if (peer.remoteIsChoked == false) {
                            peer.remoteIsChoked = true;
                            sendMessage(peer, new BitMessage(BitMessage.MessageType.CHOKE));
                            --numUnchoked;
                        }
                    }
                } else if (msg.getType() == BitMessage.MessageType.BITFIELD) {
                    boolean[] bf = BitLibrary.bitsToBoolean(msg.getBitfield(), numPieces);
                    peer.setBitfield(bf);
                    logOutput(BitLibrary.getTimeString() 
                              + ": PEER " + peer.getIP()
                              + " HAS " 
                              + BitLibrary.getBitString(peer.getBitfield()));
                } else if (msg.getType() == BitMessage.MessageType.REQUEST) {
                    logDebug("REQUEST Message: Peer wants piece " + msg.getIndex());
                    if (peer.remoteIsChoked) {
                        logDebug("But peer is choked, not sending");
                    } else {
                        BitMessage reply = null;
                        // Asegurar que el cliente tenga la pieza 
                        if (localBitfield[msg.getIndex()] == false) {
                            // el peer tiene incorrecta bitfield info, enviar otra
                            logDebug("warning: peer incorrectly thinks we have " + msg.getIndex());
                            reply = new BitMessage(BitMessage.MessageType.BITFIELD,
                                           BitLibrary.booleanToBits(localBitfield));
                        // leer esta pieza del archivo
                        } else {
                            byte[] replyData = new byte[msg.getBlockLength()];
                            int numRead = 0;
                            try {
                                file.seek(msg.getBegin());
                                numRead = file.read(replyData, 0, msg.getBlockLength());
                                logDebug("Read " + numRead + " bytes from file");
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            reply = new BitMessage(BitMessage.MessageType.PIECE,
                                        msg.getIndex(), msg.getBegin(), replyData);
                        }

                        sendMessage(peer, reply);
                        logOutput(BitLibrary.getTimeString() 
                              + ": SENT PIECE " + msg.getIndex() 
                              + " TO " + peer.getIP());
                    }
                } else if (msg.getType() == BitMessage.MessageType.PIECE) {
                    if (localBitfield[msg.getIndex()]) {
                        logDebug("warning: received piece already had");
                        continue;
                    }
                    // buscar y escribir en el archivo
                    try {
                        file.seek(msg.getBegin());
                        file.write(msg.getBlock());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    // actualizar bitfield, enviar HAVE respuesta a TODOS los peers
                    localBitfield[msg.getIndex()] = true;
                    BitMessage haveMsg 
                                   = new BitMessage(BitMessage.MessageType.HAVE,
                                                    msg.getIndex());
                    for (BitPeer p : peerList) {
                        sendMessage(p, haveMsg);
                    }
                    logOutput(BitLibrary.getTimeString() + ": NOW HAVE "
                                + BitLibrary.getBitString(localBitfield));

                    // Convertirse en un seeder si se ha descargado todo 
                    if (BitLibrary.isAllTrue(localBitfield)) {
                        logOutput(BitLibrary.getTimeString() + ": DOWNLOAD COMPLETE");
                        logDebug("local bitfield " 
                                 + BitLibrary.getBitString(localBitfield));
                        isSeeder = true;
                    }
                } else if (msg.getType() == BitMessage.MessageType.CANCEL) {
                    // utilizado en el modo "End game" (Sin funcionar solo propuesta)
                } else {
                    throw new RuntimeException("Invalid MessageType received");
                }
            }
            // (ii): actualizar el estado de interesado
            for (BitPeer peer : peerList) {
                if (!peer.localIsInterested 
                    && peer.getRarePiece(localBitfield) > -1) {
                    peer.localIsInterested = true;
                    BitMessage msg 
                            = new BitMessage(BitMessage.MessageType.INTERESTED);
                    sendMessage(peer, msg);
                }
            }

            // (iii): Pedir todas las piezas de los unchoked peers
            if (!isSeeder) {    // falta al menos una pieza
                for (BitPeer peer : peerList) {
                    int index;
                    if (!peer.localIsChoked && peer.localIsInterested
                        && (index = peer.getRarePiece(localBitfield)) > -1
                        && !peer.outstandingRequests.contains(index)) {
                        int indexLength = pieceLength;
                        if (index ==numPieces-1 && fileLength%pieceLength > 0) {
                            indexLength = fileLength % pieceLength;
                        }
                        BitMessage request 
                                = new BitMessage(BitMessage.MessageType.REQUEST,
                                       index, index * pieceLength, indexLength);
                        peer.outstandingRequests.add(index);
                        sendMessage(peer, request);
                    }
                }
            }
            //insertar pausas para la depuración
            if (runSlowly) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {

                }
            }
        }
    }

    /* sendMessage:  enviar un BitMessage a un peer espesifico */
    public static void sendMessage(BitPeer peer, BitMessage msg) {
        byte[] packedMsg = msg.pack();
        peer.write(packedMsg, 0, packedMsg.length);
        // registrar evento de mensaje enviado
        StringBuilder sb = new StringBuilder();
        sb.append(BitLibrary.getTimeString() + ": SENT " + msg.getType());
        if (msg.getType() == BitMessage.MessageType.REQUEST
            || msg.getType() == BitMessage.MessageType.PIECE) {
            sb.append(" FOR " + msg.getIndex());
        }
        sb.append(" TO " + peer.getIP());
        logOutput(sb.toString());
    }

    /* parseArgs:crear guardar archivo para escribir, obtener metadata torrent   */
    /* return -1 si falla y 0 otro caso */
    public static int parseArgs(String[] args) {
        peerList = new ArrayList<BitPeer>();
        if (args.length == 0 || args.length % 2 == 0 
            || BitLibrary.hasStr(args, "-h")) {
            logError("Uso: java BitClient [FLAGS]* torrentFile");
            logError("\t-h         \t Desplegar información");
            logError("\t-s saveFile\t Espesificar locación");
            logError("\t-p IP:port \t Incluir esta direccion como un peer");
            logError("\t-v [on|off]\t Verbose on/off");
            logError("\t-w port    \t Puerto de bienvenida");
            logError("\t-x seed    \t Iniciar este cliente como seeder");
            logError("\t-z slow    \t Correr en slowmotion para testing");
            return -1;
        }

        for (int i = 0; i < args.length - 1; i += 2) {
            if (args[i].equals("-s")) {
                savePath = args[i+1];
            } else if (args[i].equals("-p")) {
                // agregar peers a la lista
                InetAddress peerAddr = null;
                int peerPort = 0;
                try {
                    int delimPos = args[i+1].indexOf(':', 0);
                    String ipString = args[i+1].substring(0, delimPos);
                    String portString = args[i+1].substring(delimPos + 1);

                    peerAddr = InetAddress.getByName(ipString);
                    peerPort = Integer.parseInt(portString);
                    peerList.add(new BitPeer(peerAddr, peerPort));
                } catch (UnknownHostException|NumberFormatException ex) {
                    logError("error: unknown IP:port " + args[i+1]);
                    return -1;
                }
                logDebug("Added Peer: IP = " + peerAddr + ", " 
                         + "Port = " + peerPort);
            } else if (args[i].equals("-v")) {
                if (args[i+1].equals("on")) {
                    _DEBUG = true;
                } else {
                    _DEBUG = false;
                }
            } else if (args[i].equals("-w")) {
                try {
                    welcomePort = Integer.parseInt(args[i+1]);
                } catch (NumberFormatException ex) {
                    logError("error: invalid welcome port " + args[i+1]);
                    return -1;
                }
            } else if (args[i].equals("-x")) {
                isSeeder = true;
                // Archivo a transferir al encontrar savePath
            } else if (args[i].equals("-z")) {
                runSlowly = true;
            }
        }
        /* Leer informacion del torrent*/
        try {
            String torrentName = TRNT_DIR + args[args.length - 1];
            byte[] torrentData = Files.readAllBytes(Paths.get(torrentName));
            encoded = new String(torrentData, "US-ASCII");
            encoded = encoded.trim();
        } catch (IOException ex) {
            logError("error: cannot open " + args[args.length - 1]);
            return -1;
        }

        return 0;
    }

    /* initClient:analizar los metadatos del archivo de  METAINFO */
    /* return: 0 exito, -1 fallo */
    /* success ==> initialized: fileLength, pieceLength, saveBuf, pieces */
    public static int initClient() {
        BObject[] metainfo = BDecoder.read(encoded);
        if (metainfo.length != 1) {
            logError("error: invalid .torrent file");
            return -1;
        }
        BDict metaDict = (BDict) metainfo[0];
        // (a) analizar el diccionario de información dentro de metaDict
        if (metaDict.containsKey("info")) {
            BDict infoDict = (BDict) metaDict.get("info");
            infoBencoded = infoDict.encode();

            // (i) campo de longitud
            BObject len = infoDict.get("length");
            if (len == null) {
                logError("error: invalid length in .torrent file");
                return -1;
            }
            fileLength = Integer.parseInt(len.print());
            logDebug("got fileLength " + fileLength);

            // (ii) Longitud pieza campo
            BObject plen = infoDict.get("piece length");
            if (plen == null) {
                logError("error: invalid piece length in .torrent file");
                return -1;
            }
            pieceLength = Integer.parseInt(plen.print());
            logDebug("got pieceLength " + pieceLength);
            numPieces = fileLength / pieceLength;
            if (fileLength % pieceLength > 0) {
                ++numPieces;
            }
            logDebug("got numPieces " + numPieces);

            // (iii) campo de nombre de guardado sugerido ==> guardar en DNLD_DIR / <sug_name>
            BObject sname = infoDict.get("name");
            if (sname != null && savePath == null) {    // -s flag sin usar
                savePath = DNLD_DIR + sname.print();
                logDebug("got savePath " + savePath);
            }

            // (iv) SHA1 valores de las piezas
            BObject sha = infoDict.get("pieces");
            if (sha == null) {
                logError("error: invalid SHA1 encoding of pieces");
                return -1;
            }
            String piecesSHA1 = sha.print();
            if (piecesSHA1.length() % SHA_LENGTH != 0) {
                logError("error: SHA1 length not divisible by 20");
                return -1;
            } else {
                //dividir los hash SHA1 en arrayList
                pieces = new String[piecesSHA1.length() / SHA_LENGTH];
                for (int i = 0; i < pieces.length; ++i) {
                    String s = piecesSHA1.substring(SHA_LENGTH * i, 
                                                    SHA_LENGTH * (i + 1));
                    byte[] hashData;
                    try {
                        hashData = s.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        ex.printStackTrace();
                        return -1;
                    }
                    pieces[i] = BitLibrary.bytesToHex(hashData);
                }
                if (_DEBUG) {
                    logDebug("Got the following SHA1 pieces:");
                    if (_DEBUG) {
                        for (int i = 0; i < pieces.length; ++i) {
                            logDebug(pieces[i]);
                        }
                    }
                }
            }

            // (v) bitfield
            localBitfield = new boolean[numPieces];
            if (isSeeder) {
                logDebug("I AM A SEEDER");
            }
            for (int i = 0; i < localBitfield.length; ++i) {
                localBitfield[i] = isSeeder;   // todo true seeder, else false
            }
        } else {
            logError("error: no info field specified in .torrent file");
            return -1;
        }
        // (b) obtener tracker URL
        BObject tracker = metaDict.get("announce");
        if (tracker != null) {
            trackerURL = tracker.print();
        }
        logDebug("got tracker URL " + trackerURL);
        // (c) initializar archivo torrent para leer/escribir
        if (savePath == null) {
            logError("error: no save location specified");  // .torrent no CLI
            return -1;
        }
        if (isSeeder) {
            // cambiar a UPLD_DIR por un seeder
            savePath = savePath.substring(savePath.lastIndexOf('/') + 1);
            savePath = UPLD_DIR + savePath;
            logDebug("Seeder now has savePath = " + savePath);
            // asegúrese de que el archivo exista y tenga la longitud adecuada
            File source = new File(savePath);
            if (!source.isFile()) {
                logError("error: seeder does not have " + savePath);
                return -1;
            } else if (source.length() != fileLength) {
                logError("error: file length differs from torrent file specs");
                return -1;
            }
            try {
                file = new RandomAccessFile(source, "r");
                logDebug("Seeder opened file at " + source);
            } catch (IOException ex) {
                logError("error: seeder could not open " + savePath);
                return -1;
            }
        } else {
            try {
                file = new RandomAccessFile(savePath, "rw");
                file.setLength(fileLength);
                logDebug("Leecher opened new file at " + savePath);
            } catch (IOException ex) {
                logError("error: client could not open " + savePath);
                return -1;
            }
        }
            
        // (d) configurar hilo de bienvenida
        welcomeQ = new LinkedList<Socket>();
        welcomer = new BitWelcomer(welcomePort, welcomeQ);
        welcomer.start();

        return 0;
    }

    public static void logError(String str) {
        System.err.println(str);
    }

    public static void logDebug(String str) {
        if (_DEBUG) {
            System.err.println(str);
        }
    }

    public static void logOutput(String str) {
        System.out.println(str);
    }
}
