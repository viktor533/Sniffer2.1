import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Основной класс, обрабатывающий события порта и выводящий log о них.
 * Так же, при установлении соответсвущей опции осществляет пересылку
 * принятых сообщений из другого порта (возможно этото же)
 * PortEventListener вызывает метод accept(SerialPortEvent event)
 * Whiler вызывает метод noAcceptLongTime()
 * Created by viktor on 4/15/16.
 */

public class TrueJsscListener {
    /** Порт. с которого будут приниматься события */
    private SerialPort port;
    /** Порт, в который, возможно, будет происходить ретрансляция */
    private SerialPort retransmitPort;
    /** Буффер, в котором, возможно, будет хронится пересылаемое сообщение */
    private LinkedList<Integer> buffer;
    /** Время начала работы программы */
    private long startTime;
    /** Не используется. Константа, в милисеккундах, на это время приостанавливаетс порт во время чтения из него */
    public final int TIMEOUT = 2;
    /** Поток вывода данных */
    private PrintStream out;

    /** Флаг того, что началось приниматься сообщение*/
    private boolean isPackage;
    /** Флаг того, что перемылка сообщения произошла*/
    private boolean retransmitOk;
    /** Флаг, определяющий, будет ли порисходить пересылка сообщений */
    private boolean isRetransmit;
    /** Флаг, определяющий, будет ли происходить пересылка принятных байтов по одному */
    protected boolean isRetransmitByByte;

    /**
     * Выставляет настройки поумолчанию
     */
    private void defaultSettings() {
        isPackage = false;
        isRetransmit = false;
        isRetransmitByByte = false;
        retransmitOk = false;
        retransmitPort = null;
        out = System.out;
    }

    /**
     * Конструктор выставляет настройки поумолчанию, принимает слушаемый прот и время старта программы.
     * @param port слушаемый порт
     * @param startTme время старта программы в милисеккундах
     */
    public TrueJsscListener(SerialPort port, long startTme) {
        defaultSettings();
        this.port = port;
        this.startTime = startTme;
    }

    /**
     * Конструктор высталяет настройки по умолчанию, и формаирует слуателя в соответсвии с принимаемыми параметрами
     * @param port слушаемый порт
     * @param startTme время начала работы программы
     * @param isRetransmit флаг, указывающий нужно ли делать ретрансляцию сообщений
     * @param isRetransmitByByte флаг,указываюший нужно делать ретрансляцию по байтам
     */
    public TrueJsscListener(SerialPort port, long startTme, boolean isRetransmit, boolean isRetransmitByByte) {
        defaultSettings();
        this.isRetransmit = isRetransmit;
        this.isRetransmitByByte = isRetransmitByByte;
        this.port = port;
        this.startTime = startTme;
    }

    /**
     * Конструктор высталяет настройки по умолчанию, и формаирует слуателя в соответсвии с принимаемыми параметрами
     * @param port слушаемый порт
     * @param startTme время начала работы программы
     * @param out поток вывода лога
     * @param isRetransmit флаг, указывающий нужно ли делать ретрансляцию сообщений
     * @param isRetransmitByByte флаг,указываюший нужно делать ретрансляцию по байтам
     */
    public TrueJsscListener(SerialPort port, long startTme, PrintStream out, boolean isRetransmit, boolean isRetransmitByByte) {
        defaultSettings();
        this.out = out;
        this.isRetransmit = isRetransmit;
        this.isRetransmitByByte = isRetransmitByByte;
        this.port = port;
        this.startTime = startTme;
    }

    /**
     * Конструктор высталяет настройки по умолчанию, и формаирует слуателя в соответсвии с принимаемыми параметрами
     * @param port слушаемый порт
     * @param startTme время начала работы программы
     * @param retransmitPort порт из которого будет происходить пересылка сообщений
     * @param out поток вывода лога
     * @param isRetransmit флаг, указывающий нужно ли делать ретрансляцию сообщений
     * @param isRetransmitByByte флаг,указываюший нужно делать ретрансляцию по байтам
     */
    public TrueJsscListener(SerialPort port, long startTme, SerialPort retransmitPort, PrintStream out, boolean isRetransmit, boolean isRetransmitByByte) {
        defaultSettings();
        this.retransmitPort = retransmitPort;
        this.out = out;
        this.isRetransmit = isRetransmit;
        this.isRetransmitByByte = isRetransmitByByte;
        this.port = port;
        this.startTime = startTme;
    }

    /**
     * Устанавливает поток вывода, в который будет записываться лог слушателя
     * @param out поток вывода лога
     */
    public void setOutLog(PrintStream out) {
        this.out = out;
    }

    /**
     * Преобразует время от старта программы, от числа к строчке и выставляет точку
     * @return строчка, содержащее вреия от старта программы в секкундах
     */
    private String outTime(){
        return ""+(System.currentTimeMillis()- startTime) / 1000 + "."+ (System.currentTimeMillis()- startTime)%1000;
    }

