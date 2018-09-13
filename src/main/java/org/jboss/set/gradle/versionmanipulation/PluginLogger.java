package org.jboss.set.gradle.versionmanipulation;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "VMP")
public interface PluginLogger extends BasicLogger {

    PluginLogger ROOT_LOGGER = Logger.getMessageLogger(PluginLogger.class, PluginLogger.class.getPackage().getName());

    @Message("Processing exception: %s")
    RuntimeException processingException(String message, @Cause Exception cause);

}
