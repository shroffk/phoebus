/*******************************************************************************
 * Copyright (c) 2018 Oak Ridge National Laboratory.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.phoebus.applications.alarm.server;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;
import org.phoebus.applications.alarm.AlarmSystem;
import org.phoebus.applications.alarm.client.AlarmClientNode;
import org.phoebus.applications.alarm.model.AlarmTreeItem;
import org.phoebus.applications.alarm.model.SeverityLevel;
import org.phoebus.applications.alarm.model.TitleDetailDelay;
import org.phoebus.applications.alarm.server.actions.AutomatedActions;

/** JUnit test of the AutomatedActions
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class AutomatedActionTest
{
    /** Time-based tests are fragile.
     *  Pick a large enough test delay.
     */
    private static final long DELAY_MS = TimeUnit.SECONDS.toMillis(1);

    final BlockingQueue<String> action_performed = new ArrayBlockingQueue<>(1);

    final BiConsumer<AlarmTreeItem<?>, TitleDetailDelay> perform_action = (item, action) ->
    {
        System.out.println("Invoked " + action + " on " + item);
        action_performed.offer(action.title);
    };

    @BeforeClass
    public static void setup() throws Exception
    {
        final String config =
            "handlers = java.util.logging.ConsoleHandler\n" +
            ".level = FINE\n" +
            "java.util.logging.SimpleFormatter.format=%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s [%3$s] %5$s%6$s%n\n";
        final ByteArrayInputStream stream = new ByteArrayInputStream(config.getBytes());
        LogManager.getLogManager().readConfiguration(stream);

        // Raise log level
        final Logger root = Logger.getLogger("");
        root.setLevel(Level.ALL);
        for (Handler handler : root.getHandlers())
            handler.setLevel(Level.ALL);
    }

    @Test
    public void testBasicAutomatedAction() throws Exception
    {
        System.out.println("testBasicAutomatedAction");

        final TitleDetailDelay email = new TitleDetailDelay("Send Email", "mailto:fred@mail.com", (int)(DELAY_MS/1000));
        final AlarmClientNode test_item = new AlarmClientNode(null, "test");
        test_item.setActions(List.of(email));

        final AutomatedActions auto_action = new AutomatedActions(test_item, SeverityLevel.OK, perform_action);

        // Action has not been triggered, so should not be invoked
        assertThat(action_performed.poll(2*DELAY_MS, TimeUnit.MILLISECONDS), nullValue());

        // Trigger alarm
        long start = System.currentTimeMillis();
        auto_action.handleSeverityUpdate(SeverityLevel.MINOR);
        // Should now happen after the delay
        assertThat(action_performed.poll(2*DELAY_MS, TimeUnit.MILLISECONDS), equalTo("Send Email"));
        long passed = System.currentTimeMillis() - start;
        checkActionDelay(passed);


        // Trigger again because severity rises to MAJOR
        start = System.currentTimeMillis();
        auto_action.handleSeverityUpdate(SeverityLevel.MAJOR);
        // Should now happen after the delay
        assertThat(action_performed.poll(2*DELAY_MS, TimeUnit.MILLISECONDS), equalTo("Send Email"));
        passed = System.currentTimeMillis() - start;
        checkActionDelay(passed);


        // When no longer needed, close to stop timers etc.
        auto_action.cancel();
    }

    /** @param passed Actual time delay
     *  @param expected Expected time delay
     */
    private void checkActionDelay(final long passed, final long expected)
    {
        System.out.println("Action performed after " + passed + " ms (expected: " + expected + " ms)");
        // Very lenient, actual delay should be within 100% of the expected delay
        assertTrue(Math.abs(expected - passed) < DELAY_MS);
    }

    /** @param passed Actual time delay, to be checked against expected DELAY_MS */
    private void checkActionDelay(final long passed)
    {
        checkActionDelay(passed, DELAY_MS);
    }

    @Test
    public void testWasInAlarm() throws Exception
    {
        System.out.println("testWasInAlarm");

        final TitleDetailDelay email = new TitleDetailDelay("Send Email", "mailto:fred@mail.com", (int)(DELAY_MS/1000));
        final AlarmClientNode test_item = new AlarmClientNode(null, "test");
        test_item.setActions(List.of(email));

        // Assume item was already in alarm
        final AutomatedActions auto_action = new AutomatedActions(test_item, SeverityLevel.MAJOR, perform_action);
        // Receiving another alarm should _not_ trigger the action
        auto_action.handleSeverityUpdate(SeverityLevel.MAJOR);
        assertThat(action_performed.poll(2*DELAY_MS, TimeUnit.MILLISECONDS), nullValue());

        // Trigger alarm as item clears and then enters alarm
        auto_action.handleSeverityUpdate(SeverityLevel.OK);
        auto_action.handleSeverityUpdate(SeverityLevel.INVALID);
        final long start = System.currentTimeMillis();
        assertThat(action_performed.poll(2*DELAY_MS, TimeUnit.MILLISECONDS), equalTo("Send Email"));
        final long passed = System.currentTimeMillis() - start;
        checkActionDelay(passed);

        // When no longer needed, close to stop timers etc.
        auto_action.cancel();
    }

    @Test
    public void testMinorMajor() throws Exception
    {
        System.out.println("testMinorMajor");

        final TitleDetailDelay email = new TitleDetailDelay("Send Email", "mailto:fred@mail.com", (int)(DELAY_MS/1000));
        final AlarmClientNode test_item = new AlarmClientNode(null, "test");
        test_item.setActions(List.of(email));

        final AutomatedActions auto_action = new AutomatedActions(test_item, SeverityLevel.OK, perform_action);

        // Trigger alarm via MINOR alarm
        final long start = System.currentTimeMillis();
        auto_action.handleSeverityUpdate(SeverityLevel.MINOR);

        // Half way through the delay, update to MAJOR alarm
        TimeUnit.MILLISECONDS.sleep(DELAY_MS/2);
        auto_action.handleSeverityUpdate(SeverityLevel.MAJOR);

        // This should _not_ change the start time for scheduling the action
        assertThat(action_performed.poll(DELAY_MS, TimeUnit.MILLISECONDS), equalTo("Send Email"));
        final long passed = System.currentTimeMillis() - start;
        checkActionDelay(passed);

        // When no longer needed, close to stop timers etc.
        auto_action.cancel();
    }

    @Test
    public void testAutomatedActionReset() throws Exception
    {
        System.out.println("testAutomatedActionReset");

        final TitleDetailDelay email = new TitleDetailDelay("Send Email", "mailto:fred@mail.com", (int)(DELAY_MS/1000));
        final AlarmClientNode test_item = new AlarmClientNode(null, "test");
        test_item.setActions(List.of(email));

        final AutomatedActions auto_action = new AutomatedActions(test_item, SeverityLevel.OK, perform_action);

        // Trigger Action..
        auto_action.handleSeverityUpdate(SeverityLevel.MAJOR);
        // .. but acknowledge alarm within the DELAY
        TimeUnit.MILLISECONDS.sleep(DELAY_MS/2);
        auto_action.handleSeverityUpdate(SeverityLevel.MAJOR_ACK);
        // .. so nothing should happen
        assertThat(action_performed.poll(2*DELAY_MS, TimeUnit.MILLISECONDS), nullValue());

        // Trigger alarm and expect the action to be performed after DELAY_MS
        auto_action.handleSeverityUpdate(SeverityLevel.MAJOR);
        final long start = System.currentTimeMillis();
        assertThat(action_performed.poll(2*DELAY_MS, TimeUnit.MILLISECONDS), equalTo("Send Email"));
        final long passed = System.currentTimeMillis() - start;
        checkActionDelay(passed);

        auto_action.cancel();
    }

    @Test
    public void testAutomatedActionClear() throws Exception
    {
        System.out.println("testAutomatedActionClear");

        final TitleDetailDelay email = new TitleDetailDelay("Send Email", "mailto:fred@mail.com", (int)(DELAY_MS/1000));
        final AlarmClientNode test_item = new AlarmClientNode(null, "test");
        test_item.setActions(List.of(email));

        final AutomatedActions auto_action = new AutomatedActions(test_item, SeverityLevel.OK, perform_action);

        // Trigger Action..
        for (int i=0; i<5; ++i)
        {
            auto_action.handleSeverityUpdate(SeverityLevel.MAJOR);
            // .. but acknowledge alarm within the DELAY
            TimeUnit.MILLISECONDS.sleep(DELAY_MS/2);
            auto_action.handleSeverityUpdate(SeverityLevel.MAJOR_ACK);
            // .. so nothing should happen
            assertThat(action_performed.poll(2*DELAY_MS, TimeUnit.MILLISECONDS), nullValue());
        }

        auto_action.handleSeverityUpdate(SeverityLevel.OK);
        assertThat(action_performed.poll(2*DELAY_MS, TimeUnit.MILLISECONDS), nullValue());

        auto_action.cancel();
    }

    @Test
    public void testAutomatedEmailFollowup() throws Exception
    {
        System.out.println("testAutomatedEmailFollowup");

        if (! AlarmSystem.automated_action_followup.contains("mailto:"))
        {
            System.out.println("Skipping test of automated email follow up");
            return;
        }

        final TitleDetailDelay email = new TitleDetailDelay("Send Email", "mailto:fred@mail.com", (int)(DELAY_MS/1000));
        final AlarmClientNode test_item = new AlarmClientNode(null, "test");
        test_item.setActions(List.of(email));

        final AutomatedActions auto_action = new AutomatedActions(test_item, SeverityLevel.OK, perform_action);

        // Trigger Action, get email after DELAY_MS
        System.out.println("Expect alarm email in " + DELAY_MS + "ms");
        long start = System.currentTimeMillis();
        auto_action.handleSeverityUpdate(SeverityLevel.MAJOR);
        assertThat(action_performed.poll(2*DELAY_MS, TimeUnit.MILLISECONDS), equalTo("Send Email"));
        long passed = System.currentTimeMillis() - start;
        checkActionDelay(passed);

        // .. and expect another email "right away" when the alarm is acked, i.e. no longer active
        System.out.println("Expect 'OK' email right away");
        start = System.currentTimeMillis();
        auto_action.handleSeverityUpdate(SeverityLevel.MAJOR_ACK);
        Boolean testPassed = action_performed.poll(2*DELAY_MS, TimeUnit.MILLISECONDS) == "Send Email";
        if(System.getProperty("os.name").toLowerCase().contains("win") && !testPassed) {
            // Test is time sensitive and can fail under windows
            System.out.println("WARNING: Email not found right away");
        } else {
            assertTrue(testPassed);

            passed = System.currentTimeMillis() - start;
            checkActionDelay(passed, 0);

            // When the alarm then clears, there's NOT another update, already had the follow-up
            auto_action.handleSeverityUpdate(SeverityLevel.OK);
            assertThat(action_performed.poll(2*DELAY_MS, TimeUnit.MILLISECONDS), nullValue());
        }
        auto_action.cancel();
    }
}
