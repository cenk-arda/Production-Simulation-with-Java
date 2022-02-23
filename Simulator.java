import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;


public class Simulator {

    private static final Simulator _inst = new Simulator();

    public static Simulator getInstance() {
        return _inst;
    }

    private ArrayList<Miner> miners = new ArrayList<Miner>();
    private ArrayList<Smelter> smelters = new ArrayList<Smelter>();
    private ArrayList<Constructor> constructors = new ArrayList<Constructor>();
    private ArrayList<Transporter> transporters = new ArrayList<Transporter>();


    public ArrayList<Miner> getMiners() {
        return miners;
    }

    public ArrayList<Smelter> getSmelters() {
        return smelters;
    }

    public ArrayList<Transporter> getTransporters() {
        return transporters;
    }

    public ArrayList<Constructor> getConstructors() {
        return constructors;
    }


    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);

        int numberOfMiners = reader.nextInt();
        for (int i = 1; i <= numberOfMiners; i++) {
            Miner miner = new Miner(i, reader.nextInt(), reader.nextInt(), reader.nextInt(), reader.nextInt());
            Simulator.getInstance().miners.add(miner);
        }

        int numberOfSmelters = reader.nextInt();
        for (int i = 1; i <= numberOfSmelters; i++) {
            Smelter smelter = new Smelter(i, reader.nextInt(), reader.nextInt(), reader.nextInt(), reader.nextInt());
            Simulator.getInstance().smelters.add(smelter);
        }

        int numberOfConstructors = reader.nextInt();
        for (int i = 1; i <= numberOfConstructors; i++) {
            Constructor constructor = new Constructor(i, reader.nextInt(), reader.nextInt(), reader.nextInt());
            Simulator.getInstance().constructors.add(constructor);
        }

        int numberOfTransporters = reader.nextInt();
        for (int i = 1; i <= numberOfTransporters; i++) {
            Transporter transporter = new Transporter(i, reader.nextInt(), reader.nextInt(), reader.nextInt(), reader.nextInt(), reader.nextInt());
            Simulator.getInstance().transporters.add(transporter);
        }

        HW2Logger.getInstance().Initialize();
        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 0; i < Simulator.getInstance().miners.size(); i++) {
            executor.execute(Simulator.getInstance().miners.get(i));
        }
        for (int i = 0; i < Simulator.getInstance().smelters.size(); i++) {
            executor.execute(Simulator.getInstance().smelters.get(i));
        }
        for (int i = 0; i < Simulator.getInstance().constructors.size(); i++) {
            executor.execute(Simulator.getInstance().constructors.get(i));
        }
        for (int i = 0; i < Simulator.getInstance().transporters.size(); i++) {
            executor.execute(Simulator.getInstance().transporters.get(i));
        }

        executor.shutdown(); //?
        while (!executor.isTerminated()) {
            // wait for every thread to finish before exiting.
            //Note that isTerminated is never true unless
            //either shutdown or shutdownNow was called first
        }


//        for (Miner miner: Simulator.getInstance().miners
//             ) {
//            System.out.println(miner.getID());
//            System.out.println(miner.getInterval());
//            System.out.println(miner.getStorageCapacity());
//            System.out.println(miner.getOreType());
//            System.out.println(miner.getMaxOresCanBeMined());
//        }
//        for (Smelter smelter: Simulator.getInstance().smelters
//        ) {
//            System.out.println(smelter.getID());
//            System.out.println(smelter.getInterval());
//            System.out.println(smelter.getIncomingStorageCapacity());
//            System.out.println(smelter.getOutgoingStorageCapacity());
//            System.out.println(smelter.getIngotType());
//        }
//        for (Constructor constructor: Simulator.getInstance().constructors
//        ) {
//            System.out.println(constructor.getID());
//            System.out.println(constructor.getInterval());
//            System.out.println(constructor.getStorageCapacity());
//            System.out.println(constructor.getType());
//        }
//
//        for (Transporter transporter: Simulator.getInstance().transporters
//        ) {
//            System.out.println(transporter.getID());
//            System.out.println(transporter.getInterval());
//            System.out.println(transporter.getSourceMinerID());
//            System.out.println(transporter.getSourceSmelterID());
//            System.out.println(transporter.getTargetSmelterID());
//            System.out.println(transporter.getTargetConstructorID());
//        }


    }
}
