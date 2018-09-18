/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import clientetcp.Finalizar;
import clientetcp.Hello;
import clientetcp.SetStation;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

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
    private short udpPort;
    private EnviarUDP estacao[];
    private ArrayList listasClientes;

    public TrataCliente(Socket cliente, EnviarUDP[] estacao, ArrayList<Socket> clientes) {

        this.estacao = estacao;
        this.listasClientes = clientes;
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
           

            welcome.setNumStations((short) estacao.length);

            DataInputStream receber = new DataInputStream(cliente.getInputStream());

            int tamanho = receber.readInt();
            byte dados[] = new byte[tamanho];
            

            receber.read(dados, 0, tamanho);

            /*   receber.read(dados, 0, dados.length);*/
            Hello comandoHello = (Hello) tr.deserialize(dados);

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
            //System.out.println("Porta UDP=" + udpPort);
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

                    Object comandoFinal = tr.deserialize(dadosSetStation);

                    if (comandoFinal instanceof Finalizar) {

                        Finalizar fc = (Finalizar) comandoFinal;

                        if (station >= 0 && station < estacao.length) {

                            if (!sabe) {
                                estacao[station].desconnect(udpPort, cliente.getInetAddress());
                            }

                            this.listasClientes.remove(cliente);
                        } else {

                            if (!sabe) {
                                estacao[stationAnterior].desconnect(udpPort, cliente.getInetAddress());
                            }

                            this.listasClientes.remove(cliente);

                        }

                    } else if (comandoFinal instanceof SetStation) {

                        SetStation setStation = (SetStation) tr.deserialize(dadosSetStation);

                        if ((byte) setStation.getCommandType1() == 1) {

                            station = (short) setStation.getStationNumber();
                            if (station >= 0 && station < estacao.length) {

                                System.out.println("Estação escolhida: " + station);
                                System.out.println("ENVIANDO ANNOUNCE");
                                saida = new DataOutputStream(cliente.getOutputStream());

                                announce.setSongName(estacao[station].getNameStation().toCharArray());

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

                                if (station >= 0 && station < estacao.length) {

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

                                }

                                if (station >= 0 && station < estacao.length) {
                                    stationAnterior = station;
                                }

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

}
