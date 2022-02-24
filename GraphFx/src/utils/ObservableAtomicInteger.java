package utils;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableIntegerValue;

import java.util.concurrent.atomic.AtomicInteger;

public class ObservableAtomicInteger implements ObservableIntegerValue {
    AtomicInteger integer;
    IntegerProperty observable;

    public ObservableAtomicInteger(int value) {
        integer = new AtomicInteger(0);
        observable = new SimpleIntegerProperty(0);
    }

    @Override
    public int get() {
        return integer.get();
    }

    @Override
    public int intValue() {
        return integer.intValue();
    }

    @Override
    public long longValue() {
        return integer.longValue();
    }

    @Override
    public float floatValue() {
        return integer.floatValue();
    }

    @Override
    public double doubleValue() {
        return integer.doubleValue();
    }

    @Override
    public void addListener(ChangeListener<? super Number> listener) {
        observable.addListener(listener);
    }

    @Override
    public void removeListener(ChangeListener<? super Number> listener) {
        observable.removeListener(listener);
    }

    @Override
    public Number getValue() {
        return integer.get();
    }

    @Override
    public void addListener(InvalidationListener listener) {
        observable.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        observable.removeListener(listener);
    }

    public synchronized int incrementAndGet() {
        int num = integer.incrementAndGet();
        observable.set(num);
        return num;
    }

    public synchronized int getAndSet(int value) {
        int num = integer.getAndSet(value);
        observable.set(value);
        return num;
    }

    public StringBinding asString() {
        return observable.asString();
    }


    public ObjectProperty<Integer> asObject() {
        return observable.asObject();
    }

}
