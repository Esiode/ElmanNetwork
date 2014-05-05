package elmannetwork;

import java.io.*;
import java.text.*;
import java.util.ArrayList;
import java.util.Date;
import org.encog.Encog;
import org.encog.engine.network.activation.ActivationGaussian;
import org.encog.ml.CalculateScore;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.specific.CSVNeuralDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.*;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.structure.NetworkCODEC;
import org.encog.neural.networks.training.TrainingSetScore;
import org.encog.neural.networks.training.anneal.NeuralSimulatedAnnealing;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
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
    private final int INPUT_NEURONS = 10;
    private final int HIDDEN_LAYER = 1;
    private final int OUTPUT_NEURONS = 1;
    double weights[][] = null;
    CSVNeuralDataSet data = new CSVNeuralDataSet("dataToCompute.csv",
            INPUT_NEURONS, 0, true);
    private BasicMLData dataSet = new BasicMLData(data.size() * INPUT_NEURONS);

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
    // sauvegardés dans le fichier "weights.txt") par les phases
    // d'entraînements antérieures. De plus, elle fait compute l'elman network
    // avec un data set d'input provenant du fichier dataToCompute.
    public void computeNetwork() {
        try {
            double[] networkStructure = null;
            FileInputStream fileIn = new FileInputStream("NetworkStructure.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            networkStructure = (double[]) in.readObject();
            fileIn.close();
            in.close();
            NetworkCODEC.arrayToNetwork(networkStructure, elmanNetwork);
            int index = 0;
            double sommeComputeResult = 0;
            double averageComputeResult = 0;
            double computeScore = 0;
            double minComputeScore = 100;
            double maxComputeScore = 0;
            for (int i = 0; i < data.size(); i++) {
                for (int j = 0; j < data.getInputSize(); j++) {
                    dataSet.add(index, data.get(i).getInputArray()[j]);
                    //System.out.println(dataSet.getData(index));
                    index++;
                }
                String computeResultat = elmanNetwork.compute(dataSet).toString();

                StringBuilder temp = new StringBuilder();
                for (int k = 13; k < 20; k++) {
                    temp.append(computeResultat.charAt(k));
                }

                String tempCompute = temp.toString();
                computeScore = Double.parseDouble(tempCompute);
                sommeComputeResult = sommeComputeResult + computeScore;
                averageComputeResult = sommeComputeResult / i;
                computeScore = Double.parseDouble(tempCompute);

                if (minComputeScore > computeScore) {
                    minComputeScore = computeScore;
                } else if (maxComputeScore < computeScore) {
                    maxComputeScore = computeScore;
                }
                System.out.println(computeScore);

            }
            double incertitudeScore = (maxComputeScore - minComputeScore) / 2;
            System.out.println("Maximum = " + maxComputeScore + "\nMinimum = " + minComputeScore+"\nMoyenne = "+averageComputeResult+ "\nIncertitude = " + incertitudeScore);
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
        CSVNeuralDataSet trainingData = new CSVNeuralDataSet(
                "dataTrainingSet.csv", INPUT_NEURONS, OUTPUT_NEURONS, true);
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

        final MLTrain trainMain = new Backpropagation(network, trainingSet,
                0.00000001, 0.0);

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
        for (int i = 0; i < 50; i++) {
            elmanError = trainNetwork("Elman", elmanNetwork, trainingSet);
            if (meilleureErreur > elmanError) {
                System.out.println("TROUVÉ UN MEILLEUR");
                meilleureErreur = elmanError;
                meilleureStructure = NetworkCODEC.networkToArray(elmanNetwork);
            }
            elmanNetwork.reset();
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
