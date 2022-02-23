import java.util.Random;
import java.util.stream.DoubleStream;

public class Constructor extends Builder implements Runnable {
    private int ID;
    private int interval;
    private int storageCapacity;
    private int type;
    private int itemsInIncomingStorage;
    private boolean isActive;

    public Constructor(int id, int interval, int storageCapacity, int type) {
        this.ID = id;
        this.interval = interval;
        this.storageCapacity = storageCapacity;
        this.type = type;
        this.itemsInIncomingStorage = 0;
        this.isActive = true;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getStorageCapacity() {
        return storageCapacity;
    }

    public void setStorageCapacity(int storageCapacity) {
        this.storageCapacity = storageCapacity;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    // IRON: 0
    // COPPER: 1
    // LIMESTONE: 2
    @Override
    public void run() {
        HW2Logger.getInstance().Log(0, 0, 0, ID, Action.CONSTRUCTOR_CREATED);
        while ( this.checkActiveTransporters() || ((this.type == 0 && this.itemsInIncomingStorage >= 2) || (this.type == 1 && this.itemsInIncomingStorage >= 1) || (this.type == 2 && this.itemsInIncomingStorage >= 2))) {
            this.WaitCanProduce();
            HW2Logger.getInstance().Log(0, 0, 0, ID, Action.CONSTRUCTOR_STARTED);
            this.SleepForConstructing();
            if (this.type == 1) {
                this.itemsInIncomingStorage--;
            } else if (this.type == 0) { //ingotType == 1
                this.itemsInIncomingStorage -= 2;
            } else { //type == 2, limestone.
                this.itemsInIncomingStorage -= 2;
            }
            HW2Logger.getInstance().Log(0, 0, 0, ID, Action.CONSTRUCTOR_FINISHED);
            this.ConstructorProduced();
        }
        HW2Logger.getInstance().Log(0, 0, 0, ID, Action.CONSTRUCTOR_STOPPED);
        this.ConstructorStopped();
    }


    private void ConstructorStopped() {
        this.getLock().lock();
        this.isActive = false;
        this.AvailableSpaceForUnload().signalAll(); // signal transporters waiting for unload
        this.getLock().unlock();
    }

    private void ConstructorProduced() {
        this.getLock().lock();
        this.AvailableSpaceForUnload().signalAll();
        this.getLock().unlock();
    }

    // IRON: 0
    // COPPER: 1
    // LIMESTONE: 2
    private void WaitCanProduce() {
        //iron, need 1 ore for iron ingot.//copper, need 2 ores for copper ingot.
        while ( ((this.type == 0 && this.itemsInIncomingStorage < 2) || (this.type == 1 && this.itemsInIncomingStorage < 1) || (this.type == 2 && this.itemsInIncomingStorage < 2))) {

            this.getLock().lock();
            try {
                this.MaterialArrived().await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                this.getLock().unlock();
            }


            //unlock here?
        }


    }

    public boolean checkActiveTransporters() {
        for (int i = 0; i < Simulator.getInstance().getTransporters().size(); i++) {
            if (Simulator.getInstance().getTransporters().get(i).getTargetConstructorID() == ID && Simulator.getInstance().getTransporters().get(i).isActive()) {
                return true;
            }
        }
        return false;
    }

    public void SleepForConstructing() {
        //Sleep a value in range of Interval ± (Interval × 0.01) milliseconds for mining
        //duration
        Random random = new Random(System.currentTimeMillis());
        DoubleStream stream;
        stream = random.doubles(1, interval - interval * 0.01, interval + interval * 0.01);
        try {
            Thread.sleep((long) stream.findFirst().getAsDouble());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int OutStorageCount() { //does not have outgoing storage.
        return 0;
    }

    @Override
    public int InStorageCount() {
        return itemsInIncomingStorage;
    }

    @Override
    public int OutStorageCapacity() { //does not have outgoing storage capacity.
        return 0;
    }

    @Override
    public int InStorageCapacity() {
        return storageCapacity;
    }

    @Override
    public boolean isEmpty() { //for outgoing storage. always true for constructors. no constraint
        return true;
    }

    @Override
    public boolean isFull() { //incoming
        return itemsInIncomingStorage == storageCapacity;
    }

    @Override
    public boolean isActive() { //TODO
        return this.isActive;
    }

    @Override
    public boolean decreaseItem() { //TODO
        return false;
    }

    @Override
    public boolean increaseItem() { //TODO
        this.getLock().lock();
        if (itemsInIncomingStorage < storageCapacity) {
            this.itemsInIncomingStorage++;
            this.getLock().unlock();
            return true;
        } else {
            this.getLock().unlock();
            return false;
        }
    }

    @Override
    public Type typeOfBuilder() {
        return Type.CONSTRUCTOR;
    }
}
