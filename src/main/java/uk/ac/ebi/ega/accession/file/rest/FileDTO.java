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
package uk.ac.ebi.ega.accession.file.rest;

import uk.ac.ebi.ega.accession.file.model.FileModel;

@ValidHash
public class FileDTO implements FileModel {

    private String fileMd5;

    private String fileSha2;

    FileDTO() {
    }

    public FileDTO(String fileMd5, String fileSha2) {
        this.fileMd5 = fileMd5;
        this.fileSha2 = fileSha2;
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
