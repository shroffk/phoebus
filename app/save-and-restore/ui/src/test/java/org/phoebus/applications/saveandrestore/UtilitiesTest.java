/*
 * This software is Copyright by the Board of Trustees of Michigan
 * State University (c) Copyright 2016.
 *
 * Contact Information:
 *   Facility for Rare Isotope Beam
 *   Michigan State University
 *   East Lansing, MI 48824-1321
 *   http://frib.msu.edu
 */

package org.phoebus.applications.saveandrestore;

import org.epics.util.array.ArrayBoolean;
import org.epics.util.array.ArrayByte;
import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ArrayFloat;
import org.epics.util.array.ArrayInteger;
import org.epics.util.array.ArrayLong;
import org.epics.util.array.ArrayShort;
import org.epics.util.array.ArrayUByte;
import org.epics.util.array.ArrayUInteger;
import org.epics.util.array.ArrayULong;
import org.epics.util.array.ArrayUShort;
import org.epics.util.array.ListBoolean;
import org.epics.util.array.ListLong;
import org.epics.util.array.ListNumber;
import org.epics.vtype.Alarm;
import org.epics.vtype.AlarmSeverity;
import org.epics.vtype.AlarmStatus;
import org.epics.vtype.Display;
import org.epics.vtype.EnumDisplay;
import org.epics.vtype.IVBooleanArray;
import org.epics.vtype.IVEnumArray;
import org.epics.vtype.Time;
import org.epics.vtype.VBoolean;
import org.epics.vtype.VBooleanArray;
import org.epics.vtype.VByte;
import org.epics.vtype.VByteArray;
import org.epics.vtype.VDouble;
import org.epics.vtype.VDoubleArray;
import org.epics.vtype.VEnum;
import org.epics.vtype.VEnumArray;
import org.epics.vtype.VFloat;
import org.epics.vtype.VFloatArray;
import org.epics.vtype.VInt;
import org.epics.vtype.VIntArray;
import org.epics.vtype.VLong;
import org.epics.vtype.VLongArray;
import org.epics.vtype.VShort;
import org.epics.vtype.VShortArray;
import org.epics.vtype.VString;
import org.epics.vtype.VStringArray;
import org.epics.vtype.VType;
import org.epics.vtype.VUByte;
import org.epics.vtype.VUByteArray;
import org.epics.vtype.VUInt;
import org.epics.vtype.VUIntArray;
import org.epics.vtype.VULong;
import org.epics.vtype.VULongArray;
import org.epics.vtype.VUShort;
import org.epics.vtype.VUShortArray;
import org.junit.Test;
import org.phoebus.applications.saveandrestore.ui.model.Threshold;
import org.phoebus.applications.saveandrestore.ui.model.VDisconnectedData;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.Assert.*;

public class UtilitiesTest {

    /**
     * Tests {@link Utilities#valueToString(VType)} and {@link Utilities#valueToString(VType, int)}.
     */
    @Test
    public void testValueToString() {
        Alarm alarm = Alarm.none();
        Display display = Display.none();
        Time time = Time.now();

        assertNull(Utilities.valueToString(null));

        VType val = VDouble.of(5d, alarm, time, display);
        String result = Utilities.valueToString(val);
        assertEquals("5.0", result);

        val = VFloat.of(5f,alarm,time,display);
        result = Utilities.valueToString(val);
        assertEquals("5.0", result);

        val = VLong.of(5L,alarm,time,display);
        result = Utilities.valueToString(val);
        assertEquals("5", result);

        val = VInt.of(5,alarm,time,display);
        result = Utilities.valueToString(val);
        assertEquals("5", result);

        val = VShort.of((short)5,alarm,time,display);
        result = Utilities.valueToString(val);
        assertEquals("5", result);

        val = VByte.of((byte)5,alarm,time,display);
        result = Utilities.valueToString(val);
        assertEquals("5", result);

        val = VEnum.of(1, EnumDisplay.of("first", "second", "third"),alarm, time);
        result = Utilities.valueToString(val);
        assertEquals("second", result);

        val = VEnum.of(1, EnumDisplay.of("", "", ""),alarm, time);
        result = Utilities.valueToString(val);
        assertEquals("1", result);

        val = VEnum.of(1, EnumDisplay.of("a", "", ""),alarm, time);
        result = Utilities.valueToString(val);
        assertEquals("", result);

        val = VString.of("third",alarm, time);
        result = Utilities.valueToString(val);
        assertEquals("third", result);

        val = VDoubleArray.of(ArrayDouble.of(1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9),alarm,time,display);
        result = Utilities.valueToString(val);
        assertEquals("[1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0,...]", result);

        val = VFloatArray.of(ArrayFloat.of(1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9),alarm,time,display);
        result = Utilities.valueToString(val);
        assertEquals("[1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0,...]", result);

        result = Utilities.valueToString(val,3);
        assertEquals("[1.0, 2.0, 3.0,...]", result);

        result = Utilities.valueToString(val,100);
        assertEquals("[1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0]", result);

        val = VStringArray.of(Arrays.asList("first", "second", "third"),alarm, time);
        result = Utilities.valueToString(val);
        assertTrue(result.contains("not supported"));

        val = VLongArray.of(ArrayLong.of(1,2,3,4,5),alarm,time,display);
        result = Utilities.valueToString(val,3);
        assertEquals("[1, 2, 3,...]", result);

        val = VULongArray.of(ArrayULong.of(1,2,3,4,5),alarm,time,display);
        result = Utilities.valueToString(val,3);
        assertEquals("[1, 2, 3,...]", result);

        val = VIntArray.of(ArrayInteger.of(1,2,3,4,5),alarm,time,display);
        result = Utilities.valueToString(val,3);
        assertEquals("[1, 2, 3,...]", result);

        val = VUIntArray.of(ArrayUInteger.of(1,2,3,4,5),alarm,time,display);
        result = Utilities.valueToString(val,3);
        assertEquals("[1, 2, 3,...]", result);

        val = VShortArray.of(ArrayShort.of((short)1,(short)2,(short)3,(short)4,(short)5),alarm,time,display);
        result = Utilities.valueToString(val,3);
        assertEquals("[1, 2, 3,...]", result);

        val = VUShortArray.of(ArrayUShort.of((short)1,(short)2,(short)3,(short)4,(short)5),alarm,time,display);
        result = Utilities.valueToString(val,3);
        assertEquals("[1, 2, 3,...]", result);

        val = VByteArray.of(ArrayByte.of((byte)1,(byte)2,(byte)3,(byte)4,(byte)5),alarm,time,display);
        result = Utilities.valueToString(val,3);
        assertEquals("[1, 2, 3,...]", result);

        val = VUByteArray.of(ArrayUByte.of((byte)1,(byte)2,(byte)3,(byte)4,(byte)5),alarm,time,display);
        result = Utilities.valueToString(val,3);
        assertEquals("[1, 2, 3,...]", result);

        val = VIntArray.of(ArrayInteger.of(1,2,3,4,5),alarm,time,display);
        result = Utilities.valueToString(val,0);
        assertEquals("[]", result);

        val = VBoolean.of(true, alarm, time);
        result = Utilities.valueToString(val);
        assertEquals("true", result);
    }

