package elmannetwork;

import java.util.Scanner;

/**
 *
 * @author Philippe
 */
public class Main {

    public static void main(String[] args) {
        Scanner lire = new Scanner(System.in);
        DataSetElman yo = new DataSetElman();
        ElmanNetwork elman = new ElmanNetwork();
        System.out.println("Train le network?");
        int rep = lire.nextInt();
        if (rep == 1) {
            elman.startTraining();
        }
        elman.computeNetwork();
    }
}
