// Daniel Velázquez Lara A01636246

import java.io.*;
import java.util.*;

public class CYK {

    public static String palabra;
    public static String simboloInicial;

    public static ArrayList<String> terminal = new ArrayList<String>();
    public static ArrayList<String> noTerminal = new ArrayList<String>();

    // Estrucutra parecida a un hashmap, para mejorar busqueda
    public static TreeMap<String, ArrayList<String>> gramatica = new TreeMap<>();

    public static boolean esPalabraTerminal = false;

    public static Scanner openFile(String file) {
        try {
            return new Scanner(new File(file));
        } catch (FileNotFoundException e) {
            System.out.println("Error: No se encontro el archivo: " + file + "!");
            System.exit(1);
            return null;
        }
    }

    public static void procesarGramatica(String[] args) {
        Scanner sc = openFile(args[0]);
        ArrayList<String> tmp = new ArrayList<>();
        int line = 2;

        palabra = obtenerPalabra(args);
        simboloInicial = sc.next();
        sc.nextLine();

        while (sc.hasNextLine() && line <= 3) {
            tmp.addAll(Arrays.<String>asList(toArray(sc.nextLine())));
            if (line == 2) {
                terminal.addAll(tmp);
            }
            if (line == 3) {
                noTerminal.addAll(tmp);
            }
            tmp.clear();
            line++;
        }

        while (sc.hasNextLine()) {
            tmp.addAll(Arrays.<String>asList(toArray(sc.nextLine())));
            String leftSide = tmp.get(0);
            tmp.remove(0);
            gramatica.put(leftSide, new ArrayList<String>());
            gramatica.get(leftSide).addAll(tmp);
            tmp.clear();
        }
        sc.close();
    }

    public static String obtenerPalabra(String[] args) {
        if (!esPalabraTerminal) {
            return args[1];
        }
        String[] sinPalabraTerminal = new String[args.length - 1];
        for (int i = 1; i < args.length; i++) {
            sinPalabraTerminal[i - 1] = args[i];
        }
        return toString(sinPalabraTerminal);
    }

    // Imprimir procedimeintos
    public static void imprimirResultado(String[][] tablaCYK) {
        System.out.println("Word: " + palabra);
        System.out.println("\nG = (" + terminal.toString().replace("[", "{").replace("]", "}") + ", "
                + noTerminal.toString().replace("[", "{").replace("]", "}") + ", P, " + simboloInicial
                + ")\n\nProducciones(Derivaciones) P:");
        for (String s : gramatica.keySet()) {
            System.out.println(
                    s + " -> " + gramatica.get(s).toString().replaceAll("[\\[\\]\\,]", "").replaceAll("\\s", " | "));
        }
        System.out.println("\nResultado CYK:\n");
        dibujarTabla(tablaCYK);
    }

    public static void dibujarTabla(String[][] tablaCYK) {
        int l = encontrarStringMasLargo(tablaCYK) + 2;
        String formatString = "| %-" + l + "s ";
        String s = "";
        StringBuilder sb = new StringBuilder();

        // Consturir tabla
        sb.append("+");
        for (int x = 0; x <= l + 2; x++) {
            if (x == l + 2) {
                sb.append("+");
            } else {
                sb.append("-");
            }
        }
        String barra = sb.toString();
        sb.delete(0, 1);
        String derecha = sb.toString();

        // Print Table
        for (int i = 0; i < tablaCYK.length; i++) {
            for (int j = 0; j <= tablaCYK[i].length; j++) {
                System.out.print((j == 0) ? barra : (i <= 1 && j == tablaCYK[i].length - 1) ? "" : derecha);
            }
            System.out.println();
            for (int j = 0; j < tablaCYK[i].length; j++) {
                s = (tablaCYK[i][j].isEmpty()) ? "-" : tablaCYK[i][j];
                System.out.format(formatString, s.replaceAll("\\s", ","));
                if (j == tablaCYK[i].length - 1) {
                    System.out.print("|");
                }
            }
            System.out.println();
        }
        System.out.println(barra + "\n");

        // 4. Evaluar si es correcto
        if (tablaCYK[tablaCYK.length - 1][tablaCYK[tablaCYK.length - 1].length - 1].contains(simboloInicial)) {
            System.out.println("La palabra " + palabra + " SI es un elemento de la GLC G");
        } else {
            System.out.println("La palabra " + palabra + " NO es un elemento de la GLC G");
        }
    }