    /**
     * Tests {@link Utilities#valueFromString(String, VType)}.
     */
    @Test
    public void testValueFromString() {


        Alarm alarm = Alarm.none();
        Display display = Display.none();
        Time time = Time.now();

        VType val = VDouble.of(5d,alarm,time,display);
        VType result = Utilities.valueFromString("5.0", val);
        assertTrue(result instanceof VDouble);
        assertEquals(5.0, ((VDouble)result).getValue().doubleValue(),0);

        result = Utilities.valueFromString("", val);
        assertTrue(result instanceof VDouble);
        assertEquals(5.0, ((VDouble)result).getValue().doubleValue(),0);

        val = VFloat.of(5f,alarm,time,display);
        result = Utilities.valueFromString("5.0", val);
        assertTrue(result instanceof VFloat);
        assertEquals(5.0f, ((VFloat)result).getValue().floatValue(),0);

        val = VLong.of(5L,alarm,time,display);
        result = Utilities.valueFromString("5", val);
        assertTrue(result instanceof VLong);
        assertEquals(5L, ((VLong)result).getValue().longValue());

        val = VULong.of(5L,alarm,time,display);
        result = Utilities.valueFromString("5", val);
        assertTrue(result instanceof VULong);
        assertEquals(5L, ((VULong)result).getValue().longValue());

        val = VUInt.of(5,alarm,time,display);
        result = Utilities.valueFromString("5", val);
        assertTrue(result instanceof VUInt);
        assertEquals(5, ((VUInt)result).getValue().intValue());

        val = VInt.of(5,alarm,time,display);
        result = Utilities.valueFromString("5", val);
        assertTrue(result instanceof VInt);
        assertEquals(5, ((VInt)result).getValue().intValue());

        val = VShort.of((short)5,alarm,time,display);
        result = Utilities.valueFromString("5", val);
        assertTrue(result instanceof VShort);
        assertEquals((short)5, ((VShort)result).getValue().shortValue());

        val = VUShort.of((short)5,alarm,time,display);
        result = Utilities.valueFromString("5", val);
        assertTrue(result instanceof VUShort);
        assertEquals((short)5, ((VUShort)result).getValue().shortValue());

        val = VByte.of((byte)5,alarm,time,display);
        result = Utilities.valueFromString("5", val);
        assertTrue(result instanceof VByte);
        assertEquals((byte)5, ((VByte)result).getValue().byteValue());

        val = VUByte.of((byte)5,alarm,time,display);
        result = Utilities.valueFromString("5", val);
        assertTrue(result instanceof VUByte);
        assertEquals((byte)5, ((VUByte)result).getValue().byteValue());

        val = VEnum.of(1, EnumDisplay.of("first", "second", "third"),alarm, time);
        result = Utilities.valueFromString("second", val);
        assertTrue(result instanceof VEnum);
        assertEquals("second", ((VEnum)result).getValue());

        val = VEnum.of(1, EnumDisplay.of("first", "second", "third"),alarm, time);
        try {
            Utilities.valueFromString("invalid", val);
            fail("Should throw exception");
        } catch (IllegalArgumentException e) {
        }

        val = VBoolean.of(false,alarm, time);
        result = Utilities.valueFromString("false", val);
        assertTrue(result instanceof VBoolean);
        assertEquals(false, ((VBoolean)result).getValue());

        val = VString.of("third",alarm, time);
        result = Utilities.valueFromString("third", val);
        assertTrue(result instanceof VString);
        assertEquals("third", ((VString)result).getValue());


        try {
            val = VDoubleArray.of(ArrayDouble.of(1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9),alarm,time,display);
            Utilities.valueFromString("[1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0,...]", val);
            fail("Exception should happen, because the number of elements is wrong");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }

        val = VDoubleArray.of(ArrayDouble.of(1,2,3,4,5,6,7,8,9),alarm,time,display);
        result = Utilities.valueFromString("[1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0]", val);
        assertEquals(((VDoubleArray)result).getData(), ((VDoubleArray)val).getData());

        val = VFloatArray.of(ArrayFloat.of(1,2,3,4,5,6,7,8,9),alarm,time,display);
        result = Utilities.valueFromString("[1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0]", val);
        assertEquals(((VFloatArray)result).getData(), ((VFloatArray)val).getData());

        val = VULongArray.of(ArrayULong.of(1,2,3,4,5,6,7,8,9),alarm,time,display);
        result = Utilities.valueFromString("[1, 2, 3, 4, 5, 6, 7, 8, 9]", val);
        assertEquals(((VULongArray)result).getData(), ((VULongArray)val).getData());

        val = VUIntArray.of(ArrayUInteger.of(1,2,3,4,5,6,7,8,9),alarm,time,display);
        result = Utilities.valueFromString("[1, 2, 3, 4, 5, 6, 7, 8, 9]", val);
        assertEquals(((VUIntArray)result).getData(), ((VUIntArray)val).getData());

        val = VIntArray.of(ArrayInteger.of(1,2,3,4,5,6,7,8,9),alarm,time,display);
        result = Utilities.valueFromString("[1, 2, 3, 4, 5, 6, 7, 8, 9]", val);
        assertEquals(((VIntArray)result).getData(), ((VIntArray)val).getData());

        val = VShortArray.of(ArrayShort.of((short)1, (short)2, (short)3),alarm,time,display);
        result = Utilities.valueFromString("[1, 2, 3]", val);
        assertEquals(((VShortArray)result).getData(), ((VShortArray)val).getData());

        val = VUShortArray.of(ArrayUShort.of((short)1, (short)2, (short)3),alarm,time,display);
        result = Utilities.valueFromString("[1, 2, 3]", val);
        assertEquals(((VUShortArray)result).getData(), ((VUShortArray)val).getData());

        val = VByteArray.of(ArrayByte.of((byte)1, (byte)2, (byte)3),alarm,time,display);
        result = Utilities.valueFromString("[1, 2, 3]", val);
        assertEquals(((VByteArray)result).getData(), ((VByteArray)val).getData());

        val = VUByteArray.of(ArrayUByte.of((byte)1, (byte)2, (byte)3),alarm,time,display);
        result = Utilities.valueFromString("[1, 2, 3]", val);
        assertEquals(((VUByteArray)result).getData(), ((VUByteArray)val).getData());


        val = VStringArray.of(Arrays.asList("first", "second", "third"),alarm, time);
        result = Utilities.valueFromString("[first, second, third]", val);
        assertTrue(result instanceof VStringArray);
        assertArrayEquals(new String[]{"first","second","third"}, ((VStringArray)result).getData().toArray(new String[0]));

        val = VLongArray.of(ArrayLong.of(1,2,3,4,5),alarm,time,display);
        result = Utilities.valueFromString("1, 2, 3, 4, 5", val);
        assertTrue(result instanceof VLongArray);
        assertTrue(((VLongArray)result).getData() instanceof ListLong);

        val = VBooleanArray.of(ArrayBoolean.of(true,true,false,true),alarm,time);
        result = Utilities.valueFromString("[true, true, false, true]", val);
        assertTrue(result instanceof VBooleanArray);
        assertTrue(((VBooleanArray)result).getData() instanceof ListBoolean);

        val = VDisconnectedData.INSTANCE;
        result = Utilities.valueFromString("5", val);
        assertTrue(result instanceof VLong);
        assertEquals(5L, ((VLong)result).getValue().longValue());

        result = Utilities.valueFromString("5.1", val);
        assertTrue(result instanceof VDouble);
        assertEquals(5.1, ((VDouble)result).getValue().doubleValue(), 0);

        result = Utilities.valueFromString("string", val);
        assertTrue(result instanceof VString);
        assertEquals("string", ((VString)result).getValue());
    }

