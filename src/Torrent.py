from torrentool.api import Torrent #pip install torrentool


archivo = input("Inserta el archivo a convertir en torrent: \n") 
new_torrent= Torrent.create_from(archivo)
new_torrent.to_file(archivo + '.torrent')

# Leer archivo torrent
torrent=Torrent.from_file(archivo + '.torrent')
print(torrent.total_size)

print(torrent.magnet_link)