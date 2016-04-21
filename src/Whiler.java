/**
 * Класс, запускающийся в отдельном потоке и в бесконечном цикле и проверязий время, прошедшее после
 * срабатывания посленего события Event. В случае, если оно больше константы STEP, вызывает у
 * экземпляра класса TrueJsscListener метод noAcceptLongTime.
 * Created by viktor on 4/15/16.
 */
public class Whiler extends Thread {
    /** Вреия последнего срабатываения события Event */
    private long lastAcceptedTime;
    /** Шаг ожидани остутствия события Event */
    public final long STEP = 5;
    /** Слушатель, который оповещается о том, что давно (STEP) не было событий Event */
    private TrueJsscListener listener;
    /** Флаг того, что событие долгого отсутствия события, было вызвано */
    private boolean isLongTimeEvenySend;
    /** Флаг прекращения работы потока */
    private boolean setClose;

    /**
     * Конструктор принимает слушателя, который будет оповещаться классом.
     * @param listener Слушатель, который оповещается о том, что давно (STEP) не было событий Event
     */
    public Whiler(TrueJsscListener listener) {
        this.listener = listener;
        isLongTimeEvenySend = true;
        setClose = false;
    }

    /**
     * Метод прекращающий работу потока
     */
    public void close() {
        setClose = true;
    }

    /**
     * Основной метод. запускающий поток с бесконецным циклом.
     */
    @Override
    public void run() {
        while(true) {
            if (!isInterrupted()) {
                if (!isLongTimeEvenySend && (System.currentTimeMillis() - lastAcceptedTime > STEP)) {
                    listener.noAcceptLongTime();
                    isLongTimeEvenySend = true;
                }
                if ((System.currentTimeMillis() - lastAcceptedTime <= STEP)) {
                    isLongTimeEvenySend = false;
                }
            }
            if (setClose) {
                break;
            }
        }
    }

    /**
     * Обновление времения последнего принятого события
     * @param lastAcceptedTime Время последнего принятого побытия
     */
    public void setLastAcceptedTime(long lastAcceptedTime) {
        this.lastAcceptedTime = lastAcceptedTime;
    }
}
