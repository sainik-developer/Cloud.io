package com.cloudio.rest.controllers;

import com.cloudio.rest.entity.TokenStatsDO;
import com.cloudio.rest.repository.TokenStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class TokenStatsController {
    private final TokenStatsRepository tokenStatsRepository;

    @CrossOrigin
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Flux<TokenStatsDO> findByDate(@RequestParam("page") int page, @RequestParam("size") int size) {
        return tokenStatsRepository.findByDateTime(PageRequest.of(page, size, new Sort(Sort.Direction.DESC, "updatedTime")));
    }
}
