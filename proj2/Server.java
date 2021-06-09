/**
 * Ian Royer
 * 4-28-2021
 * CSC341 Project 2
 * Server/Client Communication
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    
    private static int port;

    public static void main(String[] args) throws IOException, InterruptedException
    {
        promptUser();        
        ServerSocket ss = new ServerSocket(port);  //TCP by default
        LinkedList<Socket> clientlist = new LinkedList<Socket>();    

        while(true) {
            //Accept a new client, start a thread for it, and add it to the list
            Socket s = ss.accept();
            System.out.println("New Client Connected");
            new Thread(new Server_Thread(s, clientlist)).start();
            synchronized(clientlist) {
                //Remove any closed clients before adding a new one to the list
                removeDeadConnections(clientlist);
                clientlist.addLast(s);
            }
        }
    }   

    /**
     * promptUser:
     * prompts the user for a port to listen to
     * @throws IOException
     */
    private static void promptUser() throws IOException {

        Scanner portSelector = new Scanner(System.in);

        System.out.print("Enter a port to listen to: ");
        port = portSelector.nextInt();

        portSelector.close();
    }

    /**
     * removeDeadConnections:
     * checks for any closed connections in the list and removes them
     * @param list - list of client sockets
     */
    private static void removeDeadConnections(LinkedList<Socket> list) {
        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).isClosed()) {
                list.remove(i);
                System.out.println("Removed a client from the list");
            }
        }
    }
}