package org.cloudbus.cloudsim;

import java.util.Random;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;

public class CustomDatacenter extends DatacenterBroker {
	
    public CustomDatacenter (String name) throws Exception {
		super(name);
	}
	@Override
	protected void submitCloudlets() {
		int vmIndex = 0;
		for (Cloudlet cloudlet : getCloudletList()) {
			Vm vm;
			Vm vm1;
			vm = getVmsCreatedList().get(vmIndex);
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
					+ cloudlet.getCloudletId() + " to VM #" + vm.getId());
			double estimatedFinishTime = cloudlet.getStartTime() + cloudlet.getCloudletLength() / vm.getMips();
	        if (estimatedFinishTime > cloudlet.getCloudlet_deadline()) {
	        	cloudlet.violationCostInc(estimatedFinishTime, cloudlet.getCloudlet_deadline());
	            Log.printLine("Violation for Cloudlet " + cloudlet.getCloudletId());
                Log.printLine(" on VM #" + vm.getId() + ", migrating cloudlet...");
                int vmIndexM = migrateCloudlet(vmIndex);
                vm1 = getVmsCreatedList().get(vmIndex);
                double mtc = vm1.getMips();
                if (vmIndexM != -1) {
                    vm = getVmsCreatedList().get(vmIndexM);
                    vm.incMigrationCost(getVmsCreatedList().size());
                    vm1.setMips(mtc-50);
                    double mta = vm.getMips();
                    vm.setMips(mta+50);
                    Log.printLine("Cloudlet migrated to VM #" + vm.getId());
                } 
            }	        			
			cloudlet.setVmId(vm.getId());
			send(getVmsToDatacentersMap().get(vm.getId()),cloudlet.getStartTime() ,CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;			
			Random random = new Random();
	        vmIndex = random.nextInt(getVmsCreatedList().size());
			getCloudletSubmittedList().add(cloudlet);
		}		
		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
			getCloudletList().remove(cloudlet);
		}
	}

	

	
    
}