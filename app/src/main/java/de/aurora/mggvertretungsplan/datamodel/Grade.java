package de.aurora.mggvertretungsplan.datamodel;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Rico on 16.11.2017.
 */

public class Grade {
    private String classLayer, classTitle;

    public Grade(String grade) {
        Log.d("Grade", String.format("Creating new Grade object for class: %s", grade));
        if ("K1".equals(grade) || "K2".equals(grade)) {
            classLayer = grade;
            classTitle = "";
        } else {
            try {
                Matcher matcher = Pattern.compile("([0-9]+)([a-fA-F])").matcher(grade);

                if (!matcher.find())
                    return;

                classLayer = matcher.group(1);
                classTitle = matcher.group(2);
            } catch (IllegalStateException e) {
                initializeOnException();
                Log.e("Grade", String.format("IllegalStateException: %s", e.getMessage()));
            } catch (IndexOutOfBoundsException e) {
                initializeOnException();
                Log.e("Grade", String.format("IndexOutOfBoundsException: %s", e.getMessage()));
            } catch (NullPointerException e) {
                initializeOnException();
                Log.e("Grade", String.format("NullPointerException: %s", e.getMessage()));
            }
        }
    }

    public Grade(String classLayer, String classTitle) {
        this.classLayer = classLayer;
        this.classTitle = classTitle;
    }

    public boolean matches(String classString) {
        Matcher matcher = Pattern.compile("([0-9]+)([a-f]+)").matcher(classString);

        if (matcher.find()) {
            try {
                String inputClassLayer = matcher.group(1);
                String inputClassTitle = matcher.group(2);

                if (null == inputClassLayer || null == inputClassTitle) {
                    return false;
                }

                return inputClassLayer.equals(classLayer) && inputClassTitle.contains(classTitle);
            } catch (IllegalStateException e) {
                Log.e("Grade", String.format("IllegalStateException: %s", e.getMessage()));
                return false;
            } catch (IndexOutOfBoundsException e) {
                Log.e("Grade", String.format("IndexOutOfBoundsException: %s", e.getMessage()));
                return false;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return String.format("%s%s", classLayer, classTitle);
    }

    private void initializeOnException() {
        classLayer = "5";
        classTitle = "a";
    }
}
