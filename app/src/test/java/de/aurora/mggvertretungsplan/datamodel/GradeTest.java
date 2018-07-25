package de.aurora.mggvertretungsplan.datamodel;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by Rico on 18.11.2017.
 */
public class GradeTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testEmptyConstructor() throws Exception {
        //Mockito.mock(Log.class);
        Grade grade = new Grade("");
        assertNotNull(grade);
        assertEquals("5a", grade.toString());
    }

    @Test
    public void testWrongConstructor() throws Exception {
        Grade grade = new Grade("ASDF");
        Grade grade2 = new Grade("a10");

        assertEquals("5a", grade.toString());
        assertEquals("5a", grade2.toString());
    }

    @Test
    public void testCorrectConstructor() throws Exception {
        List<String> classList = Arrays.asList("5a", "5b", "5c", "5d", "5e", "5f",
                "6a", "6b", "6c", "6d", "6e", "6f",
                "6a", "6b", "6c", "6d", "6e", "6f",
                "7a", "7b", "7c", "7d", "7e", "7f",
                "8a", "8b", "8c", "8d", "8e", "8f",
                "9a", "9b", "9c", "9d", "9e", "9f",
                "10a", "10b", "10c", "10d", "10e", "10f",
                "K1", "K2");

        for (String className : classList) {
            Grade grade = new Grade(className);
            assertEquals(className, grade.toString());
        }
    }

    @Test
    public void testNull() throws Exception {
        Grade grade = new Grade(null);

        assertEquals("5a", grade.toString());
    }

    @Test
    public void testMatches() {
        Grade grade = new Grade("5a");
        Grade grade2 = new Grade("5b");

        assertFalse(grade.toString().equals(grade2.toString()));

        Grade grade3 = new Grade("5a");
        Grade grade4 = new Grade("");

        assertEquals(grade3.toString(), grade4.toString());

        Grade grade5 = new Grade("K1");
        Grade grade6 = new Grade("K1");

        assertEquals(grade5.toString(), grade6.toString());
    }


}