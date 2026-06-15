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

import uk.mahfuj.argus.dto.ModelAliasRequest;
import uk.mahfuj.argus.dto.ModelAliasResponse;
import uk.mahfuj.argus.entity.ModelAliasEntity;
import uk.mahfuj.argus.mapper.CatalogMappers;
import uk.mahfuj.argus.service.CatalogAdminService;


/**
 * Admin CRUD for model aliases under {@code /api/admin/aliases}. No authentication
 * yet — protect before multi-user/prod exposure.
 */
@RestController
@RequestMapping("/api/admin/aliases")
public class ModelAliasAdminController {

    private final CatalogAdminService service;

    public ModelAliasAdminController(final CatalogAdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<ModelAliasResponse> list() {
        return service.listAliases().stream().map(CatalogMappers::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ModelAliasResponse get(@PathVariable final Long id) {
        return CatalogMappers.toResponse(service.getAlias(id));
    }

    @PostMapping
    public ResponseEntity<ModelAliasResponse> create(@RequestBody final ModelAliasRequest request) {
        final ModelAliasEntity saved = service.createAlias(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CatalogMappers.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ModelAliasResponse update(@PathVariable final Long id, @RequestBody final ModelAliasRequest request) {
        return CatalogMappers.toResponse(service.updateAlias(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        service.deleteAlias(id);
        return ResponseEntity.noContent().build();
    }
}