    public static int encontrarStringMasLargo(String[][] cykTable) {
        int x = 0;
        for (String[] s : cykTable) {
            for (String d : s) {
                if (d.length() > x) {
                    x = d.length();
                }
            }
        }
        return x;
    }

    // Creación de la tabla CYK
    public static String[][] crearTablaCYK() {

        int length = esPalabraTerminal ? toArray(palabra).length : palabra.length(); // Ternario

        String[][] tablaCYK = new String[length + 1][];
        tablaCYK[0] = new String[length];
        for (int i = 1; i < tablaCYK.length; i++) {
            tablaCYK[i] = new String[length - (i - 1)];
        }
        for (int i = 1; i < tablaCYK.length; i++) {
            for (int j = 0; j < tablaCYK[i].length; j++) {
                tablaCYK[i][j] = "";
            }
        }
        return tablaCYK;
    }

    public static String[][] doCyk(String[][] cykTable) {
        // 1.Crear header
        for (int i = 0; i < cykTable[0].length; i++) {
            cykTable[0][i] = modificarPalabra(palabra, i);
        }

        // 2. Hacer producciones para símbolos terminales con longitud 1
        for (int i = 0; i < cykTable[1].length; i++) {
            String[] validCombinations = esProductor(new String[] { cykTable[0][i] });
            cykTable[1][i] = toString(validCombinations);
        }
        if (palabra.length() <= 1) {
            return cykTable;
        }

        // 3. Hacer producciones para símbolos con longitud 2 o más
        for (int i = 0; i < cykTable[2].length; i++) {
            String[] downwards = toArray(cykTable[1][i]);
            String[] diagonal = toArray(cykTable[1][i + 1]);
            String[] validCombinations = esProductor(obtenerCombinaciones(downwards, diagonal));
            cykTable[2][i] = toString(validCombinations);
        }
        if (palabra.length() <= 2) {
            return cykTable;
        }

        // Producciones para símbolos con longitud n
        TreeSet<String> currentValues = new TreeSet<String>();

        for (int i = 3; i < cykTable.length; i++) {
            for (int j = 0; j < cykTable[i].length; j++) {
                for (int compareFrom = 1; compareFrom < i; compareFrom++) {
                    String[] downwards = cykTable[compareFrom][j].split("\\s");
                    String[] diagonal = cykTable[i - compareFrom][j + compareFrom].split("\\s");
                    String[] combinations = obtenerCombinaciones(downwards, diagonal);
                    String[] validCombinations = esProductor(combinations);
                    if (cykTable[i][j].isEmpty()) {
                        cykTable[i][j] = toString(validCombinations);
                    } else {
                        String[] oldValues = toArray(cykTable[i][j]);
                        ArrayList<String> newValues = new ArrayList<String>(Arrays.asList(oldValues));
                        newValues.addAll(Arrays.asList(validCombinations));
                        currentValues.addAll(newValues);
                        cykTable[i][j] = toString(currentValues.toArray(new String[currentValues.size()]));
                    }
                }
                currentValues.clear();
            }
        }
        return cykTable;
    }

    public static String modificarPalabra(String word, int position) {
        if (!esPalabraTerminal) {
            return Character.toString(word.charAt(position));
        }
        return toArray(word)[position];
    }

    public static String[] esProductor(String[] toCheck) {
        ArrayList<String> storage = new ArrayList<>();
        for (String s : gramatica.keySet()) {
            for (String current : toCheck) {
                if (gramatica.get(s).contains(current)) {
                    storage.add(s);
                }
            }
        }
        if (storage.size() == 0) {
            return new String[] {};
        }
        return storage.toArray(new String[storage.size()]);
    }

    public static String[] obtenerCombinaciones(String[] from, String[] to) {
        int length = from.length * to.length;
        int counter = 0;
        String[] combinations = new String[length];
        if (length == 0) {
            return combinations;
        }
        ;
        for (int i = 0; i < from.length; i++) {
            for (int j = 0; j < to.length; j++) {
                combinations[counter] = from[i] + to[j];
                counter++;
            }
        }
        return combinations;
    }

    public static String toString(String[] input) {
        return Arrays.toString(input).replaceAll("[\\[\\]\\,]", "");
    }

    public static String[] toArray(String input) {
        return input.split("\\s");
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Ingresar: java CYK <File> <Word>.");
            System.exit(1);
        } else if (args.length > 2) {
            esPalabraTerminal = true;
        }
        procesarGramatica(args);
        String[][] cykTable = crearTablaCYK();
        imprimirResultado(doCyk(cykTable));
    }

}
