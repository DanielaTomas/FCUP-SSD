package Main;

import java.util.Scanner;

public class PeerMainMenu implements Runnable {

    private Scanner scanner;

    public PeerMainMenu(){
        this.scanner = new Scanner(System.in);
    }

    public void printMenu(){
        System.out.println("\n");
        System.out.println("----------------------------------");
        System.out.println("1 - coisas");
        System.out.println("----------------------------------");

    }

    @Override
    public void run() {
        printMenu();
    }
}
