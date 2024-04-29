package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;

public class Opti extends DatacenterBroker {
	
    public Opti (String name) throws Exception {
		super(name);
	}  
	@Override
	protected void submitCloudlets() {
		Random random = new Random();
		for (Cloudlet cloudlet : getCloudletList()) {
			int maxPriority = Integer.MAX_VALUE;
			for (Vm vm : vmsCreatedList) {
			    int priority = vm.getPriority();
			    if (priority < maxPriority) {
			        maxPriority = priority;
			    }
			}
			List<Vm> vmsWithMaxPriority = new ArrayList<>();
			for (Vm vm : vmsCreatedList) {
			    if (vm.getPriority() == maxPriority) {
			        vmsWithMaxPriority.add(vm);
			    }
			}
			Vm vm;
			Vm vm1;
			int vmIndex = random.nextInt(vmsWithMaxPriority.size());
			vm = vmsWithMaxPriority.get(vmIndex);			
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
					+ cloudlet.getCloudletId() + " to VM #" + vm.getId());
			double estimatedFinishTime = cloudlet.getStartTime() + cloudlet.getCloudletLength() / vm.getMips();
	        if (estimatedFinishTime > cloudlet.getCloudlet_deadline()) {
	        	cloudlet.violationCostInc(estimatedFinishTime, cloudlet.getCloudlet_deadline());
	            Log.printLine("Violation for Cloudlet " + cloudlet.getCloudletId());
                Log.printLine(" on VM #" + vm.getId() + ", migrating cloudlet...");
                int vmIndexM;
                do {
                	vmIndexM = migrateCloudlet(vmIndex);
                }
                while (!vmsWithMaxPriority.contains(vmsCreatedList.get(vmIndexM)));
                vm1 = getVmsCreatedList().get(vmIndex);
                double mtc = vm1.getMips();
                if (vmIndexM != -1) {
                    vm = getVmsCreatedList().get(vmIndexM);
                    vm.incMigrationCost(vmsWithMaxPriority.size());
                    vm1.setMips(mtc-50);
                    double mta = vm.getMips();
                    vm.setMips(mta+50);
                    vm.setPriority(2);
                    Log.printLine("Cloudlet migrated to VM #" + vm.getId());
                } 
            }	        			
			cloudlet.setVmId(vm.getId());
			send(getVmsToDatacentersMap().get(vm.getId()),cloudlet.getStartTime() ,CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;			
	        vmIndex = random.nextInt(getVmsCreatedList().size());
			getCloudletSubmittedList().add(cloudlet);
		}		
		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
			getCloudletList().remove(cloudlet);
		}		
	}
	

	

	
    
}