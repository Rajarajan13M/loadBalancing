package lb;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.CustomDatacenter;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class Impl {
    
    private static final int NUM_VMS = 4;
    private static final int HOST_MIPS= 15000;
    private static final int MIPS_PER_VM= HOST_MIPS/NUM_VMS;
    private static final int NUM_CLOUDLETS = 50;
    private static final int TASK_LENGTH_THRESHOLD = 1000000;
    private static final int DEADLINE_THRESHOLD = 2000;

    public static void main(String[] args) {

        try {
        	int num_user = 1; 
    		Calendar calendar = Calendar.getInstance();
    		boolean trace_flag = false;
    		CloudSim.init(num_user, calendar, trace_flag);
    		int numDatacenters = 2;             
            List<Datacenter> datacenters = new ArrayList<>();
            for (int i = 0; i < numDatacenters; i++) {
                Datacenter datacenter = createDatacenter("Datacenter" + (i + 1));
                datacenters.add(datacenter);
            }
            CustomDatacenter broker = new CustomDatacenter("Broker");
            List<Vm> vmList = createVms(NUM_VMS, broker.getId());
            List<Cloudlet> cloudletList = createCloudlets(NUM_CLOUDLETS, broker.getId());
            broker.submitVmList(vmList);           
            broker.submitCloudletList(cloudletList);   
            CloudSim.startSimulation();
            List<Cloudlet> resultList = broker.getCloudletReceivedList();
            printCloudletList(resultList);  
            calculateMetrics(vmList,cloudletList);
            CloudSim.stopSimulation();
            Log.printLine("Success");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("error");
        }
    }

    private static Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(HOST_MIPS)));
        hostList.add(new Host(1, new RamProvisionerSimple(2048), new BwProvisionerSimple(1000000), 1000000, peList, new VmSchedulerTimeShared(peList)));
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;
        LinkedList<Storage> storageList = new LinkedList<>();
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 10);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static List<Vm> createVms(int numVms, int brokerId) {
        List<Vm> vmList = new ArrayList<>();
        for (int i = 0; i < numVms; i++) {
            Vm vm = new Vm(i, brokerId, MIPS_PER_VM, 1, 512, 1000, 100, "Xen", new CloudletSchedulerTimeShared());
            vmList.add(vm);
        }
        return vmList;
    }

    private static List<Cloudlet> createCloudlets(int numCloudlets, int brokerId) {
        List<Cloudlet> cloudletList = new ArrayList<>();
        Random random = new Random();
        double baseArrivalTime = 0.0;
        double maxRandomDelay = 20.0;
        for (int i = 0; i < numCloudlets; i++) {
            long length = Math.round(Math.random() * TASK_LENGTH_THRESHOLD);
            long deadline = Math.round(Math.random() * DEADLINE_THRESHOLD);
            int fileSize = 300;
            int outputSize = 300;  
            UtilizationModel utilizationModel = new UtilizationModelFull();            
            double randomDelay = random.nextDouble() * maxRandomDelay;
            double arrivalTime = baseArrivalTime + randomDelay;            
            Cloudlet cloudlet = new Cloudlet(i, length, 1, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel, deadline);
            cloudlet.setUserId(brokerId);         
            cloudlet.setStartTime(arrivalTime);            
            cloudletList.add(cloudlet);
        }
        return cloudletList;
    }

private static void calculateMetrics(List<Vm> vmList, List<Cloudlet> cloudletList) {
        double totalExT = 0.0;
        double totalMT = 0.0;
        double avgExt = 0.0;
        double avgMT = 0.0;
        int mC = 0;
        double vC = 0.0;
        for (Vm vm : vmList) {
            double maxCT = Double.MIN_VALUE;
            for (Cloudlet cloudlet : cloudletList) {
                if (cloudlet.getVmId() == vm.getId()) {
                    double completionTime = cloudlet.getFinishTime();
                    if (completionTime > maxCT) {
                        maxCT = completionTime;
                    }
                }
            }
            totalMT += maxCT;
        }
        avgMT=totalMT/NUM_VMS;
        for (Cloudlet cloudlet : cloudletList) {
            totalExT += cloudlet.getActualCPUTime() - cloudlet.getExecStartTime();
        }
        avgExt=totalExT/NUM_CLOUDLETS;
        double RU = (avgExt / avgMT);
        double RUavg = (RU)*100;       
        for (Vm vm : vmList) {
        	mC += vm.getMigrationCost();
        }
        for (Cloudlet cloudlet : cloudletList) {
            vC += cloudlet.getViolationCost();
        }
        System.out.println(" ");
        if(RUavg>100) {
        	System.out.println("ERROR");
        }
        else {
	        System.out.println("Makespan : " + avgMT);
	        System.out.println("Execution Time : " + avgExt);
	        System.out.println("Resource Utilization : " + RUavg + " %");
	        System.out.println("Violation Cost: " + vC);
	        System.out.println("Migration Cost: " + mC);
        }
    }


    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;
        DecimalFormat dft = new DecimalFormat("0000.00");
        DecimalFormat dft2 = new DecimalFormat("00.00");
        String indent = "           ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("   Cloudlet ID" + indent 
        		+ "Status" + indent 
        		+ "Length" + indent 
        		+ "Deadline" + "     " 
        		+ "VM ID" + indent 
        		+ "Time" + indent 
        		+ "Start Time" + "    " 
        		+ "Finish Time");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + String.format("%02d", cloudlet.getCloudletId()) + indent );
            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");
                Log.printLine(indent + String.format("%06d", cloudlet.getCloudletLength())        		
				+ indent + String.format("%04d",cloudlet.getCloudlet_deadline())
				+ indent + cloudlet.getVmId()
				+ indent + dft.format(cloudlet.getActualCPUTime()) 
				+ indent + dft2.format(cloudlet.getExecStartTime())
				+ indent + dft.format(cloudlet.getFinishTime()));
            }
        }
    }
}