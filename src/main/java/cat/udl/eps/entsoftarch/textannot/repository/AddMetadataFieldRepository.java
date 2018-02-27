package cat.udl.eps.entsoftarch.textannot.repository;


import cat.udl.eps.entsoftarch.textannot.domain.AddMetaDataField;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface AddMetadataFieldRepository extends PagingAndSortingRepository<AddMetaDataField, Integer> {
}
