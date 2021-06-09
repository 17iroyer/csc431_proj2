/**
 * Ian Royer
 * 4-28-2021
 * CSC341 Project 2
 * Server/Client Communication
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.InputMismatchException;
import java.util.Scanner;


public class Client {
    
    final static private int MESSAGE_LENGTH = 10;
    
    private static int port = 12000;
    private static String ipaddr = "127.0.0.1";    //The local address
    private static Socket mysock = null;

    public static void main(String[] args) throws UnknownHostException, IOException
    {
  
        Scanner userin = new Scanner(System.in);
        promptUser(userin);

        //Take user input for what ip and port to use
        try {
            mysock = new Socket(ipaddr, port); //127.0.0.1
        } catch(Exception e) {
            System.out.println("Connection to server could not be established");
            System.exit(0);
        } 
        System.out.println("Started Client");

        //Set up a thread to listen for any responses
        (new Thread(new Client_Thread(mysock))).start();

        //Configure Scanner and buffered writer to write to server
        BufferedWriter servsend = new BufferedWriter(new PrintWriter(mysock.getOutputStream()));

        //Read lines from the user until message is "exit"
        while(true) {
            System.out.print("Message:");
            String msg = userin.next();

            //Check if we want to exit. Close the connection
            if(msg.startsWith("exit"))  {
                servsend.close();
                mysock.close();
                userin.close();
                System.exit(0);
            }

            //Check and add spaces to pad the message
            msg = makeSpaces(msg);

            //If everyting is good, send to the server
            if(checkFormat(msg)) {
                servsend.write(msg);
                servsend.flush();
                printLog(LocalTime.now(), "Sent", msg);
            } else {
                System.out.println("Incorrect message format!");
            }
        }
    }

    /**
     * promptUser:
     * Prompts the user for an IPv4 address and a port to connect to
     * @throws IOException
     */
    private static void promptUser(Scanner userScan) throws IOException {
        
        try{
            System.out.print("Enter an ip address to connect to: ");
            ipaddr = userScan.next();
            System.out.print("Enter a port to connect to: ");
            port = userScan.nextInt();
        } catch(InputMismatchException e){
            System.out.println("Incorrect input type");
            System.exit(0);
        }
    }

    /**
     * makeSpaces:
     * Adds any extra spaces to the message.
     * Ensures last message has 10 characters
     * @param msg - partial message to change
     * @return
     */
    private static String makeSpaces(String msg) {
        String outmsg = msg;

        for(int i = msg.length(); i < MESSAGE_LENGTH; i++) {
            outmsg = outmsg + " ";
        }

        return outmsg;
    }

    /**
     * checkFormat:
     * checks the format of a given message to ensure it is correct
     * @param msg - message to be checked
     * @return - true if the message is correct, false otherwise
     */
    private static boolean checkFormat(String msg) {
        char firstchar = msg.charAt(0);

        if(firstchar != 'F' && firstchar != '1' && firstchar != '0') {
            return false;
        } else if(msg.length() != 10) {
            return false;
        }
        
        return true;
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

        writer.append(time + " | " + mysock.getInetAddress().getHostAddress() + " | " + status + " | " + message + "\n");
        writer.close();
    }

}