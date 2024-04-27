package Main;

import java.util.Scanner;

public class PeerMainMenu implements Runnable {

    private Scanner scanner;

    public PeerMainMenu(){
        this.scanner = new Scanner(System.in);
    }

    public String menu(){
        return "----------------------------------" + '\n' +
        " 1 - Find Node" + '\n' +
        "----------------------------------";

    }

    @Override
    public void run() {
        String input;
        System.out.println(menu());
        while (true){


            input = scanner.nextLine();

            switch (input){
                case "menu":
                    System.out.println(menu());
                case "1"://TODO add interfacing with kademlia here, the singleton design pattern might be useful

            }

        }
    }
}
