package project;

import javax.swing.Timer;

public class StepControl 
{
	static final int TICK = 500;
	boolean autoStepOn = false;
	Timer timer;
	GUIMediator gui;
	
	public StepControl(GUIMediator gui)
	{
		this.gui = gui;
	}

	public boolean isAutoStepOn() {
		return autoStepOn;
	}

	public void setAutoStepOn(boolean autoStepOn) {
		this.autoStepOn = autoStepOn;
	}
	
	void toggleAutoStep()
	{
		if (isAutoStepOn() == true)
		{
			autoStepOn = false;
		}
		else
		{
			autoStepOn = true;
		}
	}
	
	void setPeriod(int period)
	{
		timer.setDelay(period);
	}
	
	public void start() {
		timer = new Timer(TICK, e -> {if(autoStepOn) gui.step();});
		timer.start();
	}

}
