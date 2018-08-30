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
import uk.ac.ebi.ega.accession.sample.controlled.model.SampleControlled;
import uk.ac.ebi.ega.accession.sample.controlled.persistence.HistoricLogSampleControlledEntity;
import uk.ac.ebi.ega.accession.sample.controlled.persistence.HistoricLogSampleControlledRepository;
import uk.ac.ebi.ega.accession.sample.controlled.persistence.HistoricSampleControlledEntity;
import uk.ac.ebi.ega.accession.sample.controlled.persistence.HistoricSampleControlledRepository;
import uk.ac.ebi.ega.accession.sample.controlled.persistence.SampleControlledEntity;
import uk.ac.ebi.ega.accession.sample.controlled.persistence.SampleControlledEntityRepository;
import uk.ac.ebi.ega.accession.sample.open.model.SampleOpen;
import uk.ac.ebi.ega.accession.sample.open.persistence.HistoricLogSampleOpenEntity;
import uk.ac.ebi.ega.accession.sample.open.persistence.HistoricLogSampleOpenRepository;
import uk.ac.ebi.ega.accession.sample.open.persistence.HistoricSampleOpenEntity;
import uk.ac.ebi.ega.accession.sample.open.persistence.HistoricSampleOpenRepository;
import uk.ac.ebi.ega.accession.sample.open.persistence.SampleOpenEntity;
import uk.ac.ebi.ega.accession.sample.open.persistence.SampleOpenEntityRepository;

import java.util.Collection;

@Configuration
@EntityScan({"uk.ac.ebi.ega.accession.sample.controlled.persistence",
        "uk.ac.ebi.ega.accession.sample.open.persistence"})
@EnableJpaRepositories(basePackages = {"uk.ac.ebi.ega.accession.sample.controlled.persistence",
        "uk.ac.ebi.ega.accession.sample.open.persistence"})
public class SampleControlledServiceConfiguration {

    private final String PAD_FORMAT = "%011d";

    private final String PREFIX = "EGAN";

    private final String CATEGORY_ID = "sample";

    @Value("${accessioning.instanceId}")
    private String applicationInstanceId;

    @Autowired
    private ContiguousIdBlockService blockService;

    @Autowired
    private SampleControlledEntityRepository repository;

    @Autowired
    private HistoricSampleControlledRepository historicRepository;

    @Autowired
    private HistoricLogSampleControlledRepository historicLogRepository;

    @Autowired
    private SampleOpenEntityRepository openRepository;

    @Autowired
    private HistoricSampleOpenRepository historicOpenRepository;

    @Autowired
    private HistoricLogSampleOpenRepository historicLogOpenRepository;

    @Bean
    public AccessioningService<SampleControlled, String, String> prefixSampleControlledAccessionService() {
        return DecoratedAccessioningService.buildPrefixPaddedLongAccessionService(sampleControlledAccessionService(), PREFIX,
                PAD_FORMAT, Long::parseLong);
    }

    @Bean
    public AccessioningService<SampleControlled, String, Long> sampleControlledAccessionService() {
        return new BasicAccessioningService<>(
                new MonotonicAccessionGenerator<>(CATEGORY_ID, applicationInstanceId + "-controlled-sample",
                        blockService,
                        monotonicDatabaseService()),
                sampleControlledAccessioningDatabaseService(),
                sampleControlled -> sampleControlled.getSubmissionAccount() + "_" + sampleControlled.getAlias(),
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
    public DatabaseService<SampleControlled, String, Long> sampleControlledAccessioningDatabaseService() {
        return new BasicSpringDataRepositoryDatabaseService<>(repository,
                SampleControlledEntity::new,
                historicSampleControlledAccessionService());
    }

    @Bean
    public InactiveAccessionService<SampleControlled, Long, SampleControlledEntity> historicSampleControlledAccessionService() {
        return new BasicJpaInactiveAccessionService<>(
                historicLogRepository,
                HistoricSampleControlledEntity::new,
                historicRepository,
                HistoricLogSampleControlledEntity::new);
    }

    @Bean
    public AccessioningService<SampleOpen, String, String> prefixSampleOpenAccessionService() {
        return DecoratedAccessioningService.buildPrefixPaddedLongAccessionService(sampleOpenAccessionService(), PREFIX,
                PAD_FORMAT, Long::parseLong);
    }

    @Bean
    public AccessioningService<SampleOpen, String, Long> sampleOpenAccessionService() {
        return new BasicAccessioningService<>(
                new MonotonicAccessionGenerator<>(CATEGORY_ID, applicationInstanceId + "-open-sample", blockService,
                        monotonicDatabaseService()),
                sampleOpenAccessioningDatabaseService(),
                sampleOpen -> sampleOpen.getBiosampleAccession(),
                summary -> summary);
    }

    @Bean
    public DatabaseService<SampleOpen, String, Long> sampleOpenAccessioningDatabaseService() {
        return new BasicSpringDataRepositoryDatabaseService<>(openRepository,
                SampleOpenEntity::new,
                historicSampleOpenAccessionService());
    }

    @Bean
    public InactiveAccessionService<SampleOpen, Long, SampleOpenEntity> historicSampleOpenAccessionService() {
        return new BasicJpaInactiveAccessionService<>(
                historicLogOpenRepository,
                HistoricSampleOpenEntity::new,
                historicOpenRepository,
                HistoricLogSampleOpenEntity::new);
    }

}
