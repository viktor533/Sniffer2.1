import com.sun.corba.se.impl.naming.cosnaming.NamingUtils;
import jssc.SerialPort;
import jssc.SerialPortException;

import java.io.PrintStream;

/**
 * Created by viktor on 4/15/16.
 */
public class Port {
    private SerialPort port;
    private TrueJsscListener listener;
    private PortEventListener portEventListener;
    private Whiler whiler;
    private long startTime;
    private boolean defaultIsRetransmit;
    private boolean defaultIsRetransmitByByte;
    private SerialPort retransmitPort;
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

    public void setOutLog(PrintStream out) {
        this.out = out;
        if (listener != null) {
            listener.setOutLog(out);
        }
    }

    private String outTime(){
        return ""+(System.currentTimeMillis()-startTime) / 1000 + "."+ (System.currentTimeMillis()-startTime)%1000;
    }

    private String outPrefix() {
        return "" + port.getPortName() + ", " + outTime() + ": ";
    }

    public void sendMassage(String massage) {
        try {
            port.writeString(massage);
            out.println(outPrefix() + "snd: " + massage);
        } catch (SerialPortException e) {
            System.out.println("Can not send massage.");
            System.out.println(e.getMessage());
        }
    }

    public void sendMassage(int[] massage) {
        try {
            port.writeIntArray(massage);
            System.out.println(outPrefix() + "snd: ");

        } catch (SerialPortException e) {
            System.out.println("Can not send massage.");
            System.out.println(e.getMessage());
        }
    }

    public void sendMassage(int massage) {
        try {
            port.writeInt(massage);
            System.out.println(outPrefix() + "snd: " + massage);

        } catch (SerialPortException e) {
            System.out.println("Can not send massage.");
            System.out.println(e.getMessage());
        }
    }

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

    public SerialPort getSerialPort() {
        return port;
    }

    public void setNoRetransmit() {
        listener.setIsRetransmit(false);
        listener.setRetransmitByByte(false);
    }

    public void setRetransmit(boolean isRetransmit) {
        if (listener != null) {
            listener.setIsRetransmit(isRetransmit);
        } else {
            this.defaultIsRetransmit = isRetransmit;
        }
    }

    public void setRetransmitByByte(boolean isRetransmitByByte) {
        if (listener != null) {
            listener.setRetransmitByByte(isRetransmitByByte);
        } else {
            defaultIsRetransmitByByte = isRetransmitByByte;
        }
    }

    public void setRetransmitPort(SerialPort retransmitPort) {
        if (listener == null) {
            this.retransmitPort = retransmitPort;
        } else {
            listener.setRetransmitPort(retransmitPort);
        }
    }

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
