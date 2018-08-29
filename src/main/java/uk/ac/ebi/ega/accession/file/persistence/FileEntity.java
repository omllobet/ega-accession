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
package uk.ac.ebi.ega.accession.file.persistence;

import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionWrapper;
import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.entities.AccessionedEntity;
import uk.ac.ebi.ega.accession.file.model.FileModel;
import uk.ac.ebi.ega.accession.file.model.HashType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
public class FileEntity extends AccessionedEntity<FileModel, Long> implements FileModel {

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private HashType hashType;

    @Column(nullable = false, unique = true)
    private String hash;

    FileEntity() {
        super(null, null, -1);
    }

    public FileEntity(HashType hashType, String hash, Long accession, String hashedMessage) {
        super(hashedMessage, accession);
        this.hashType = hashType;
        this.hash = hash;
    }

    public FileEntity(HashType hashType, String hash, Long accession, String hashedMessage, int version) {
        super(hashedMessage, accession, version);
        this.hashType = hashType;
        this.hash = hash;
    }

    public FileEntity(FileModel model, Long accession, String hashedMessage, int version) {
        this(model.getHashType(), model.getHash(), accession, hashedMessage, version);
    }

    public FileEntity(AccessionWrapper<FileModel, String, Long> wrapper) {
        this(wrapper.getData(), wrapper.getAccession(), wrapper.getHash(), wrapper.getVersion());
    }

    @Override
    public FileModel getModel() {
        return this;
    }

    @Override
    public String getHash() {
        return hash;
    }

    @Override
    public HashType getHashType() {
        return hashType;
    }
}
