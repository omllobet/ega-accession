/*
 *
 * Copyright 2018 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package uk.ac.ebi.ega.accession.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.ac.ebi.ampt2d.commons.accession.core.AccessioningService;
import uk.ac.ebi.ampt2d.commons.accession.core.BasicAccessioningService;
import uk.ac.ebi.ampt2d.commons.accession.core.DatabaseService;
import uk.ac.ebi.ampt2d.commons.accession.core.DecoratedAccessioningService;
import uk.ac.ebi.ampt2d.commons.accession.generators.monotonic.MonotonicAccessionGenerator;
import uk.ac.ebi.ampt2d.commons.accession.generators.monotonic.MonotonicRange;
import uk.ac.ebi.ampt2d.commons.accession.hashing.SHA1HashingFunction;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.service.ContiguousIdBlockService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.service.MonotonicDatabaseService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.service.BasicJpaInactiveAccessionService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.services.BasicSpringDataRepositoryDatabaseService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.services.InactiveAccessionService;
import uk.ac.ebi.ega.accession.run.model.Run;
import uk.ac.ebi.ega.accession.run.persistence.HistoricLogRunEntity;
import uk.ac.ebi.ega.accession.run.persistence.HistoricLogRunRepository;
import uk.ac.ebi.ega.accession.run.persistence.HistoricRunEntity;
import uk.ac.ebi.ega.accession.run.persistence.HistoricRunRepository;
import uk.ac.ebi.ega.accession.run.persistence.RunEntity;
import uk.ac.ebi.ega.accession.run.persistence.RunEntityRepository;

import java.util.Collection;

@Configuration
@EntityScan({"uk.ac.ebi.ega.accession.run.persistence"})
@EnableJpaRepositories(basePackages = {"uk.ac.ebi.ega.accession.run.persistence"})
public class RunServiceConfiguration {

    private final String PAD_FORMAT = "%011d";

    private final String PREFIX = "EGAR";

    private final String CATEGORY_ID = "run";

    @Value("${accessioning.instanceId}")
    private String applicationInstanceId;

    @Autowired
    private ContiguousIdBlockService blockService;

    @Autowired
    private RunEntityRepository repository;

    @Autowired
    private HistoricRunRepository historicRepository;

    @Autowired
    private HistoricLogRunRepository historicLogRepository;

    @Bean
    public AccessioningService<Run, String, String> prefixRunAccessionService() {
        return DecoratedAccessioningService.buildPrefixPaddedLongAccessionService(runAccessionService(), PREFIX,
                PAD_FORMAT, Long::parseLong);
    }

    @Bean
    public AccessioningService<Run, String, Long> runAccessionService() {
        return new BasicAccessioningService<>(
                new MonotonicAccessionGenerator<>(CATEGORY_ID, applicationInstanceId, blockService,
                        monotonicDatabaseService()),
                runAccessioningDatabaseService(),
                run -> run.getSubmissionAccount() + "_" + run.getAlias(),
                new SHA1HashingFunction());
    }

    public MonotonicDatabaseService monotonicDatabaseService() {
        return new MonotonicDatabaseService() {
            @Override
            public long[] getAccessionsInRanges(Collection<MonotonicRange> collection) {
                //Not implemented yet
                return new long[0];
            }
        };
    }

    @Bean
    public DatabaseService<Run, String, Long> runAccessioningDatabaseService() {
        return new BasicSpringDataRepositoryDatabaseService<>(repository,
                RunEntity::new,
                historicRunAccessionService());
    }

    @Bean
    public InactiveAccessionService<Run, Long, RunEntity> historicRunAccessionService() {
        return new BasicJpaInactiveAccessionService<>(
                historicLogRepository,
                HistoricRunEntity::new,
                historicRepository,
                HistoricLogRunEntity::new);
    }

}
