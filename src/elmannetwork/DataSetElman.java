package elmannetwork;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe qui crée un Dataset à partir d'un fichier contenant des appels
 * systèmes.
 *
 * @author Philippe Marcotte et Olivier Vincent
 */
public class DataSetElman {

    /**
     * Nombre d'inputs d'une séquence.
     */
    private final int INPUT = 100;
    /**
     * ArrayList qui contient les appels systèmes isolés et triés pour facilité
     * leur traitement.
     */
    ArrayList<String> trace = new ArrayList<String>();
    /**
     * Gérénateur aléatoire simple.
     */
    private Random rnd = new Random();
    private Scanner lr = new Scanner(System.in);
    private Scanner read = new Scanner(System.in);
    private String docToTrace;
    private ArrayList<Double> listeSysCall = new ArrayList<Double>();
    private static double dataHigh = 991081119910795103101116116105109101.0; //Représentation de l'appel système clock_gettime qui a la plus grande valeur en ASCII
    private static double dataLow = 11410197100.0; //Représentation de l'appel système read qui a la plus petite valeur en ASCII
    private static double normalizedHigh = 1.0;
    private static double normalizedLow = 0.0;
    private boolean indiceAnormale = false;
    private int indiceNormalite = 1;

    /**
     * Constructeur de la classe qui crée un dataset à partir d'une série
     * d'appels systèmes storés dans un fichier.
     */
    public DataSetElman() {

        boolean continuer = false;
        int rep = 0;
        while (!continuer) {
            System.out.println("Créer un trainingSet ou un computeSet?");
            rep = lr.nextInt();
            if (rep == 0) {
                System.out.println("Quel fichier to Trace?");
                docToTrace = read.nextLine();
                traces();
                createTrainingDataSet(trace);
                System.out.println("Faire autre chose?");
                continuer = lr.nextBoolean();
            } else if (rep == 1) {
                System.out.println("Quel fichier to Trace?");
                docToTrace = read.nextLine();
                traces();
                createComputeDataSet(trace);
                System.out.println("Faire autre chose?");
                continuer = lr.nextBoolean();
            } else if (rep == 2) {
                break;
            }

        }
    }

    /**
     * Méthode qui trie les appels systèmes (contenus dans un fichier) et les
     * organise facilité la création d'un dataset d'input. Méthode probablement
     * temporaire. Elle nous était nécessaire pour initialiser l'arrayList de
     * trace contenant les input.On se savait pas sous quelle forme on allait
     * obtenir les appels systèmes en réalité, alors cette méthode risque de
     * devoir être adaptée.
     */
    public void traces() {
        trace.clear();
        BufferedReader lire = null;
        try {
            lire = new BufferedReader(new FileReader(docToTrace + ".txt"));
            String[] traceBrute = null;
            StringBuffer sysCall = new StringBuffer();
            String sysCalls = lire.readLine();
            while (sysCalls != null) {
                sysCall.append(sysCalls);
                sysCalls = lire.readLine();
                System.out.println(sysCall);
            }
            String traces = sysCall.toString();
            traceBrute = traces.split(","); // Voir le fichier strace.txt, cette
            // action sépare les appels
            // systèmes entre eux.
            System.out.println("Quel taux de pollution?");
            int pollution = lr.nextInt();
            double compteurAnomalie = 0;
            for (int i = 0; i < traceBrute.length; i++) {
                if (!traceBrute[i].equalsIgnoreCase("clock_gettime")
                        && !traceBrute[i].equalsIgnoreCase("recvfrom")) {
                    if (i > 101) {
                        int sysCallRnd = rnd.nextInt(1000);
                        if (sysCallRnd < pollution) {
                            traceBrute[i] = "anomalie";
                            compteurAnomalie++;
                        }
                    }

                    trace.add(traceBrute[i]);
                }
            }
            System.out.println(trace.size());
            System.out.println(trace.get(0));
            double pourcentage = compteurAnomalie / trace.size() * 100;
            System.out.println(pourcentage);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DataSetElman.class.getName()).log(Level.SEVERE,
                    null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataSetElman.class.getName()).log(Level.SEVERE,
                    null, ex);
        } finally {
            try {
                lire.close();
            } catch (IOException ex) {
                Logger.getLogger(DataSetElman.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * Création du Dataset de training. Méthode qui prend les appels systèmes,
     * les transforme en code chiffré et les écrits sous forme d'inputs valides
     * pour le network dans un fichier .csv.
     *
     * @param trace ArrayList de String contenant les appels systèmes triés et
     * isolés.
     */
    public void createTrainingDataSet(ArrayList<String> trace) {
        listeSysCall.clear();
        int trim = trace.size() % 100;
        for (int i = 0; i < trace.size() - trim; i++) {
            StringBuffer sysCallContainer = new StringBuffer();
            for (int k = 0; k < trace.get(i).length(); k++) {
                int temp = trace.get(i).charAt(k);
                sysCallContainer.append(temp); // Crée un code, formé de la
                // somme des code ASCII de
                // chaque charactère qui
                // forment l'appel système, qui
                // représente l'appel système
            }
            String sysCallValue = sysCallContainer.toString();
            listeSysCall.add(Double.parseDouble(sysCallValue));
        }

        PrintWriter write = null;
        int compteur = 0;
        double compteurAnomalie = 0;
        try {
            write = new PrintWriter(new FileOutputStream("dataTrainingSet.csv"));
            write.println();
            for (int i = 0; i < listeSysCall.size(); i++) {
                compteur++;
                double sysCallToWrite = listeSysCall.get(i);

                if (sysCallToWrite == 9711011110997108105101.0) {
                    indiceAnormale = true;
                }
                if (compteur % INPUT == 0) {
                    if (indiceAnormale) {
                        indiceNormalite = 0;
                    }
                    write.println(normalize(sysCallToWrite) + ","
                            + indiceNormalite);
                    indiceAnormale = false;
                    indiceNormalite = 1;
                    if (compteur != 0) {
                        i = i - (INPUT - 1);
                    }
                } else {
                    write.print(normalize(sysCallToWrite) + ",");
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } finally {
            write.close();
        }
    }

    public static double normalize(double x) {
        return ((x - dataLow) / (dataHigh - dataLow))
                * (normalizedHigh - normalizedLow) + normalizedLow;
    }

    public void createComputeDataSet(ArrayList<String> trace) {
        listeSysCall.clear();
        int trim = trace.size() % 100;
        for (int i = 0; i < trace.size() - trim; i++) {
            StringBuffer sysCallContainer = new StringBuffer();
            for (int k = 0; k < trace.get(i).length(); k++) {
                int temp = trace.get(i).charAt(k);
                sysCallContainer.append(temp); // Crée un code, formé de la
                // somme des code ASCII de
                // chaque charactère qui
                // forment l'appel système, qui
                // représente l'appel système
            }
            String sysCallValue = sysCallContainer.toString();
            listeSysCall.add(Double.parseDouble(sysCallValue));
        }

        PrintWriter write = null;
        int compteur = 0;
        try {
            write = new PrintWriter(new FileOutputStream("dataToCompute.csv"));
            write.println();
            double compteurAnomalie = 0;
            for (int i = 0; i < listeSysCall.size(); i++) {
                compteur++;
                double sysCallToWrite = listeSysCall.get(i);

                if (compteur % INPUT == 0) {
                    write.println(normalize(sysCallToWrite) + ",0");
                    i = i - (INPUT - 1);
                } else {
                    write.print(normalize(sysCallToWrite) + ",");
                }

            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } finally {
            write.close();
        }
    }
}
