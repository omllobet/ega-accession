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

import uk.ac.ebi.ampt2d.commons.accession.persistence.jpa.entities.InactiveAccessionEntity;
import uk.ac.ebi.ega.accession.file.model.FileModel;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class HistoricFileEntity extends InactiveAccessionEntity<FileModel, Long> implements FileModel {

    @Column(nullable = false, unique = true)
    private String fileMd5;

    @Column()
    private String fileSha2;

    HistoricFileEntity() {
        super();
    }

    public HistoricFileEntity(FileEntity entity) {
        super(entity);
        this.fileMd5 = entity.getFileMd5();
        this.fileSha2 = entity.getFileSha2();
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
