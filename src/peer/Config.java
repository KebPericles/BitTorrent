package peer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import util.dotenv.Dotenv;

public class Config {
        public static boolean _DEBUG = true;

        private static final String CONFIG_FILE = ".env";
        private static Dotenv dotenv = Dotenv
                        .configure()
                        .filename(CONFIG_FILE)
                        .load();

        // --------- Llaves --------- //
        private static final String KEY_DIR_DESCARGAS = "DIR_DESCARGAS";
        private static final String KEY_DIR_TORRENTS = "DIR_TORRENTS";
        private static final String KEY_DIR_COMPARTIDOS = "DIR_COMPARTIDOS";
        private static final String KEY_DIR_TEMP = "DIR_TEMP";
        private static final String KEY_TRACKER_IP = "TRACKER_IP";
        private static final String KEY_TRACKER_PORT = "TRACKER_PORT";
        private static final String KEY_PEER_PORT = "PEER_PORT";

        // --------- Directorios --------- //
        private static String DIR_TORRENTS = "torrents/";
        private static String DIR_DESCARGAS = "descargas/";
        private static String DIR_COMPARTIDOS = "compartidos/";
        private static String DIR_TEMP = "temp/";

        // --------- Tracker --------- //
        // Configuración del tracker

        // Nota:
        // Se considera solo un tracker para la aplicación,
        // en caso de que se quiera usar más de un tracker
        // se debe modificar el código

        private static String TRACKER_IP = "";
        private static int TRACKER_PORT = 0;

        // --------- Peer --------- //
        // Configuración del peer
        /**
         * Puerto principal del peer
         */
        private static int PEER_PORT = 0;

        public static void load() {
                final String MSG_DIRENVNF = "Se usará el directorio por defecto: "; // Mensaje de directorio no
                                                                                    // encontrado
                final String MSG_ENVVARNF = "No se encontró la configuración de: "; // Mensaje de variable de entorno
                                                                                    // no encontrada

                // ------------ 1 Cargar directorios ------------ //
                // * Nota: se usa el operador ternario para que si no se encuentra la variable
                // * de entorno se use el directorio por defecto, esto a su vez porque el setter
                // * valida la existencia del directorio.

                if (dotenv.get(KEY_DIR_TORRENTS) == null) {
                        System.out.println(MSG_ENVVARNF + KEY_DIR_TORRENTS);
                        System.out.println(MSG_DIRENVNF + DIR_TORRENTS);
                }
                DIR_TORRENTS(dotenv.get(KEY_DIR_TORRENTS) != null
                                ? dotenv.get(KEY_DIR_TORRENTS)
                                : DIR_TORRENTS);

                if (dotenv.get(KEY_DIR_DESCARGAS) == null) {
                        System.out.println(MSG_ENVVARNF + KEY_DIR_DESCARGAS);
                        System.out.println(MSG_DIRENVNF + DIR_DESCARGAS);
                }
                DIR_DESCARGAS(dotenv.get(KEY_DIR_DESCARGAS) != null
                                ? dotenv.get(KEY_DIR_DESCARGAS)
                                : DIR_DESCARGAS);

                if (dotenv.get(KEY_DIR_COMPARTIDOS) == null) {
                        System.out.println(MSG_ENVVARNF + KEY_DIR_COMPARTIDOS);
                        System.out.println(MSG_DIRENVNF + DIR_COMPARTIDOS);
                } else {
                        DIR_COMPARTIDOS = dotenv.get(KEY_DIR_COMPARTIDOS);
                }
                DIR_COMPARTIDOS(dotenv.get(KEY_DIR_COMPARTIDOS) != null
                                ? dotenv.get(KEY_DIR_COMPARTIDOS)
                                : DIR_COMPARTIDOS);

                if (dotenv.get(KEY_DIR_TEMP) == null) {
                        System.out.println(MSG_ENVVARNF + KEY_DIR_TEMP);
                        System.out.println(MSG_DIRENVNF + DIR_TEMP);

                }
                DIR_TEMP(dotenv.get(KEY_DIR_TEMP) != null
                                ? dotenv.get(KEY_DIR_TEMP)
                                : DIR_TEMP);

                // ------------ 2 Cargar IP y puerto ------------ //

                if (dotenv.get(KEY_TRACKER_IP) == null) {
                        System.out.println(MSG_ENVVARNF + KEY_TRACKER_IP);

                        System.out.println("Ingrese la IP del tracker: ");
                        TRACKER_IP = System.console().readLine();
                } else {
                        TRACKER_IP = dotenv.get(KEY_TRACKER_IP);
                }
                Validator.validarIP(TRACKER_IP);

                if (dotenv.get(KEY_TRACKER_PORT) == null) {
                        System.out.println(MSG_ENVVARNF + KEY_TRACKER_PORT);
                }

                TRACKER_PORT = Validator.validarPuerto(
                                // * Nota: la función acepta un 0 para que se pida el puerto,
                                // * en caso contrario valida el puerto
                                dotenv.get(KEY_TRACKER_PORT) != null
                                                ? Integer.parseInt(dotenv.get(KEY_TRACKER_PORT))
                                                : 0);

                if (dotenv.get(KEY_PEER_PORT) == null) {
                        System.out.println(MSG_ENVVARNF + KEY_PEER_PORT);
                }
                PEER_PORT = Validator.validarPuerto(
                                // * Nota: la función acepta un 0 para que se pida el puerto,
                                // * en caso contrario valida el puerto
                                dotenv.get(KEY_PEER_PORT) != null
                                                ? Integer.parseInt(dotenv.get(KEY_PEER_PORT))
                                                : 0);

                show();
        }

