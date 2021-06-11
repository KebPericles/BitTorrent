# BitTorrent
 Proyecto final Sistemas Distribuidos Programa

'javac ./*.java ./util/lib/*.java ./util/bencode/*.java'

Seeder: java BitClient -z slow -x seed -w 7777 hubble.jpg.torrent
java BitClient -z slow -p localhost:7777 -w 6666 -s ./test/downloads/hubbleChameleon.jpg hubble.jpg.torrent
java BitClient -z slow -w 7777 -p 127.0.0.1:7777 -p 127.0.0.1:6666 -s ./test/downloads/hubbleDolphin.jpg hubble.jpg.torrent 
