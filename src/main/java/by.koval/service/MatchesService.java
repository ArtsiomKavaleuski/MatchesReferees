package by.koval.service;

import by.koval.storage.DataLoader;
import by.koval.storage.WordWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.File;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchesService {
    @Autowired
    private final DataLoader dataLoader;
    @Autowired
    private final WordWorker wordWorker;

    public URI getMatchesByRound(String competitionCode, long round) {
        return null;
    }
}
