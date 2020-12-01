/**
 * Copyright (C) 2019 European Spallation Source ERIC.
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.phoebus.applications.saveandrestore;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.epics.vtype.gson.GsonMessageBodyHandler;
import org.phoebus.applications.saveandrestore.data.DataProvider;
import org.phoebus.applications.saveandrestore.data.providers.jmasar.JMasarDataProvider;
import org.phoebus.applications.saveandrestore.data.providers.jmasar.JMasarJerseyClient;
import org.phoebus.applications.saveandrestore.service.SaveAndRestoreService;
import org.phoebus.applications.saveandrestore.ui.SaveAndRestoreController;
import org.phoebus.applications.saveandrestore.ui.SaveAndRestoreWithSplitController;
import org.phoebus.applications.saveandrestore.ui.TagSearchController;
import org.phoebus.applications.saveandrestore.ui.saveset.SaveSetController;
import org.phoebus.applications.saveandrestore.ui.saveset.SaveSetFromSelectionController;
import org.phoebus.applications.saveandrestore.ui.saveset.SaveSetSelectionController;
import org.phoebus.applications.saveandrestore.ui.saveset.SaveSetSelectionWithSplitController;
import org.phoebus.applications.saveandrestore.ui.snapshot.SnapshotController;
import org.phoebus.framework.preferences.PhoebusPreferenceService;
import org.phoebus.framework.preferences.PreferencesReader;
import org.phoebus.pv.PVFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;


@Configuration
public class AppConfig {

    private static final int DEFAULT_READ_TIMEOUT = 1000; // ms
    private static final int DEFAULT_CONNECT_TIMEOUT = 1000; // ms

    private PreferencesReader preferencesReader;
    private Preferences preferences;

    @PostConstruct
    public void init(){
        preferences = PhoebusPreferenceService.userNodeForClass(SaveAndRestoreApplication.class);
        preferencesReader = new PreferencesReader(getClass(), "/save_and_restore_preferences.properties");
    }

    /**
     * @brief Get an instance of the @ref SnapshotController.
     *
     * This static method is intended for use from Jython scripts. Using this method ensures that all class fields are
     * properly dependency injected by Spring.
     *
     * @return Instance of the @ref SnapshotController
     */
    public static SnapshotController snapshotControllerInstance() {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        return (SnapshotController) context.getBean("snapshotController");
    }

    @Bean
    public Preferences preferences(){
        return preferences;
    }

    @Bean("preferencesReader")
    public PreferencesReader preferencesReader(){
        return preferencesReader;
    }

    @Bean
    public JMasarJerseyClient jmasarClient(){
        JMasarJerseyClient jMasarJerseyClient = new JMasarJerseyClient();
        jMasarJerseyClient.setServiceUrl(preferencesReader.get("jmasar.service.url"));

        return jMasarJerseyClient;
    }

    @Bean
    public DataProvider dataProvider(){
        return new JMasarDataProvider();
    }

    @Bean
    @Scope("singleton")
    public SaveAndRestoreService saveAndRestoreService(){
        return new SaveAndRestoreService();
    }

    @Bean
    public SaveAndRestoreController saveAndRestoreController(){
        return new SaveAndRestoreController();
    }

    @Bean
    public SaveAndRestoreWithSplitController saveAndRestoreWithSplitController(){
        return new SaveAndRestoreWithSplitController();
    }

    @Bean
    @Scope("prototype")
    public SnapshotController snapshotController(){
        return new SnapshotController();
    }

    @Bean
    @Scope("prototype")
    public SaveSetController saveSetController(){
        return new SaveSetController();
    }

    @Bean
    @Scope("prototype")
    public SaveSetFromSelectionController saveSetFromSelectionController(){
        return new SaveSetFromSelectionController();
    }

    @Bean
    @Scope("prototype")
    public SaveSetSelectionController saveSetSelectionController() {
        return new SaveSetSelectionController();
    }

    @Bean
    @Scope("prototype")
    public SaveSetSelectionWithSplitController saveSetSelectionWithSplitController() {
        return new SaveSetSelectionWithSplitController();
    }

    @Bean
    @Scope("prototype")
    public TagSearchController tagSearchController() {
        return new TagSearchController();
    }

    @Bean
    public ApplicationContextProvider applicationContextProvider(){
        return new ApplicationContextProvider();
    }

    @Bean
    public ExecutorService executor(){
        return new ThreadPoolExecutor(1, 1, 0L,TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    @Bean
    public Client client(){
        Logger logger = LoggerFactory.getLogger(AppConfig.class.getName());

        int readTimeout = DEFAULT_READ_TIMEOUT;
        String readTimeoutString = preferencesReader.get("httpClient.readTimeout");
        try {
            readTimeout = Integer.parseInt(readTimeoutString);
            logger.debug("JMasar client using read timeout " + readTimeout + " ms");
        } catch (NumberFormatException e) {
            logger.error("Property httpClient.readTimeout \"" + readTimeoutString + "\" is not a number, using default value " + DEFAULT_READ_TIMEOUT + " ms");
        }

        int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        String connectTimeoutString = preferencesReader.get("httpClient.connectTimeout");
        try {
            connectTimeout = Integer.parseInt(connectTimeoutString);
            logger.debug("JMasar client using connect timeout " + connectTimeout + " ms");
        } catch (NumberFormatException e) {
            logger.error("Property httpClient.connectTimeout \"" + connectTimeoutString + "\" is not a number, using default value " + DEFAULT_CONNECT_TIMEOUT + " ms");
        }

        DefaultClientConfig defaultClientConfig = new DefaultClientConfig();
        defaultClientConfig.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, readTimeout);
        defaultClientConfig.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, connectTimeout);
        defaultClientConfig.getClasses().add(GsonMessageBodyHandler.class);
        return Client.create(defaultClientConfig);
    }

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean
    public String defaultEpicsProtocol(){
        PreferencesReader preferencesReader = new PreferencesReader(PVFactory.class, "/pv_preferences.properties");
        return preferencesReader.get("default");
    }
}
