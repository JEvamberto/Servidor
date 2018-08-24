/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import clientetcp.Hello;
import clientetcp.SetStation;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jose
 */
public class TrataCliente implements Runnable {

    private Socket cliente;

    //1.welcome
    Welcome welcome = new Welcome();
    //2.Announce
    Announce announce = new Announce();
    //3. InvalidCommand:
    InvalidCommand invalidCommand = new InvalidCommand();

    Serializacao tr = new Serializacao();

    short station;
    File arquivo;
    short udpPort;

    public TrataCliente(Socket cliente) {

        this.cliente = cliente;
        Thread t = new Thread(this);
        this.welcome = new Welcome();
        this.announce = new Announce();
        this.invalidCommand = new InvalidCommand();
        t.start();

    }

    @Override
    public void run() {

        try {
            //hello
            File file = new File("music");
            File[] arquivo = file.listFiles();

            welcome.setNumStations((short) arquivo.length);

            DataInputStream receber = new DataInputStream(cliente.getInputStream());

            int tamanho = receber.readInt();
            byte dados[] = new byte[tamanho];
            System.out.println(dados.length);

            receber.read(dados, 0, tamanho);

            /*   receber.read(dados, 0, dados.length);*/
            Hello comandoHello = (Hello) TrataCliente.toObject(dados);

            if (comandoHello.getCommandType() == 0) {
                udpPort = comandoHello.getUpdPort();
                //welcome
                System.out.println("RECIBIDO COMANDO HELLO");
                System.out.println("ENVIANDO COMANDO WELCOME");

                DataOutputStream enviarCliente = new DataOutputStream(cliente.getOutputStream());

                byte[] dadosWelcome = tr.serialize(this.welcome);

                enviarCliente.writeInt(dadosWelcome.length);
                enviarCliente.write(dadosWelcome);

            } else {

                System.out.println("Falha:  CommandType de HELLO diferente 0 ");

            }

            int stationAnterior = 0;
            System.out.println("Porta UDP=" + udpPort);
            boolean sabe = true;
            EnviarUDP enviar = null;
            while (true) {
                //recebendoSetStation

                DataInputStream recebeSetStation = new DataInputStream(cliente.getInputStream());

                //SetStation setStation = (SetStation) recebeSetStation.readObject();
                int tamanho1 = recebeSetStation.readInt();
                byte[] dadosSetStation = new byte[tamanho1];
                
                recebeSetStation.read(dadosSetStation, 0, tamanho1);
                
                SetStation setStation=(SetStation)tr.deserialize(dadosSetStation);
                
                if ((byte) setStation.getCommandType1() == 1) {
                    //Lido com sucesso
                    station = (short) setStation.getStationNumber();
                    System.out.println("Estação escolhida: " + station);

                    /*  2.Announce
                    static byte replayType = 1;
                    static byte songNameSize;
                    static char songName[] = new char[songNameSize];
                     */
                    System.out.println("ENVIANDO ANNOUNCE");
                    DataOutputStream saida = new DataOutputStream(cliente.getOutputStream());

                    announce.setSongName(arquivo[station].getName().toCharArray());
                    
                    byte [] dadosAnnounce = tr.serialize(announce);
                    
                    saida.writeInt(dadosAnnounce.length);
                    saida.write(dadosAnnounce);
                    saida.flush();
                    
                    
                    // saida.writeObject(arquivo[station].getName());

                    if (sabe) {

                        enviar = new EnviarUDP(arquivo, udpPort, station);
                        stationAnterior = station;
                        sabe = false;
                    } else {
                        if (stationAnterior != station && enviar.getT().isAlive()) {
                            enviar.getT().interrupt();
                            enviar = null;
                            enviar = new EnviarUDP(arquivo, udpPort, station);
                        }
                        stationAnterior = station;

                    }

                }

            }

        } catch (IOException ex) {
            Logger.getLogger(TrataCliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TrataCliente.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
        Object obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            obj = ois.readObject();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
        return obj;
    }

}
