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
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.service.ContiguousIdBlockService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.monotonic.service.MonotonicDatabaseService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.service.BasicJpaInactiveAccessionService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.services.BasicSpringDataRepositoryDatabaseService;
import uk.ac.ebi.ampt2d.commons.accession.persistence.services.InactiveAccessionService;
import uk.ac.ebi.ega.accession.file.model.FileModel;
import uk.ac.ebi.ega.accession.file.persistence.FileEntity;
import uk.ac.ebi.ega.accession.file.persistence.FileEntityRepository;
import uk.ac.ebi.ega.accession.file.persistence.HistoricFileEntity;
import uk.ac.ebi.ega.accession.file.persistence.HistoricFileRepository;
import uk.ac.ebi.ega.accession.file.persistence.HistoricLogFileEntity;
import uk.ac.ebi.ega.accession.file.persistence.HistoricLogFileRepository;

import java.util.Collection;

@Configuration
@EntityScan({"uk.ac.ebi.ega.accession.file.persistence"})
@EnableJpaRepositories(basePackages = {"uk.ac.ebi.ega.accession.file.persistence"})
public class FileServiceConfiguration {

    private final String PAD_FORMAT = "%011d";

    private final String PREFIX = "EGAF";

    private final String CATEGORY_ID = "file";

    @Autowired
    private ContiguousIdBlockService blockService;

    @Autowired
    private FileEntityRepository repository;

    @Autowired
    private HistoricFileRepository historicFileRepository;

    @Autowired
    private HistoricLogFileRepository historicLogFileRepository;

    @Bean
    public AccessioningService<FileModel, String, String> prefixFileAccessionService() {
        return DecoratedAccessioningService.buildPrefixPaddedLongAccessionService(fileAccessionService(), PREFIX,
                PAD_FORMAT, Long::parseLong);
    }

    @Bean
    public AccessioningService<FileModel, String, Long> fileAccessionService() {
        return new BasicAccessioningService<>(
                new MonotonicAccessionGenerator<>(CATEGORY_ID, "ega-accession-01", blockService, monotonicDatabaseService()),
                fileAccessioningDatabaseService(),
                fileModel -> fileModel.getHash(),
                message -> message);
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
    public DatabaseService<FileModel, String, Long> fileAccessioningDatabaseService() {
        return new BasicSpringDataRepositoryDatabaseService<>(repository,
                FileEntity::new,
                historicFileAccessionService());
    }

    @Bean
    public InactiveAccessionService<FileModel, Long, FileEntity> historicFileAccessionService() {
        return new BasicJpaInactiveAccessionService<>(
                historicLogFileRepository,
                HistoricFileEntity::new,
                historicFileRepository,
                HistoricLogFileEntity::new);
    }

}
