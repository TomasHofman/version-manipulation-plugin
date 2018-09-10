package org.jboss.set.gradle4.versionmanipulation;

import org.jboss.logging.Logger;

public interface PluginLogger {

    Logger ROOT_LOGGER = Logger.getLogger(PluginLogger.class, PluginLogger.class.getPackage().getName());

}
