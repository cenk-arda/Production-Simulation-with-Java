import java.util.Random;
import java.util.stream.DoubleStream;

public class Transporter implements Runnable {
    private int ID;
    private int interval;

    // only one of sourceMinerID and sourceSmelterID can be 0.
    // likewise, only one of targetSmelterID and targetConstructorId can be 0.
    private boolean isActive;
    private int sourceMinerID; //if 0, source is not a miner, its smelter.
    private int sourceSmelterID; // if 0, source is not a smelter, its miner.
    private int targetSmelterID; // if 0, target is not a smelter, its a constructor.
    private int targetConstructorID; // if 0, target is not a constructor, its a smelter.
    private Builder source; //miner or smelter
    private Builder target; //smelter or constructor

    public Transporter(int id,
                       int interval,
                       int sourceMinerID,
                       int sourceSmelterID,
                       int targetSmelterID,
                       int targetConstructorID) {
        this.ID = id;
        this.interval = interval;
        this.sourceMinerID = sourceMinerID;
        this.sourceSmelterID = sourceSmelterID;
        this.targetSmelterID = targetSmelterID;
        this.targetConstructorID = targetConstructorID;
        this.isActive = true; //set to false when out of run.
        if (sourceMinerID != 0) {
            this.source = Simulator.getInstance().getMiners().get(sourceMinerID - 1);
        } else {
            this.source = Simulator.getInstance().getSmelters().get(sourceSmelterID - 1);
        }
        if (targetSmelterID != 0) {
            this.target = Simulator.getInstance().getSmelters().get(targetSmelterID - 1);
        } else {
            this.target = Simulator.getInstance().getConstructors().get(targetConstructorID - 1);
        }
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

    public int getSourceMinerID() {
        return sourceMinerID;
    }

    public void setSourceMinerID(int sourceMinerID) {
        this.sourceMinerID = sourceMinerID;
    }

    public int getSourceSmelterID() {
        return sourceSmelterID;
    }

    public void setSourceSmelterID(int sourceSmelterID) {
        this.sourceSmelterID = sourceSmelterID;
    }

    public int getTargetSmelterID() {
        return targetSmelterID;
    }

    public void setTargetSmelterID(int targetSmelterID) {
        this.targetSmelterID = targetSmelterID;
    }

    public int getTargetConstructorID() {
        return targetConstructorID;
    }

    public void setTargetConstructorID(int targetConstructorID) {
        this.targetConstructorID = targetConstructorID;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public void run() {

        HW2Logger.getInstance().Log(0, 0, ID, 0, Action.TRANSPORTER_CREATED);

        if (this.source.typeOfBuilder() == Type.MINER) {
            HW2Logger.getInstance().Log(this.sourceMinerID, 0, ID, 0, Action.TRANSPORTER_GO);
        } else {
            HW2Logger.getInstance().Log(0, this.sourceSmelterID, ID, 0, Action.TRANSPORTER_GO);
        }
        while (source.isActive() || (source.OutStorageCount() > 0) ) {
//            if(ID==2){
//                System.out.println("2 girdi");
//            }
            this.SleepForTravelling();
            if (this.source.typeOfBuilder() == Type.MINER) {
                HW2Logger.getInstance().Log(this.sourceMinerID, 0, ID, 0, Action.TRANSPORTER_ARRIVE);
            } else {
                HW2Logger.getInstance().Log(0, this.sourceSmelterID, ID, 0, Action.TRANSPORTER_ARRIVE);
            }
            boolean shouldContinue = this.WaitNextLoad();
            if(!shouldContinue) break;
            if (this.source.typeOfBuilder() == Type.MINER) {
                HW2Logger.getInstance().Log(this.sourceMinerID, 0, ID, 0, Action.TRANSPORTER_TAKE);
            } else {
                HW2Logger.getInstance().Log(0, this.sourceSmelterID, ID, 0, Action.TRANSPORTER_TAKE);
            }
            this.Loaded();
            if (this.source.typeOfBuilder() == Type.MINER) {
                if (this.target.typeOfBuilder() == Type.SMELTER) {
                    HW2Logger.getInstance().Log(this.sourceMinerID, this.targetSmelterID, ID, 0, Action.TRANSPORTER_GO);
                } else {
                    HW2Logger.getInstance().Log(this.sourceMinerID, 0, ID, this.targetConstructorID, Action.TRANSPORTER_GO);
                }
            } else {
                HW2Logger.getInstance().Log(0, this.sourceSmelterID, ID, this.targetConstructorID, Action.TRANSPORTER_GO);
            }
            this.SleepForTravelling();
            if (this.target.typeOfBuilder() == Type.SMELTER) {
                HW2Logger.getInstance().Log(0, this.targetSmelterID, ID, 0, Action.TRANSPORTER_ARRIVE);
            } else {
                HW2Logger.getInstance().Log(0, 0, ID, this.targetConstructorID, Action.TRANSPORTER_ARRIVE);
            }

            this.WaitUnload();
//            if(!this.isActive()){
//                break;
//            }
            if (this.target.typeOfBuilder() == Type.SMELTER) {
                HW2Logger.getInstance().Log(0, this.targetSmelterID, ID, 0, Action.TRANSPORTER_DROP);
            } else {
                HW2Logger.getInstance().Log(0, 0, ID, this.targetConstructorID, Action.TRANSPORTER_DROP);
            }
            this.Unloaded();
            if(this.source.typeOfBuilder()==Type.MINER){
                if(this.target.typeOfBuilder()==Type.SMELTER){
                    HW2Logger.getInstance().Log(this.sourceMinerID, this.targetSmelterID, ID, 0, Action.TRANSPORTER_GO);
                }
                else{
                    HW2Logger.getInstance().Log(this.sourceMinerID, 0, ID, this.targetConstructorID, Action.TRANSPORTER_GO);
                }
            }
            else{
                HW2Logger.getInstance().Log(0, this.sourceSmelterID, ID, this.targetConstructorID, Action.TRANSPORTER_GO);
            }
           // System.out.println("Transporter "+ ID + "stucked");

        }


//        this.target.getLock().lock();
//        this.target.MaterialArrived().signalAll();
//        this.target.getLock().unlock();
        this.isActive = false;
        HW2Logger.getInstance().Log(0, 0, ID, 0, Action.TRANSPORTER_STOPPED);
    }


    public void SleepForTravelling() {
        //Sleep a value in range of Interval ± (Interval × 0.01) milliseconds for travelling
        Random random = new Random(System.currentTimeMillis());
        DoubleStream stream;
        stream = random.doubles(1, interval - interval * 0.01, interval + interval * 0.01);
        try {
            Thread.sleep((long) stream.findFirst().getAsDouble());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean WaitNextLoad() { //wait for source to produce something.
        while (!this.source.decreaseItem()) {
            if(!this.source.isActive()) return false;
            this.source.getLock().lock();
            try {
                this.source.getAnItemProduced().await();
                // check whether source is still active. if not, let other transporters quit.
                // i.e if not active, break
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally{
                this.source.getLock().unlock();
            }

        }

        return true;
    }

    private void Loaded() {  //this transporter took a item from some source's out storage.
        this.source.getLock().lock();
        this.source.getAnItemTakenCondition().signal();
        this.source.getLock().unlock();
    }

    private void Unloaded() { //this transporter bring an item to some source's in storage
        this.target.getLock().lock();
        this.target.MaterialArrived().signal();
        this.target.getLock().unlock();
    }

    private void WaitUnload() { //if full, wait for availablespace in incomingstorage of target.?
        while (!this.target.increaseItem()) { //if cannot increase, wait.
//            if(!this.target.isActive()) {
//                this.isActive = false;
//                break;
//            }

            this.target.getLock().lock();
            try {
                this.target.AvailableSpaceForUnload().await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                this.target.getLock().unlock();
            }
        }

    }
}
