package uk.mahfuj.argus.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.mahfuj.argus.dto.ModelRequest;
import uk.mahfuj.argus.dto.ModelResponse;
import uk.mahfuj.argus.entity.ModelEntity;
import uk.mahfuj.argus.mapper.CatalogMappers;
import uk.mahfuj.argus.service.CatalogAdminService;


/**
 * Admin CRUD for models under {@code /api/admin/models}. No authentication yet —
 * protect before multi-user/prod exposure.
 */
@RestController
@RequestMapping("/api/admin/models")
public class ModelAdminController {

    private final CatalogAdminService service;

    public ModelAdminController(final CatalogAdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<ModelResponse> list() {
        return service.listModels().stream().map(CatalogMappers::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ModelResponse get(@PathVariable final Long id) {
        return CatalogMappers.toResponse(service.getModel(id));
    }

    @PostMapping
    public ResponseEntity<ModelResponse> create(@RequestBody final ModelRequest request) {
        final ModelEntity saved = service.createModel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CatalogMappers.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ModelResponse update(@PathVariable final Long id, @RequestBody final ModelRequest request) {
        return CatalogMappers.toResponse(service.updateModel(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        service.deleteModel(id);
        return ResponseEntity.noContent().build();
    }
}
