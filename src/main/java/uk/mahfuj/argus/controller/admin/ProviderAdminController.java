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

import uk.mahfuj.argus.dto.ProviderRequest;
import uk.mahfuj.argus.dto.ProviderResponse;
import uk.mahfuj.argus.entity.ProviderEntity;
import uk.mahfuj.argus.mapper.CatalogMappers;
import uk.mahfuj.argus.service.CatalogAdminService;


/**
 * Admin CRUD for providers under {@code /api/admin/providers}. No authentication
 * yet — matches the current {@code /api} posture; protect before multi-user/prod
 * exposure. Provider API keys are accepted (write-only) and never returned.
 */
@RestController
@RequestMapping("/api/admin/providers")
public class ProviderAdminController {

    private final CatalogAdminService service;

    public ProviderAdminController(final CatalogAdminService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProviderResponse> list() {
        return service.listProviders().stream().map(CatalogMappers::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ProviderResponse get(@PathVariable final Long id) {
        return CatalogMappers.toResponse(service.getProvider(id));
    }

    @PostMapping
    public ResponseEntity<ProviderResponse> create(@RequestBody final ProviderRequest request) {
        final ProviderEntity saved = service.createProvider(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CatalogMappers.toResponse(saved));
    }

    @PutMapping("/{id}")
    public ProviderResponse update(@PathVariable final Long id, @RequestBody final ProviderRequest request) {
        return CatalogMappers.toResponse(service.updateProvider(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        service.deleteProvider(id);
        return ResponseEntity.noContent().build();
    }
}
