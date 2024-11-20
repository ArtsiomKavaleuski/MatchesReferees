package by.koval.storage;

import by.koval.model.Match;

import java.util.List;

public interface DataStorage {
    void refreshMatchesDb();
    void refreshRefereesDb();
    int updateMatchesDb();
    int updateRefereesDb();
    List<Match> getMatchesByCompetitionByRound(String competitionName, int round);
}
