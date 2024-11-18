package controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import service.MatchesService;

import java.io.File;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matches")
@Slf4j
public class vlController {
    @Autowired
    private final MatchesService matchesService;

    @GetMapping("/{league}/round/{number}")
    public File getMatchesByRound(@PathVariable("league") String league,
                                  @PathVariable("number") long number){
        return matchesService.getMatchesByRound(league, number);
    }
}
