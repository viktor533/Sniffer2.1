import com.sun.corba.se.impl.naming.cosnaming.NamingUtils;
import jssc.SerialPort;
import jssc.SerialPortException;

import java.io.PrintStream;

/**
 * Created by viktor on 4/15/16.
 */
public class Port {
    /** Переменная порта */
    private SerialPort port;
    /** Слушатель, в котором происходит обработка событий*/
    private TrueJsscListener listener;
    /** Слушатель, принимающий сообщения из порта и передающий их в listener*/
    private PortEventListener portEventListener;
    /** Поток с бесконечным циклом, генерирущий собтыие, того, что давно не было событий и передающий его в listener*/
    private Whiler whiler;
    /** Время старта программы в милисеккундах */
    private long startTime;
    /** Переменная сохраняющая флаг слушателя, нужно ли делать ретранлсяцию, до создания слушателя */
    private boolean defaultIsRetransmit;
    /** Переменная сохраняющая флаг слушателя, нужно ли делать ретранлсяцию по байту, до создания слушателя */
    private boolean defaultIsRetransmitByByte;
    /** Переменная порта, в который при необходимости будет происходить ретранлсяция */
    private SerialPort retransmitPort;
    /** Поток вывода данных слушателя этого порта. В самом классе вывод происходит в консоль */
    private PrintStream out;

    public Port(SerialPort port, long startTime) {
        this.port = port;
        this.startTime = startTime;
        listener = null;
        portEventListener = null;
        whiler = null;
        retransmitPort = null;
        out = System.out;
    }

    /**
     * Устаналивает поток вывода лога слушателя. По умолчанию - в консоль.
     * @param out Поток вывода лога слушателя
     */
    public void setOutLog(PrintStream out) {
        this.out = out;
        if (listener != null) {
            listener.setOutLog(out);
        }
    }
    /**
     * Преобразует время от старта программы, от числа к строчке и выставляет точку
     * @return строчка, содержащее вреия от старта программы в секкундах
     */
    private String outTime(){
        return ""+(System.currentTimeMillis()-startTime) / 1000 + "."+ (System.currentTimeMillis()-startTime)%1000;
    }

    /**
     * Возвращает префикс лога. содержаший информацию о порте и время от сарта программы
     * @return префикс лога
     */
    private String outPrefix() {
        return "" + port.getPortName() + ", " + outTime() + ": ";
    }

    /**
     * Отправляет сообщение из порта.
     * @param massage отправляемое сообщение
     */
    public void sendMassage(String massage) {
        try {
            port.writeString(massage);
            out.println(outPrefix() + "snd: " + massage);
        } catch (SerialPortException e) {
            System.out.println("Can not send massage.");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Отправляет сообщение из порта, состоящее из массива байтов.
     * В переменной int хранится именно байт, значение должно лежать в диапазона 0-255
     * @param massage отправляемое сообщение
     */
    public void sendMassage(int[] massage) {
        try {
            port.writeIntArray(massage);
            System.out.println(outPrefix() + "snd: ");

        } catch (SerialPortException e) {
            System.out.println("Can not send massage.");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Отправлят сообщение из порта, состоящее из одного байта
     * В переменной int хранится именно байт, значение должно лежать в диапазона 0-255
     * @param massage байт сообщения
     */
    public void sendMassage(int massage) {
        try {
            port.writeInt(massage);
            System.out.println(outPrefix() + "snd: " + massage);

        } catch (SerialPortException e) {
            System.out.println("Can not send massage.");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Создает слушателя, который будет принимать все входящие в порт сообщения
     */
    public void addListener () {
        listener = new TrueJsscListener(port, startTime, retransmitPort, out, defaultIsRetransmit, defaultIsRetransmitByByte); //inputStream, retransmitPort, port.getName(), defaultIsRetransmit, defaultIsRetransmitByByte, startTime);
        whiler = new Whiler(listener);
        portEventListener = new PortEventListener(listener, whiler);
        try {
            port.addEventListener(portEventListener);
            whiler.start();
            out.println("Start listen at: " + outTime());
        } catch (SerialPortException e) {
            System.out.println("Can not add listener: to many listeners added.");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Возвращает экземпляр класса, описывающего данный порт
     * @return порт
     */
    public SerialPort getSerialPort() {
        return port;
    }

    /**
     * Сбрасывает ретрансляцию, какая бы ни была установленна
     */
    public void setNoRetransmit() {
        listener.setIsRetransmit(false);
        listener.setRetransmitByByte(false);
    }

    /**
     * Устанавливает или выклчает режим ретрансляции со сброкой сообщений
     * @param isRetransmit флаг того, будет ли происходить ретрансляция
     */
    public void setRetransmit(boolean isRetransmit) {
        if (listener != null) {
            listener.setIsRetransmit(isRetransmit);
        } else {
            this.defaultIsRetransmit = isRetransmit;
        }
    }

    /**
     * Устанавливает или выключает режим ретрансляции по байту (принял - сразу передал)
     * @param isRetransmitByByte флаг того, будет ли происходить ретрансляция по байту
     */
    public void setRetransmitByByte(boolean isRetransmitByByte) {
        if (listener != null) {
            listener.setRetransmitByByte(isRetransmitByByte);
        } else {
            defaultIsRetransmitByByte = isRetransmitByByte;
        }
    }

    /**
     * Устаналивает порт, из которго при неоходимости будет производиться ретрансляция
     * @param retransmitPort порт ретрансляции
     */
    public void setRetransmitPort(SerialPort retransmitPort) {
        if (listener == null) {
            this.retransmitPort = retransmitPort;
        } else {
            listener.setRetransmitPort(retransmitPort);
        }
    }

    /**
     * Закрывает порт и останавливает поток, анализирующем долго ли не было сообщений.
     */
    public void close() {
        whiler.close();
        try {
            port.closePort();
        } catch (SerialPortException e) {
            System.out.println("Can not close port.");
            System.out.println(e.getMessage());
        }
    }
}

