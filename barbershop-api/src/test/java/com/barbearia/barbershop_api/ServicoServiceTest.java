package com.barbearia.barbershop_api;

import com.barbearia.barbershop_api.dto.DadosEntradaCadastroAgendamento;
import com.barbearia.barbershop_api.model.Agendamento;
import com.barbearia.barbershop_api.model.Cliente;
import com.barbearia.barbershop_api.model.Servico;
import com.barbearia.barbershop_api.repository.AgendamentoRepository;
import com.barbearia.barbershop_api.repository.ClienteRepository;
import com.barbearia.barbershop_api.repository.ServicoRepository;
import com.barbearia.barbershop_api.service.ServicoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServicoServiceTest {

    @InjectMocks
    private ServicoService service;

    @Mock
    private AgendamentoRepository agendamentoRepository;
    @Mock
    private ServicoRepository servicoRepository;
    @Mock
    private ClienteRepository clienteRepository;

    @Test
    void deveImpedirExlusaoServicoCasoHajaAgendamentoFuturo(){
        Integer id = 1;

        when(agendamentoRepository.existeAgendamentoFuturoParaOServico(id)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.excluirId(id));

        Mockito.verify(servicoRepository, never()).deleteById(any());

    }
    @Test
    void deveApagarCasoNaoHajaAgendamentos(){
        Integer id = 1;
        when(agendamentoRepository.existeAgendamentoFuturoParaOServico(id)).thenReturn(false);
        when(servicoRepository.existsById(id)).thenReturn(true);
        service.excluirId(id);
        verify(servicoRepository, times(1)).deleteById(id);

    }

}
