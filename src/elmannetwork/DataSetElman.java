package elmannetwork;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe qui crée un Dataset à partir d'un fichier texte contenant des appels
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
     * ArrayList qui contient les appels systèmes triés pour facilité
     * leur traitement.
     */
    ArrayList<String> trace = new ArrayList<String>();
    /**
     * Gérénateur aléatoire simple.
     */
    private Random rnd = new Random();
    private Scanner lr = new Scanner(System.in);
    /**
     * Scanner principal qui permet de lire les réponses de l'utilisateur.
     */
    private Scanner lire = new Scanner(System.in);
    /**
     * Scanner secondaire qui permet de lire les réponses de l'utilisateur 
     * lorsque le scanner principal est occupé.
     */
    private Scanner read = new Scanner(System.in);
    /**
     * String contenant le nom du fichier à tracer.
     */
    private String docToTrace;
    /**
     * Liste des appels système une fois convertis en chaîne de code "ASCII".
     */
    private ArrayList<Double> listeSysCall = new ArrayList<Double>();
    /**
     * Représentation de l'appel système clock_gettime, qui a la plus grande
     * valeur en ASCII. Ça sera la valeur maximum à normaliser.
     */
    private static double dataHigh = 991081119910795103101116116105109101.0; 
    /**
     * Représentation de l'appel système read qui a la plus petite valeur
     * en ASCII. Ça sera la valeur minimale à normaliser.
     */
    private static double dataLow = 11410197100.0;
    /**
     * Borne maximum de la normalisation.
     */
    private static double normalizedHigh = 0.9;
    /**
     * Borne minimale de la normalisation.
     */
    private static double normalizedLow = 0.1;
    /**
     * Devient true lorsqu'il y a une anomalie dans la séquence. 
     * Nous l'utilisons seulement pour faciliter les tests.
     */
    private boolean indiceAnormale = false;
    /**
     * Valeur par défaut du output idéal, s'il n'y a pas d'anomalies dans
     * la séquence. Devient 0 si la séquence contient une ou plusieurs anomalies.
     */
    private int indiceNormalite = 1;

    /**
     * Constructeur de la classe qui crée un dataset à partir d'une série
     * d'appels systèmes storés dans un fichier. Depuis ces commandes il est
     * possible de créer un dataset d'entrainement (Trainingset) ou un dataset
     * à évaluer (Computeset).
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
     * organise pour facilité la création d'un dataset d'input. Méthode 
     * probablement temporaire. Elle nous était nécessaire pour initialiser 
     * l'ArrayList de trace contenant les input.On se savait pas sous quelle 
     * forme on allait obtenir les appels systèmes en réalité, alors cette 
     * méthode risque de devoir être adaptée.
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
            traceBrute = traces.split(",");
            /*
            Trie des appels système "clock_gettime" et "recvfrom", considérés
            comme inoffensifs et qui sont présent en trop grande quantité.
            Leur trie ne fait qu'alléger la série d'appels systèmes.
            */
            for (int i = 0; i < traceBrute.length; i++) {
                if (!traceBrute[i].equalsIgnoreCase("clock_gettime")
                        && !traceBrute[i].equalsIgnoreCase("recvfrom")) {
                    if (i > 500) {
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
     * @param trace ArrayList de String contenant les appels systèmes triés.
     */
    public void createTrainingDataSet(ArrayList<String> trace){
        listeSysCall.clear();
        int trim = trace.size() % 100;
        for (int i = 0; i < trace.size() - trim; i++) {
            StringBuffer sysCallContainer = new StringBuffer();
            /*
            Conversion en chaîne ASCII des appels système.
            */
            for (int k = 0; k < trace.get(i).length(); k++) {
                int temp = trace.get(i).charAt(k);
                sysCallContainer.append(temp);
            }
            String sysCallValue = sysCallContainer.toString();
            listeSysCall.add(Double.parseDouble(sysCallValue));
        }

        FileWriter out = null;
        PrintWriter write = null;
        int compteur = 0;
        double compteurAnomalie = 0;
        try {
            System.out.println("Voulez-vous commencer un nouveau training ou ajouter à celui existant?");
            int isAppend = lr.nextInt();
            if(isAppend == 0){
                out = new FileWriter("dataTrainingSet.csv");
            } else if (isAppend == 1){
                out = new FileWriter("dataTrainingSet.csv",true);
            }
            out.write("\n");
            System.out.println("Cette séquence est normale ou anormale?");
            indiceAnormale = lr.nextBoolean();
            for (int i = 0; i < listeSysCall.size(); i++) {
                compteur++;
                double sysCallToWrite = listeSysCall.get(i);

//                if (sysCallToWrite == 9711011110997108105101.0) {
//                    indiceAnormale = true;
//                }
                if (compteur % INPUT == 0) {
                    if (indiceAnormale) {
                        indiceNormalite = 0;
                    }
                    out.write(normalize(sysCallToWrite) + ","
                            + indiceNormalite+"\n");
                   // indiceAnormale = false;
                    indiceNormalite = 1;
                    if (compteur != 0) {
                        i = i - (INPUT - 1);
                    }
                } else {
                    out.write(normalize(sysCallToWrite) + ",");
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(DataSetElman.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(DataSetElman.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        double pourcentage = compteurAnomalie/trace.size();
        System.out.println(pourcentage*100);
    }

    /** Fonction qui normalise les appels système entre 0 et 1.
     *
     * @param x Un appel système en chaîne ASCII à normaliser.
     * @return Cet appel système normaliser entre 0 et 1.
     */
    public static double normalize(double x) {
        return ((x - dataLow) / (dataHigh - dataLow))
                * (normalizedHigh - normalizedLow) + normalizedLow;
    }

    /**Méthode qui crée un Dataset d'évaluation (computeDataSet). Elle converti
     * les appels système à évaluer en chaîne ASCII et les normalise. Dans
     * l'application, les séries d'appels systèmes seront reçues par une
     * liste (ArrayList) et non d'un fichier texte. Cette méthode devra être
     * modifiée pour fonctionner dans le cadre de l'application.
     *
     * @param trace Liste contenant tous les appels système.
     */
    public void createComputeDataSet(ArrayList<String> trace) {
        listeSysCall.clear();
        int trim = trace.size() % 100;
        for (int i = 0; i < trace.size() - trim; i++) {
            StringBuffer sysCallContainer = new StringBuffer();
            /*
            Conversion en chaîne ASCII des appels système.
            */
            for (int k = 0; k < trace.get(i).length(); k++) {
                int temp = trace.get(i).charAt(k);
                sysCallContainer.append(temp);
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
                /*
                Gestion de la polution manuelle.
                */
//                if (sysCallRnd < 500) {
//                    sysCallToWrite = 9711011111410997108101.0;
//                    indiceAnormale = true;
//                    compteurAnomalie ++;
//                }
                if (compteur % INPUT == 0) {
                    write.println(normalize(sysCallToWrite));
                    i = i - (INPUT - 1);
                } else {
                    write.print(normalize(sysCallToWrite) + ",");
                }

            }
            System.out.println(compteurAnomalie*100/trace.size());
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } finally {
            write.close();
        }
    }
}
