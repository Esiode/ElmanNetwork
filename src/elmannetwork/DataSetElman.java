package elmannetwork;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
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
     * ArrayList qui contient les appels systèmes triés pour facilité leur
     * traitement.
     */
    ArrayList<String> trace = new ArrayList<String>();
    /**
     * Gérénateur aléatoire simple.
     */
    private Random rnd = new Random();
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
    private String docSysCall;
    /**
     * Liste des appels système une fois convertis en chaîne de code "ASCII".
     */
    private ArrayList<Double> listeSysCall = new ArrayList<Double>();
    /**
     * Plus grande valeur possible des appels systèmes convertit en chiffre.
     * Ceci ne fait que servir pour la normalisation.
     */
    private double dataHigh = 180.0;
    /**
     * Plus petite valeur possible des appels systèmes convertit en chiffre.
     * Ceci ne fait que servir pour la normalisation.
     */
    private double dataLow = -41.0;
    /**
     * Borne maximum de la normalisation.
     */
    private double normalisationMax = 0.9;
    /**
     * Borne minimale de la normalisation.
     */
    private double normalisationMin = -0.9;

    /**
     * Constructeur de la classe qui crée un dataset à partir d'une série
     * d'appels systèmes storés dans un fichier. Depuis ces commandes il est
     * possible de créer un dataset d'entrainement (Trainingset) ou un dataset à
     * évaluer (Computeset).
     */
    public DataSetElman() {
        menu();

    }

    private void menu() {
        boolean continuer = false;
        int rep = 0;
        while (!continuer) {
            try{
            System.out.println("Créer un set d'entraînement ou un set de test? (1 ou 2 respectivement ou 3 pour aller à l'entraînement directement)");
            rep = lire.nextInt();
            }catch(InputMismatchException er){
                lire.next();
            }
            switch (rep) {
                case 1:
                    System.out.println("Quel fichier utiliser pour les appels systèmes? (nom du fichier sans extension)");
                    docSysCall = read.nextLine();
                    traces();
                    creationSetEntrainement(trace);
                    System.out.println("Faire autre chose? (true pour passer à l'entraînement et false pour revenir au début)");
                    continuer = lire.nextBoolean();
                    break;
                case 2:
                    System.out.println("Quel fichier utiliser pour les appels systèmes? (nom du fichier sans extension)");
                    docSysCall = read.nextLine();
                    traces();
                    creationSetTest(trace);
                    System.out.println("Faire autre chose? (true pour passer à l'entraînement et false pour revenir au début)");
                    continuer = lire.nextBoolean();
                    break;
                case 3:
                    continuer = true;
                    break;
                default:
                    System.out.println("Ceci ne fait pas partit des choix");
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
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(docSysCall + ".txt"));
            String[] traceBrute = null;
            StringBuffer sysCall = new StringBuffer();
            String sysCalls = in.readLine();
            while (sysCalls != null) {
                sysCall.append(sysCalls);
                sysCalls = in.readLine();
                System.out.println(sysCall);
            }
            String traces = sysCall.toString();
            System.out.println("Quel taux de pollution?");
            int pollution = lire.nextInt();
            double compteurAnomalie = 0;
            traceBrute = traces.split(",");
            /*
             Filtrage de l'appel système "clock_gettime", considéré
             comme inoffensif et qui est présent en trop grande quantité.
             Son trie ne fait qu'alléger la série d'appels systèmes.
             */
            for (int i = 0; i < traceBrute.length; i++) {
                if (!traceBrute[i].equalsIgnoreCase("clock_gettime")) {
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
        } catch (FileNotFoundException ex) {
            System.out.println("Ce fichier n'existe pas");
            menu();
        } catch (IOException ex) {
            Logger.getLogger(DataSetElman.class.getName()).log(Level.SEVERE,
                    null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(DataSetElman.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * Méthode servant à convertir chaque appel système en une suite d'addition
     * et de soustraction des code ASCII de chaque caractères de l'appel
     * système.
     *
     * @param listeSysCall ArrayList contenant les appels systèmes convertis.
     */
    private void conversionAppelSystème(ArrayList<String> trace) {
        for (String appelSysteme : trace) {
            double sysCallCode = 0;
            /*
             Conversion en chaîne ASCII des appels système.
             */
            for (int k = 0; k < appelSysteme.length(); k++) {
                if (k % 2 == 0) {
                    double codeASCII = appelSysteme.charAt(k);
                    sysCallCode = sysCallCode + codeASCII;
                } else {
                    double codeASCII = appelSysteme.charAt(k);
                    sysCallCode = sysCallCode - codeASCII;
                }
            }
            listeSysCall.add(sysCallCode);
        }
    }

    /**
     * Création du Dataset de training. Méthode qui prend les appels systèmes,
     * les transforme en code chiffré et les écrits sous forme d'inputs valides
     * pour le network dans un fichier .csv.
     *
     * @param trace ArrayList de String contenant les appels systèmes filtrés.
     */
    public void creationSetEntrainement(ArrayList<String> trace) {
        listeSysCall.clear();

        conversionAppelSystème(trace);

        FileWriter out = null;
        int compteur = 0;
        double compteurAnomalie = 0;
        try {
            System.out.println("Voulez-vous commencer un nouveau training ou ajouter à celui existant? (creer ou ajouter)");
            String creerOuAjouter = read.nextLine();
            if (creerOuAjouter.equalsIgnoreCase("creer")) {
                out = new FileWriter("setEntrainement.csv");
            } else if (creerOuAjouter.equalsIgnoreCase("ajouter")) {
                out = new FileWriter("setEntrainement.csv", true);
            }
            out.write("\n");
            System.out.println("Cette séquence est normale ou anormale? (1 pour normale et 0 pour anormale)");
            int indiceNormalite = lire.nextInt();
            for (int i = 0; i < listeSysCall.size(); i++) {
                compteur++;
                double sysCallAEcrire = listeSysCall.get(i);

                if (compteur % INPUT == 0) {
                    out.write(normalisation(sysCallAEcrire) + ","
                            + indiceNormalite + "\n");
                    if (compteur != 0) {
                        i = i - (INPUT - 1);
                    }
                } else {
                    out.write(normalisation(sysCallAEcrire) + ",");
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
    }

    /**
     * Fonction qui normalise les appels système entre 0 et 1.
     *
     * @param x Un appel système en chaîne ASCII à normaliser.
     * @return Cet appel système normaliser entre 0 et 1.
     */
    private double normalisation(double x) {
        return ((x - dataLow) / (dataHigh - dataLow))
                * (normalisationMax - normalisationMin) + normalisationMin;
    }

    /**
     * Méthode qui crée un Dataset d'évaluation (computeDataSet). Elle converti
     * les appels système à évaluer en chaîne ASCII et les normalise. Dans
     * l'application, les séries d'appels systèmes seront reçues par une liste
     * (ArrayList) et non d'un fichier texte. Cette méthode devra être modifiée
     * pour fonctionner dans le cadre de l'application.
     *
     * @param trace Liste contenant tous les appels système.
     */
    public void creationSetTest(ArrayList<String> trace) {
        listeSysCall.clear();

        conversionAppelSystème(trace);

        FileWriter out = null;

        int compteur = 0;
        try {
            out = new FileWriter("test.csv");
            out.write("\n");
            double compteurAnomalie = 0;
            for (int i = 0; i < listeSysCall.size(); i++) {
                compteur++;
                double sysCallAEcrire = listeSysCall.get(i);
                if (compteur % INPUT == 0) {
                    out.write(sysCallAEcrire + "");
                    if (compteur != 0) {
                        i = i - (INPUT - 1);
                    }
                } else {
                    out.write(normalisation(sysCallAEcrire) + ",");
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
