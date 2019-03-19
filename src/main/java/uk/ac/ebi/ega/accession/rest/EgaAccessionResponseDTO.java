package uk.ac.ebi.ega.accession.rest;

import java.util.function.Function;
import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionWrapper;
import uk.ac.ebi.ampt2d.commons.accession.rest.dto.AccessionResponseDTO;
import uk.ac.ebi.ega.accession.models.ExistingAccessionWrapper;

public class EgaAccessionResponseDTO<DTO, MODEL, HASH, ACCESSION> extends AccessionResponseDTO<DTO, MODEL, HASH, ACCESSION> {
  private boolean exists;

  public EgaAccessionResponseDTO(
      AccessionWrapper<MODEL, HASH, ACCESSION> accessionWrapper,
      Function<MODEL, DTO> modelToDto) {
    super(accessionWrapper, modelToDto);
    if (accessionWrapper instanceof ExistingAccessionWrapper) {
      this.exists = ((ExistingAccessionWrapper) accessionWrapper).getExists();
    }
  }

  public boolean getExists() {
    return this.exists;
  }
}
