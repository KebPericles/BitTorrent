package peer;

public class Menu {
        private static final String[] OPTIONS = {
                        "MOSTRAR ESTADO DE LA RED",
                        "COMPARTIR ARCHIVO",
                        "DESCARGAR ARCHIVO",
                        "GENERAR .TORRENT"
        };

        private static final String[] STATIC_OPTIONS = {
                        "CONFIGURACION",
                        "SALIR"
        };

        public static void loop() {
                int opcion = 0;

                do {
                        try {
                                System.out.println(" |");
                                System.out.println(" |--------MENU--------");
                                System.out.println(" |");
                                for (int i = 0; i < OPTIONS.length; i++) {
                                        System.out.println(" " + (i + 1) + ". " + OPTIONS[i]);
                                }

                                System.out.println(" |");
                                System.out.println(" 8. " + STATIC_OPTIONS[0]);
                                System.out.println(" 9. " + STATIC_OPTIONS[1]);

                                System.out.print("Ingrese una opcion: ");

                                opcion = Integer.parseInt(System.console().readLine());

                                System.out.println();

                                switch (opcion) {
                                        case 1:
                                                // TODO MOSTRAR ESTADO DE LA RED
                                                System.out.println("MOSTRAR ESTADO DE LA RED");
                                                break;
                                        case 2:
                                                // TODO COMPARTIR ARCHIVO
                                                System.out.println("COMPARTIR ARCHIVO");

                                                // CONSIDERAR GENERAR .TORRENT AUTOMATICAMENTE AL COMPARTIR

                                                break;
                                        case 3:
                                                // TODO DESCARGAR ARCHIVO
                                                System.out.println("DESCARGAR ARCHIVO");
                                                break;
                                        case 4:
                                                // TODO GENERAR .TORRENT
                                                System.out.println("GENERAR .TORRENT");
                                                break;
                                        case 8:
                                                Config.show();
                                                break;
                                        case 9:
                                                break;
                                        default:
                                                System.out.println("Opcion invalida");
                                                break;
                                }

                                System.out.println("\n");
                        } catch (NumberFormatException e) {
                                System.out.println("Opcion invalida");
                        }
                } while (opcion != 9);

                System.out.println("Saliendo...");
                System.out.println("Gracias por usar el programa");
        }
}
