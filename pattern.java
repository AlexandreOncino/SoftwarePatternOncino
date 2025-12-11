import java.util.ArrayList;
import java.util.List;

// ----- Subject -----
interface Subject {
    void addObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers();
}

// ----- Concrete Subject -----
class WeatherStation implements Subject {
    private List<Observer> observers = new ArrayList<>();
    private float temperature;

    public void setTemperature(float temperature) {
        this.temperature = temperature;
        notifyObservers();
    }

    public float getTemperature() {
        return temperature;
    }

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers)
            o.update(temperature);
    }
}

// ----- Observer -----
interface Observer {
    void update(float temperature);
}

// ----- Concrete Observers -----
class PhoneDisplay implements Observer {
    @Override
    public void update(float temperature) {
        System.out.println("[Phone] Temperature: " + temperature + "°C");
    }
}

class WindowDisplay implements Observer {
    @Override
    public void update(float temperature) {
        System.out.println("[Window] Temperature: " + temperature + "°C");
    }
}

// ----- Demo -----
public class pattern {
    public static void main(String[] args) {
        WeatherStation station = new WeatherStation();
        Observer phone = new PhoneDisplay();
        Observer window = new WindowDisplay();

        station.addObserver(phone);
        station.addObserver(window);

        station.setTemperature(22.5f);
        station.setTemperature(25.0f);
    }
}
