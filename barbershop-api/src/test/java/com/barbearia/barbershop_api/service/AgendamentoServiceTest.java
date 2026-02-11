package com.barbearia.barbershop_api.service;

import com.barbearia.barbershop_api.dto.DadosEntradaCadastroAgendamento;
import com.barbearia.barbershop_api.model.*;
import com.barbearia.barbershop_api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AgendamentoService - realizarAgendamento com validação")
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ServicoRepository servicoRepository;
    @Mock
    private DiaEspecialRepository diaEspecialRepository;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private Usuario usuarioAdmin;
    private Usuario usuarioCliente;
    private Cliente cliente;
    private Servico servico;
    private DadosEntradaCadastroAgendamento dto;

    @BeforeEach
    void setUp() {
        // Setup usuário ADMIN
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1);
        usuarioAdmin.setPerfil(Perfil.ADMIN);

        // Setup usuário CLIENTE
        usuarioCliente = new Usuario();
        usuarioCliente.setId(2);
        usuarioCliente.setPerfil(Perfil.CLIENTE);

        // Setup cliente
        cliente = new Cliente();
        cliente.setId(1);
        cliente.setUsuario(usuarioCliente);

        // Setup serviço
        servico = new Servico();
        servico.setId(1);
        servico.setDuracaoMinutos(30);
        servico.setValor(50.0);

        // Setup DTO
        LocalDateTime dataAgendamento = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
        dto = new DadosEntradaCadastroAgendamento(
                dataAgendamento,
                1,
                2
        );
    }

    @Test
    @DisplayName("Deve realizar agendamento com ADMIN validando cliente corretamente")
    void testRealizarAgendamentoComAdmin() {
        // Arrange
        when(clienteRepository.findByUsuarioId(2)).thenReturn(Optional.of(cliente));
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));
        when(diaEspecialRepository.findByData(any())).thenReturn(Optional.empty());
        when(agendamentoRepository.findConflitos(any(), any())).thenReturn(java.util.List.of());
        when(agendamentoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Agendamento resultado = agendamentoService.realizarAgendamento(dto, usuarioAdmin);

        // Assert
        assertNotNull(resultado);
        assertEquals(cliente.getId(), resultado.getCliente().getId());
        assertEquals(servico.getId(), resultado.getServico().getId());
        verify(clienteRepository, times(1)).findByUsuarioId(2);
        verify(agendamentoRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve realizar agendamento com CLIENTE validando seu próprio perfil")
    void testRealizarAgendamentoComCliente() {
        // Arrange
        DadosEntradaCadastroAgendamento dtoCliente = new DadosEntradaCadastroAgendamento(
                LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0),
                1,
                null
        );

        when(clienteRepository.findByUsuarioId(2)).thenReturn(Optional.of(cliente));
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));
        when(diaEspecialRepository.findByData(any())).thenReturn(Optional.empty());
        when(agendamentoRepository.findConflitos(any(), any())).thenReturn(java.util.List.of());
        when(agendamentoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Agendamento resultado = agendamentoService.realizarAgendamento(dtoCliente, usuarioCliente);

        // Assert
        assertNotNull(resultado);
        assertEquals(cliente.getId(), resultado.getCliente().getId());
        verify(clienteRepository, times(1)).findByUsuarioId(2);
    }

    @Test
    @DisplayName("Deve lançar erro ao ADMIN não informar clienteId")
    void testRealizarAgendamentoAdminSemClienteId() {
        // Arrange
        DadosEntradaCadastroAgendamento dtoSemCliente = new DadosEntradaCadastroAgendamento(
                LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0),
                1,
                null
        );

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            agendamentoService.realizarAgendamento(dtoSemCliente, usuarioAdmin),
            "Admin deve informar o id do cliente!"
        );
    }

    @Test
    @DisplayName("Deve lançar erro quando cliente tenta agendar para outro usuário")
    void testRealizarAgendamentoClienteOutroUsuario() {
        // Arrange
        Usuario outroCliente = new Usuario();
        outroCliente.setId(3);
        outroCliente.setPerfil(Perfil.CLIENTE);

        Cliente clienteOutro = new Cliente();
        clienteOutro.setId(2);
        clienteOutro.setUsuario(outroCliente);

        when(clienteRepository.findByUsuarioId(2)).thenReturn(Optional.of(clienteOutro));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            agendamentoService.realizarAgendamento(dto, usuarioCliente),
            "Erro! Você não tem permissão para realizar está ação!"
        );
    }

    @Test
    @DisplayName("Deve lançar erro quando cliente não encontrado")
    void testRealizarAgendamentoClienteNaoEncontrado() {
        // Arrange
        when(clienteRepository.findByUsuarioId(2)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            agendamentoService.realizarAgendamento(dto, usuarioCliente)
        );
    }

    @Test
    @DisplayName("Deve lançar erro quando serviço não encontrado")
    void testRealizarAgendamentoServicoNaoEncontrado() {
        // Arrange
        when(clienteRepository.findByUsuarioId(2)).thenReturn(Optional.of(cliente));
        when(servicoRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            agendamentoService.realizarAgendamento(dto, usuarioCliente)
        );
    }

    @Test
    @DisplayName("Deve lançar erro quando horário está fora do expediente")
    void testRealizarAgendamentoHorarioFora() {
        // Arrange
        LocalDateTime horarioNoite = LocalDateTime.now().plusDays(2).withHour(22).withMinute(0);
        DadosEntradaCadastroAgendamento dtoNoite = new DadosEntradaCadastroAgendamento(horarioNoite, 1, null);

        when(clienteRepository.findByUsuarioId(2)).thenReturn(Optional.of(cliente));
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));
        when(diaEspecialRepository.findByData(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            agendamentoService.realizarAgendamento(dtoNoite, usuarioCliente),
            "Horário fora do expediente da Barbearia!"
        );
    }

    @Test
    @DisplayName("Deve lançar erro quando horário está conflitando")
    void testRealizarAgendamentoComConflito() {
        // Arrange
        Agendamento agendamentoConflito = new Agendamento();
        agendamentoConflito.setId(99);

        when(clienteRepository.findByUsuarioId(2)).thenReturn(Optional.of(cliente));
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));
        when(diaEspecialRepository.findByData(any())).thenReturn(Optional.empty());
        when(agendamentoRepository.findConflitos(any(), any())).thenReturn(java.util.List.of(agendamentoConflito));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            agendamentoService.realizarAgendamento(dto, usuarioCliente),
            "Horário indisponível. Alguém já reservou!"
        );
    }

    @Test
    @DisplayName("Deve lançar erro quando tenta agendar em dia de folga")
    void testRealizarAgendamentoEmDiaFolga() {
        // Arrange
        LocalDate dataFolga = LocalDate.now().plusDays(2);
        DadosEntradaCadastroAgendamento dtoFolga = new DadosEntradaCadastroAgendamento(
                dataFolga.atTime(10, 0),
                1,
                null
        );

        DiaEspecial diaFolga = new DiaEspecial();
        diaFolga.setData(dataFolga);
        diaFolga.setDiaFolga(true);

        when(clienteRepository.findByUsuarioId(2)).thenReturn(Optional.of(cliente));
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));
        when(diaEspecialRepository.findByData(dataFolga)).thenReturn(Optional.of(diaFolga));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            agendamentoService.realizarAgendamento(dtoFolga, usuarioCliente),
            "É dia de folga! Estamos fechados!"
        );
    }

    @Test
    @DisplayName("Deve realizar agendamento respeitando horários customizados de dia especial")
    void testRealizarAgendamentoComDiaEspecialHorarioCustomizado() {
        // Arrange
        LocalDate dataEspecial = LocalDate.now().plusDays(2);
        LocalDateTime dataHoraInicio = dataEspecial.atTime(9, 0); // Mais cedo que normal
        DadosEntradaCadastroAgendamento dtoEspecial = new DadosEntradaCadastroAgendamento(
                dataHoraInicio,
                1,
                null
        );

        DiaEspecial diaEspecial = new DiaEspecial();
        diaEspecial.setData(dataEspecial);
        diaEspecial.setDiaFolga(false);
        diaEspecial.setHorarioAbertura(LocalTime.of(9, 0));
        diaEspecial.setHorarioFechamento(LocalTime.of(17, 0));

        when(clienteRepository.findByUsuarioId(2)).thenReturn(Optional.of(cliente));
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));
        when(diaEspecialRepository.findByData(dataEspecial)).thenReturn(Optional.of(diaEspecial));
        when(agendamentoRepository.findConflitos(any(), any())).thenReturn(java.util.List.of());
        when(agendamentoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Agendamento resultado = agendamentoService.realizarAgendamento(dtoEspecial, usuarioCliente);

        // Assert
        assertNotNull(resultado);
        assertEquals(dataHoraInicio, resultado.getDataHoraInicio());
        verify(agendamentoRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro quando horário está antes da abertura em dia especial")
    void testRealizarAgendamentoAntesDiaEspecial() {
        // Arrange
        LocalDate dataEspecial = LocalDate.now().plusDays(2);
        LocalDateTime dataHoraInicio = dataEspecial.atTime(8, 0); // Antes das 9h
        DadosEntradaCadastroAgendamento dtoAntes = new DadosEntradaCadastroAgendamento(
                dataHoraInicio,
                1,
                null
        );

        DiaEspecial diaEspecial = new DiaEspecial();
        diaEspecial.setData(dataEspecial);
        diaEspecial.setDiaFolga(false);
        diaEspecial.setHorarioAbertura(LocalTime.of(9, 0));
        diaEspecial.setHorarioFechamento(LocalTime.of(17, 0));

        when(clienteRepository.findByUsuarioId(2)).thenReturn(Optional.of(cliente));
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));
        when(diaEspecialRepository.findByData(dataEspecial)).thenReturn(Optional.of(diaEspecial));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            agendamentoService.realizarAgendamento(dtoAntes, usuarioCliente),
            "Horário fora do expediente da Barbearia!"
        );
    }

    @Test
    @DisplayName("Deve validar corretamente admin com permissão total")
    void testValidarUsuarioAdminComPermissao() {
        // Act & Assert - não deve lançar exceção
        assertDoesNotThrow(() -> agendamentoService.validarUsuario(usuarioAdmin, cliente));
    }

    @Test
    @DisplayName("Deve validar corretamente cliente como dono")
    void testValidarUsuarioClienteDono() {
        // Act & Assert - não deve lançar exceção
        assertDoesNotThrow(() -> agendamentoService.validarUsuario(usuarioCliente, cliente));
    }

    @Test
    @DisplayName("Deve lançar erro quando usuário não é admin e não é dono")
    void testValidarUsuarioNaoAutorizado() {
        // Arrange
        Usuario usuarioOutro = new Usuario();
        usuarioOutro.setId(999);
        usuarioOutro.setPerfil(Perfil.CLIENTE);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            agendamentoService.validarUsuario(usuarioOutro, cliente),
            "Erro! Você não tem permissão para realizar está ação!"
        );
    }
}
