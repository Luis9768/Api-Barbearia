package com.barbearia.barbershop_api;

import com.barbearia.barbershop_api.dto.DadosEntradaCadastroAgendamento;
import com.barbearia.barbershop_api.model.Agendamento;
import com.barbearia.barbershop_api.model.Cliente;
import com.barbearia.barbershop_api.model.Servico;
import com.barbearia.barbershop_api.repository.AgendamentoRepository;
import com.barbearia.barbershop_api.repository.ClienteRepository;
import com.barbearia.barbershop_api.repository.ServicoRepository;
import com.barbearia.barbershop_api.service.AgendamentoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita o Mockito no JUnit 5
class AgendamentoServiceTest {

    @InjectMocks // Diz: "Injete os mocks aqui dentro"
    private AgendamentoService service;

    @Mock // Diz: "Isso aqui é mentirinha"
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Test
    void deveCadastrarAgendamentoComSucesso() {
        // 1. ARRANGE (Preparar)
        // Criar Cliente Fake
        Cliente cliente = new Cliente();
        cliente.setId(1);

        // Criar Serviço Fake
        Servico servico = new Servico();
        servico.setId(1);
        servico.setDuracaoMinutos(30);
        servico.setPreco(BigDecimal.TEN); // Importe java.math.BigDecimal

        // Criar o DTO (Aqui está a correção da data)
        LocalDateTime data = LocalDateTime.of(2026, 1, 20, 10, 0);
        DadosEntradaCadastroAgendamento dados = new DadosEntradaCadastroAgendamento(data, null, 1, 1);
        // Ensinar os Mocks
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));
        // Importe o any(): import static org.mockito.ArgumentMatchers.any;
        when(agendamentoRepository.contarConflitos(any(), any())).thenReturn(0);

        // 2. ACT (Executar)
        service.cadastrar(dados);

        // 3. ASSERT (Conferir)
        verify(agendamentoRepository).save(any());
    }
    @Test
    void deveLancarErroQuandoHorarioEstiverOcupado() {
        // 1. ARRANGE
        LocalDateTime data = LocalDateTime.of(2026, 1, 20, 10, 0);
        DadosEntradaCadastroAgendamento dados = new DadosEntradaCadastroAgendamento(data, null, 1, 1);

        // Mocks de Suporte
        Servico servicoFake = new Servico();
        servicoFake.setId(1);
        servicoFake.setDuracaoMinutos(60); // 1 hora de duração
        servicoFake.setPreco(BigDecimal.TEN);

        lenient().when(clienteRepository.findById(1)).thenReturn(Optional.of(new Cliente()));
        lenient().when(servicoRepository.findById(1)).thenReturn(Optional.of(servicoFake));


        when(agendamentoRepository.contarConflitos(any(), any())).thenReturn(1);
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(dados));    }
    @Test
    void deveReagendarComSucesso() {
        // 1. ARRANGE
        // Definindo tudo como Integer (sem o 'L')
        Integer idAgendamento = 1;
        Integer idCliente = 1;
        Integer idServico = 1;

        LocalDateTime dataOriginal = LocalDateTime.of(2026, 1, 20, 10, 0);
        LocalDateTime novaData = LocalDateTime.of(2026, 1, 20, 15, 0);

        DadosEntradaCadastroAgendamento dadosNovos = new DadosEntradaCadastroAgendamento(
                novaData,
                null,
                idCliente,
                idServico
        );

        Cliente clienteFake = new Cliente();
        clienteFake.setId(idCliente); // Passando Integer

        Servico servicoFake = new Servico();
        servicoFake.setId(idServico); // Passando Integer
        servicoFake.setDuracaoMinutos(60);

        Agendamento agendamentoAntigo = new Agendamento();
        agendamentoAntigo.setId(idAgendamento);
        agendamentoAntigo.setCliente(clienteFake);
        agendamentoAntigo.setServico(servicoFake);
        agendamentoAntigo.setDataHoraInicio(dataOriginal);
        agendamentoAntigo.setDataHoraFim(dataOriginal.plusMinutes(60));

        when(agendamentoRepository.findById(idAgendamento)).thenReturn(Optional.of(agendamentoAntigo));

        when(servicoRepository.findById(idServico)).thenReturn(Optional.of(servicoFake));

        when(clienteRepository.findById(idCliente)).thenReturn(Optional.of(clienteFake));

        when(agendamentoRepository.contarConflitosParaReagendamento(any(), any(), eq(idAgendamento)))
                .thenReturn(0);

        service.reagendar(idAgendamento, dadosNovos);

        verify(agendamentoRepository).save(argThat(agendamentoSalvo ->
                agendamentoSalvo.getDataHoraInicio().equals(novaData) &&
                        agendamentoSalvo.getId().equals(idAgendamento)
        ));
    }
}

