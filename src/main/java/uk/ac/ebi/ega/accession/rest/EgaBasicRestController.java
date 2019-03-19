package uk.ac.ebi.ega.accession.rest;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.ac.ebi.ampt2d.commons.accession.core.AccessioningService;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionCouldNotBeGeneratedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDeprecatedException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionDoesNotExistException;
import uk.ac.ebi.ampt2d.commons.accession.core.exceptions.AccessionMergedException;
import uk.ac.ebi.ampt2d.commons.accession.rest.controllers.BasicRestController;
import uk.ac.ebi.ampt2d.commons.accession.rest.dto.AccessionResponseDTO;


public class EgaBasicRestController<DTO extends MODEL, MODEL, HASH, ACCESSION extends Serializable> extends
    BasicRestController<DTO, MODEL, HASH, ACCESSION> {

  public EgaBasicRestController(
      AccessioningService<MODEL, HASH, ACCESSION> service,
      Function<MODEL, DTO> modelToDTO) {
    super(service, modelToDTO);
  }

  @RequestMapping(
      value = {"/{accession}"},
      method = {RequestMethod.GET},
      produces = {"application/json"}
  )
  public AccessionResponseDTO<DTO, MODEL, HASH, ACCESSION> get(@PathVariable ACCESSION accession) throws AccessionDoesNotExistException, AccessionMergedException, AccessionDeprecatedException {
    return new EgaAccessionResponseDTO(super.getService().getByAccession(accession), super.getModelToDTO());
  }

  @RequestMapping(
      method = {RequestMethod.POST},
      produces = {"application/json"},
      consumes = {"application/json"}
  )
  public List<AccessionResponseDTO<DTO, MODEL, HASH, ACCESSION>> generateAccessions(@RequestBody @Valid List<DTO> dtos) throws AccessionCouldNotBeGeneratedException {
    return (List)super.getService().getOrCreate(dtos).stream().map((accessionModel) -> {
      return new EgaAccessionResponseDTO(accessionModel, super.getModelToDTO());
    }).collect(Collectors.toList());
  }

}
