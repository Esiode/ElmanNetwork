package elmannetwork;

import java.util.Scanner;

/**
 *
 * @author Philippe
 */
public class Main {

    public static void main(String[] args) {
        String mot = "clock_gettime";//1011121111081089511997105116
        //991081119910795103101116116105109101
        StringBuffer sysCallContainer = new StringBuffer();
        for (int i = 0; i < mot.length(); i++) {


            int temp = mot.charAt(i);
            sysCallContainer.append(temp); // Crée un code, formé de la
            // somme des code ASCII de
            // chaque charactère qui
            // forment l'appel système, qui
            // représente l'appel système


        }
        String sysCallValue = sysCallContainer.toString();
        System.out.println(sysCallValue);
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