        public static void save() {

                try (FileWriter configFile = new FileWriter(CONFIG_FILE)) {
                        BufferedWriter bufferedWriter = new BufferedWriter(configFile);

                        // ------------ 1 Guardar directorios ------------ //
                        writeString(bufferedWriter, KEY_DIR_TORRENTS, DIR_TORRENTS);
                        writeString(bufferedWriter, KEY_DIR_DESCARGAS, DIR_DESCARGAS);
                        writeString(bufferedWriter, KEY_DIR_COMPARTIDOS, DIR_COMPARTIDOS);
                        writeString(bufferedWriter, KEY_DIR_TEMP, DIR_TEMP);

                        // ------------ 2 Guardar IP y puerto ------------ //
                        writeString(bufferedWriter, KEY_TRACKER_IP, TRACKER_IP);
                        writeString(bufferedWriter, KEY_TRACKER_PORT, String.valueOf(TRACKER_PORT));
                        writeString(bufferedWriter, KEY_PEER_PORT, String.valueOf(PEER_PORT));

                        bufferedWriter.close();
                } catch (IOException e) {
                        System.err.println("Error al guardar la configuración");
                        e.printStackTrace();

                        System.exit(1);
                }

        }

        public static void show() {
                int maxKeyLength = Math.max(Math.max(DIR_TORRENTS.length(), DIR_DESCARGAS.length()), Math.max(DIR_COMPARTIDOS.length(), DIR_TEMP.length()));
                int separatorLength = maxKeyLength + 27; // 26 is the length of "| Configuración          | Valor             |"
                String separatorLine = "+" + "-".repeat(separatorLength) + "+";

                System.out.println(separatorLine);
                System.out.println("| Configuración          | Valor" + " ".repeat(separatorLength - 26 - 1) + "|");
                System.out.println(separatorLine);
                System.out.println("| Directorio de torrents | " + formatValue(DIR_TORRENTS, maxKeyLength) + " |");
                System.out.println("| Directorio de descargas| " + formatValue(DIR_DESCARGAS, maxKeyLength) + " |");
                System.out.println("| Directorio compartidos | " + formatValue(DIR_COMPARTIDOS, maxKeyLength) + " |");
                System.out.println("| Directorio temporal    | " + formatValue(DIR_TEMP, maxKeyLength) + " |");
                
                System.out.println("| IP del tracker         | " + formatValue(TRACKER_IP, maxKeyLength) + " |");
                System.out.println("| Puerto del tracker     | " + formatValue(String.valueOf(TRACKER_PORT), maxKeyLength) + " |");
                System.out.println("| Puerto del peer        | " + formatValue(String.valueOf(PEER_PORT), maxKeyLength) + " |");
                System.out.println(separatorLine);
        
        }

        private static String formatValue(String value, int width) {
                StringBuilder formattedValue = new StringBuilder(value);
                while (formattedValue.length() < width) {
                        formattedValue.append(" ");
                }
                return formattedValue.toString();
        }

        /**
         * Escribe una llave y un valor en un archivo .env
         * 
         * @param writer BufferedWriter para escribir en el archivo
         * @param key    llave
         * @param value  valor
         * @throws IOException
         */
        private static void writeString(BufferedWriter writer, String key, String value) throws IOException {
                writer.write(key + "=" + value);
                writer.newLine();
        }

        // --------- Getters --------- //
        public static String DIR_TORRENTS() {
                return DIR_TORRENTS;
        }

        public static String DIR_DESCARGAS() {
                return DIR_DESCARGAS;
        }

        public static String DIR_COMPARTIDOS() {
                return DIR_COMPARTIDOS;
        }

        public static String DIR_TEMP() {
                return DIR_TEMP;
        }

        public static String TRACKER_IP() {
                return TRACKER_IP;
        }

        public static int TRACKER_PORT() {
                return TRACKER_PORT;
        }

        public static int PEER_PORT() {
                return PEER_PORT;
        }

        // --------- Setters --------- //

        /**
         * Valida que el directorio exista, si no existe lo crea
         * 
         * @param dIR_TORRENTS directorio a validar
         */
        private static void DIR_TORRENTS(String dIR_TORRENTS) {
                DIR_TORRENTS = dIR_TORRENTS;
                Validator.validarDirectorio(dIR_TORRENTS);
        }

        /**
         * Valida que el directorio exista, si no existe lo crea
         * 
         * @param dIR_DESCARGAS directorio a validar
         */
        private static void DIR_DESCARGAS(String dIR_DESCARGAS) {
                DIR_DESCARGAS = dIR_DESCARGAS;
                Validator.validarDirectorio(dIR_DESCARGAS);
        }

        /**
         * Valida que el directorio exista, si no existe lo crea
         * 
         * @param dIR_COMPARTIDOS directorio a validar
         */
        private static void DIR_COMPARTIDOS(String dIR_COMPARTIDOS) {
                DIR_COMPARTIDOS = dIR_COMPARTIDOS;
                Validator.validarDirectorio(dIR_COMPARTIDOS);
        }

        /**
         * Valida que el directorio exista, si no existe lo crea
         * 
         * @param dIR_TEMP directorio a validar
         */
        private static void DIR_TEMP(String dIR_TEMP) {
                DIR_TEMP = dIR_TEMP;
                Validator.validarDirectorio(dIR_TEMP);
        }

        private static void TRACKER_IP(String tRACKER_IP) {
                TRACKER_IP = tRACKER_IP;
        }

        private static void TRACKER_PORT(int tRACKER_PORT) {
                TRACKER_PORT = tRACKER_PORT;
        }

        private static void PEER_PORT(int pEER_PORT) {
                PEER_PORT = pEER_PORT;
        }

}
