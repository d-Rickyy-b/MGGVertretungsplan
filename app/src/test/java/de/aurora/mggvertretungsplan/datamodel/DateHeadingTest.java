package de.aurora.mggvertretungsplan.datamodel;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

/**
 * Created by Rico on 19.11.2017.
 */
public class DateHeadingTest {

    @Before
    public void setUp() {
    }

    @Test
    public void testGetWholeDate() {
        LocalDateTime date = LocalDateTime.of(2017, 1, 20, 0, 0);
        DateHeading dateHeading = new DateHeading(date);
        assertEquals("Freitag, 20.01.2017", dateHeading.getWholeDate());

        LocalDateTime date2 = LocalDateTime.of(2020, 2, 29, 0, 0);
        DateHeading dateHeading2 = new DateHeading(date2);
        assertEquals("Samstag, 29.02.2020", dateHeading2.getWholeDate());
    }

    @Test
    public void testGetWeek() {
        LocalDateTime date = LocalDateTime.of(2017, 1, 20, 0, 0);
        DateHeading dateHeading = new DateHeading(date);
        Week week = dateHeading.getWeek();
        Week comparison = new Week("A");
        assertEquals("A", week.toString());
        assertEquals(comparison.toString(), week.toString());

        Week week1 = new Week("B");
        DateHeading dateHeading1 = new DateHeading(date, week1);
        // Make sure toString returns the correct String
        assertEquals("B", week1.toString());
        assertEquals(week1.toString(), dateHeading1.getWeek().toString());
    }

}