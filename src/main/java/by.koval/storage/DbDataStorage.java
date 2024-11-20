package by.koval.storage;

import by.koval.model.Match;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DbDataStorage implements DataStorage {
    private JdbcTemplate jdbc;

    @Override
    public void refreshMatchesDb() {

    }

    @Override
    public void refreshRefereesDb() {

    }

    @Override
    public int updateMatchesDb() {
        return 0;
    }

    @Override
    public int updateRefereesDb() {
        return 0;
    }

    @Override
    public List<Match> getMatchesByCompetitionByRound(String competitionName, int round) {
        return List.of();
    }
}
