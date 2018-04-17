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
package uk.ac.ebi.ega.accession.file;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ampt2d.commons.accession.generators.ModelHashAccession;
import uk.ac.ebi.ega.accession.file.persistence.FileAccessioningDatabaseService;
import uk.ac.ebi.ega.accession.file.rest.FileDTO;
import uk.ac.ebi.ega.test.configuration.FileAccessioningServiceTestConfiguration;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(FileAccessioningServiceTestConfiguration.class)
public class FileAccessioningDatabaseServiceTest {

    private static final String CHECKSUM_A = "checksumAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String CHECKSUM_B = "checksumBBBBBBBBBBBBBBBBBBBBBBBB";

    @Autowired
    private FileAccessioningDatabaseService databaseService;

    @Test
    public void testFileAccessionsAreStoredInTheRepositoryAndRetrived() throws Exception {
        List<ModelHashAccession<FileModel, String, String>> fileAccessionsToStore = Arrays.asList(
                ModelHashAccession.of((FileModel) new FileDTO(HashType.MD5, CHECKSUM_A), CHECKSUM_A, CHECKSUM_A),
                ModelHashAccession.of((FileModel) new FileDTO(HashType.MD5, CHECKSUM_B), CHECKSUM_B, CHECKSUM_B)
        );

        databaseService.save(fileAccessionsToStore);

        Collection<String> messageHashes = Arrays.asList(CHECKSUM_A, CHECKSUM_B);
        Map<String, ? extends FileModel> accessionsFromRepository =
                databaseService.findAllAccessionsByHash(messageHashes);

        assertTrue(accessionsFromRepository.containsKey(CHECKSUM_A));
        assertTrue(accessionsFromRepository.containsKey(CHECKSUM_B));
    }

    @Test
    public void addingTheSameFilesWithSameAccessionsTwiceOverwritesObject() throws Exception {
        List<ModelHashAccession<FileModel, String, String>> fileAccessionsToStore = Arrays.asList(
                ModelHashAccession.of((FileModel) new FileDTO(HashType.MD5, CHECKSUM_A), CHECKSUM_A, CHECKSUM_A),
                ModelHashAccession.of((FileModel) new FileDTO(HashType.MD5, CHECKSUM_B), CHECKSUM_B, CHECKSUM_B)
        );

        databaseService.save(fileAccessionsToStore);
        databaseService.save(fileAccessionsToStore);

        Collection<String> messageHashes = Arrays.asList(CHECKSUM_A, CHECKSUM_B);
        Map<String, ? extends FileModel> accessionsFromRepository =
                databaseService.findAllAccessionsByHash(messageHashes);

        assertTrue(accessionsFromRepository.containsKey(CHECKSUM_A));
        assertTrue(accessionsFromRepository.containsKey(CHECKSUM_B));
    }

    //JpaSystemException is due to the id of entity being null
    @Test(expected = org.springframework.orm.jpa.JpaSystemException.class)
    public void cantStoreFileWithoutAccession() {
        List<ModelHashAccession<FileModel, String, String>> fileAccessionsToStore = Arrays.asList(
                ModelHashAccession.of((FileModel) new FileDTO(HashType.MD5, CHECKSUM_A), CHECKSUM_A, null),
                ModelHashAccession.of((FileModel) new FileDTO(HashType.MD5, CHECKSUM_B), CHECKSUM_B, CHECKSUM_B)
        );

        databaseService.save(fileAccessionsToStore);
    }
}