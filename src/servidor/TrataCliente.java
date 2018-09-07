/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import clientetcp.Finalizar;
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

    private short station;
    private File arquivo;
    private short udpPort;
    private EnviarUDP estacao[];
    private ArrayList listasClientes;
    

    public TrataCliente(Socket cliente, EnviarUDP[] estacao, ArrayList<Socket> clientes) {

        this.estacao = estacao;
        this.listasClientes=clientes;
        this.cliente = cliente;
        Thread t = new Thread(this);
        this.welcome = new Welcome();
        this.announce = new Announce();
        this.invalidCommand = new InvalidCommand();
        t.start();
    }

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

            welcome.setNumStations((short) estacao.length);

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
                InvalidCommand comandoInvalido = new InvalidCommand();

                comandoInvalido.setInvalidreplyString(("Falha:  CommandType de HELLO diferente 0 ").toCharArray());
                comandoInvalido.setInvalidreplyType((byte) 2);

                DataOutputStream enviarInvalid = new DataOutputStream(this.cliente.getOutputStream());

                byte dadosInvalido[] = tr.serialize(comandoInvalido);

                enviarInvalid.writeInt(dadosInvalido.length);
                enviarInvalid.write(dadosInvalido);

                System.out.println("Falha:  CommandType de HELLO diferente 0 ");

            }

            int stationAnterior = 0;
            System.out.println("Porta UDP=" + udpPort);
            boolean sabe = true;
            EnviarUDP enviar = null;
            DataOutputStream saida;
            while (true) {
                //recebendoSetStation
                if (!cliente.isClosed()) {

                    DataInputStream recebeSetStation = new DataInputStream(cliente.getInputStream());

                    //SetStation setStation = (SetStation) recebeSetStation.readObject();
                    int tamanho1 = recebeSetStation.readInt();
                    byte[] dadosSetStation = new byte[tamanho1];

                    recebeSetStation.read(dadosSetStation, 0, tamanho1);
                    
                    
                    
                    Object comandoFinal= tr.deserialize(dadosSetStation);
                    
                    if (comandoFinal instanceof Finalizar) {
                        
                        Finalizar fc=(Finalizar)comandoFinal;
                        
                        if (station >= 0 && station < estacao.length) {
                            
                            if (!sabe) {
                                estacao[station].desconnect(udpPort, cliente.getInetAddress());
                            }
                            
                            this.listasClientes.remove(cliente);
                        }
                        
                        
                    }else if(comandoFinal instanceof SetStation){
                    
                        
                    SetStation setStation = (SetStation) tr.deserialize(dadosSetStation);

                    if ((byte) setStation.getCommandType1() == 1) {
                        //Lido com sucesso

                        /*  2.Announce
                    static byte replayType = 1;
                    static byte songNameSize;
                    static char songName[] = new char[songNameSize];
                         */
                        station = (short) setStation.getStationNumber();
                        if (station >= 0 && station < estacao.length) {

                            System.out.println("Estação escolhida: " + station);
                            System.out.println("ENVIANDO ANNOUNCE");
                            saida = new DataOutputStream(cliente.getOutputStream());

                            announce.setSongName(arquivo[station].getName().toCharArray());

                            byte[] dadosAnnounce = tr.serialize(announce);

                            saida.writeInt(dadosAnnounce.length);
                            saida.write(dadosAnnounce);
                            saida.flush();

                        } else {
                            
                            
                            
                            saida = new DataOutputStream(cliente.getOutputStream());
                            InvalidCommand comandoInvalidd = new InvalidCommand();
                            String erro = "Erro: A estação " + station + " não existe";
                            comandoInvalidd.setInvalidreplyString(erro.toCharArray());
                            byte[] dadosInvalidd = tr.serialize(comandoInvalidd);

                            saida.writeInt(dadosInvalidd.length);
                            saida.write(dadosInvalidd);
                            saida.flush();
                           

                            saida = null;

                        }

                        // saida.writeObject(arquivo[station].getName());
                        if (sabe) {
                            
                            if (station>=0 && station<estacao.length) {
                                
                            estacao[station].connect(udpPort, this.cliente.getInetAddress());

                            //enviar = new EnviarUDP(arquivo, udpPort, station);
                            stationAnterior = station;
                            sabe = false;
                                
                            }

                        } else {
                            if (stationAnterior != station) {
                                if (station >= 0 && station < estacao.length) {
                                    
                                    estacao[stationAnterior].desconnect(udpPort, this.cliente.getInetAddress());
                                    estacao[station].connect(udpPort, this.cliente.getInetAddress());
                                }

                                //enviar.getT().interrupt();
                                //enviar = null;
                                //enviar = new EnviarUDP(arquivo, udpPort, station);
                            }
                            stationAnterior = station;

                        }

                    }
                    
                    
                    
                    }
                    


                    saida = null;

                }
            }

        } catch (IOException ex) {
            //  Logger.getLogger(TrataCliente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            // Logger.getLogger(TrataCliente.class.getName()).log(Level.SEVERE, null, ex);
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
