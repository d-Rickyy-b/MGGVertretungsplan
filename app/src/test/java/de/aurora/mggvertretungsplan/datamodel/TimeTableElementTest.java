package de.aurora.mggvertretungsplan.datamodel;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by Rico on 18.11.2017.
 */
public class TimeTableElementTest extends TestCase {
    String hour;
    String class_name;
    String subject;
    String newSubject;
    String room;
    String newRoom;
    String info;

    @Before
    public void setUp() throws Exception {
        hour = "3-4";
        class_name = "10a";
        subject = "D";
        newSubject = "E";
        room = "H105";
        newRoom = "H207";
        info = "";
    }

    @Test
    public void testGetHour_I() throws Exception {
        TimeTableElement tte = new TimeTableElement(hour, class_name, subject, newSubject, room, newRoom, info);

        assertEquals(3, tte.getHour_I());
    }

    @Test
    public void testGetType() throws Exception {
        TimeTableElement tte = new TimeTableElement(hour, class_name, subject, newSubject, room, newRoom, info);
        assertEquals(TimeTableElement.SUBSTITUTION, tte.getType());

        TimeTableElement tte2 = new TimeTableElement(hour, class_name, subject, "---", room, "---", info);
        assertEquals(TimeTableElement.CANCELLATION, tte2.getType());

        TimeTableElement emptyElement = new TimeTableElement();
        assertEquals(TimeTableElement.EMPTY, emptyElement.getType());
    }

    @Test
    public void testGetInfoForDisplay() throws Exception {
        TimeTableElement tte = new TimeTableElement(hour, class_name, subject, newSubject, room, newRoom, "Raumänderung");

        assertEquals(String.format("%s - %s", "Englisch", "Raumänderung"), tte.getInfoForDisplay());
    }

    @Test
    public void testEquals() throws Exception {
        TimeTableElement tte = new TimeTableElement(hour, class_name, subject, newSubject, room, newRoom, info);
        TimeTableElement tte2 = new TimeTableElement(hour, class_name, subject, newSubject, room, newRoom, info);

        assertTrue(tte.equals(tte2));

        TimeTableElement tte3 = new TimeTableElement(hour, class_name, "E", newSubject, room, newRoom, info);

        assertFalse(tte.equals(tte3));
    }

    @Test
    public void testGetDiffAmount() throws Exception {
        String hour = "3";
        String class_name = "10a";
        String subject = "D";
        String newSubject = "E";
        String room = "H105";
        String newRoom = "H207";
        String info = "";
        TimeTableElement tte = new TimeTableElement(hour, class_name, subject, newSubject, room, newRoom, info);
        TimeTableElement tte2 = new TimeTableElement(hour, class_name, subject, "D", room, newRoom, info);

        assertEquals(1, tte.getDiffAmount(tte2));

        tte2 = new TimeTableElement(hour, class_name, subject, "D", room, "H208", info);

        assertEquals(2, tte.getDiffAmount(tte2));

        tte2 = new TimeTableElement(hour, class_name, subject, "D", room, "H208", "Raumänderung");
    }

    @Test
    public void testToString() throws Exception {
    }

}