    /**
     * Tests {@link Utilities#toRawValue(VType)}.
     */
    @Test
    public void testToRawValue() {
        Alarm alarm = Alarm.none();
        Display display = Display.none();
        Time time = Time.now();

        assertNull(Utilities.toRawValue(null));

        VType val = VDouble.of(5d,alarm,time,display);
        Object d = Utilities.toRawValue(val);
        assertTrue(d instanceof Double);
        assertEquals(5.0,d);

        val = VFloat.of(5f,alarm,time,display);
        d = Utilities.toRawValue(val);
        assertTrue(d instanceof Float);
        assertEquals(5.0f,d);

        val = VLong.of(5L,alarm,time,display);
        d = Utilities.toRawValue(val);
        assertTrue(d instanceof Long);
        assertEquals(5L,d);

        val = VInt.of(5,alarm,time,display);
        d = Utilities.toRawValue(val);
        assertTrue(d instanceof Integer);
        assertEquals(5,d);

        val = VShort.of((short)5,alarm,time,display);
        d = Utilities.toRawValue(val);
        assertTrue(d instanceof Short);
        assertEquals((short)5,d);

        val = VByte.of((byte)5,alarm,time,display);
        d = Utilities.toRawValue(val);
        assertTrue(d instanceof Byte);
        assertEquals((byte)5,d);

        val = VEnum.of(1, EnumDisplay.of("first", "second", "third"),alarm, time);
        d = Utilities.toRawValue(val);
        assertTrue(d instanceof String);
        assertEquals("second",d);

        val = VEnum.of(1, EnumDisplay.of("", "", ""),alarm, time);
        d = Utilities.toRawValue(val);
        assertTrue(d instanceof String);
        assertEquals("1", d);

        val = VEnum.of(1, EnumDisplay.of("a", "", ""),alarm, time);
        d = Utilities.toRawValue(val);
        assertTrue(d instanceof String);
        assertEquals("", d);

        val = VString.of("third",alarm, time);
        d = Utilities.toRawValue(val);
        assertTrue(d instanceof String);
        assertEquals("third",d);

        ArrayDouble arrayDouble = ArrayDouble.of(1,2,3,4,5);
        val = VDoubleArray.of(arrayDouble,alarm,time,display);
        d = Utilities.toRawValue(val);
        assertTrue(d instanceof ListNumber);
        ListNumber l = (ListNumber)d;
        for (int i = 0; i < l.size(); i++) {
            assertEquals(arrayDouble.getDouble(i), l.getDouble(i), 0);
        }

        val = VStringArray.of(Arrays.asList("a", "b", "c"), alarm, time);
        d = Utilities.toRawValue(val);
        assertTrue(d instanceof String[]);

        val = VBooleanArray.of(ArrayBoolean.of(true, false, true), alarm, time);
        d = Utilities.toRawValue(val);
        assertTrue(d instanceof ArrayBoolean);
        ArrayBoolean arrayBoolean = (ArrayBoolean)d;
        assertTrue(arrayBoolean.getBoolean(0));
        assertFalse(arrayBoolean.getBoolean(1));

        val = VEnumArray.of(ArrayInteger.of(0, 1, 2, 3, 4), EnumDisplay.of("a", "b", "c", "d", "e"), alarm, time);
        d = Utilities.toRawValue(val);
        assertTrue(d instanceof List);
        assertEquals("a", ((List)d).get(0));
        assertEquals("e", ((List)d).get(4));

        val = VBoolean.of(true, alarm, time);
        d = Utilities.toRawValue(val);
        assertTrue(d instanceof Boolean);
        assertTrue(((Boolean)d));

        assertNull(Utilities.toRawValue(VDisconnectedData.INSTANCE));
    }



