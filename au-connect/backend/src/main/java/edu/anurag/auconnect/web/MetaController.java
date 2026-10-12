package edu.anurag.auconnect.web;

import edu.anurag.auconnect.config.Catalog;
import edu.anurag.auconnect.dto.Dtos.MetaResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetaController {

    @GetMapping("/api/meta")
    public MetaResponse meta() {
        return new MetaResponse(Catalog.DEPARTMENTS, Catalog.COMPLAINT_CATEGORIES);
    }
}
