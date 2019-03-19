package uk.ac.ebi.ega.accession.services;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import uk.ac.ebi.ampt2d.commons.accession.core.AccessioningService;
import uk.ac.ebi.ampt2d.commons.accession.core.DecoratedAccessioningService;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionCouldNotBeGeneratedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDeprecatedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDoesNotExistException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionMergedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.HashAlreadyExistsException;
import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionVersionsWrapper;
import uk.ac.ebi.ampt2d.commons.accession.core.models.AccessionWrapper;
import uk.ac.ebi.ega.accession.models.ExistingAccessionWrapper;

public class EgaDecoratedAccessioningService<MODEL, HASH, DB_ACCESSION, ACCESSION> extends DecoratedAccessioningService<MODEL, HASH, DB_ACCESSION, ACCESSION> {

  private final AccessioningService<MODEL, HASH, DB_ACCESSION> service;
  private final Function<DB_ACCESSION, ACCESSION> decoratingFunction;
  private final Function<ACCESSION, DB_ACCESSION> parsingFunction;

  public EgaDecoratedAccessioningService(
        AccessioningService<MODEL, HASH, DB_ACCESSION> service,
        Function<DB_ACCESSION, ACCESSION> decoratingFunction,
        Function<ACCESSION, DB_ACCESSION> parsingFunction) {
      super(service, decoratingFunction, parsingFunction);
      this.service = service;
      this.decoratingFunction = decoratingFunction;
      this.parsingFunction = parsingFunction;
  }

  @Override
  public List<AccessionWrapper<MODEL, HASH, ACCESSION>> getOrCreate(List<? extends MODEL> messages)
        throws AccessionCouldNotBeGeneratedException {
      List<AccessionWrapper<MODEL, HASH, DB_ACCESSION>>  result = service.getOrCreate(messages);
      return decorate(result);
  }

  private List<AccessionWrapper<MODEL, HASH, ACCESSION>> decorate(
        List<AccessionWrapper<MODEL, HASH, DB_ACCESSION>> accessionWrappers) {
      return accessionWrappers.stream().map(this::decorate).collect(Collectors.toList());
  }

  private AccessionWrapper<MODEL, HASH, ACCESSION> decorate(AccessionWrapper<MODEL, HASH, DB_ACCESSION> wrapper) {
    if (wrapper instanceof ExistingAccessionWrapper) {
      return new ExistingAccessionWrapper<>(decoratingFunction.apply(wrapper.getAccession()),
          wrapper.getHash(),
          wrapper.getData(), wrapper.getVersion(),
          ((ExistingAccessionWrapper)wrapper).getExists());
    } else {
      return new AccessionWrapper<>(decoratingFunction.apply(wrapper.getAccession()),
          wrapper.getHash(), wrapper.getData(), wrapper.getVersion());
    }
  }

  @Override
  public List<AccessionWrapper<MODEL, HASH, ACCESSION>> get(List<? extends MODEL> accessionedObjects) {
    return decorate(service.get(accessionedObjects));
  }

  @Override
  public AccessionWrapper<MODEL, HASH, ACCESSION> getByAccession(ACCESSION accession)
      throws AccessionDoesNotExistException, AccessionMergedException, AccessionDeprecatedException {
    return decorate(service.getByAccession(parse(accession)));
  }

  private List<DB_ACCESSION> parse(List<ACCESSION> accessions) {
    return accessions.stream().map(parsingFunction).filter(Objects::nonNull).collect(Collectors.toList());
  }

  @Override
  public AccessionWrapper<MODEL, HASH, ACCESSION> getByAccessionAndVersion(ACCESSION accession, int version)
      throws AccessionDoesNotExistException, AccessionMergedException, AccessionDeprecatedException {
    return decorate(service.getByAccessionAndVersion(parse(accession), version));
  }

  private DB_ACCESSION parse(ACCESSION accession) throws AccessionDoesNotExistException {
    DB_ACCESSION dbAccession = parsingFunction.apply(accession);
    if (dbAccession == null) {
      throw new AccessionDoesNotExistException(accession);
    }
    return dbAccession;

  }

  @Override
  public AccessionVersionsWrapper<MODEL, HASH, ACCESSION> update(ACCESSION accession, int version, MODEL message)
      throws AccessionDoesNotExistException, HashAlreadyExistsException, AccessionDeprecatedException,
      AccessionMergedException {
    return decorate(service.update(parse(accession), version, message));
  }

  private AccessionVersionsWrapper<MODEL, HASH, ACCESSION> decorate(
      AccessionVersionsWrapper<MODEL, HASH, DB_ACCESSION> accessionVersions) {
    return new AccessionVersionsWrapper<>(decorate(accessionVersions.getModelWrappers()));
  }

  @Override
  public AccessionVersionsWrapper<MODEL, HASH, ACCESSION> patch(ACCESSION accession, MODEL message)
      throws AccessionDoesNotExistException, HashAlreadyExistsException, AccessionDeprecatedException,
      AccessionMergedException {
    return decorate(service.patch(parse(accession), message));
  }

  @Override
  public void deprecate(ACCESSION accession, String reason) throws AccessionMergedException,
      AccessionDoesNotExistException, AccessionDeprecatedException {
    service.deprecate(parse(accession), reason);
  }

  @Override
  public void merge(ACCESSION accessionOrigin, ACCESSION mergeInto, String reason) throws AccessionMergedException,
      AccessionDoesNotExistException, AccessionDeprecatedException {
    service.merge(parse(accessionOrigin), parse(mergeInto), reason);
  }

  public static <MODEL, HASH, DB_ACCESSION> DecoratedAccessioningService<MODEL, HASH, DB_ACCESSION, String>
  buildPrefixAccessionService(AccessioningService<MODEL, HASH, DB_ACCESSION> service, String prefix,
      Function<String, DB_ACCESSION> parseFunction) {
    return new EgaDecoratedAccessioningService<>(service, accession -> prefix + accession,
        s -> {
          if (s.length() <= prefix.length() || !Objects.equals(s.substring(0, prefix.length()), prefix)) {
            return null;
          }
          return parseFunction.apply(s.substring(prefix.length()));
        });
  }

  public static <MODEL, HASH> DecoratedAccessioningService<MODEL, HASH, Long, String>
  buildPrefixPaddedLongAccessionService(AccessioningService<MODEL, HASH, Long> service, String prefix,
      String padFormat, Function<String, Long> parseFunction) {
    return new EgaDecoratedAccessioningService<>(service, accession -> prefix + String.format(padFormat, accession),
        s -> {
          if (s.length() <= prefix.length() || !Objects.equals(s.substring(0, prefix.length()), prefix)) {
            return null;
          }
          return parseFunction.apply(s.substring(prefix.length()));
        });
  }

}
