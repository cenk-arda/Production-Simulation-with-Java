import java.util.Random;
import java.util.stream.DoubleStream;

public class Smelter extends Builder implements Runnable {
    private int ID;
    private int interval;
    private int incomingStorageCapacity;
    private int outgoingStorageCapacity;
    private int ingotType;
    private int oresInIncomingStorage;
    private int ingotsInOutgoingStorage;
    private boolean isActive;

    public Smelter(int id, int interval, int incomingStorageCapacity, int outgoingStorageCapacity, int ingotType) {
        this.ID = id;
        this.interval = interval;
        this.incomingStorageCapacity = incomingStorageCapacity;
        this.outgoingStorageCapacity = outgoingStorageCapacity;
        this.ingotType = ingotType;
        this.oresInIncomingStorage = 0;
        this.ingotsInOutgoingStorage = 0;
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

    public int getIncomingStorageCapacity() {
        return incomingStorageCapacity;
    }

    public void setIncomingStorageCapacity(int incomingStorageCapacity) {
        this.incomingStorageCapacity = incomingStorageCapacity;
    }

    public int getOutgoingStorageCapacity() {
        return outgoingStorageCapacity;
    }

    public void setOutgoingStorageCapacity(int outgoingStorageCapacity) {
        this.outgoingStorageCapacity = outgoingStorageCapacity;
    }

    public int getIngotType() {
        return ingotType;
    }

    public void setIngotType(int ingotType) {
        this.ingotType = ingotType;
    }

    public boolean DecreaseIngots() {
        this.getLock().lock();
        if (this.ingotsInOutgoingStorage > 0) {
            this.ingotsInOutgoingStorage--;
            this.getLock().unlock();
            return true;
        } else {
            this.getLock().unlock();
            return false;
        }
    }

    public boolean checkActiveTransporters() {
        for (int i = 0; i < Simulator.getInstance().getTransporters().size(); i++) {
            if (Simulator.getInstance().getTransporters().get(i).getTargetSmelterID() == ID && Simulator.getInstance().getTransporters().get(i).isActive()) {
                return true;
            }
        }
        return false;
    }

    private boolean isOutgoingFull() {
        return this.ingotsInOutgoingStorage == this.outgoingStorageCapacity;
    }

    @Override
    public void run() {

        HW2Logger.getInstance().Log(0, ID, 0, 0, Action.SMELTER_CREATED);
        this.isActive = true;
        while ((this.ingotType == 0 && this.oresInIncomingStorage >= 1) || (this.ingotType == 1 && this.oresInIncomingStorage >= 2) || this.checkActiveTransporters()) {
            this.WaitCanProduce();
            HW2Logger.getInstance().Log(0, ID, 0, 0, Action.SMELTER_STARTED);
            this.SleepForSmelting();
            if(this.ingotType == 0){
                this.oresInIncomingStorage--;
            }
            else{ //ingotType == 1
                this.oresInIncomingStorage-=2;
            }
            this.ingotsInOutgoingStorage++;
            HW2Logger.getInstance().Log(0, ID, 0, 0, Action.SMELTER_FINISHED);
            this.IngotProduced();
        }
        this.SmelterStopped();
        HW2Logger.getInstance().Log(0, ID, 0, 0, Action.SMELTER_STOPPED);
    }
    //IRON: 0
    //COPPER: 1

    private void SmelterStopped() {
        this.getLock().lock();
        this.isActive = false;
        this.getAnItemProduced().signalAll(); //for those transporters waiting the next load.
        this.getLock().unlock();
    }

    private void IngotProduced() {
        this.getLock().lock();
        this.getAnItemProduced().signal();
        this.AvailableSpaceForUnload().signalAll();
        this.getLock().unlock();
    }


    private boolean ActiveTransportersWillTake(){
        for (int i = 0; i < Simulator.getInstance().getTransporters().size(); i++) {
            if (Simulator.getInstance().getTransporters().get(i).getSourceSmelterID() == ID && Simulator.getInstance().getTransporters().get(i).isActive()) {
                return true;
            }
        }
        return false;
    }
    private void WaitCanProduce() {//problem
    //iron, need 1 ore for iron ingot.//copper, need 2 ores for copper ingot.

        while (isOutgoingFull()) {//or if?
            this.getLock().lock();
            try {
                this.getAnItemTakenCondition().await();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                this.getLock().unlock();
            }
        }
        while ( this.checkActiveTransporters() && ((this.ingotType == 0 && this.oresInIncomingStorage < 1) || (this.ingotType == 1 && this.oresInIncomingStorage < 2)) ) {
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

    public void SleepForSmelting() {
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
    public int OutStorageCount() {
        return this.ingotsInOutgoingStorage;
    }

    @Override
    public int InStorageCount() {
        return this.oresInIncomingStorage;
    }

    @Override
    public int OutStorageCapacity() {
        return this.outgoingStorageCapacity;
    }

    @Override
    public int InStorageCapacity() {
        return this.incomingStorageCapacity;
    }

    @Override
    public boolean isEmpty() { //wait while checking this i.e. while no product has builded yet to take.
        return (this.ingotsInOutgoingStorage == 0);
    }

    @Override
    public boolean isFull() { //wait while checking this i.e. while there is no space to put materials.
        return (this.oresInIncomingStorage == this.incomingStorageCapacity);
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public boolean decreaseItem() {
        return this.DecreaseIngots();
    }

    @Override
    public boolean increaseItem() {
        this.getLock().lock();
        if (!this.isFull()) {
            this.oresInIncomingStorage++;
            this.getLock().unlock();
            return true;
        } else {
            this.getLock().unlock();
            return false;
        }
    }


    @Override
    public Type typeOfBuilder() {
        return Type.SMELTER;
    }
}

