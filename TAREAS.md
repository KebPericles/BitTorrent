# Cliente
- Crear un nuevo cliente (peer)
  - Menu
  - Hilo de escucha (comunicación tracker, nuevos peers, etc)
  - Clase TorrentHandler (hilo) (para descargar y subir archivo/torrent) para cada archivo/torrent
    - Detalles del archivo/torrent, incluyendo el estado del archivo, si es seeder o leecher, las partes que tiene
    - Lista de peers
      - Si es seeder, lista de peers que están descargando el archivo
      - Si es leecher, lista de peers que están subiendo el archivo

## Estado de la red
Hace una consulta al tracker para obtener el estado de la red. El tracker le responde con la lista de peers que están conectados y los archivos que están compartiendo.

Debe mostrar:
- Número de peers conectados (total)
- Lista de nodos con lista de archivos que comparte cada uno

# Tracker
- Hilo de escucha
  - Nuevos clientes
  - Estado de la red
  - Lista de peers
- Hilo para cada cliente que consulte cada `x` tiempo el estado del peer
  - Lista de archivos que comparte
  - Lista de archivos que está descargando