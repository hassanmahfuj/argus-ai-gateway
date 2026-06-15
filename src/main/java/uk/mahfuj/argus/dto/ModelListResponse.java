package uk.mahfuj.argus.dto;

import java.util.List;


/**
 * OpenAI-shaped {@code GET /v1/models} response. Each entry's {@code id} is the
 * client-facing model string: {@code provider/model} for registered models, or the
 * bare alias name for virtual aliases.
 */
public record ModelListResponse(String object, List<Entry> data) {

    public record Entry(String id, String object, long created, String ownedBy) {}
}