    /**
     * Tests {@link Utilities#valueToCompareString(VType, VType, Optional)}. The test doesn't cover all possible
     * combinations, but it does cover a handful of them.
     */
    @Test
    public void testValueToCompareString() {
        Alarm alarm = Alarm.none();
        Display display = Display.none();
        Time time = Time.now();

        Optional<Threshold<?>> threshold = Optional.of(new Threshold<>(5d,-5d));

        Utilities.VTypeComparison result = Utilities.valueToCompareString(null, null, threshold);
        assertEquals(VDisconnectedData.INSTANCE.toString(), result.getString());
        assertEquals(0, result.getValuesEqual());
        assertTrue(result.isWithinThreshold());

        VType val1 = VDouble.of(5d,alarm,time,display);
        result = Utilities.valueToCompareString(null, val1, threshold);
        assertEquals(VDisconnectedData.INSTANCE.toString(), result.getString());
        assertEquals(-1, result.getValuesEqual());
        assertFalse(result.isWithinThreshold());

        result = Utilities.valueToCompareString(val1, null, threshold);
        assertEquals("5.0", result.getString());
        assertEquals(1, result.getValuesEqual());
        assertFalse(result.isWithinThreshold());

        result = Utilities.valueToCompareString(VDisconnectedData.INSTANCE, val1, threshold);
        assertEquals(VDisconnectedData.INSTANCE.toString(), result.getString());
        assertEquals(-1, result.getValuesEqual());
        assertFalse(result.isWithinThreshold());

        result = Utilities.valueToCompareString(val1, VDisconnectedData.INSTANCE, threshold);
        assertEquals("5.0", result.getString());
        assertEquals(1, result.getValuesEqual());
        assertFalse(result.isWithinThreshold());

        val1 = VDouble.of(5d,alarm,time,display);
        VType val2 = VDouble.of(6d,alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("5 \u0394-1.0", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertTrue(result.isWithinThreshold());

        val1 = VDouble.of(15d,alarm,time,display);
        val2 = VDouble.of(6d,alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("15 \u0394+9.0", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VFloat.of(15f,alarm,time,display);
        val2 = VFloat.of(6f,alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("15 \u0394+9.0", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VDouble.of(6d,alarm,time,display);
        val2 = VDouble.of(6d,alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("6 \u03940.0", result.getString());
        assertEquals(0,result.getValuesEqual());
        assertTrue(result.isWithinThreshold());

        val1 = VFloat.of(5f,alarm,time,display);
        val2 = VFloat.of(6f,alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("5 \u0394-1.0", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertTrue(result.isWithinThreshold());

        val1 = VFloat.of(5f,alarm,time,display);
        val2 = VFloat.of(6f,alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("5 \u0394-1.0", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertFalse(result.isWithinThreshold());

        val1 = VFloat.of(5f,alarm,time,display);
        val2 = VFloat.of(6f,alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("5 \u0394-1.0", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertFalse(result.isWithinThreshold());


        val1 = VLong.of(15L,alarm,time,display);
        val2 = VDouble.of(6d,alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("15 \u0394+9", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VLong.of(15L,alarm,time,display);
        val2 = VDouble.of(6d,alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("15 \u0394+9", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VULong.of(15L,alarm,time,display);
        val2 = VULong.of(6L,alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("15 \u0394+9", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VULong.of(5L,alarm,time,display);
        val2 = VULong.of(6L,alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("5 \u0394-1", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertFalse(result.isWithinThreshold());

        val1 = VUInt.of(15,alarm,time,display);
        val2 = VUInt.of(6,alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("15 \u0394+9", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VUInt.of(15,alarm,time,display);
        val2 = VUInt.of(6,alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("15 \u0394+9", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VInt.of(15,alarm,time,display);
        val2 = VInt.of(6,alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("15 \u0394+9", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VDouble.of(15d,alarm,time,display);
        val2 = VLong.of(6L,alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("15 \u0394+9.0", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VDouble.of(15d,alarm,time,display);
        val2 = VLong.of(6L,alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("15 \u0394+9.0", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VDouble.of(15d,alarm,time,display);
        val2 = VLong.of(15L,alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("15 \u03940.0", result.getString());
        assertEquals(0,result.getValuesEqual());
        assertTrue(result.isWithinThreshold());

        val1 = VString.of("first",alarm,time);
        val2 = VLong.of(15L,alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("first", result.getString());
        assertNotEquals(0, result.getValuesEqual());
        assertFalse(result.isWithinThreshold());

        val1 = VDoubleArray.of(ArrayDouble.of(1,2,3),alarm,time,display);
        val2 = VDoubleArray.of(ArrayDouble.of(1,2,3),alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("[1.0, 2.0, 3.0]", result.getString());
        assertEquals(0,result.getValuesEqual());
        assertTrue(result.isWithinThreshold());

        val1 = VDoubleArray.of(ArrayDouble.of(1,2,3),alarm,time,display);
        val2 = VLongArray.of(ArrayLong.of(1,2,3),alarm,time,display);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("[1.0, 2.0, 3.0]", result.getString());
        assertNotEquals(0, result.getValuesEqual());
        assertFalse(result.isWithinThreshold());

        //compare long values: equal, first less than second, second less than first
        val1 = VLong.of(6L,alarm,time,display);
        val2 = VLong.of(6L,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("6 \u03940", result.getString());
        assertEquals(0, result.getValuesEqual());
        assertTrue(result.isWithinThreshold());

        val1 = VLong.of(5L,alarm,time,display);
        val2 = VLong.of(6L,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("5 \u0394-1", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertTrue(result.isWithinThreshold());

        val1 = VLong.of(6L,alarm,time,display);
        val2 = VLong.of(5L,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("6 \u0394+1", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertTrue(result.isWithinThreshold());

        //compare int values: equal, first less than second, second less than first
        val1 = VInt.of(6,alarm,time,display);
        val2 = VInt.of(6,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("6 \u03940", result.getString());
        assertEquals(0, result.getValuesEqual());
        assertTrue(result.isWithinThreshold());

        val1 = VInt.of(5,alarm,time,display);
        val2 = VInt.of(6,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("5 \u0394-1", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertTrue(result.isWithinThreshold());

        val1 = VInt.of(6,alarm,time,display);
        val2 = VInt.of(5,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("6 \u0394+1", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertTrue(result.isWithinThreshold());

        //compare short values: equal, first less than second, second less than first
        val1 = VShort.of((short)6,alarm,time,display);
        val2 = VShort.of((short)6,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("6 \u03940", result.getString());
        assertEquals(0, result.getValuesEqual());
        assertTrue(result.isWithinThreshold());

        val1 = VShort.of((short)5,alarm,time,display);
        val2 = VShort.of((short)6,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("5 \u0394-1", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertTrue(result.isWithinThreshold());

        val1 = VShort.of((short)6,alarm,time,display);
        val2 = VShort.of((short)5,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("6 \u0394+1", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertTrue(result.isWithinThreshold());

        val1 = VUShort.of((short)6,alarm,time,display);
        val2 = VUShort.of((short)5,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("6 \u0394+1", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertTrue(result.isWithinThreshold());

        val1 = VUShort.of((short)6,alarm,time,display);
        val2 = VUShort.of((short)6,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("6 \u03940", result.getString());
        assertEquals(0, result.getValuesEqual());
        assertTrue(result.isWithinThreshold());

        val1 = VUShort.of((short)5,alarm,time,display);
        val2 = VUShort.of((short)6,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("5 \u0394-1", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertTrue(result.isWithinThreshold());

        val1 = VShort.of((short)5,alarm,time,display);
        val2 = VShort.of((short)6,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("5 \u0394-1", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertFalse(result.isWithinThreshold());

        val1 = VUShort.of((short)5,alarm,time,display);
        val2 = VUShort.of((short)6,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("5 \u0394-1", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertFalse(result.isWithinThreshold());

        //compare enum values: equal, first less than second, second less than first
        EnumDisplay labels = EnumDisplay.of("val1","val2","val3");

        val1 = VEnum.of(1,labels,alarm,time);
        val2 = VEnum.of(1,labels,alarm,time);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("val2", result.getString());
        assertEquals(0, result.getValuesEqual());
        assertTrue(result.isWithinThreshold());

        val1 = VEnum.of(1,labels,alarm,time);
        val2 = VEnum.of(2,labels,alarm,time);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("val2", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertFalse(result.isWithinThreshold());

        val1 = VEnum.of(2,labels,alarm,time);
        val2 = VEnum.of(1,labels,alarm,time);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("val3", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VByte.of((byte)5,alarm,time,display);
        val2 = VByte.of((byte)6,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("5 \u0394-1", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertTrue(result.isWithinThreshold());

        val1 = VByte.of((byte)6,alarm,time,display);
        val2 = VByte.of((byte)5,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("6 \u0394+1", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertTrue(result.isWithinThreshold());

        val1 = VByte.of((byte)6,alarm,time,display);
        val2 = VByte.of((byte)5,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("6 \u0394+1", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VUByte.of((byte)5,alarm,time,display);
        val2 = VUByte.of((byte)6,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("5 \u0394-1", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertTrue(result.isWithinThreshold());

        val1 = VUByte.of((byte)6,alarm,time,display);
        val2 = VUByte.of((byte)5,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, threshold);
        assertEquals("6 \u0394+1", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertTrue(result.isWithinThreshold());

        val1 = VUByte.of((byte)6,alarm,time,display);
        val2 = VUByte.of((byte)5,alarm,time, display);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("6 \u0394+1", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VBoolean.of(false,alarm,time);
        val2 = VBoolean.of(false,alarm,time);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("false", result.getString());
        assertTrue(result.getValuesEqual() == 0);
        assertTrue(result.isWithinThreshold());

        val1 = VString.of("a", alarm,time);
        val2 = VString.of("b", alarm,time);
        result = Utilities.valueToCompareString(val1, val2, Optional.empty());
        assertEquals("a", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertFalse(result.isWithinThreshold());
    }

    /**
     * Tests {@link Utilities#areVTypesIdentical(VType, VType, boolean)} method.
     *
     * @throws InterruptedException
     */
    @Test
    public void testVTypesIdentical() {
        Alarm alarm = Alarm.of(AlarmSeverity.INVALID, AlarmStatus.NONE, "name");
        Alarm alarm2 = Alarm.of(AlarmSeverity.INVALID, AlarmStatus.NONE, "name");
        Display display = Display.none();
        Time time = Time.now();
        Time time2 = Time.of(time.getTimestamp().plus(1, ChronoUnit.SECONDS));
        VType val1 = VDouble.of(5d,alarm,time,display);
        VType val2 = VDouble.of(6d,alarm2,time2,display);

        assertTrue(Utilities.areVTypesIdentical(null, null, false));
        assertFalse(Utilities.areVTypesIdentical(null, val1, false));
        assertFalse(Utilities.areVTypesIdentical(val1, null, false));

        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        assertFalse(Utilities.areVTypesIdentical(val1, val2, false));
        val2 = VDouble.of(5d,alarm2,time,display);
        assertTrue(Utilities.areVTypesIdentical(val1, val2, true));
        assertTrue(Utilities.areVTypesIdentical(val1, val2, false));

        val2 = VDouble.of(5d,Alarm.none(),time,display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        assertTrue(Utilities.areVTypesIdentical(val1, val2, false));

        val2 = VDouble.of(5d,alarm2,time2,display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        assertTrue(Utilities.areVTypesIdentical(val1, val2, false));

        val2 = VLong.of(5L,alarm2,time,display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        assertFalse(Utilities.areVTypesIdentical(val1, val2, false));

        val1 = VFloat.of(5f,alarm,time,display);
        val2 = VFloat.of(6d,alarm2,time2,display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        assertFalse(Utilities.areVTypesIdentical(val1, val2, false));

        val1 = VULong.of(5L,alarm,time,display);
        val2 = VULong.of(6L,alarm2,time2,display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        assertFalse(Utilities.areVTypesIdentical(val1, val2, false));

        val1 = VLong.of(5L,alarm,time,display);
        val2 = VLong.of(6L,alarm2,time2,display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        assertFalse(Utilities.areVTypesIdentical(val1, val2, false));

        val1 = VUInt.of(5,alarm,time,display);
        val2 = VUInt.of(6,alarm2,time2,display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        assertFalse(Utilities.areVTypesIdentical(val1, val2, false));

        val1 = VInt.of(5,alarm,time,display);
        val2 = VInt.of(6,alarm2,time2,display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        assertFalse(Utilities.areVTypesIdentical(val1, val2, false));

        val1 = VUShort.of((short)5,alarm,time,display);
        val2 = VUShort.of((short)6,alarm2,time2,display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        assertFalse(Utilities.areVTypesIdentical(val1, val2, false));

        val1 = VShort.of((short)5,alarm,time,display);
        val2 = VShort.of((short)6,alarm2,time2,display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        assertFalse(Utilities.areVTypesIdentical(val1, val2, false));

        val1 = VUByte.of((byte)5,alarm,time,display);
        val2 = VUByte.of((byte)6,alarm2,time2,display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        assertFalse(Utilities.areVTypesIdentical(val1, val2, false));

        val1 = VByte.of((byte)5,alarm,time,display);
        val2 = VByte.of((byte)6,alarm2,time2,display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        assertFalse(Utilities.areVTypesIdentical(val1, val2, false));

        val1 = VBoolean.of(true,alarm,time);
        val2 = VBoolean.of(false,alarm2,time2);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        assertFalse(Utilities.areVTypesIdentical(val1, val2, false));

        val1 = VEnum.of(1, EnumDisplay.of("a", "b", "c"), alarm,time);
        val2 = VEnum.of(2, EnumDisplay.of("a", "b", "c"),alarm2,time2);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        assertFalse(Utilities.areVTypesIdentical(val1, val2, false));
        VType val3 = VEnum.of(1, EnumDisplay.of("a", "b", "c"),alarm,time);
        assertTrue(Utilities.areVTypesIdentical(val1, val3, true));

        val1 = VDoubleArray.of(ArrayDouble.of(1,2,3),alarm,time,display);
        val2 = VDoubleArray.of(ArrayDouble.of(1,2,3),alarm,time,display);
        assertTrue(Utilities.areVTypesIdentical(val1, val2, true));
        assertTrue(Utilities.areVTypesIdentical(val1, val2, false));

        val2 = VDoubleArray.of(ArrayDouble.of(1,2,3,4),alarm,time,display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        val2 = VDoubleArray.of(ArrayDouble.of(1,2,4),alarm,time,display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));

        val1 = VLongArray.of(ArrayLong.of(1,2,3),alarm,time,display);
        val2 = VLongArray.of(ArrayLong.of(1,2,3),alarm,time,display);
        assertTrue(Utilities.areVTypesIdentical(val1, val2, true));
        assertTrue(Utilities.areVTypesIdentical(val1, val2, false));

        val2 = VLongArray.of(ArrayLong.of(1,2,3,4),alarm,time,display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        val2 = VLongArray.of(ArrayLong.of(1,2,4),alarm,time,display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));

        val1 = VULongArray.of(ArrayULong.of(1,2,3),alarm,time,display);
        val2 = VULongArray.of(ArrayULong.of(1,2,3),alarm,time,display);
        assertTrue(Utilities.areVTypesIdentical(val1, val2, true));
        assertTrue(Utilities.areVTypesIdentical(val1, val2, false));

        val2 = VULongArray.of(ArrayULong.of(1,2,3,4),alarm,time,display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        val2 = VULongArray.of(ArrayULong.of(1,2,4),alarm,time,display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));

        val1 = VLongArray.of(ArrayLong.of(1,2,3),alarm,time,display);
        val2 = VLong.of(10L, alarm, time, display);
        assertFalse(Utilities.areVTypesIdentical(val1, val2, true));
        assertFalse(Utilities.areVTypesIdentical(val2, val1, true));
    }

    @Test
    public void testDeltaValueToString(){
        Alarm alarm = Alarm.none();
        Display display = Display.none();
        Time time = Time.now();

        Optional<Threshold<?>> threshold = Optional.of(new Threshold<>(5d,-5d));

        Utilities.VTypeComparison result =  Utilities.deltaValueToString(null, null, threshold);
        assertEquals(VDisconnectedData.INSTANCE.toString(), result.getString());
        assertEquals(0, result.getValuesEqual());
        assertTrue(result.isWithinThreshold());

        VType val1 = VDouble.of(5d,alarm,time,display);
        result = Utilities.deltaValueToString(null, val1, threshold);
        assertEquals(VDisconnectedData.INSTANCE.toString(), result.getString());
        assertEquals(-1, result.getValuesEqual());
        assertFalse(result.isWithinThreshold());

        result = Utilities.deltaValueToString(val1, null, threshold);
        assertEquals("5.0", result.getString());
        assertEquals(1, result.getValuesEqual());
        assertFalse(result.isWithinThreshold());

        result = Utilities.deltaValueToString(VDisconnectedData.INSTANCE, val1, threshold);
        assertEquals(VDisconnectedData.INSTANCE.toString(), result.getString());
        assertEquals(-1, result.getValuesEqual());
        assertFalse(result.isWithinThreshold());

        result = Utilities.deltaValueToString(val1, VDisconnectedData.INSTANCE, threshold);
        assertEquals("5.0", result.getString());
        assertEquals(1, result.getValuesEqual());
        assertFalse(result.isWithinThreshold());

        val1 = VDouble.of(5d,alarm,time,display);
        VType val2 = VDouble.of(6d,alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("-1.0", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertTrue(result.isWithinThreshold());


        val1 = VDouble.of(15d,alarm,time,display);
        val2 = VDouble.of(6d,alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("+9.0", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VFloat.of(15f,alarm,time,display);
        val2 = VFloat.of(6f,alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("+9.0", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VDouble.of(6d,alarm,time,display);
        val2 = VDouble.of(6d,alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("0.0", result.getString());
        assertEquals(0,result.getValuesEqual());
        assertTrue(result.isWithinThreshold());

        val1 = VFloat.of(5f,alarm,time,display);
        val2 = VFloat.of(6f,alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("-1.0", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertTrue(result.isWithinThreshold());

        val1 = VFloat.of(5f,alarm,time,display);
        val2 = VFloat.of(6f,alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("-1.0", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertFalse(result.isWithinThreshold());

        val1 = VFloat.of(5f,alarm,time,display);
        val2 = VFloat.of(6f,alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("-1.0", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertFalse(result.isWithinThreshold());


        val1 = VLong.of(15L,alarm,time,display);
        val2 = VDouble.of(6d,alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("+9", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VLong.of(15L,alarm,time,display);
        val2 = VDouble.of(6d,alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("+9", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VULong.of(15L,alarm,time,display);
        val2 = VULong.of(6L,alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("+9", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VULong.of(5L,alarm,time,display);
        val2 = VULong.of(6L,alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("-1", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertFalse(result.isWithinThreshold());

        val1 = VUInt.of(15,alarm,time,display);
        val2 = VUInt.of(6,alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("+9", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VUInt.of(15,alarm,time,display);
        val2 = VUInt.of(6,alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("+9", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VInt.of(15,alarm,time,display);
        val2 = VInt.of(6,alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("+9", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VDouble.of(15d,alarm,time,display);
        val2 = VLong.of(6L,alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("+9.0", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VDouble.of(15d,alarm,time,display);
        val2 = VLong.of(6L,alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("+9.0", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VDouble.of(15d,alarm,time,display);
        val2 = VLong.of(15L,alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("0.0", result.getString());
        assertEquals(0,result.getValuesEqual());
        assertTrue(result.isWithinThreshold());

        val1 = VString.of("first",alarm,time);
        val2 = VLong.of(15L,alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("first", result.getString());
        assertNotEquals(0, result.getValuesEqual());
        assertFalse(result.isWithinThreshold());

        val1 = VDoubleArray.of(ArrayDouble.of(1,2,3),alarm,time,display);
        val2 = VDoubleArray.of(ArrayDouble.of(1,2,3),alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("[1.0, 2.0, 3.0]", result.getString());
        assertEquals(0,result.getValuesEqual());
        assertTrue(result.isWithinThreshold());

        val1 = VDoubleArray.of(ArrayDouble.of(1,2,3),alarm,time,display);
        val2 = VLongArray.of(ArrayLong.of(1,2,3),alarm,time,display);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("[1.0, 2.0, 3.0]", result.getString());
        assertNotEquals(0, result.getValuesEqual());
        assertFalse(result.isWithinThreshold());

        //compare long values: equal, first less than second, second less than first
        val1 = VLong.of(6L,alarm,time,display);
        val2 = VLong.of(6L,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("0", result.getString());
        assertEquals(0, result.getValuesEqual());
        assertTrue(result.isWithinThreshold());

        val1 = VLong.of(5L,alarm,time,display);
        val2 = VLong.of(6L,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("-1", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertTrue(result.isWithinThreshold());

        val1 = VLong.of(6L,alarm,time,display);
        val2 = VLong.of(5L,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("+1", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertTrue(result.isWithinThreshold());

        //compare int values: equal, first less than second, second less than first
        val1 = VInt.of(6,alarm,time,display);
        val2 = VInt.of(6,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("0", result.getString());
        assertEquals(0, result.getValuesEqual());
        assertTrue(result.isWithinThreshold());

        val1 = VInt.of(5,alarm,time,display);
        val2 = VInt.of(6,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("-1", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertTrue(result.isWithinThreshold());

        val1 = VInt.of(6,alarm,time,display);
        val2 = VInt.of(5,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("+1", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertTrue(result.isWithinThreshold());

        //compare short values: equal, first less than second, second less than first
        val1 = VShort.of((short)6,alarm,time,display);
        val2 = VShort.of((short)6,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("0", result.getString());
        assertEquals(0, result.getValuesEqual());
        assertTrue(result.isWithinThreshold());

        val1 = VShort.of((short)5,alarm,time,display);
        val2 = VShort.of((short)6,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("-1", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertTrue(result.isWithinThreshold());

        val1 = VShort.of((short)6,alarm,time,display);
        val2 = VShort.of((short)5,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("+1", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertTrue(result.isWithinThreshold());

        val1 = VUShort.of((short)6,alarm,time,display);
        val2 = VUShort.of((short)5,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("+1", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertTrue(result.isWithinThreshold());

        val1 = VUShort.of((short)6,alarm,time,display);
        val2 = VUShort.of((short)6,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("0", result.getString());
        assertEquals(0, result.getValuesEqual());
        assertTrue(result.isWithinThreshold());

        val1 = VUShort.of((short)5,alarm,time,display);
        val2 = VUShort.of((short)6,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("-1", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertTrue(result.isWithinThreshold());

        val1 = VShort.of((short)5,alarm,time,display);
        val2 = VShort.of((short)6,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("-1", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertFalse(result.isWithinThreshold());

        val1 = VUShort.of((short)5,alarm,time,display);
        val2 = VUShort.of((short)6,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("-1", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertFalse(result.isWithinThreshold());

        //compare enum values: equal, first less than second, second less than first
        EnumDisplay labels = EnumDisplay.of("val1","val2","val3");

        val1 = VEnum.of(1,labels,alarm,time);
        val2 = VEnum.of(1,labels,alarm,time);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("val2", result.getString());
        assertEquals(0, result.getValuesEqual());
        assertTrue(result.isWithinThreshold());

        val1 = VEnum.of(1,labels,alarm,time);
        val2 = VEnum.of(2,labels,alarm,time);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("val2", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertFalse(result.isWithinThreshold());

        val1 = VEnum.of(2,labels,alarm,time);
        val2 = VEnum.of(1,labels,alarm,time);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("val3", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VByte.of((byte)5,alarm,time,display);
        val2 = VByte.of((byte)6,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("-1", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertTrue(result.isWithinThreshold());

        val1 = VByte.of((byte)6,alarm,time,display);
        val2 = VByte.of((byte)5,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("+1", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertTrue(result.isWithinThreshold());

        val1 = VByte.of((byte)6,alarm,time,display);
        val2 = VByte.of((byte)5,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("+1", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VUByte.of((byte)5,alarm,time,display);
        val2 = VUByte.of((byte)6,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("-1", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertTrue(result.isWithinThreshold());

        val1 = VUByte.of((byte)6,alarm,time,display);
        val2 = VUByte.of((byte)5,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, threshold);
        assertEquals("+1", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertTrue(result.isWithinThreshold());

        val1 = VUByte.of((byte)6,alarm,time,display);
        val2 = VUByte.of((byte)5,alarm,time, display);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("+1", result.getString());
        assertTrue(result.getValuesEqual() > 0);
        assertFalse(result.isWithinThreshold());

        val1 = VBoolean.of(false,alarm,time);
        val2 = VBoolean.of(false,alarm,time);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("false", result.getString());
        assertTrue(result.getValuesEqual() == 0);
        assertTrue(result.isWithinThreshold());

        val1 = VString.of("a", alarm,time);
        val2 = VString.of("b", alarm,time);
        result = Utilities.deltaValueToString(val1, val2, Optional.empty());
        assertEquals("a", result.getString());
        assertTrue(result.getValuesEqual() < 0);
        assertFalse(result.isWithinThreshold());
    }

    @Test
    public void testDeltaValueToPercentage(){
        Alarm alarm = Alarm.none();
        Display display = Display.none();
        Time time = Time.now();

        Optional<Threshold<?>> threshold = Optional.of(new Threshold<>(5d,-5d));

        VDouble val1 = VDouble.of(5d, alarm, time, display);
        VDouble val2 = VDouble.of(4d, alarm, time, display);
        String result = Utilities.deltaValueToPercentage(val1, val2);
        assertEquals("20%", result);

        val1 = VDouble.of(4d, alarm, time, display);
        result = Utilities.deltaValueToPercentage(val1, val2);
        assertEquals("", result);

        val2 = VDouble.of(0d, alarm, time, display);
        result = Utilities.deltaValueToPercentage(val1, val2);
        assertEquals("0 Live", result);

        val2 = VDouble.of(1d, alarm, time, display);
        val1 = VDouble.of(0d, alarm, time, display);
        result = Utilities.deltaValueToPercentage(val1, val2);
        assertEquals("0 Stored", result);

        VDoubleArray val = VDoubleArray.of(ArrayDouble.of(1d), alarm, time, display);
        result = Utilities.deltaValueToPercentage(val, val2);
        assertEquals("", result);
    }

    @Test
    public void testAreValuesEqual(){
        Alarm alarm = Alarm.none();
        Display display = Display.none();
        Time time = Time.now();

        Optional<Threshold<?>> threshold = Optional.of(new Threshold<>(5d,-5d));
        Optional<Threshold<?>> threshold2 = Optional.of(new Threshold<>(0.5d,-0.5d));

        assertTrue(Utilities.areValuesEqual(null, null, threshold));

        VType val1 = VDouble.of(10d, alarm, time, display);

        assertFalse(Utilities.areValuesEqual(val1, null, threshold));
        assertFalse(Utilities.areValuesEqual(null, val1, threshold));
        assertTrue(Utilities.areValuesEqual(VDisconnectedData.INSTANCE, VDisconnectedData.INSTANCE, threshold));
        assertFalse(Utilities.areValuesEqual(VDisconnectedData.INSTANCE, val1, threshold));
        assertFalse(Utilities.areValuesEqual(val1, VDisconnectedData.INSTANCE, threshold));

        VType val2 = VDouble.of(10d, alarm, time, display);

        boolean result = Utilities.areValuesEqual(val1, val2, Optional.empty());

        assertTrue(result);
        VType val3 = VDouble.of(11d, alarm, time, display);
        result = Utilities.areValuesEqual(val1, val3, threshold);
        assertTrue(result);
        result = Utilities.areValuesEqual(val1, val3, threshold2);
        assertFalse(result);

        val1 = VFloat.of(10f, alarm, time, display);
        result = Utilities.areValuesEqual(val1, val2, Optional.empty());
        assertTrue(result);
        result = Utilities.areValuesEqual(val1, val3, threshold);
        assertTrue(result);
        result = Utilities.areValuesEqual(val1, val3, threshold2);
        assertFalse(result);

        val2 = VULong.of(10L, alarm, time, display);
        val1 = VULong.of(10L, alarm, time, display);
        result = Utilities.areValuesEqual(val1, val2, Optional.empty());
        assertTrue(result);
        val3 = VULong.of(11L, alarm, time, display);
        result = Utilities.areValuesEqual(val1, val3, threshold);
        assertTrue(result);
        result = Utilities.areValuesEqual(val1, val3, threshold2);
        assertFalse(result);

        val2 = VLong.of(10L, alarm, time, display);
        val1 = VLong.of(10L, alarm, time, display);
        result = Utilities.areValuesEqual(val1, val2, Optional.empty());
        assertTrue(result);
        val3 = VLong.of(11L, alarm, time, display);
        result = Utilities.areValuesEqual(val1, val3, threshold);
        assertTrue(result);
        result = Utilities.areValuesEqual(val1, val3, threshold2);
        assertFalse(result);

        val2 = VUInt.of(10, alarm, time, display);
        val1 = VUInt.of(10, alarm, time, display);
        result = Utilities.areValuesEqual(val1, val2, Optional.empty());
        assertTrue(result);
        val3 = VUInt.of(11, alarm, time, display);
        result = Utilities.areValuesEqual(val1, val3, threshold);
        assertTrue(result);
        result = Utilities.areValuesEqual(val1, val3, threshold2);
        assertFalse(result);

        val2 = VInt.of(10L, alarm, time, display);
        val1 = VInt.of(10L, alarm, time, display);
        result = Utilities.areValuesEqual(val1, val2, Optional.empty());
        assertTrue(result);
        val3 = VInt.of(11L, alarm, time, display);
        result = Utilities.areValuesEqual(val1, val3, threshold);
        assertTrue(result);
        result = Utilities.areValuesEqual(val1, val3, threshold2);
        assertFalse(result);

        val2 = VUShort.of((short)10, alarm, time, display);
        val1 = VUShort.of((short)10, alarm, time, display);
        result = Utilities.areValuesEqual(val1, val2, Optional.empty());
        assertTrue(result);
        val3 = VUShort.of((short)11, alarm, time, display);
        result = Utilities.areValuesEqual(val1, val3, threshold);
        assertTrue(result);
        result = Utilities.areValuesEqual(val1, val3, threshold2);
        assertFalse(result);

        val2 = VShort.of((short)10, alarm, time, display);
        val1 = VShort.of((short)10, alarm, time, display);
        result = Utilities.areValuesEqual(val1, val2, Optional.empty());
        assertTrue(result);
        val3 = VShort.of((short)11, alarm, time, display);
        result = Utilities.areValuesEqual(val1, val3, threshold);
        assertTrue(result);
        result = Utilities.areValuesEqual(val1, val3, threshold2);
        assertFalse(result);

        val2 = VUByte.of((byte)10, alarm, time, display);
        val1 = VUByte.of((byte)10, alarm, time, display);
        result = Utilities.areValuesEqual(val1, val2, Optional.empty());
        assertTrue(result);
        val3 = VUByte.of((byte)11, alarm, time, display);
        result = Utilities.areValuesEqual(val1, val3, threshold);
        assertTrue(result);
        result = Utilities.areValuesEqual(val1, val3, threshold2);
        assertFalse(result);

        val2 = VByte.of((byte)10, alarm, time, display);
        val1 = VByte.of((byte)10, alarm, time, display);
        result = Utilities.areValuesEqual(val1, val2, Optional.empty());
        assertTrue(result);
        val3 = VByte.of((byte)11, alarm, time, display);
        result = Utilities.areValuesEqual(val1, val3, threshold);
        assertTrue(result);
        result = Utilities.areValuesEqual(val1, val3, threshold2);
        assertFalse(result);

        val1 = VBoolean.of(true, alarm, time);
        val2 = VBoolean.of(true, alarm, time);
        val3 = VBoolean.of(false, alarm, time);
        assertTrue(Utilities.areValuesEqual(val1, val2, Optional.empty()));
        assertFalse(Utilities.areValuesEqual(val1, val3, Optional.empty()));
        assertFalse(Utilities.areValuesEqual(val1, null, Optional.empty()));
        assertFalse(Utilities.areValuesEqual(null, val3, Optional.empty()));
        assertTrue(Utilities.areValuesEqual(null, null, Optional.empty()));

        val1 = VEnum.of(1, EnumDisplay.of("a", "b", "c"), alarm, time);
        val2 = VEnum.of(1, EnumDisplay.of("a", "b", "c"), alarm, time);
        val3 = VEnum.of(2, EnumDisplay.of("a", "b", "c"), alarm, time);
        assertTrue(Utilities.areValuesEqual(val1, val2, Optional.empty()));
        assertFalse(Utilities.areValuesEqual(val1, val3, Optional.empty()));
        assertFalse(Utilities.areValuesEqual(val1, null, Optional.empty()));
        assertFalse(Utilities.areValuesEqual(null, val3, Optional.empty()));
        assertTrue(Utilities.areValuesEqual(null, null, Optional.empty()));

        val1 = VString.of("a", alarm, time);
        val2 = VString.of("a", alarm, time);
        val3 = VString.of("b", alarm, time);
        assertTrue(Utilities.areValuesEqual(val1, val2, Optional.empty()));
        assertFalse(Utilities.areValuesEqual(val1, val3, Optional.empty()));
        assertFalse(Utilities.areValuesEqual(val1, null, Optional.empty()));
        assertFalse(Utilities.areValuesEqual(null, val3, Optional.empty()));
        assertTrue(Utilities.areValuesEqual(null, null, Optional.empty()));

        val1 = VDoubleArray.of(ArrayDouble.of(1, 2, 3), alarm, time, display);
        val2 = VDoubleArray.of(ArrayDouble.of(1, 2, 3), alarm, time, display);
        val3 = VDoubleArray.of(ArrayDouble.of(1, 2, 3, 4), alarm, time, display);
        assertTrue(Utilities.areValuesEqual(val1, val2, Optional.empty()));
        assertFalse(Utilities.areValuesEqual(val1, val3, Optional.empty()));
        VType val4 = VDoubleArray.of(ArrayDouble.of(1, 2, 4), alarm, time, display);
        assertFalse(Utilities.areValuesEqual(val1, val4, Optional.empty()));

        val1 = VUIntArray.of(ArrayUInteger.of(1, 2, 3), alarm, time, display);
        val2 = VUIntArray.of(ArrayUInteger.of(1, 2, 3), alarm, time, display);
        val3 = VUIntArray.of(ArrayUInteger.of(1, 2, 3, 4), alarm, time, display);
        assertTrue(Utilities.areValuesEqual(val1, val2, Optional.empty()));
        assertFalse(Utilities.areValuesEqual(val1, val3, Optional.empty()));
        val4 = VUIntArray.of(ArrayUInteger.of(1, 2, 4), alarm, time, display);
        assertFalse(Utilities.areValuesEqual(val1, val4, Optional.empty()));

        val1 = VLongArray.of(ArrayLong.of(1, 2, 3), alarm, time, display);
        val2 = VLongArray.of(ArrayLong.of(1, 2, 3), alarm, time, display);
        val3 = VLongArray.of(ArrayLong.of(1, 2, 3, 4), alarm, time, display);
        assertTrue(Utilities.areValuesEqual(val1, val2, Optional.empty()));
        assertFalse(Utilities.areValuesEqual(val1, val3, Optional.empty()));
        val4 = VLongArray.of(ArrayLong.of(1, 2, 4), alarm, time, display);
        assertFalse(Utilities.areValuesEqual(val1, val4, Optional.empty()));

        val1 = VULongArray.of(ArrayULong.of(1, 2, 3), alarm, time, display);
        val2 = VULongArray.of(ArrayULong.of(1, 2, 3), alarm, time, display);
        val3 = VULongArray.of(ArrayULong.of(1, 2, 3, 4), alarm, time, display);
        assertTrue(Utilities.areValuesEqual(val1, val2, Optional.empty()));
        assertFalse(Utilities.areValuesEqual(val1, val3, Optional.empty()));
        val4 = VULongArray.of(ArrayULong.of(1, 2, 4), alarm, time, display);
        assertFalse(Utilities.areValuesEqual(val1, val4, Optional.empty()));

        val1 = VStringArray.of(Arrays.asList("a", "b", "c"), alarm, time);
        val2 = VStringArray.of(Arrays.asList("a", "b", "c"), alarm, time);
        assertFalse(Utilities.areValuesEqual(val1, val2, Optional.empty()));
    }
}
