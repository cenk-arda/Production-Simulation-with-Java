import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Builder {

    private Lock lock = new ReentrantLock();
    private Condition AnItemTaken = lock.newCondition();

    private Condition AnItemProduced = lock.newCondition();

    private Condition AvailableSpace = lock.newCondition();

    private Condition ArrivedMaterial = lock.newCondition();

    public Lock getLock() {
        return lock;
    }

    public Condition getAnItemTakenCondition() {
        return AnItemTaken;
    }

    public Condition getAnItemProduced() {
        return AnItemProduced;
    }

    public Condition AvailableSpaceForUnload() {
        return AvailableSpace;
    }
    public Condition MaterialArrived(){
        return ArrivedMaterial;
    }

    public abstract int OutStorageCount(); // returns number of items in outgoing storage

    public abstract int InStorageCount(); // returns number of items in ingoing storage

    public abstract int OutStorageCapacity();

    public abstract int InStorageCapacity();

    public abstract boolean isEmpty(); //subject of WaitNextLoad(outgoing storage).

    public abstract boolean isFull(); // subject of WaitUnload(ingoing storage)

    public abstract boolean isActive();

    public abstract boolean decreaseItem();

    public abstract boolean increaseItem();

    public abstract Type typeOfBuilder();



}
