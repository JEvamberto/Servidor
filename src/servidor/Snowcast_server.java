/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
    static ArrayList<Socket> listaCliente;

    public static void servidor(String porta, String DMusicas) {

        listaCliente = new ArrayList<>();

        File file = new File(DMusicas);
        File[] arquivo = file.listFiles();

        EnviarUDP estacao[] = new EnviarUDP[arquivo.length];

        for (int i = 0; i < estacao.length; i++) {

            estacao[i] = new EnviarUDP(arquivo[i]);
        }

        try {
            serverTCP = new ServerSocket(Integer.parseInt(porta));

           
            ComandoQeP comando = new ComandoQeP(estacao, listaCliente);

            while (true) {

                Socket cliente = serverTCP.accept();

                listaCliente.add(cliente);

                new TrataCliente(cliente, estacao, listaCliente);

            }

        } catch (IOException ex) {
            System.out.println("Mesmo ip do servidor já está em uso");
            System.exit(0);
            Logger.getLogger(Snowcast_server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
       
        String porta = null, musicas = null;

        if (args.length == 0 || args.length >=3) {
            System.out.println("Digite um parâmetro válido <porta> <diretorioDasMusicas>");
            System.exit(0);
        } else {

            porta = args[0];
            musicas = args[1];


        }

        servidor(porta,musicas);
    }

}
