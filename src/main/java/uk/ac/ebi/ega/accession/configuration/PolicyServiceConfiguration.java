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
import uk.ac.ebi.ega.accession.policy.model.Policy;
import uk.ac.ebi.ega.accession.policy.persistence.HistoricLogPolicyEntity;
import uk.ac.ebi.ega.accession.policy.persistence.HistoricLogPolicyRepository;
import uk.ac.ebi.ega.accession.policy.persistence.HistoricPolicyEntity;
import uk.ac.ebi.ega.accession.policy.persistence.HistoricPolicyRepository;
import uk.ac.ebi.ega.accession.policy.persistence.PolicyEntity;
import uk.ac.ebi.ega.accession.policy.persistence.PolicyEntityRepository;

import java.util.Collection;

@Configuration
@EntityScan({"uk.ac.ebi.ega.accession.policy.persistence"})
@EnableJpaRepositories(basePackages = {"uk.ac.ebi.ega.accession.policy.persistence"})
public class PolicyServiceConfiguration {

    private final String PAD_FORMAT = "%011d";

    private final String PREFIX = "EGAP";

    private final String CATEGORY_ID = "policy";

    @Value("${accessioning.instanceId}")
    private String applicationInstanceId;

    @Autowired
    private ContiguousIdBlockService blockService;

    @Autowired
    private PolicyEntityRepository repository;

    @Autowired
    private HistoricPolicyRepository historicRepository;

    @Autowired
    private HistoricLogPolicyRepository historicLogRepository;

    @Bean
    public AccessioningService<Policy, String, String> prefixPolicyAccessionService() {
        return DecoratedAccessioningService.buildPrefixPaddedLongAccessionService(policyAccessionService(), PREFIX,
                PAD_FORMAT, Long::parseLong);
    }

    @Bean
    public AccessioningService<Policy, String, Long> policyAccessionService() {
        return new BasicAccessioningService<>(
                new MonotonicAccessionGenerator<>(CATEGORY_ID, applicationInstanceId, blockService,
                        monotonicDatabaseService()),
                policyAccessioningDatabaseService(),
                policy -> policy.getSubmissionAccount() + "_" + policy.getAlias(),
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
    public DatabaseService<Policy, String, Long> policyAccessioningDatabaseService() {
        return new BasicSpringDataRepositoryDatabaseService<>(repository,
                PolicyEntity::new,
                historicPolicyAccessionService());
    }

    @Bean
    public InactiveAccessionService<Policy, Long, PolicyEntity> historicPolicyAccessionService() {
        return new BasicJpaInactiveAccessionService<>(
                historicLogRepository,
                HistoricPolicyEntity::new,
                historicRepository,
                HistoricLogPolicyEntity::new);
    }

}
