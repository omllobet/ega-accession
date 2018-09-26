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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class HashValidator implements ConstraintValidator<ValidHash, FileModel> {

    @Override
    public void initialize(ValidHash constraintAnnotation) {
    }

    @Override
    public boolean isValid(FileModel fileModel, ConstraintValidatorContext constraintValidatorContext) {

        constraintValidatorContext.disableDefaultConstraintViolation();

        if (fileModel.getFileMd5() == null || !isValidMd5(fileModel.getFileMd5())) {
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate("Please provide a valid lowercase hash for md5")
                    .addConstraintViolation();
            return false;
        }

        if (fileModel.getFileSha2() != null && !isValidSha2(fileModel.getFileSha2())) {
            constraintValidatorContext.
                    buildConstraintViolationWithTemplate("Please provide a valid lowercase hash for sha256")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean isValidMd5(String string) {
        return string.length() == 32 && isLowerCase(string);
    }

    private boolean isLowerCase(String stringToCheck) {
        return Objects.equals(stringToCheck, stringToCheck.toLowerCase());
    }

    private boolean isValidSha2(String string) {
        return string.length() == 64 && isLowerCase(string);
    }
    
}
