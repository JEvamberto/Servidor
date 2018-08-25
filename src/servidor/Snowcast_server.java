/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jose
 */
public class Snowcast_server {

   
//server
    static ServerSocket serverTCP;
    static DatagramSocket serverUDP;

    public static void servidor() {
        
        File file = new File("music");
        File[] arquivo = file.listFiles();
        
        EnviarUDP estacao[]=new EnviarUDP[arquivo.length];
        
        for (int i = 0; i < estacao.length; i++) {
            
            estacao[i]=new EnviarUDP(arquivo[i]);
        }
        
        

        try {
            serverTCP = new ServerSocket(12333);
          while(true){     
            
            Socket cliente = serverTCP.accept();
            
            new TrataCliente(cliente,estacao);
          
          }
          
        } catch (IOException ex) {
            Logger.getLogger(Snowcast_server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
        servidor();
    }

}
