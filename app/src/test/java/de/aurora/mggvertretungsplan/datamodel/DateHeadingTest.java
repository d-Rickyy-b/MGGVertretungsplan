package de.aurora.mggvertretungsplan.datamodel;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Created by Rico on 19.11.2017.
 */
public class DateHeadingTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetWholeDate() throws Exception {
        Date date = new Date(117, 0, 20);
        DateHeading dateHeading = new DateHeading(date);
        assertEquals("Freitag, 20.01.2017", dateHeading.getWholeDate());

        Date date2 = new Date(120, 1, 29);
        DateHeading dateHeading2 = new DateHeading(date2);
        assertEquals("Samstag, 29.02.2020", dateHeading2.getWholeDate());
    }

}