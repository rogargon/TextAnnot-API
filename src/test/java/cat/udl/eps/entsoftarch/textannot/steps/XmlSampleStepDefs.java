package cat.udl.eps.entsoftarch.textannot.steps;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cat.udl.eps.entsoftarch.textannot.domain.MetadataField;
import cat.udl.eps.entsoftarch.textannot.domain.MetadataTemplate;
import cat.udl.eps.entsoftarch.textannot.domain.MetadataValue;
import cat.udl.eps.entsoftarch.textannot.domain.XmlSample;
import cat.udl.eps.entsoftarch.textannot.repository.MetadataFieldRepository;
import cat.udl.eps.entsoftarch.textannot.repository.MetadataTemplateRepository;
import cat.udl.eps.entsoftarch.textannot.repository.MetadataValueRepository;
import cat.udl.eps.entsoftarch.textannot.repository.XmlSampleRepository;
import cucumber.api.java.en.And;
import cucumber.api.java.en.When;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public class XmlSampleStepDefs {

    private String newResourceUri;

    @Autowired private StepDefs stepDefs;
    private MetadataValue metaValue;
    private MetadataField metaField;

    @Autowired private XmlSampleRepository xmlSampleRepository;
    @Autowired private MetadataTemplateRepository metadataTemplateRepository;
    @Autowired private MetadataFieldRepository metadataFieldRepository;
    @Autowired private MetadataValueRepository metadataValueRepository;


    @When("^I upload a XmlSample with text \"([^\"]*)\" and content$")
    public void iUploadAXmlSampleWithTextAndContent(String text, String content) throws Throwable {
        org.json.JSONObject xmlSamples = new org.json.JSONObject();
        xmlSamples.put("text", text);
        xmlSamples.put("content", content);
        stepDefs.result = stepDefs.mockMvc.perform(
                post("/xmlSamples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(xmlSamples.toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .with(AuthenticationStepDefs.authenticate()))
                .andDo(print());
        newResourceUri = stepDefs.result.andReturn().getResponse().getHeader("Location");
    }


    @And("^It has been created a XmlSample with text \"([^\"]*)\" and content$")
    public void itHasBeenCreatedAXmlSampleWithTextAndContent(String text, String content) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get(newResourceUri)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(AuthenticationStepDefs.authenticate()))
                .andDo(print())
                .andExpect(jsonPath("$.text", is(text)))
                .andExpect(jsonPath("$.content", is(content)));
    }

    @And("^It has been created a new metadatafield with name \"([^\"]*)\" and type \"([^\"]*)\" and Id (\\d+)$")
    public void itHasBeenCreatedANewMetadatafieldWithNameAndTypeAndId(String name, String type, int id) throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
                get("/metadataFields/{id}", id)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(AuthenticationStepDefs.authenticate()))
                .andDo(print())
                .andExpect(jsonPath("$.name", is(name)))
                .andExpect(jsonPath("$.type", is(type)))
                .andExpect(jsonPath("$.id", is(id)));
    }

    @And("^It has been created a new metadataValue with value \"([^\"]*)\" for metadataField with name \"([^\"]*)\"$")
    public void itHasBeenCreatedANewMetadatavauleWithValueForMetadatafieldWithName(String value, String name) throws Throwable {
        metaValue=metadataValueRepository.findByValue(value);
        stepDefs.result = stepDefs.mockMvc.perform(
                get("/metadataValues/{id}", metaValue.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .with(AuthenticationStepDefs.authenticate()))
                .andDo(print())
                .andExpect(jsonPath("$.value", is(value)));

        stepDefs.mockMvc.perform(
                get("/metadataValues/"+metaValue.getId()+"/valued")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(AuthenticationStepDefs.authenticate()))
                .andDo(print())
                .andExpect(jsonPath("$.name", is(name)))
                .andExpect(status().is(200));
    }

    @When("^I upload the XML file with filename \"([^\"]*)\" described by \"([^\"]*)\"$")
    public void iUploadTheXMLFileWithFilename(String filename, String template) throws Throwable {
        XmlSample sample = new XmlSample();
        Resource resource = stepDefs.wac.getResource("classpath:"+filename);
        byte[] content = Files.readAllBytes(resource.getFile().toPath());
        sample.setContent(new String(content, StandardCharsets.UTF_8));
        sample.setDescribedBy(metadataTemplateRepository.findByName(template));

        String message = stepDefs.mapper.writeValueAsString(sample);

        stepDefs.result = stepDefs.mockMvc.perform(
            post("/xmlSamples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(message)
                .accept(MediaType.APPLICATION_JSON)
                .with(AuthenticationStepDefs.authenticate()))
            .andDo(print());
        newResourceUri = stepDefs.result.andReturn().getResponse().getHeader("Location");
    }

    @And("^It has been created a XmlSample with the following (\\d+) values")
    public void itHasBeenCreatedAXmlSampleWith(int count, Map<String, String> expectedFieldValues)
        throws Throwable {
        stepDefs.result = stepDefs.mockMvc.perform(
            get(newResourceUri+"/has")
                .accept(MediaType.APPLICATION_JSON)
                .with(AuthenticationStepDefs.authenticate()))
            .andDo(print())
            .andExpect(status().is(200))
            .andExpect(jsonPath("$._embedded.metadataValues", hasSize(count)));

        expectedFieldValues.forEach((k,v) -> {
            try {
                stepDefs.result.andExpect(jsonPath("$._embedded.metadataValues[?(@.fieldName=='"+k+"')].value",
                    hasItem(v)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @And("^The metadata template \"([^\"]*)\" has fields$")
    public void theMetadataTemplateHasFields(String templateName, List<List<String>> fields) throws Throwable {
        MetadataTemplate template = metadataTemplateRepository.findByName(templateName);
        fields.forEach(fieldNameType -> {
            MetadataField field = new MetadataField();
            field.setName(fieldNameType.get(0));
            field.setType(fieldNameType.get(1));
            field.setDefinedIn(template);
            metadataFieldRepository.save(field);
        });
    }

    @And("^It has been created a XmlSample containing the text \"([^\"]*)\"$")
    public void itHasBeenCreatedASample(String text) throws Throwable {
        stepDefs.result.andExpect(jsonPath("$.text", Matchers.containsString(text)));
    }
}