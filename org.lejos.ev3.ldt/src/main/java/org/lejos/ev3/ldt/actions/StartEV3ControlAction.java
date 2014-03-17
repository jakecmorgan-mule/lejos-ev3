package org.lejos.ev3.ldt.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;
import org.lejos.ev3.ldt.LeJOSEV3Plugin;
import org.lejos.ev3.ldt.util.LeJOSEV3Util;
import org.lejos.ev3.ldt.util.ToolStarter;

public class StartEV3ControlAction  extends ActionDelegate implements IWorkbenchWindowActionDelegate  {
	
	@Override
	public void run(IAction action) {
		// open progress monitor
		Job ev3Control = new Job("Starting EV3Control") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				startEV3Control(monitor);
				return Status.OK_STATUS;
			}
		};
		ev3Control.schedule();
	}
	
	private void startEV3Control(IProgressMonitor progressMonitor) {
		LeJOSEV3Plugin.getDefault().getConsole().activate();
		
		try {
			progressMonitor.beginTask("Starting EV3Control ...", IProgressMonitor.UNKNOWN);
			try
			{
				// start EV3Control
				ToolStarter starter = LeJOSEV3Util.getCachedExternalStarter();
				
				ArrayList<String> args = new ArrayList<String>();
				
				int r = starter.invokeSwingTool(LeJOSEV3Util.TOOL_EV3CONTROL, args);
				if (r == 0)
					LeJOSEV3Util.message("EV3Control has been started successfully");
				else
					LeJOSEV3Util.error("Starting EV3Control failed with exit status "+r);
			}
			finally
			{
				progressMonitor.done();
			}
		} catch (Throwable t) {
			if (t instanceof InvocationTargetException)
				t = ((InvocationTargetException)t).getTargetException();
			
			// log
			LeJOSEV3Util.error("Starting EV3Control failed", t);
		}
	}

	public void init(IWorkbenchWindow window) {
		// Do nothing
	}
}
