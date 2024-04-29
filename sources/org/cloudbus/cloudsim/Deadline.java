package org.cloudbus.cloudsim;

public class Deadline extends Cloudlet {

    private long cloudlet_deadline = 0;
    
    
    public Deadline(int cloudletId, 
    		long cloudletLength, 
    		int pesNumber, 
    		long cloudletFileSize, 
    		long cloudletOutputSize, 
    		UtilizationModel utilizationModelCpu, 
    		UtilizationModel utilizationModelRam, 
    		UtilizationModel utilizationModelBw) {
        super(cloudletId, 
        		cloudletLength, 
        		pesNumber, 
        		cloudletFileSize, 
        		cloudletOutputSize, 
        		utilizationModelCpu, 
        		utilizationModelRam, 
        		utilizationModelBw);
    }
    
    public Deadline(int cloudletId, 
    		long cloudletLength, 
    		int pesNumber, 
    		long cloudletFileSize, 
    		long cloudletOutputSize, 
    		UtilizationModel utilizationModelCpu, 
    		UtilizationModel utilizationModelRam, 
    		UtilizationModel utilizationModelBw, 
    		long deadline) {
        super(cloudletId, 
        		cloudletLength, 
        		pesNumber, 
        		cloudletFileSize, 
        		cloudletOutputSize, 
        		utilizationModelCpu, 
        		utilizationModelRam, 
        		utilizationModelBw);
        this.cloudlet_deadline = deadline;
    }

    public long getCloudlet_deadline() {
        return cloudlet_deadline;
    }

    public void setCloudlet_deadline(int cloudlet_deadline) {
        this.cloudlet_deadline = cloudlet_deadline;
    }
}
