package cat.udl.eps.entsoftarch.textannot.domain;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class MetadataField extends UriEntity<Integer> {

    @NotBlank
    String name, type;

    String category;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "values")
    @JsonIdentityReference(alwaysAsId = true)
    private List<MetadataValue> valued = new ArrayList<>();

    @ManyToOne
    private MetadataTemplate definedAt;

    @Override
    public Integer getId() {
        return id;
    }

    public MetadataField(){
        this.name = "";
    }

    public MetadataField(String name, String type){
        this.name = name;
        this.type = type;
    }
}
