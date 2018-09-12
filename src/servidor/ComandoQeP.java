/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jose
 */
public class ComandoQeP implements Runnable {

    private EnviarUDP[] estacao;
    private Thread thread;
    private ArrayList<Socket> clientes;

    public ComandoQeP(EnviarUDP[] estacao, ArrayList<Socket> cliente) {
        this.estacao = estacao;
        this.clientes = cliente;
        this.thread = new Thread(this);
        this.thread.start();

    }

    @Override
    public void run() {
        System.out.println("Eu COMANDOQeP foi criado");
        Scanner teclado;
        char comando;
        while (true) {
            teclado = new Scanner(System.in);
            System.out.println("Comando P ou Q:");
            comando = teclado.nextLine().charAt(0);

            if (comando == 'P' || comando == 'p') {
                this.listarClientes();
            } else if (comando == 'Q' || comando == 'q') {
                this.fecharConexao();
            }
            teclado = null;

        }

    }

    public void listarClientes() {
        System.out.println("Cliente no servidor: " + clientes.size());

        for (int i = 0; i < this.estacao.length; i++) {

            System.out.println("Estação " + i + ": Clientes: " + estacao[i].getQuantidadeDeOuvintes());
        }
    }

    public void fecharConexao() {

   
            
            try {

                
                for (int i = 0; i < clientes.size(); i++) {
                    if (clientes.get(i)!=null) {
                        clientes.get(i).close();
                        clientes.remove(clientes.get(i));
                       
                    }
                }
                 System.exit(0);
                
            } catch (IOException ex) {
                Logger.getLogger(ComandoQeP.class.getName()).log(Level.SEVERE, null, ex);
            }
        
    }

}
