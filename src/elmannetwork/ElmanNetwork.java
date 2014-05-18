package elmannetwork;

import java.io.*;
import org.encog.Encog;
import org.encog.engine.network.activation.ActivationGaussian;
import org.encog.ml.CalculateScore;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.specific.CSVNeuralDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.*;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.structure.NetworkCODEC;
import org.encog.neural.networks.training.TrainingSetScore;
import org.encog.neural.networks.training.anneal.NeuralSimulatedAnnealing;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.neural.pattern.ElmanPattern;

/**
 * Classe qui représente le neural network et qui contient les méthodes pour
 * l'entrainer et l'utiliser. Soutenu par la librairie Encog.
 *
 * @author Philippe Marcotte et Olivier Vincent
 */
public class ElmanNetwork {
    /**
     * Le Elman network.
     */
    public BasicNetwork elmanNetwork = createElmanNetwork();
    /**
     * Le data set d'entrainement qui est utilisé pour ajuster les poids.
     */
    private MLDataSet trainingSet = dataSet();
    /**
     * Nombre d'input que prend l'algorithme à la fois.
     */
    private final int INPUT_NEURONS = 100;
    /**
     * Nombre de "Hidden layer" dans le Elman network.
     */
    private final int HIDDEN_LAYER = 1;
    /**
     * Nombre d'output qui ressort du Elman network à la fin de son traitement.
     */
    private final int OUTPUT_NEURONS = 1;
    /**
     * Matrice contenant les poids.
     */
    double weights[][] = null;
    /**
     * Fichier CSV regroupant les séquences d'appels système à évaluer.
     */
    CSVNeuralDataSet data = new CSVNeuralDataSet("dataToCompute.csv",
            INPUT_NEURONS, 0, true);
    private MLDataSet dataSet = data;
    private ActivationGaussian activation = new ActivationGaussian();
    /**
     * Construit un neural network de type Elman ayant comme fonction
     * d'activation par défaut la fonction gaussienne.
     * @return La structure du réseau Elman.
     */
    private BasicNetwork createElmanNetwork() {
        ElmanPattern pattern = new ElmanPattern();
        pattern.setActivationFunction(new ActivationGaussian());
        pattern.setInputNeurons(INPUT_NEURONS);
        pattern.addHiddenLayer(HIDDEN_LAYER);
        pattern.setOutputNeurons(OUTPUT_NEURONS);

        return (BasicNetwork) pattern.generate();
    }
    /** Méthode qui "compute" l'Elman network avec un data set d'appels
     * système provenant d'une liste ou (pour l'instant) d'un fichier texte.
     * Les poids ou "weights" enregistrées d'un training précédant seronts
     * reprises et utilisées pour évaluer chaque séquence.
     */
    public void computeNetwork() {
        try {
            double[] networkStructure = null;
            
            System.out.println("YOYOYOYOYOYOYOYOYO");
            System.out.println(elmanNetwork.classify(trainingSet.get(0).getInput()));
            System.out.println(elmanNetwork.getLayerBiasActivation(0));
            System.out.println(elmanNetwork.winner(trainingSet.get(0).getInput()));
            System.out.println(elmanNetwork.getLayerTotalNeuronCount(0));
            System.out.println("YOYOYOYOYOYOYOYOYO");
            FileInputStream fileIn = new FileInputStream("NetworkStructure.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            networkStructure = (double[]) in.readObject();
            for (double structure : networkStructure) {
                System.out.println(structure);
            }
            fileIn.close();
            in.close();
            NetworkCODEC.arrayToNetwork(networkStructure, elmanNetwork);
            System.out.println(elmanNetwork.dumpWeights());
            int index = 0;
            double sommeComputeResult = 0;
            double averageComputeResult = 0;
            double computeScore = 0;
            double minComputeScore = 100;
            double maxComputeScore = 0;
//            for (int i = 0; i < data.size(); i++) {
//                for (int j = 0; j < data.getInputSize(); j++) {
//                    dataSet.add(index, data.get(i).getInputArray()[j]);
//                    index++;
//                    
//                }
//                
//
//            }
            
            for(MLDataPair pair: trainingSet ) {
			final MLData output = elmanNetwork.compute(pair.getInput());
                        sommeComputeResult = sommeComputeResult + output.getData(0);
			System.out.println(pair.getInput().getData(0) + "," + pair.getInput().getData(1)
					+ ", actual=" + output.getData(0) +", ideal="+pair.getIdeal());
		}
            System.out.println(sommeComputeResult/dataSet.getRecordCount());
//            for (int i = 0; i < data.size(); i++) {
//                for (int j = 0; j < data.getInputSize(); j++) {
//                    dataSet.add(index, data.get(i).getInputArray()[j]);
//                    index++;
//                }
//            }
//            for (int i = 0; i < 50; i++) {
//                String computeResultat = elmanNetwork.compute(dataSet).toString();
//                StringBuilder temp = new StringBuilder();
//                for (int k = 13; k < 20; k++) {
//                    temp.append(computeResultat.charAt(k));
//                }
//                String tempCompute = temp.toString();
//                computeScore = Double.parseDouble(tempCompute);
//                sommeComputeResult = sommeComputeResult + computeScore;
//                if (minComputeScore > computeScore) {
//                    minComputeScore = computeScore;
//                }
//                if (maxComputeScore < computeScore) {
//                    maxComputeScore = computeScore;
//                    System.out.println("New max : " + maxComputeScore);
//                }
//                System.out.println(computeScore);
//            }
            averageComputeResult = sommeComputeResult / 50;
            double incertitudeScore = (maxComputeScore - minComputeScore) / 2;
            System.out.println(/*"Maximum = " + maxComputeScore + "\nMinimum = " + minComputeScore+"\n*/"Moyenne = " + averageComputeResult + ""/*"\nIncertitude = " + incertitudeScore*/);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    /**Fonction qui crée le training set à partir du fichier
     * dataTrainingSet.csv (pour l'ouvrir utiliser un editeur de texte comme
     * Notepad++ plutôt qu'Excel)
     * 
     * @return Un MLDataSet du data set d'entrainement contenu dans le fichier
     * dataTrainingSet.csv
     */
    private MLDataSet dataSet() {
        CSVNeuralDataSet trainingData = new CSVNeuralDataSet("dataTrainingSet.csv", INPUT_NEURONS, OUTPUT_NEURONS, true);
        MLDataSet dataTrainingSet = trainingData;
        return dataTrainingSet;
    }
    /**Fonction qui s'occupe des séences d'entrainements.
     * Compare les outputs des séquences d'appels systèmes qu'il calcule
     * avec l'output idéal. Il ajuste la valeur des poids en conséquence. 
     * 
     * @param network Le Elman network.
     * @param trainingSet Le set d'entrainement.
     * @return Le taux d'erreur de son entrainement.
     */ 
    private double trainNetwork(final BasicNetwork network,
            final MLDataSet trainingSet) {
        CalculateScore score = new TrainingSetScore(trainingSet);
        final MLTrain trainAlt = new NeuralSimulatedAnnealing(network, score,
                10, 2, 100);
        final MLTrain trainMain = new ResilientPropagation(network, trainingSet);
        final StopTrainingStrategy stop = new StopTrainingStrategy();
        trainMain.addStrategy(new Greedy());
        trainMain.addStrategy(new HybridStrategy(trainAlt));
        trainMain.addStrategy(stop);
        int iteration = 0;
        while (!stop.shouldStop()) {
            trainMain.iteration();
            System.out.println("Training Elman, Itération #"
                    + iteration + " Error:" + trainMain.getError());
            iteration++;
        }
        return trainMain.getError();
    }

    /**Méthode qui débute une séance d'entrainement.
     * En ce moment, cette action fait 50 séence d'entrainement et garde les
     * résultats de la meilleure séence d'entrainement.
     */
    public void startTraining() {
        double elmanError = 0.0;
        double meilleureErreur = 100.0;
        double[] meilleureStructure = null;
        for (int i = 0; i < 1; i++) {
            elmanError = trainNetwork(elmanNetwork, trainingSet);
            if (meilleureErreur > elmanError) {
                System.out.println("TROUVÉ UN MEILLEUR");
                System.out.println(elmanNetwork.dumpWeights());

                meilleureErreur = elmanError;
                meilleureStructure = NetworkCODEC.networkToArray(elmanNetwork);
                for (double yo : meilleureStructure) {
                    System.out.print(yo+",");
                }
            }

            } 
        System.out.println("Best error rate with Elman Network: " + meilleureErreur);
        sauvegarderNetwork(meilleureStructure);
        Encog.getInstance().shutdown();
        }
        
    
    /**Méthode qui sauvegarde la structure du network dans un fichier 
     * séréalisable.
     * 
     * @param networkStructure La structure du network.
     */
    private void sauvegarderNetwork(double[] networkStructure) {
        FileOutputStream fileOut;
        String fileName = "NetworkStructure";
        try {
            fileOut = new FileOutputStream((fileName + ".ser"));
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(networkStructure);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Generale I/O exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
