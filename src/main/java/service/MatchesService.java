package service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.File;

@Service
@Slf4j
@Validated
public class MatchesService {
    public File getMatchesByRound(String league, long number) {
        return null;
    }
}
