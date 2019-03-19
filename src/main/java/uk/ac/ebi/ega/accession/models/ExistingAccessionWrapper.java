package uk.ac.ebi.ega.accession.models;

import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionWrapper;

public class ExistingAccessionWrapper<MODEL, HASH, ACCESSION> extends
    AccessionWrapper<MODEL, HASH, ACCESSION> {

  private boolean exists;

  public ExistingAccessionWrapper(ACCESSION accession, HASH hash, MODEL data) {
    super(accession, hash, data);
    this.exists = false;
  }

  public ExistingAccessionWrapper(ACCESSION accession, HASH hash, MODEL data, boolean exists) {
    super(accession, hash, data);
    this.exists = exists;
  }

  public ExistingAccessionWrapper(ACCESSION accession, HASH hash, MODEL data, int version,
      boolean exists) {
    super(accession, hash, data, version);
    this.exists = exists;
  }

  public boolean getExists() {
    return this.exists;
  }
}
