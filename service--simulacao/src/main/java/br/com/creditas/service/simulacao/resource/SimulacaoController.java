package br.com.creditas.service.simulacao.resource;

import br.com.creditas.service.simulacao.dto.SimulacaoRequest;
import br.com.creditas.service.simulacao.dto.SimulacaoResponse;
import br.com.creditas.service.simulacao.service.SimulacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/simulacoes")
public class SimulacaoController {
    private final SimulacaoService service;

    public SimulacaoController(SimulacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SimulacaoResponse> simular(@RequestBody @Valid SimulacaoRequest request) {
        return ResponseEntity.ok(service.simular(request));
    }

    @PostMapping("/lista")
    public ResponseEntity<List<SimulacaoResponse>> simular(@RequestBody @Valid List<SimulacaoRequest> request) {
        return ResponseEntity.ok(service.simular(request));
    }
}
