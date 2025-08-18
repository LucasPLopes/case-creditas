package br.com.creditas.service.simulacao.resource;

import br.com.creditas.service.simulacao.dto.SimulacaoResponse;
import br.com.creditas.service.simulacao.dto.SimulacaoSolicitacao;
import br.com.creditas.service.simulacao.dto.SimulacaoResultado;
import br.com.creditas.service.simulacao.service.SimulacaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/simulacoes")
public class SimulacaoController {
    private final SimulacaoService service;

    public SimulacaoController(SimulacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SimulacaoResponse> simular(@RequestBody
                                                     @Valid SimulacaoSolicitacao request) {
        return ResponseEntity.ok(service.simular(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimulacaoResponse> buscarSimulacao(@PathVariable String id) {
        SimulacaoResponse simulacao = service.buscarSimulacao(id);
        if(Objects.isNull(simulacao)) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(simulacao);
    }

    @PostMapping("/lista")
    public ResponseEntity<List<SimulacaoResultado>> simular(@RequestBody
                                                           @Valid
                                                           @NotNull
                                                           @NotEmpty(message = "Lista de simulações não pode ser vazia")
                                                           List<SimulacaoSolicitacao> request) {
        return ResponseEntity.ok(service.simular(request));
    }
}
