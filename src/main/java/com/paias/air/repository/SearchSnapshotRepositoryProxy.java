package com.paias.air.repository;

import com.paias.air.model.n4j.SearchSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SearchSnapshotRepositoryProxy {
    private final SearchSnapshotRepository snapshotRepository;

    @Value("${tequila.enable-caching}")
    private boolean enableCaching;
    public SearchSnapshotRepositoryProxy(SearchSnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
    }

    public void save(SearchSnapshot snapshot){
        if (!enableCaching) {
            return;
        }
        snapshotRepository.save(snapshot);
    }

    public SearchSnapshot findByQuery(String queryParams) {
        if (!enableCaching) {
            return null;
        }
        return snapshotRepository.findByQuery(queryParams);
    }
}
