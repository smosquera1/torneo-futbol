package torneoFutbol;

import java.io.*;
import java.util.*;

public class main {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            // Cargar equipos desde archivo
            List<String> equipos = leerEquiposDesdeArchivo("equipos.txt");
            if (equipos.size() < 2) {
                System.out.println("Debe haber al menos 2 equipos en el archivo.");
                return;
            }

            List<String> resultados = new ArrayList<>();
            Map<String, Estadisticas> estadisticas = inicializarEstadisticas(equipos);

            // Mostrar el menú para seleccionar la fase inicial
            int faseSeleccionada = seleccionarFase(scanner);

            // Procesar el torneo desde la fase seleccionada
            procesarTorneoDesdeFase(equipos, resultados, estadisticas, faseSeleccionada);

            // Guardar resultados y estadísticas completas en archivos
            guardarResultados("resultados.txt", resultados);
            guardarEstadisticas("estadisticas.txt", estadisticas);
            System.out.println("¡Resultados y estadísticas guardados en los archivos!");
        } catch (IOException e) {
            System.err.println("Error al procesar archivos: " + e.getMessage());
        }
    }

    private static int seleccionarFase(Scanner scanner) {
        while (true) {
            System.out.println("\nSeleccione la fase inicial del torneo:");
            System.out.println("1. Octavos de final");
            System.out.println("2. Cuartos de final");
            System.out.println("3. Semifinales");
            System.out.println("4. Final");
            System.out.println("5. Salir");
            try {
                int opcion = Integer.parseInt(scanner.nextLine());
                if (opcion >= 1 && opcion <= 5) {
                    return opcion;
                }
                System.out.println("Por favor, selecciona una opción válida.");
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Ingresa un número del 1 al 5.");
            }
        }
    }

    private static void procesarTorneoDesdeFase(List<String> equipos, List<String> resultados, Map<String, Estadisticas> estadisticas, int faseInicial) {
        String[] etapas = {"Octavos de final", "Cuartos de final", "Semifinales", "Final"};
        int partidos = equipos.size() / 2;

        // Ajustar el número de partidos dependiendo de la fase inicial
        for (int i = 1; i < faseInicial; i++) {
            partidos /= 2;
        }

        for (int i = faseInicial - 1; i < etapas.length && partidos >= 1; i++) {
            System.out.println("\n--- " + etapas[i] + " ---");
            resultados.add("\n--- " + etapas[i] + " ---");
            equipos = jugarEtapa(equipos, resultados, estadisticas, partidos);
            partidos /= 2;

            if (equipos.size() == 1) {
                String campeon = equipos.get(0);
                System.out.println("\n¡El campeón del torneo es: " + campeon + "!");
                resultados.add("\n¡El campeón del torneo es: " + campeon + "!");
                estadisticas.get(campeon).setCampeon(true);
                break;
            }
        }
    }

    private static List<String> leerEquiposDesdeArchivo(String archivo) throws IOException {
        List<String> equipos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                equipos.add(linea.trim());
            }
        }
        if (equipos.isEmpty()) {
            throw new IOException("El archivo está vacío.");
        }
        return equipos;
    }

    private static Map<String, Estadisticas> inicializarEstadisticas(List<String> equipos) {
        Map<String, Estadisticas> estadisticas = new HashMap<>();
        for (String equipo : equipos) {
            estadisticas.put(equipo, new Estadisticas());
        }
        return estadisticas;
    }

    private static List<String> jugarEtapa(List<String> equipos, List<String> resultados, Map<String, Estadisticas> estadisticas, int partidos) {
        Random random = new Random();
        List<String> ganadores = new ArrayList<>();
        List<String[]> enfrentamientos = emparejarEquipos(equipos);

        for (int i = 0; i < partidos; i++) {
            String equipo1 = enfrentamientos.get(i)[0];
            String equipo2 = enfrentamientos.get(i)[1];
            String ganador = random.nextBoolean() ? equipo1 : equipo2;

            System.out.println("Partido " + (i + 1) + ": " + equipo1 + " vs " + equipo2 + " -> Ganador: " + ganador);
            resultados.add("Partido " + (i + 1) + ": " + equipo1 + " vs " + equipo2 + " -> Ganador: " + ganador);

            actualizarEstadisticas(estadisticas, equipo1, ganador.equals(equipo1));
            actualizarEstadisticas(estadisticas, equipo2, ganador.equals(equipo2));
            ganadores.add(ganador);
        }

        return ganadores;
    }

    private static List<String[]> emparejarEquipos(List<String> equipos) {
        List<String[]> enfrentamientos = new ArrayList<>();
        Collections.shuffle(equipos);
        for (int i = 0; i < equipos.size(); i += 2) {
            enfrentamientos.add(new String[]{equipos.get(i), equipos.get(i + 1)});
        }
        return enfrentamientos;
    }

    private static void actualizarEstadisticas(Map<String, Estadisticas> estadisticas, String equipo, boolean gano) {
        Estadisticas stats = estadisticas.get(equipo);
        stats.jugarPartido(gano);
    }

    private static void guardarResultados(String archivo, List<String> resultados) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            for (String resultado : resultados) {
                bw.write(resultado);
                bw.newLine();
            }
        }
    }

    private static void guardarEstadisticas(String archivo, Map<String, Estadisticas> estadisticas) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            bw.write("Estadísticas del Torneo:\n\n");
            for (Map.Entry<String, Estadisticas> entry : estadisticas.entrySet()) {
                bw.write(entry.getKey() + ":\n" + entry.getValue() + "\n\n");
            }
        }
    }
}

class Estadisticas {
    private int partidosJugados;
    private int partidosGanados;
    private boolean campeon;

    public void jugarPartido(boolean gano) {
        partidosJugados++;
        if (gano) partidosGanados++;
    }

    public void setCampeon(boolean esCampeon) {
        this.campeon = esCampeon;
    }

    @Override
    public String toString() {
        return "- Partidos jugados: " + partidosJugados +
               "\n- Partidos ganados: " + partidosGanados +
               (campeon ? "\n- ¡Campeón del torneo!" : "");
    }
}
