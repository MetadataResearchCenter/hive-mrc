package org.unc.hive.sync;

import java.util.Date;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.unc.hive.server.VocabularyService;

import edu.unc.ils.mrc.hive.api.SKOSScheme;
import edu.unc.ils.mrc.hive.api.impl.elmo.SKOSSchemeImpl;
import edu.unc.ils.mrc.hive.sync.lcsh.AtomSynchronizer;

/**
 */
public class SyncJob implements Job {

	private static final Log logger = LogFactory.getLog(SyncJob.class);
	
	static boolean isRunning = false;
    /**
     * Empty constructor for job initilization
     */
    public SyncJob() {
    }

    /**
     * <p>
     * Called by the <code>{@link org.quartz.Scheduler}</code> when a
     * <code>{@link org.quartz.Trigger}</code> fires that is associated with
     * the <code>Job</code>.
     * </p>
     * 
     * @throws JobExecutionException
     *             if there is an exception while executing the job.
     */
    @SuppressWarnings("unchecked")
    public void execute(JobExecutionContext context)
        throws JobExecutionException {
    	
        // This job simply prints out its job name and the
        // date and time that it is running
    	JobKey jobKey = context.getJobDetail().getKey();
        logger.info("Executing job: " + jobKey + " executing at " + new Date() + ", fired by: " + context.getTrigger().getKey());
        
        if(context.getMergedJobDataMap().size() > 0) {
            Set<String> keys = context.getMergedJobDataMap().keySet();
            for(String key: keys) {
                String val = context.getMergedJobDataMap().getString(key);
                logger.info(" - jobDataMap entry: " + key + " = " + val);
            }
        }
        
        
        try
        {

        	if (!isRunning)
        	{
        		isRunning = true;
	        	ServletContext servletContext = (ServletContext)context.getScheduler().getContext().get("QuartzServletContext");
	        	String path = servletContext.getRealPath("");
	        	String confPath = path + "/WEB-INF/conf";
	        	SKOSScheme scheme = new SKOSSchemeImpl(confPath, "lcsh", true);
	        	AtomSynchronizer sync = new AtomSynchronizer(scheme);
	        	sync.processUpdates();
	        	isRunning = false;
        	}
        } catch (Exception e) {
        	logger.error(e);
        }
        
        
        context.setResult("Success");
    }

}
