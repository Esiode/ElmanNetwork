package elmannetwork;

import java.util.Scanner;

/**
 *
 * @author Philippe
 */
public class Main {

    public static void main(String[] args) {
        /*String mot = "clock_gettime";
        StringBuffer sysCallContainer = new StringBuffer();
        /
            Conversion en chaîne ASCII des appels système.
         /
        for (int i = 0; i < mot.length(); i++) {
            int temp = mot.charAt(i);
            sysCallContainer.append(temp);
        }
        String sysCallValue = sysCallContainer.toString();
        */
        Scanner lire = new Scanner(System.in);
        DataSetElman dataSet = new DataSetElman();
        ElmanNetwork elman = new ElmanNetwork();
        System.out.println("Train le network?");
        int rep = lire.nextInt();
        if (rep == 1) {
            elman.startTraining();
        }
        elman.computeNetwork();
    }
}
