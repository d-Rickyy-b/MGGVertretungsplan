package de.aurora.mggvertretungsplan.datamodel;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Created by Rico on 19.11.2017.
 */
public class DateHeadingTest extends TestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGetWholeDate() throws Exception {
        Date date = new Date(117, 0, 20);
        DateHeading dateHeading = new DateHeading(date);
        assertEquals("Freitag, 20.01.2017", dateHeading.getWholeDate());

        date = new Date(120, 1, 29);
        dateHeading = new DateHeading(date);
        assertEquals("Samstag, 29.02.2020", dateHeading.getWholeDate());
    }

}