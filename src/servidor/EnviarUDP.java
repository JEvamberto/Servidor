/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jose
 */
public class EnviarUDP implements Runnable {

    private short station;
    private File[] arquivo;
    private File ArquivoOficial;
    private short udpPort = 12335;
    private int quantidadeDeOuvintes;
    private Thread t;
    private ArrayList portas= new ArrayList();
    byte audio[];

    public EnviarUDP(File arquivoOficial) {
        this.ArquivoOficial = arquivoOficial;
        this.station = station;
        this.arquivo = arquivo;
        this.udpPort = udpPort;
        this.t = new Thread(this);
        t.start();
        System.out.println("Eu foi criado" + station + " " + udpPort);
    }

    public EnviarUDP(File[] arquivo, short udpPort, short station) {
        this.station = station;
        this.arquivo = arquivo;
        this.udpPort = udpPort;
        this.t = new Thread(this);
        t.start();
        System.out.println("Eu foi criado" + station + " " + udpPort);

    }

    public Thread getT() {
        return t;
    }

    public void setT(Thread t) {
        this.t = t;
    }

    public void connect(short portas) {
        this.quantidadeDeOuvintes++;
        this.portas.add(portas);
    }

    public void desconnect(short portas) {
        this.quantidadeDeOuvintes--;
        this.portas.remove((Object) portas );
    }

    public int getQuantidadeDeOuvintes() {
        return this.quantidadeDeOuvintes;
    }

    @Override
    public void run() {
        try {

            InetAddress addr = InetAddress.getByName("192.168.0.255");

            int pacoteTam = 50000;
            byte pacote[] = new byte[pacoteTam];

            //   double numberPkg = Math.ceil((int) arquivo[station].length() / pacote);
            //Enviar tamanho do arquivo
            //Fim do tamanho do arquivo
            File file = this.ArquivoOficial;
            this.audio = EnviarUDP.getBytes(file);

            DatagramPacket pkg;
            DatagramSocket enviar = new DatagramSocket();

            enviar.setReuseAddress(true);

            int count = 0;

            while (true) {

                for (int i = 0; i < audio.length; i++) {
                    // System.out.println("SERÁ");
                    pacote[count] = audio[i];
                    if (count == pacote.length - 1) {
                        for (int j = 0; j < this.portas.size(); j++) {
                            pkg = new DatagramPacket(pacote, pacote.length, addr, (short)this.portas.get(j));
                            enviar.send(pkg);
                        }

                        count = 0;
                        Thread.sleep(3000);
                    }
                    count++;

                }
                // System.out.println("oi");
            }

            /*  for (double i = 0; i < numberPkg + 1; i++) {
                    
                    byte[] bufferPacote = new byte[pacote];
                    buffStream.read(bufferPacote, 0, bufferPacote.length);
                    System.out.println("Pacote:" + (i + 1));
                    pkg = new DatagramPacket(bufferPacote, bufferPacote.length, addr, udpPort);
                    enviar.send(pkg);
          
              Thread.sleep(10);
                }*/
        } catch (UnknownHostException ex) {
            Logger.getLogger(EnviarUDP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SocketException ex) {
            Logger.getLogger(EnviarUDP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EnviarUDP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(EnviarUDP.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Mensagem enviada");

    }

    public void setUdpPort(short udpPort) {
        if (udpPort != 0) {
            this.udpPort = udpPort;
        }
    }

    public static byte[] getBytes(File file) {
        int len = (int) file.length();
        byte[] sendBuf = new byte[len];
        FileInputStream inFile = null;
        try {
            inFile = new FileInputStream(file);
            inFile.read(sendBuf, 0, len);
        } catch (FileNotFoundException fnfex) {
        } catch (IOException ioex) {
        }
        return sendBuf;
    }

}
