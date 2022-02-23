import java.util.Random;
import java.util.concurrent.locks.*;
import java.util.stream.DoubleStream;

public class Miner extends Builder implements Runnable{
    private int ID;
    private int interval;
    private int storageCapacity;
    private int oreType;
    private int maxOresCanBeMined;
    private int oresInStorage;
    private int producedOres;
    private boolean isActive;


    public Miner(int id, int interval, int storageCapacity, int oreType, int maxOresCanBeMined) {
        this.ID = id;
        this.interval = interval;
        this.storageCapacity = storageCapacity;
        this.oreType = oreType;
        this.maxOresCanBeMined = maxOresCanBeMined;
        this.oresInStorage = 0;
        this.producedOres = 0;
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

    public int getOreType() {
        return oreType;
    }

    public void setOreType(int oreType) {
        this.oreType = oreType;
    }

    public int getMaxOresCanBeMined() {
        return maxOresCanBeMined;
    }

    public void setMaxOresCanBeMined(int maxOresCanBeMined) {
        this.maxOresCanBeMined = maxOresCanBeMined;
    }


    @Override
    public void run() {
        HW2Logger.getInstance().Log(ID, 0, 0, 0, Action.MINER_CREATED);
        this.isActive = true;
        while (producedOres < maxOresCanBeMined) {
            this.WaitCanProduce();
            HW2Logger.getInstance().Log(ID, 0, 0, 0, Action.MINER_STARTED);
            this.SleepForMining();
            this.oresInStorage++;
            this.producedOres++;
            HW2Logger.getInstance().Log(ID, 0, 0, 0, Action.MINER_FINISHED);
            this.OreProduced();
        }
        this.MinerStopped();
        HW2Logger.getInstance().Log(ID, 0, 0, 0, Action.MINER_STOPPED);
    }

    public void WaitCanProduce() {
        while (this.isFull()) {//or if?
            this.getLock().lock();
            try {
                this.getAnItemTakenCondition().await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                this.getLock().unlock();
            }
        }
    }

    public void SleepForMining() {
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

    public void OreProduced() {
        this.getLock().lock();
        this.getAnItemProduced().signalAll();
        this.getLock().unlock();
    }

    public void MinerStopped() {
        this.getLock().lock();
        this.isActive = false;
        this.getAnItemProduced().signalAll(); //for those transporters waiting the next load.
        this.getLock().unlock();
    }

    public boolean DecreaseOre() {
        this.getLock().lock();
        if(this.oresInStorage > 0) {
            this.oresInStorage--;
            this.getLock().unlock();
            return true;
        }
        else{
            this.getLock().unlock();
            return false;
        }

    }

    @Override
    public int OutStorageCount() {
        return this.oresInStorage;
    }

    @Override
    public int InStorageCount() { //should not be used
        return 0;  //does not have ingoing storage. not a subject of any WaitUnload.
    }

    @Override
    public int OutStorageCapacity() {
        return this.storageCapacity;
    }

    @Override
    public int InStorageCapacity() {
        return 0; //no incoming storage.
    }

    @Override
    public boolean isEmpty() { //
        return (oresInStorage==0);
    }

    @Override
    public boolean isFull() {  // subject of waitCanProduce.
        return oresInStorage == storageCapacity;
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public boolean decreaseItem() {
        return this.DecreaseOre();
    }

    @Override
    public boolean increaseItem() { // miner has no incoming storage, hence cannot increase it.
        return false;
    }

    @Override
    public Type typeOfBuilder() {
        return Type.MINER;
    }
}

