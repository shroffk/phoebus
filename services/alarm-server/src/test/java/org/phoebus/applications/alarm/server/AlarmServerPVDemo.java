/*******************************************************************************
 * Copyright (c) 2021 Oak Ridge National Laboratory.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.phoebus.applications.alarm.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** JUnit demo of stopping/restarting many PVs
 *
 *  Kafka with topic "Accelerator" must be running,
 *  but no actual configuration required.
 *
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class AlarmServerPVDemo
{
    private static long count(final List<AlarmServerPV> pvs)
    {
        return pvs.stream()
                  .filter(AlarmServerPV::isConnected)
                  .count();
    }

    public static void main(String[] args) throws Exception
    {
        // Logging
        final Logger logger = Logger.getLogger("");
        logger.setLevel(Level.WARNING);
        for (Handler handler : logger.getHandlers())
            handler.setLevel(logger.getLevel());

        // Dummy server model
        final ServerModelListener listener = new ServerModelListener()
        {
            @Override
            public void handleCommand(String path, String json)
            {
                // Ignore
            }
        };
        final ServerModel model = new ServerModel("localhost:9092", "Accelerator", null, listener);

        // Create many PVs
        final List<AlarmServerPV> pvs = new ArrayList<>();
        for (int i=0; i<50000; ++i)
            pvs.add(new AlarmServerPV(model, "/Accelerator", String.format("Ramp%05d", i), null));

        // Start them all
        System.out.println("Start all");
        for (AlarmServerPV pv : pvs)
            pv.start();

        while (true)
        {
            TimeUnit.SECONDS.sleep(5);
            System.out.println("Connected: " + count(pvs));

            System.out.println("Stop some");
            for (int i=0; i<400; ++i)
                pvs.get(i).stop();

            System.out.println("Re-start");
            for (int i=0; i<400; ++i)
                pvs.get(i).start();

            System.out.println("Connected: " + count(pvs));
        }

//        System.exit(0);
    }
}
