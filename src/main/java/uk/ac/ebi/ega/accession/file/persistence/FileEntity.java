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

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class FileEntity extends AccessionedEntity<FileModel, Long> implements FileModel {

    @Column(nullable = false, unique = true)
    private String fileMd5;

    @Column()
    private String fileSha2;

    FileEntity() {
        super(null, null, -1);
    }

    public FileEntity(String fileMd5, Long accession, String hashedMessage) {
        super(hashedMessage, accession);
        this.fileMd5 = fileMd5;
    }

    public FileEntity(String fileMd5, String fileSha2, Long accession, String hashedMessage) {
        super(hashedMessage, accession);
        this.fileMd5 = fileMd5;
        this.fileSha2 = fileSha2;
    }

    public FileEntity(String fileMd5, String fileSha2, Long accession, String hashedMessage, int version) {
        super(hashedMessage, accession, version);
        this.fileMd5 = fileMd5;
        this.fileSha2 = fileSha2;
    }

    public FileEntity(FileModel model, Long accession, String hashedMessage, int version) {
        this(model.getFileMd5(), model.getFileSha2(), accession, hashedMessage, version);
    }

    public FileEntity(AccessionWrapper<FileModel, String, Long> wrapper) {
        this(wrapper.getData(), wrapper.getAccession(), wrapper.getHash(), wrapper.getVersion());
    }

    @Override
    public FileModel getModel() {
        return this;
    }

    @Override
    public String getFileMd5() {
        return fileMd5;
    }

    @Override
    public String getFileSha2() {
        return fileSha2;
    }
}
