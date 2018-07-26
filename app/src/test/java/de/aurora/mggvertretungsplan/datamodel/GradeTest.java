package de.aurora.mggvertretungsplan.datamodel;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
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

        Grade newGrade = new Grade("5", "a");
        Grade newGrade1 = new Grade("6", "c");
        Grade newGrade2 = new Grade("K1", "");
        Grade newGrade3 = new Grade("8", "e");
        Grade newGrade4 = new Grade("10", "a");

        assertEquals("5a", newGrade.toString());
        assertEquals("6c", newGrade1.toString());
        assertEquals("K1", newGrade2.toString());
        assertEquals("8e", newGrade3.toString());
        assertEquals("10a", newGrade4.toString());
    }

    @Test
    public void testGradeGeneral() throws Exception {
        Grade grade = new Grade("5a");
        Grade grade2 = new Grade("5b");

        assertFalse(grade.toString().equals(grade2.toString()));

        Grade grade3 = new Grade("5a");
        Grade grade4 = new Grade(""); // When passing an empty string, the grade should be initialized with "5a"

        assertEquals(grade3.toString(), grade4.toString());

        Grade grade5 = new Grade("K1");
        Grade grade6 = new Grade("K1");

        assertEquals(grade5.toString(), grade6.toString());
    }

    @Test
    public void testNull() throws Exception {
        Grade grade = new Grade(null);

        assertEquals("5a", grade.toString());
    }

    @Test
    public void testMatches() throws Exception {
        Grade grade1 = new Grade("8a");
        assertTrue(grade1.matches("8ab_Nd_N"));

        Grade grade2 = new Grade("8b");
        assertTrue(grade2.matches("8ab_Nd_N"));
        assertTrue(grade2.matches("8abcd_NWT"));
        assertTrue(grade2.matches("8ab_Nd_N"));

        Grade grade3 = new Grade("7e");
        assertTrue(grade3.matches("7de_EFS"));

        Grade grade4 = new Grade("K1");
        assertTrue(grade4.matches("K1_M1"));

        Grade grade5 = new Grade("K2");
        assertTrue(grade5.matches("K2"));

        Grade grade6 = new Grade("10a");
        assertFalse(grade6.matches("10bcd"));
    }


}