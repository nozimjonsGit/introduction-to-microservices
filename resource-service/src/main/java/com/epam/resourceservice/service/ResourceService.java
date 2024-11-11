package com.epam.resourceservice.service;

import com.epam.resourceservice.entity.Resource;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

public interface ResourceService {
    Resource createAndProcessResource(byte[] audio) throws TikaException, SAXException, IOException;
    Resource getResourceById(Long id);
    List<Long> deleteResources(List<Long> ids);
}
