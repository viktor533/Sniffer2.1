import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;

/**
 * Слушатель, удовлетворящий интерфейсу jssc.
 * При принятии события, передает его в экземпляр класса TrueJsscListener и
 * обновляет время последнего принятого события в экзамеляре класса Whiler.
 * Created by viktor on 4/15/16.
 */
public class PortEventListener implements SerialPortEventListener {
    private TrueJsscListener listener;
    private Whiler whiler;

    /**
     * Конструктор принимает необходимые экземпляры классов
     * @param listener Слушатель, которому будут передаваться события
     * @param whiler Поток с бесконеынм циклом.
     */
    public PortEventListener(TrueJsscListener listener, Whiler whiler) {
        this.listener = listener;
        this.whiler = whiler;
    }

    /**
     * Метод, передаюший событие и обновляюший время посленего принятого события в Whiler -е
     * @param event событие, передаваемое jssc.SerialPort
     */
    @Override
    public void serialEvent(SerialPortEvent event) {
        whiler.setLastAcceptedTime(System.currentTimeMillis());
        listener.accept(event);
    }

}