    /**
     * Возвращает префикс лога. содержаший информацию о порте и время от сарта программы
     * @return префикс лога
     */
    private String outPrefix() {
        return (port.getPortName() + ", " + outTime() + ": ");
    }

    /**
     * Устанавливает порт, в который, при необходимости, будет произсодить ретрансляция
     * @param retransmitPort Порт в который будет происходить ретрансляция
     */
    public void  setRetransmitPort(SerialPort retransmitPort) {
        this.retransmitPort = retransmitPort;
    }

    /**
     * Устанавливает или выклчает режим ретрансляции со сброкой сообщений
     * @param isRetransmitor флаг того, будет ли происходить ретрансляция
     */
    public void setIsRetransmit(boolean isRetransmitor) {
        this.isRetransmit = isRetransmitor;
        if (isRetransmitor && isRetransmitByByte) {
            isRetransmitByByte = false;
        }
    }

    /**
     * Устанавливает или выключает режим ретрансляции по байту (принял - сразу передал)
     * @param retransmitByByte флаг того, будет ли происходить ретрансляция по байту
     */
    public void setRetransmitByByte(boolean retransmitByByte) {
        this.isRetransmitByByte = retransmitByByte;
        if (retransmitByByte && isRetransmit) {
            isRetransmit = false;
        }
    }

    /**
     * Переодит собщение, находящеется в буффере, являющимся списком, к массиву
     * @return массив сообщения из буфера
     */
    private int[] toArray() {
        int[] array;
        int i;
        if (false) {//port.getPortName().equals("/dev/ttyUSB0")) {
            array = new int[buffer.size() + 1];
            array[0] = (int) '<';
            i = 1;
        } else {
            array = new int[buffer.size()];
            i = 0;
        }
        Iterator<Integer> iterator = buffer.iterator();
        while (iterator.hasNext()) {
            array[i++] = iterator.next();
        }
        return array;
    }

    /**
     * Выводит в установленный поток вывода принимаемцый массив
     * @param array массив для вывода
     */
    private void outIntArray(int[] array) {
        for (int i = 0; i < array.length; i++) {
            out.print(array[i] + " ");
        }
        out.println("");
    }

    /**
     * Пересылает полученное сообщение, собранное в буфере на указанный порт
     */
    private void retransmit() {
        if (buffer == null) {
            return;
        }
        if (retransmitPort == null) {
            System.out.println("Can not Retransmit: retransmit port not added.");
            return;
        }
        try {
            int[] output = toArray();
            retransmitPort.writeIntArray(output);
            out.print(outPrefix() + "ret: ");
            outIntArray(output);
        } catch (SerialPortException e) {
            System.out.println(outPrefix() + "Can not retransmit massage!");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Выводит в поток вывода сообщение о том, что принято сообщение и ыводит это сообщение
     */
    public void acceptMsg() {
        out.print(outPrefix() + "acm: ");
        outIntArray(toArray());
    }

    /**
     * Соытие того, что долгое время (STEP в классе Whiler) ничего не приходило. По получению этого события
     * сообщение, сформированное в буфере считается полученым и при необходимости происходит его ретрансляция.
     */
    public void noAcceptLongTime() {
        //out.println(outPrefix() + "Nol Long Time Event Event.");
        if (isPackage) {
            acceptMsg();
        }
        if (isRetransmit && !isRetransmitByByte) {
            if (!retransmitOk) {
                retransmit();
                retransmitOk = true;
            }
        }
        isPackage = false;
    }

    /**
     * Событие принятия сообщения. переаваемого из PortEventListener. Метод читает массив int ом и
     * обрабатывает их, в зависимости от установленных флагов
     * @param event переменная события
     */
    public void accept(SerialPortEvent event) {
        try {
            int[] input = port.readIntArray(event.getEventValue());//), TIMEOUT);
            //out.print(outPrefix() + "acc: ");
            //outIntArray(input);
            if (!isRetransmitByByte) {
                if (isPackage) {
                    //out.println("AccPack");
                    for (int i = 0; i < input.length; i++) {
                        buffer.add(new Integer(input[i]));
                    }
                } else {
                    //out.print("Start Package!\n");
                    buffer = new LinkedList<Integer>();
                    for (int i = 0; i < input.length; i++) {
                        buffer.add(new Integer(input[i]));
                    }
                    isPackage = true;
                    retransmitOk = false;
                }
            } else {
                if (retransmitPort != null) {
                    try {
                        out.print(outPrefix() + "rbb: ");
                        outIntArray(input);
                        retransmitPort.writeIntArray(input);
                    } catch (SerialPortException e) {
                        System.out.println("Error in accepting event.");
                        System.out.println(e.getMessage());
                    }
                } else {
                    System.out.println("Can not retransmit by byte: retransmit port not added.");
                }
            }
        } catch (SerialPortException e) {
            System.out.print("Can not read from port!");
            System.out.println(e.getMessage());
        } /*catch (SerialPortTimeoutException e) {
            System.out.print("Problem with timeout in read.");
            System.out.println(e.getMessage());
        }*/
    }

}

