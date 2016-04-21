import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 * Класс, организующий общение с пользователем в консоли.
 * Created by viktor on 4/15/16.
 */
public class Console {
    /** HashMap , возвращающая по ключу String, содержащему имя порта, значение Port */
    private HashMap<String, Port> portMap;
    /** Веря старта программы. В милисеккундах. */
    private long start;

    public Console() {
        start = System.currentTimeMillis();
        portMap = new HashMap<String, Port>();
    }

    /**
     * Выводит в консоль список всех найденный COM портов
     */
    private void list() {
        String[] portNames = SerialPortList.getPortNames().clone();
        for (int i = 0; i < portNames.length; i++) {
            System.out.println(portNames[i]);
        }
    }

    /**
     * Открывает новый порт с заданным именем и добавляет его в PortMap. Порт открывается с параметрами 9600, 8, 1
     * @param portName имя добавляемого портв
     */
    private void add(String portName) {
        SerialPort port = new SerialPort(portName);
        try {
            port.openPort();
            port.setParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            portMap.put(portName, new Port(port, start));
            System.out.println("Port added.");
        } catch (SerialPortException e) {
            System.out.println("Can not open port!");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Выводит в консоль справочную инормацию по работе программы
     */
    private void outHelp() {
        System.out.println("                               Welcome to Sniffer2!");
        System.out.println("            List of commands:");
        System.out.println("    >list                                 -   list of com-ports");
        System.out.println("    >add <com mane>                       -   add to sniffer com port with name <com name>");
        System.out.println("    >listen <com name>                    -   start to listen com port <com name>");
        System.out.println("    >send <com name> <massage>            -   send from <com name> port massage <massage>");
        System.out.println("    >setRet <com name 1> <com name 2>     -   all accepted massage from <com name 1> port");
        System.out.println("                                              sending in <com name 2>. Work with buffer by 5 ms");
        System.out.println("    >setRetBB <com name 1> <com name 2>   -   all accepted bytes from <com name 1> port sending");
        System.out.println("                                              in <com name 2>. Resending by byte, without buffer");
        System.out.println("    >setNoRet <com name>                  -   set off all retransmitions");
        System.out.println("    >close <co name>                      -   close port and remove from Sniffer2");
        System.out.println("    >setOut <file name>                   -   set out of listeners to file <file nam>");
        System.out.println("    >help                                 -   print help, about Sniffer2");
        System.out.println("    >exit                                 -   close all ports and exit");
        System.out.println("        Good luck, your Sniffer!");
    }

    /**
     * Запускает общение с пользователем. Спрашивает ввод команды и обрабатывает ее.
     * По команде auto можно написать сценарий, для быстрого формирования портов в программе
     */
    public void run () {
        Scanner in = new Scanner(System.in);
        String command = new String("");
        while (true) {
            System.out.print(">");
            command = in.next();
            if (command.equals("auto")) {
                //list();
                /*add("COM24");
                add("COM25");
                portMap.get("COM24").addListener();
                portMap.get("COM25").addListener();
                */
                add("/dev/ttyUSB0");
                /*
                add("/dev/ttyAMA0");
в
                try {
                    PrintStream out = new PrintStream("log.txt");
                    Iterator<Port> iterator = portMap.values().iterator();
                    while (iterator.hasNext()) {
                        iterator.next().setOutLog(out);
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("File not found.");
                    System.out.println(e.getMessage());
                }

                portMap.get("/dev/ttyUSB0").setRetransmitPort(portMap.get("/dev/ttyAMA0").getSerialPort());
                portMap.get("/dev/ttyUSB0").setRetransmit(true);
                */
                portMap.get("/dev/ttyUSB0").addListener();
                /*
                portMap.get("/dev/ttyAMA0").setRetransmitPort(portMap.get("/dev/ttyUSB0").getSerialPort());
                portMap.get("/dev/ttyAMA0").setRetransmit(true);
                portMap.get("/dev/ttyAMA0").addListener();
                // */
                //portMap.get("/dev/ttyUSB0").setRetransmitByByte(true);
                System.out.println("Auto all ok.");
                //portMap.get("/dev/ttyUSB1").sendMassage("Hello!");
                continue;
            }

            if (command.equals("list")) {
                list();
                continue;
            }
            if (command.equals("add")) {
                add(in.next());
                continue;
            }
            if (command.equals("send")) {
                Port port = portMap.get(in.next());
                if (port != null) {
                    port.sendMassage(in.next());
                } else {
                    System.out.println("can not send massage: port not added.");
                }
                continue;
            }
            if (command.equals("sendInt")) {
                Port port = portMap.get(in.next());
                if (port != null) {
                    port.sendMassage(in.nextInt());
                } else {
                    System.out.println("can not send massage: port not added.");
                }
                continue;
            }

            if (command.equals("listen")) {
                Port port = portMap.get(in.next());
                if (port != null) {
                    port.addListener();
                    System.out.println("Listener added.");
                } else {
                    System.out.println("Can not start listen port: port not added.");
                }
                continue;
            }

            if (command.equals("setRet")) {
                Port port1 = portMap.get(in.next());
                if (port1 != null) {
                    Port port2 = portMap.get(in.next());
                    if (port2 != null) {
                        port1.setRetransmitPort(port2.getSerialPort());
                        port1.setRetransmit(true);
                        System.out.println("Retransmit set on.");
                    } else {
                        System.out.println("Can not setRet: port 2 not added.");
                    }
                } else {
                    System.out.println("Can not setRet: port 1 not added.");
                }
                continue;
            }
            if (command.equals("setRetBB")) {
                Port port1 = portMap.get(in.next());
                if (port1 != null) {
                    Port port2 = portMap.get(in.next());
                    if (port2 != null) {
                        port1.setRetransmitPort(port2.getSerialPort());
                        port1.setRetransmitByByte(true);
                        System.out.println("Retransmit by byte set on.");
                    } else {
                        System.out.println("Can not setRetBB: port 2 not added.");
                    }
                } else {
                    System.out.println("Can not setRetBB: port 1 not added.");
                }
                continue;
            }
            if (command.equals("setNoRet")) {
                Port port = portMap.get(in.next());
                if (port != null) {
                    port.setNoRetransmit();
                    System.out.println("All retransmit set off.");
                } else {
                    System.out.println("Can not setNoRet: port not added.");
                }
                continue;
            }

            if (command.equals("close")) {
                String portNme = in.next();
                Port port = portMap.get(portNme);
                if (port != null) {
                    port.close();
                    portMap.remove(portNme);
                    System.out.println("Port is closed.");
                } else {
                    System.out.println("Can not close port: port not added.");
                }
                continue;
            }
            if (command.equals("setOut")) {
                try {
                    PrintStream out = new PrintStream(in.next());
                    Iterator<Port> iterator = portMap.values().iterator();
                    while (iterator.hasNext()) {
                        iterator.next().setOutLog(out);
                    }
                    System.out.println("Out to file added.");
                } catch (FileNotFoundException e) {
                    System.out.println("File not found.");
                    System.out.println(e.getMessage());
                }
                continue;
            }

            if (command.equals("help")) {
                outHelp();
                continue;
            }
            if (command.equals("exit")) {

                Iterator<Port> iterator = portMap.values().iterator();
                while (iterator.hasNext()){
                    iterator.next().close();
                }
                System.out.println("All added ports closed.\nGoodbye!");
                return;
            }
            System.out.println("Incorrect command.");
        }
    }

}
