package org.phoebus.archive.reader.appliance;

import org.phoebus.archive.reader.ArchiveReader;
import org.phoebus.archive.reader.spi.ArchiveReaderFactory;

/**
 * SPI for "pbraw:" archive URLs
 * @author Miha Novak <miha.novak@cosylab.com>
 */
public class ApplianceArchiveReaderFactory implements ArchiveReaderFactory{

    public static final String PREFIX = "pbraw:";

    @Override
    public String getPrefix()
    {
        return PREFIX;
    }

    @Override
    public ArchiveReader createReader(final String url) throws Exception
    {
        final ApplianceArchiveReader reader = new ApplianceArchiveReader(url,
                AppliancePreferences.useStatisticsForOptimizedData,
                AppliancePreferences.useNewOptimizedOperator);
        return reader;
    }

}
