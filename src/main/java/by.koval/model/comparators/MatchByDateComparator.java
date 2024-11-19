package by.koval.model.comparators;

import by.koval.model.Match;

import java.util.Comparator;

public class MatchByDateComparator implements Comparator<Match> {
    @Override
    public int compare(Match m1, Match m2) {
        if (m1.equals(m2)) {
            return 0;
        } else if (m1.getDateTime().isBefore(m2.getDateTime())) {
            return -1;
        } else {
            return 1;
        }
    }
}
