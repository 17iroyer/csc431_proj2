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
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Server_Thread implements Runnable{

    final static private int MESSAGE_LENGTH = 10;

    public Socket connectedsocket;
    private LinkedList<Socket> clientlist;

    private enum Msgtype {
        BROADCAST, NEXT, ECHO
    }

    public Server_Thread(Socket s, LinkedList<Socket> list) {
        connectedsocket = s;
        clientlist = list;
    }

    @Override
    public void run() {
        Scanner clientin = null;
        String msg = "";
        try{
            clientin = new Scanner(connectedsocket.getInputStream());
        } catch(IOException e) {
            System.out.println("Error opening client scanner");
            e.printStackTrace();
        }


        //Continue reading while the client is sending
        while(true) {

            //Read the message
            try {
                msg = getClientMsg(connectedsocket, clientin);
            } catch (IOException e) {
                System.out.println("Error getting client message");
                e.printStackTrace();
            }

            //Prints the message for clarity 
            System.out.println(msg.substring(1));

            synchronized(clientlist) {
                //If we were sent a blank message, close the connection (Should never happen)
                if(msg.isEmpty()) {
                    clientin.close();
                    clientlist.remove(connectedsocket);
                    return;
                }

                //Write to the log and process the message accordingly
                try {
                    printLog(LocalTime.now(), getMsgType(msg), msg.substring(1));
                    processMessage(msg, clientlist);
                } catch (IOException e) {
                    System.out.println("Error processing message");
                    e.printStackTrace();
                }
            }
            
        }
    }

    /**
     * getClientMsg:
     * Will get the next message coming from the client
     * @param client - Socket to read from
     * @param clientin - SCanner being used to read
     * @return the message as a string
     * @throws IOException
     */
    private static String getClientMsg(Socket client, Scanner clientin) throws IOException {
        clientin.useDelimiter("");
        String msg = "";
        for(int i = 0; i < MESSAGE_LENGTH; i++) {
            try{
                msg += clientin.next();
            } catch(NoSuchElementException e) {
                
            }
        }
        return msg;
    }

    /**
     * getMsgType:
     * Given a message, checks the first character and returns the appropriate type
     * @param msg - message to check
     * @return the messages type
     */
    private static Msgtype getMsgType(String msg) {
        switch(msg.charAt(0)) {
            case 'F':
                return Msgtype.BROADCAST;
            case '1':
                return Msgtype.NEXT;
            case '0':
                return Msgtype.ECHO;
            default:
                return Msgtype.ECHO;
        }
    }
       

    /**
     * processMessage:
     * given a message and the list, will get the message's type and hand it to the proper
     * method to be sent
     * @param msg - message being sent
     * @param clientlist - list of client sockets
     * @throws IOException
     */
    private void processMessage(String msg, LinkedList<Socket> clientlist) throws IOException {
        switch(getMsgType(msg)) {
            case BROADCAST:
                broadcastMsg(msg, clientlist);
                System.out.println("broadcast");
                break;
            case NEXT:
                nextMsg(msg, clientlist);
                System.out.println("next");
                break;
            case ECHO:
                echoMsg(msg);
                System.out.println("echo");
                break;
            default:
                System.out.println("Error processing message type");
                break;
        }
    }

    /**
     * echoMsg:
     * sends the message back to the original sender
     * @param msg - message to be sent
     * @param cursock - current socket
     * @throws IOException
     */
    private void echoMsg(String msg) throws IOException {
        BufferedWriter bw = new BufferedWriter(new PrintWriter(connectedsocket.getOutputStream()));
        bw.write(msg.substring(1));
        bw.flush();
        //bw.close();
    }

    /**
     * broadcastMsg:
     * sends the message to the entire list (including original sender)
     * @param msg - message to be sent
     * @param list - list of all client sockets
     * @throws IOException
     */
    private static void broadcastMsg(String msg, LinkedList<Socket> list) throws IOException {
        int length = list.size();
        for(int i = 0; i < length; i++) {
            BufferedWriter bw = new BufferedWriter(new PrintWriter(list.get(i).getOutputStream()));

            bw.write(msg.substring(1));
            bw.flush();
        }
    }

    /**
     * nextMsg:
     * sends the message to the next client in the list
     * @param msg - message to be sent
     * @param list - list of all client sockets
     * @param cursock - current socket
     * @throws IOException
     */
    private void nextMsg(String msg, LinkedList<Socket> list) throws IOException {
        int length = list.size();
        int destination = (list.indexOf(connectedsocket) + 1) % length;

        BufferedWriter bw = new BufferedWriter(new PrintWriter(list.get(destination).getOutputStream()));

        bw.write(msg.substring(1));
        bw.flush();
    }

    /**
     * printLog:
     * Append a message to the log.txt file upon connect/disconnect
     * @param time - current time 
     * @param status - whether the message was sent or recieved
     * @param message - string conatining message from server 
     * @throws IOException
     */
    private void printLog(LocalTime time, Msgtype type, String message) throws IOException {
        FileWriter writer = new FileWriter(new File("serverlog.txt"), true);

        writer.append(time + " | " + connectedsocket.getInetAddress().getHostAddress() + " | " + type + " | " + message + "\n");
        writer.close();
    }
}
