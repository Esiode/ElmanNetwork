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
 * Classe qui repr�sente le neural network et qui contient les m�thodes pour
 * l'entrainer et l'utiliser.
 *
 * @author Philippe Marcotte et Olivier Vincent
 */
public class ElmanNetwork {

    public BasicNetwork elmanNetwork = createElmanNetwork();
    private MLDataSet trainingSet = dataSet();
    private final int INPUT_NEURONS = 100;
    private final int HIDDEN_LAYER = 1;
    private final int OUTPUT_NEURONS = 1;
    double weights[][] = null;
    CSVNeuralDataSet data = new CSVNeuralDataSet("dataToCompute.csv",
            INPUT_NEURONS, 0, true);
    private MLDataSet dataSet = data;
    private ActivationGaussian activation = new ActivationGaussian();

    /*
     * Fonction qui crée la structure du Elman Network
     */
    private BasicNetwork createElmanNetwork() {
        // construct an Elman type network
        ElmanPattern pattern = new ElmanPattern();
        pattern.setActivationFunction(new ActivationGaussian());
        pattern.setInputNeurons(INPUT_NEURONS);
        pattern.addHiddenLayer(HIDDEN_LAYER);
        pattern.setOutputNeurons(OUTPUT_NEURONS);

        return (BasicNetwork) pattern.generate();
    }

    // Méthode qui permet d'utiliser les weights qui ont été déterminés (et
    // sauvegardés dans le fichier "NetworkStructure.ser") par les phases
    // d'entraînements antérieures. De plus, elle fait compute l'elman network
    // avec un data set d'input provenant du fichier dataToCompute.
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
//            for (int i = 0; i < 50; i++) {
//                
//
//                StringBuilder temp = new StringBuilder();
//                for (int k = 13; k < 20; k++) {
//                    //temp.append(computeResultat.charAt(k));
//                }
//
//                String tempCompute = temp.toString();
//                computeScore = Double.parseDouble(tempCompute);
//                sommeComputeResult = sommeComputeResult + computeScore;
//                
//
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // Fonction qui cr�e le training set � partir du fichier
    // dataTrainingSet.csv (pour l'ouvrir utiliser un editeur de texte comme
    // Notepad++ plutot qu'Excel)
    private MLDataSet dataSet() {
        CSVNeuralDataSet trainingData = new CSVNeuralDataSet("dataTrainingSet.csv", INPUT_NEURONS, OUTPUT_NEURONS, true);
        MLDataSet dataTrainingSet = trainingData;
        return dataTrainingSet;
    }

    // Fonction qui prend le training set et fait compute l'Elman network pour
    // ensuite compar� l'output avec ce qui �tait attendu (l'output id�al).
    // Les weights du neural network seront ensuite modifi�s en fonction du
    // score obtenu pour se rapprocher de ces outputs id�aux.
    private double trainNetwork(final String what, final BasicNetwork network,
            final MLDataSet trainingSet) {
        // train the neural network
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
            System.out.println("Training " + what + ", Itération #"
                    + iteration + " Error:" + trainMain.getError());
            iteration++;
        }
        return trainMain.getError();
    }

    // Fonction qui sert �commencer le training et � sauvegarder les weights
    // TODO M�thode pour effectuer plus d'un training et sauvegarder seulement
    // les meilleurs weights qui ont �t� utilis�.
    public void startTraining() {
        double elmanError = 0.0;
        double meilleureErreur = 100.0;
        double[] meilleureStructure = null;
        for (int i = 0; i < 1; i++) {
            elmanError = trainNetwork("Elman", elmanNetwork, trainingSet);
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
