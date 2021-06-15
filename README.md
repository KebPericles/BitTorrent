# BitTorrent
 Proyecto final Sistemas Distribuidos 
 
 Integrantes:

Rosas Palacios Alan

Enriquez Melendez Jesus

Gamboa Del Angel Alan Eduardo

Compilar: 

'javac ./*.java ./util/lib/*.java ./util/bencode/*.java'

Ejecutar:

Seeder:  java BitClient -z slow -x seed -w 7777 hubble.jpg.torrent

Lecheer: java BitClient -z slow -p localhost:7777 -w 6666 -s ./test/downloads/hubbleChameleon.jpg hubble.jpg.torrent

Lecheer: java BitClient -z slow -w 7777 -p 127.0.0.1:7777 -p 127.0.0.1:6666 -s ./test/downloads/hubbleDolphin.jpg hubble.jpg.torrent


