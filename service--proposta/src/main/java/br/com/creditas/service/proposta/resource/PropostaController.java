package br.com.creditas.service.proposta.resource;

import br.com.creditas.service.proposta.dto.PropostaConsultaResponse;
import br.com.creditas.service.proposta.dto.PropostaRequest;
import br.com.creditas.service.proposta.dto.PropostaResponse;
import br.com.creditas.service.proposta.service.PropostaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/proposta")
public class PropostaController {
    private final PropostaService service;

    public PropostaController(PropostaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PropostaResponse> incluirProposta(@Valid @RequestBody PropostaRequest request) {
        return ResponseEntity.status(200).body(service.incluirProposta(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropostaConsultaResponse> buscarProposta(@PathVariable String id) {
        return ResponseEntity.ok(service.buscarProposta(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> incluirProposta(@PathVariable String id) {
        service.deletarProposta(id);
        return ResponseEntity.ok().body(null);
    }


}
