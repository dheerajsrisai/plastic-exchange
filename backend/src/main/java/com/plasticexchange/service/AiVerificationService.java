package com.plasticexchange.service;

import com.plasticexchange.dto.AiResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

/** Calls the Python ML microservice. If it is down, the pickup goes to manual review. */
@Service
public class AiVerificationService {
    @Value("${ml.service.url}") private String mlUrl;
    private final RestTemplate rest;

    public AiVerificationService() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(5000);
        f.setReadTimeout(30000);
        this.rest = new RestTemplate(f);
    }

    @SuppressWarnings("unchecked")
    public AiResult verify(byte[] image, String filename) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(image) {
                @Override public String getFilename() { return filename; }
            });
            ResponseEntity<Map> res = rest.postForEntity(mlUrl + "/predict", new HttpEntity<>(body, headers), Map.class);
            Map<String, Object> m = res.getBody();
            if (m == null) return new AiResult("UNKNOWN", 0);
            return new AiResult(String.valueOf(m.get("type")), ((Number) m.get("confidence")).doubleValue());
        } catch (Exception e) {
            System.err.println("ML service unavailable: " + e.getMessage());
            return new AiResult("UNKNOWN", 0);
        }
    }
}
