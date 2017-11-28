package de.aurora.mggvertretungsplan.parsing;

import de.aurora.mggvertretungsplan.datamodel.TimeTable;

/**
 * Created by Rico on 28.11.2017.
 */

public interface ParsingCompleteListener {
    void onParsingComplete(TimeTable timeTable);
}
