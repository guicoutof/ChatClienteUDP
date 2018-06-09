/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatcliente;
import chatcliente.IU.IUConverchation;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
//import socket.io.*;
/**
 *
 * @author AlphaLegends
 */
public class ChatCliente {
    static int port = 9999;
    static boolean auxNick = true;
    static String nickname;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket(); //não é criado uma conexão entre os socket por ser UDP, portanto Datagrama
        InetAddress IPAddress = InetAddress.getByName("localhost"); // ip do servidor
        
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];

        do{
        String nick = "10\n"; //login
        nickname = JOptionPane.showInputDialog("Insira seu nickname");
        nick += nickname;
        nick += "\n";
        sendData = nick.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,IPAddress,port);
        clientSocket.send(sendPacket);
        System.out.println("Aguardando servidor!");

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);      
        clientSocket.receive(receivePacket);
        abririnterface(receivePacket,clientSocket);
        }while(auxNick);
        
        
    }
    
    public static void abririnterface(DatagramPacket receivePacket,DatagramSocket clientSocket) throws IOException{
        String sentence = new String(receivePacket.getData());
        byte[] receiveData = new byte[1024*1024];
        
        switch(sentence.charAt(0)){
            case '1'://foi logado
                if(sentence.charAt(1) == '1'){
                    IUConverchation IU = new IUConverchation(clientSocket);
                    IU.setTitle(nickname);
                    receivePacket = new DatagramPacket(receiveData, receiveData.length);      
                    clientSocket.receive(receivePacket);
                    IU.tratarMensagem(receivePacket);
                    
                    receivePacket = new DatagramPacket(receiveData, receiveData.length);      
                    clientSocket.receive(receivePacket);
                    IU.tratarMensagem(receivePacket);
                    
                    IU.setVisible(true);
                    auxNick = false;
                    
                    ReceiveThread ReceiveThread = new ReceiveThread(clientSocket,IU);
                    SendThread SendThread = new SendThread(clientSocket,IU);
                    
                    ReceiveThread.start();
                    SendThread.start();
                }

                
                break;
            case '9'://erros
                if(sentence.charAt(1)==0){//erro no nick
                    JOptionPane.showMessageDialog(null,"Nick inválido ou já existente");
                    auxNick = true;
                }
                break;
            default:
                
                break;
                
        }
        
    }
    
     
    
}

class ReceiveThread extends Thread{
    DatagramSocket clientSocket;
    IUConverchation IU;

    public ReceiveThread(DatagramSocket clientSocket,IUConverchation IU) {
        this.clientSocket = clientSocket;
        this.IU = IU;
    }
    
    
    @Override
    public void run(){

        while(true){
            byte[] receiveData = new byte[1024*1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                clientSocket.receive(receivePacket);
            } catch (IOException ex) {
                Logger.getLogger(ReceiveThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            IU.tratarMensagem(receivePacket);
        }
    }
}

class SendThread extends Thread{
    DatagramSocket clientSocket;
    IUConverchation IU;

    public SendThread(DatagramSocket clientSocket,IUConverchation IU) {
        this.clientSocket = clientSocket;
        this.IU = IU;
    }
    
    @Override
    public void run(){
        while(true){
            try {
                IU.sendMessage(clientSocket);
            } catch (IOException ex) {
                Logger.getLogger(SendThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}