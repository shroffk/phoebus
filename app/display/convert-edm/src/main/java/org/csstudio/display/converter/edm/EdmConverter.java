/*******************************************************************************
 * Copyright (c) 2019 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.csstudio.display.converter.edm;

import static org.csstudio.display.converter.edm.Converter.logger;

import java.lang.reflect.Constructor;
import java.util.logging.Level;

import org.csstudio.display.builder.model.DisplayModel;
import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.converter.edm.widgets.ConverterBase;
import org.csstudio.opibuilder.converter.model.EdmDisplay;
import org.csstudio.opibuilder.converter.model.EdmEntity;
import org.csstudio.opibuilder.converter.model.EdmWidget;

/** Convert {@link EdmDisplay} to {@link DisplayModel}
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class EdmConverter
{
    private final DisplayModel model = new DisplayModel();

    private int offset_x = 0, offset_y = 0;

    public EdmConverter(final String name, final EdmDisplay edm)
    {
        model.propName().setValue(name);
        model.propX().setValue(edm.getX());
        model.propY().setValue(edm.getY());
        model.propWidth().setValue(edm.getW());
        model.propHeight().setValue(edm.getH());

        // TODO Global edm.getFont()?
        ConverterBase.convertColor(edm.getBgColor(), model.propBackgroundColor());

        if (edm.getTitle() != null)
            model.propName().setValue(edm.getTitle());

        model.propGridVisible().setValue(edm.isShowGrid());
        if (edm.getGridSize() > 0)
        {
            model.propGridStepX().setValue(edm.getGridSize());
            model.propGridStepY().setValue(edm.getGridSize());
        }

        for (EdmEntity edm_widget : edm.getWidgets())
            convertWidget(model, edm_widget);
    }

    /** @return {@link DisplayModel} */
    public DisplayModel getDisplayModel()
    {
        return model;
    }

    /** @param x X offset and
     *  @param y Y offset of widgets within currently handled container
     */
    public void addPositionOffset(final int x, final int y)
    {
        offset_x += x;
        offset_y += y;
    }

    /** @return X offset of widgets within currently handled container */
    public int getOffsetX()
    {
        return offset_x;
    }

    /** @return Y offset of widgets within currently handled container */
    public int getOffsetY()
    {
        return offset_y;
    }

    /** Convert one widget
     *  @param parent Parent
     *  @param edm EDM widget to convert
     */
    public void convertWidget(final Widget parent, final EdmEntity edm)
    {
        // Given an EDM Widget type like "activeXTextClass",
        // locate the matching "Convert_activeXTextClass"
        final Class<?> clazz;
        try
        {
            final String wc_name = ConverterBase.class.getPackageName() + ".Convert_" + edm.getType();
            clazz = Class.forName(wc_name);
        }
        catch (ClassNotFoundException ex)
        {
            logger.log(Level.WARNING, "No converter for EDM " + edm.getType());
            return;
        }

        try
        {
            for (Constructor<?> c : clazz.getConstructors())
            {   // Look for suitable constructor
                final Class<?>[] parms = c.getParameterTypes();
                if (parms.length == 3  &&
                    parms[0] == EdmConverter.class &&
                    parms[1] == Widget.class       &&
                    EdmWidget.class.isAssignableFrom(parms[2]))
                {
                    // Simply constructing the converter will perform the conversion
                    c.newInstance(this, parent, edm);
                    return;
                }
            }
            throw new Exception(clazz.getSimpleName() + " lacks required constructor");
        }
        catch (Exception ex)
        {
            logger.log(Level.WARNING, "Cannot convert " + edm.getType(), ex);
        }
    }
}