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
import uk.ac.ebi.ega.accession.array.model.Array;
import uk.ac.ebi.ega.accession.array.persistence.ArrayEntity;
import uk.ac.ebi.ega.accession.array.persistence.ArrayEntityRepository;
import uk.ac.ebi.ega.accession.array.persistence.HistoricArrayEntity;
import uk.ac.ebi.ega.accession.array.persistence.HistoricArrayRepository;
import uk.ac.ebi.ega.accession.array.persistence.HistoricLogArrayEntity;
import uk.ac.ebi.ega.accession.array.persistence.HistoricLogArrayRepository;

import java.util.Collection;

@Configuration
@EntityScan({"uk.ac.ebi.ega.accession.array.persistence"})
@EnableJpaRepositories(basePackages = {"uk.ac.ebi.ega.accession.array.persistence"})
public class ArrayServiceConfiguration {

    private final String PAD_FORMAT = "%011d";

    private final String PREFIX = "EGAA";

    private final String CATEGORY_ID = "array";

    @Value("${accessioning.instanceId}")
    private String applicationInstanceId;

    @Autowired
    private ContiguousIdBlockService blockService;

    @Autowired
    private ArrayEntityRepository repository;

    @Autowired
    private HistoricArrayRepository historicRepository;

    @Autowired
    private HistoricLogArrayRepository historicLogRepository;

    @Bean
    public AccessioningService<Array, String, String> prefixArrayAccessionService() {
        return DecoratedAccessioningService.buildPrefixPaddedLongAccessionService(arrayAccessionService(), PREFIX,
                PAD_FORMAT, Long::parseLong);
    }

    @Bean
    public AccessioningService<Array, String, Long> arrayAccessionService() {
        return new BasicAccessioningService<>(
                new MonotonicAccessionGenerator<>(CATEGORY_ID, applicationInstanceId, blockService,
                        monotonicDatabaseService()),
                arrayAccessioningDatabaseService(),
                array -> array.getSubmissionAccount() + "_" + array.getAlias(),
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
    public DatabaseService<Array, String, Long> arrayAccessioningDatabaseService() {
        return new BasicSpringDataRepositoryDatabaseService<>(repository,
                ArrayEntity::new,
                historicArrayAccessionService());
    }

    @Bean
    public InactiveAccessionService<Array, Long, ArrayEntity> historicArrayAccessionService() {
        return new BasicJpaInactiveAccessionService<>(
                historicLogRepository,
                HistoricArrayEntity::new,
                historicRepository,
                HistoricLogArrayEntity::new);
    }

}
