/**
 * Ian Royer
 * 4-28-2021
 * CSC341 Project 2
 * Server/Client Communication
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalTime;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client_Thread implements Runnable {

    final static private int MESSAGE_LENGTH = 9;

    private static Socket connectedsocket;
    
    public Client_Thread(Socket s) {
        connectedsocket = s;
    }

    @Override
    public void run() {
        //Initialize the scanner
        Scanner servscan = null;
        try {
            servscan = new Scanner(connectedsocket.getInputStream());
            servscan.useDelimiter("");
        } catch(IOException e) {
            e.printStackTrace();
        }

        //Keep waiting for responses
        while(true) {
            try {
                String response = waitForResponse(servscan);
                printLog(LocalTime.now(), "Received", response);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error in reading response");
            }
        }
    }
    
    /**
     * waitForResponse:
     * Wait for the server response message, return it as a string
     * @param scan - scanner that is waiting for server response
     * @return response from server in a string
     */
    private static String waitForResponse(Scanner scan) {
        String response = "";
        for(int i = 0; i < MESSAGE_LENGTH; i++) {
            try{
                response += scan.next();
            } catch (NoSuchElementException e) {}
        }

        return response;
    }

    /**
     * printLog:
     * Append a message to the log.txt file upon connect/disconnect
     * @param time - current time 
     * @param status - whether the message was sent or recieved
     * @param message - string conatining message from server 
     * @throws IOException
     */
    private static void printLog(LocalTime time, String status, String message) throws IOException {
        FileWriter writer = new FileWriter(new File("clientlog.txt"), true);

        writer.append(time + " | " + connectedsocket.getInetAddress().getHostAddress() + " | " +  status + " | " + message + "\n");
        writer.close();
    }
}
