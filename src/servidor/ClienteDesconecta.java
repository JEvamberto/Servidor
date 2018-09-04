/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jose
 */
public class ClienteDesconecta implements Runnable{
    
    ArrayList<Socket>  clientes;
    EnviarUDP[] estacao ;
    Thread thread;
  

    ClienteDesconecta(ArrayList<Socket> listaCliente, EnviarUDP[] estacao) {
       clientes=listaCliente;
       this.estacao=estacao;
       this.thread= new Thread(this);
       thread.start();
       
    }

    @Override
    public void run() {
       
        
        Socket cliente;
        while(true){
            
        for (int i = 0; i < clientes.size(); i++) {
            System.out.println("Tamanho clientes:"+ clientes.size());
            cliente=clientes.get(i);
            try {
                ObjectInputStream verificar = new ObjectInputStream (cliente.getInputStream());
                verificar.available();
            } catch (IOException ex) {
                System.out.println("Usuário desconectado");
                Logger.getLogger(ClienteDesconecta.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            if (cliente.isOutputShutdown()) {
                
                
                System.out.println("Cliente:" + cliente.toString());
               /*
                for (int j = 0; j < estacao.length; j++) {
                    
                    for (int k = 0; k <estacao[j].getEnderecos().size(); k++) {
                        InetAddress enderecoCliente= (InetAddress)estacao[j].getEnderecos().get(k);
                        
                        if (enderecoCliente==cliente.getInetAddress()) {
                                Object porta=estacao[j].getPortas().get(k);
                                System.out.println("Hello");
                                estacao[j].desconnect((short) porta, enderecoCliente);
                            
                            
                        }
                    }
                    
                    
                    
                    
                    
                    
                }*/
                
            }
            
            
        }}
    }
    
    
    
    
    
    
    
    
}
