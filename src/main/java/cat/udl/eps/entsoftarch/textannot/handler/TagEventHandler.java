package cat.udl.eps.entsoftarch.textannot.handler;

import cat.udl.eps.entsoftarch.textannot.domain.Tag;
import cat.udl.eps.entsoftarch.textannot.exception.TagDeleteException;
import cat.udl.eps.entsoftarch.textannot.repository.TagRepository;
import cat.udl.eps.entsoftarch.textannot.service.TagHierarchyPrecalcService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
@RepositoryEventHandler
@Component
public class TagEventHandler {
    final Logger logger = LoggerFactory.getLogger(Tag.class);

    @Autowired
    TagRepository tagRepository;

    @Autowired
    TagHierarchyPrecalcService tagHierarchyPrecalcService;

    @HandleAfterCreate
    @HandleAfterSave
    @HandleAfterDelete
    @Transactional
    public void handleTagPostCreateAndSave(Tag tag) throws JsonProcessingException {
        if (tag.getProject() != null)
            tagHierarchyPrecalcService.recalculateTagHierarchyTree(tag.getProject());
    }

    @HandleAfterLinkSave
    @HandleAfterLinkDelete
    public void handleTagPostLinkSave(Tag tag, Object o) throws JsonProcessingException {
        if (tag.getProject() != null)
            tagHierarchyPrecalcService.recalculateTagHierarchyTree(tag.getProject());
    }

    @HandleBeforeDelete
    @Transactional
    public void handletagPreDelete(Tag tag) throws TagDeleteException{

       List<Tag> sibilingsTags =  tagRepository.findByParent(tag);

       if (!sibilingsTags.isEmpty()){

           throw new TagDeleteException();

       }

    }




}
