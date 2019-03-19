package uk.ac.ebi.ega.accession.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ampt2d.commons.accession.core.BasicAccessioningService;
import uk.ac.ebi.ampt2d.commons.accession.core.DatabaseService;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionCouldNotBeGeneratedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.MissingUnsavedAccessionsException;
import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.models.SaveResponse;
import uk.ac.ebi.ampt2d.commons.accession.generators.AccessionGenerator;
import uk.ac.ebi.ega.accession.models.ExistingAccessionWrapper;


public class EgaAccessioningService<MODEL, HASH, ACCESSION extends Serializable> extends
    BasicAccessioningService<MODEL, HASH, ACCESSION> {
  private static final Logger logger = LoggerFactory.getLogger(BasicAccessioningService.class);
  private final Function<MODEL, HASH> hashingFunction;

  public EgaAccessioningService(
      AccessionGenerator<MODEL, ACCESSION> accessionGenerator,
      DatabaseService<MODEL, HASH, ACCESSION> dbService,
      Function<MODEL, String> summaryFunction, Function<String, HASH> hashingFunction) {
    super(accessionGenerator, dbService, summaryFunction, hashingFunction);
    this.hashingFunction = summaryFunction.andThen(hashingFunction);
  }

  public List<AccessionWrapper<MODEL, HASH, ACCESSION>> getOrCreate(List<? extends MODEL> messages) throws AccessionCouldNotBeGeneratedException {
    return this.saveAccessions(super.getAccessionGenerator().generateAccessions(this.mapHashOfMessages(messages)));
  }

  private Map<HASH, MODEL> mapHashOfMessages(List<? extends MODEL> messages) {
    return (Map)messages.stream().collect(Collectors.toMap(this.hashingFunction, (e) -> {
      return e;
    }, (r, o) -> {
      return r;
    }));
  }

  private List<AccessionWrapper<MODEL, HASH, ACCESSION>> saveAccessions(List<AccessionWrapper<MODEL, HASH, ACCESSION>> accessions) {
    SaveResponse<ACCESSION> response = super.getDbService().save(accessions);
    super.getAccessionGenerator().postSave(response);
    List<AccessionWrapper<MODEL, HASH, ACCESSION>> savedAccessions = new ArrayList();
    List<AccessionWrapper<MODEL, HASH, ACCESSION>> unsavedAccessions = new ArrayList();
    accessions.stream().forEach((accessionModel) -> {
      if (response.isSavedAccession(accessionModel.getAccession())) {
        savedAccessions.add(accessionModel);
      } else {
        unsavedAccessions.add(accessionModel);
      }

    });
    if (!unsavedAccessions.isEmpty()) {
      savedAccessions.addAll(this.getPreexistingAccessions(unsavedAccessions));

    }

    return savedAccessions;
  }

  private List<ExistingAccessionWrapper<MODEL, HASH, ACCESSION>> getPreexistingAccessions(List<AccessionWrapper<MODEL, HASH, ACCESSION>> saveFailedAccessions) {
    Set<HASH> unsavedHashes = (Set)saveFailedAccessions.stream().map(AccessionWrapper::getHash).collect(Collectors.toSet());
    List<AccessionWrapper<MODEL, HASH, ACCESSION>> dbAccessions = super.getDbService().findAllByHash(unsavedHashes);
    if (dbAccessions.size() != unsavedHashes.size()) {
      logger.error("Lists of unsaved hashes and pre-existing accessions differ in size");
      logger.error("Failed hashes: '" + unsavedHashes.toString() + "'");
      logger.error("Accessions retrieved from database: '" + dbAccessions + "'");
      throw new MissingUnsavedAccessionsException(saveFailedAccessions, dbAccessions);
    } else {
      List<ExistingAccessionWrapper<MODEL, HASH, ACCESSION>> existingAccessions = new ArrayList<>();
      for (AccessionWrapper<MODEL, HASH, ACCESSION> dbAccession: dbAccessions ) {
        existingAccessions.add(new ExistingAccessionWrapper<>(dbAccession.getAccession(),dbAccession.getHash(),dbAccession.getData(),true));
      }
      return existingAccessions;
    }
  }
}
