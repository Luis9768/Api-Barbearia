package com.barbearia.barbershop_api.service;

import com.barbearia.barbershop_api.dto.DadosEntradaReagendamento;
import com.barbearia.barbershop_api.dto.SaidaAgendamentoDTO;
import com.barbearia.barbershop_api.dto.DadosEntradaCadastroAgendamento;
import com.barbearia.barbershop_api.model.*;
import com.barbearia.barbershop_api.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class AgendamentoService {

    @Autowired
    private AgendamentoRepository agendamentoRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ServicoRepository servicoRepository;
    @Autowired
    private DiaEspecialRepository diaEspecialRepository;


    @Transactional
    public Agendamento realizarAgendamento(DadosEntradaCadastroAgendamento dto, Usuario usuarioLogado) {

        validarDataPassado(dto.dataHoraInicio());
        LocalDate validacao = dto.dataHoraInicio().toLocalDate();
        validarDatasEntrada(validacao);

        Cliente cliente;
        if (usuarioLogado.getPerfil() == Perfil.ADMIN) {
            if (dto.clienteId() == null) {
                throw new IllegalArgumentException("Admin deve informar o id do cliente!");
            }
            cliente = clienteRepository.findById(dto.clienteId()).orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado!"));
        } else {
            cliente = clienteRepository.findByUsuarioId(usuarioLogado.getId()).orElseThrow(() -> new IllegalArgumentException("Perfil de cliente não encontrado para este usuário!"));
        }

        // 3. Validar permissão do usuário
        validarUsuario(usuarioLogado, cliente);

        // 4. Buscar serviço
        var servico = servicoRepository.findById(dto.servicoId()).orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado com o ID informado!"));

        // 5. Calcular horários
        LocalDateTime dataInicio = dto.dataHoraInicio();
        LocalDateTime dataFim = dataInicio.plusMinutes(servico.getDuracaoMinutos());

        // 6. Buscar horários de funcionamento do dia
        validacaoHorarioExpediente(dataInicio, dataFim);

        // 7. Validar conflitos de horário
        validarConflitosHorario(dataInicio, dataFim);

        // 9. Criar e salvar agendamento
        Agendamento agendamento = new Agendamento();
        agendamento.setDataHoraInicio(dataInicio);
        agendamento.setDataHoraFim(dataFim);
        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setStatusAgendamento(StatusAgendamento.AGENDADO);
        agendamentoRepository.save(agendamento);
        return agendamento;
    }

    @Transactional
    public void cancelarAgendamento(Integer id, Usuario usuarioLogado) {
        var agendamento = agendamentoRepository.findById(id).orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
        validarUsuario(usuarioLogado, agendamento.getCliente());

        if (agendamento.getStatusAgendamento() != StatusAgendamento.AGENDADO) {
            throw new RuntimeException("Este agendamento não pode ser cancelado pois está: " + agendamento.getStatusAgendamento());
        }

        if (usuarioLogado.getPerfil() != Perfil.ADMIN) {

            LocalDateTime dataAgendamento = agendamento.getDataHoraInicio();
            LocalDateTime agora = LocalDateTime.now();
            if (agora.isAfter(dataAgendamento)) {
                throw new RuntimeException("Não é possível cancelar um agendamento que já ocorreu.");
            }
            long minutosRestantes = Duration.between(agora, dataAgendamento).toMinutes();
            if (minutosRestantes < 120) {
                throw new RuntimeException("Cancelamento negado! O prazo limite é de 2 horas de antecedência.");
            }
        }
        agendamento.setStatusAgendamento(StatusAgendamento.CANCELADO);
        // 6. salva cancelamento no banco de dados
        agendamentoRepository.save(agendamento);
    }

    @Transactional
    public Agendamento reagendar(Integer id, DadosEntradaReagendamento dados, Usuario usuarioLogado) {
        LocalDate validacao = dados.dataHoraInicio().toLocalDate();
        validarDatasEntrada(validacao);

        Agendamento agendamento = agendamentoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado")); //busca o id

        if (agendamento.getStatusAgendamento() != StatusAgendamento.AGENDADO) {
            throw new IllegalArgumentException("Este agendamento não pode ser reagendado pois está: " + agendamento.getStatusAgendamento());
        }

        validarUsuario(usuarioLogado, agendamento.getCliente()); // valida o usuario logado para ver se ele tem permissão para reagendar o agendamento

        Servico servico = servicoRepository.findById(dados.servicoId()).orElseThrow(() -> new IllegalArgumentException("Servico não encontrado")); //pesquisa serviço

        validarDataPassado(dados.dataHoraInicio());
        LocalDateTime dataInicio = dados.dataHoraInicio();
        LocalDateTime dataFinal = dados.dataHoraInicio().plusMinutes(servico.getDuracaoMinutos());
        validacaoHorarioExpediente(dataInicio, dataFinal);

        var buscarAgendamento = agendamentoRepository.contarConflitosParaReagendamento(dados.dataHoraInicio(), dataFinal, id); //metodo para contar se tem algum agendamento que conflite com o reagendamento, levando em consideração o id do agendamento atual para não contar ele mesmo como conflito

        if (buscarAgendamento > 0) {
            throw new IllegalArgumentException("Horário ocupado! Escolha outro horário.");
        }
        agendamento.setServico(servico);
        agendamento.setDataHoraInicio(dados.dataHoraInicio());
        agendamento.setStatusAgendamento(StatusAgendamento.AGENDADO);
        return agendamentoRepository.save(agendamento);
    }

    public List<LocalTime> listarHorariosDisponiveis(LocalDate data, Integer servicoId) {
        validarDatasEntrada(data);

        var duracaoMinutos = servicoRepository.findById(servicoId); // pesquisar servico e guardar numa variavel
        var diaEspecial = diaEspecialRepository.findByData(data); // pesquisar ver se é uma data especial e guardar numa variavel

        LocalDateTime inicioExpediente; // variavel controle de inicio de expediente
        LocalDateTime fimExpediente;    // variavel controle de fim de expediente

        if (diaEspecial.isPresent()) {      //se tiver alguma data especial com anotação ex: natal,emergencia, compromisso
            DiaEspecial dia = diaEspecial.get(); //variavel para acessar o dia especial

            if (dia.getDiaFolga() == true) {
                return new ArrayList<>(); //se for um dia de folga retorna um array vazio sem agendamentos
            }

            inicioExpediente = LocalDateTime.of(data, dia.getHorarioAbertura()); // variavel que recebe o horario de abertura

            if (dia.getHorarioFechamento().isBefore(dia.getHorarioAbertura())) {
                fimExpediente = LocalDateTime.of(data.plusDays(1), dia.getHorarioFechamento()); // se o horario de fechamento for acabar no outro dia cujo qual o dia era pra ser do expediente seguinte ele recebe um plusDay de 1
            } else {
                fimExpediente = LocalDateTime.of(data, dia.getHorarioFechamento()); // se não segue padrão normal
            }
        } else {
            inicioExpediente = LocalDateTime.of(data, LocalTime.of(8, 0)); //se não segue horario normal abertura
            fimExpediente = LocalDateTime.of(data, LocalTime.of(18, 0)); // se não segue horario normal de fechamento
        }

        List<Agendamento> agendamentoDia = agendamentoRepository.findByDataHoraInicioBetween(inicioExpediente, fimExpediente); //metodo para pesquisar no banco o fim e o inicio do expediendo do dia

        List<LocalTime> horariosLivres = new ArrayList<>(); // array vazio para colocar os horarios livres

        LocalDateTime cursor = inicioExpediente;

        //Logica agendamento e horarios disponiveis
        while (cursor.plusMinutes(duracaoMinutos.get().getDuracaoMinutos()).isBefore(fimExpediente) || cursor.plusMinutes(duracaoMinutos.get().getDuracaoMinutos()).isEqual(fimExpediente)) {
            boolean ocupado = false; //vaga de ocupado = false
            LocalDateTime fimDoSlot = cursor.plusMinutes(duracaoMinutos.get().getDuracaoMinutos()); // duração do servico reservada do horaio x ate o horario x
            for (Agendamento agendamento : agendamentoDia) {     //for para leitura
                if (cursor.isBefore(agendamento.getDataHoraFim()) && fimDoSlot.isAfter(agendamento.getDataHoraInicio())) { // se o agendamento for depois do fechamento e antes da abertura a lista fica fechada
                    ocupado = true;
                    break;
                }
            }
            if (!ocupado) {
                horariosLivres.add(cursor.toLocalTime()); // se não estiver com vaga preenciada adiciona na lista de agendamentos
            }
            cursor = cursor.plusMinutes(duracaoMinutos.get().getDuracaoMinutos()); // faz a contagem da duração de minutos do servico
        }
        return horariosLivres; // e retorna os horarios livres
    }

    public List<SaidaAgendamentoDTO> listarAgendamentos(LocalDate data, Usuario usuarioLogado) {
        if (data == null) {
            data = LocalDate.now(); // Se o cliente não mandar data, assume que ele quer ver os de hoje!
        }
        Cliente cliente = clienteRepository.findByUsuarioId(usuarioLogado.getId()).orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado!")); //pesquisa o cliente logado para garantir que ele existe no banco de dados, caso contrário lança uma exceção
        //metodo listar agendamentos caso a data esteja vazia
        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.atTime(23, 59, 59);

        List<Agendamento> listarAgendamentos;

        if (cliente.getUsuario().getPerfil() == Perfil.ADMIN) {
            listarAgendamentos = agendamentoRepository.findByDataHoraInicioBetween(inicio, fim);
        } else {
            listarAgendamentos = agendamentoRepository.findByClienteAndDataHoraInicioBetween(cliente, inicio, fim);
        }

        return listarAgendamentos
                .stream()
                .map(SaidaAgendamentoDTO::new)
                .toList(); //se vier com a data preenchida ele retorna com esse filtro de data
    }

    public Double calcularFaturamento(LocalDate data, Usuario usuarioLogado) {
        if (usuarioLogado.getPerfil() != Perfil.ADMIN) {
            throw new IllegalArgumentException("Você não tem permissão para acessar essa informação!");
        }
        if (data == null) {
            data = LocalDate.now();
        }
        validarDataFuturo(data);
        var inicio = data.atStartOfDay();
        var fim = data.atTime(23, 59, 59);
        var status = StatusAgendamento.CONCLUIDO;
        Double resultado = agendamentoRepository.somarFaturamentoPorStatus(inicio, fim, status); //realiza a consulta no banco de dados para somar o faturamento do dia, caso o resultado seja nulo retorna 0.00
        return resultado == null ? 0.0 : resultado;
    }

    public List<SaidaAgendamentoDTO> listarHistoricoCLiente(Integer id, Usuario usuarioLogado) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado!"));
        validarUsuario(usuarioLogado, cliente);
        var agendamento = agendamentoRepository.findByClienteIdOrderByDataHoraInicioDesc(id);
        if (agendamento.isEmpty()) {
            throw new IllegalArgumentException("Nenhum agendamento encontrado para este cliente!");
        }
        return agendamento.stream()
                .map(SaidaAgendamentoDTO::new)
                .toList();
    }

    public List<ItemRankingDTO> listarRankingServicos() {
        return agendamentoRepository.findRankingServicos().stream()
                .limit(5)
                .toList();
    }

    private void validarUsuario(Usuario usuarioLogado, Cliente cliente) {
        boolean ehAdmin = usuarioLogado.getPerfil() == Perfil.ADMIN;
        boolean ehDono = cliente.getUsuario().getId().equals(usuarioLogado.getId());
        if (!ehAdmin && !ehDono) {
            throw new IllegalArgumentException("Erro! Você não tem permissão para realizar está ação!");
        }
    }

    private void validarDataPassado(LocalDateTime data) {
        if (data.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é permitido agendar no passado! Selecione uma data futura.");
        }
    }

    private void validarDataFuturo(LocalDate data) {
        if (data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Não é permitido consultar datas futuras!");
        }
    }

    private void validacaoHorarioExpediente(LocalDateTime dataInicio, LocalDateTime dataFim) {

        LocalDate diaAgendamento = dataInicio.toLocalDate(); //data do agendamento para pesquisar o dia especial
        var diaEspecialOpt = diaEspecialRepository.findByData(diaAgendamento); // pesquisa se tem um dia especial cadastrado para a data do agendamento, caso tenha ele retorna um Optional com o dia especial, caso contrário ele retorna um Optional vazio

        LocalDateTime limiteAbertura;
        LocalDateTime limiteFechamento;

        if (diaEspecialOpt.isPresent()) {
            var dia = diaEspecialOpt.get();
            if (Boolean.TRUE.equals(dia.getDiaFolga())) {
                throw new IllegalArgumentException("É dia de folga! Estamos fechados!");
            }
            limiteAbertura = LocalDateTime.of(diaAgendamento, dia.getHorarioAbertura());
            if (dia.getHorarioFechamento().isBefore(dia.getHorarioAbertura())) {
                limiteFechamento = LocalDateTime.of(diaAgendamento.plusDays(1), dia.getHorarioFechamento());
            } else {
                limiteFechamento = LocalDateTime.of(diaAgendamento, dia.getHorarioFechamento());
            }
        } else {
            limiteAbertura = LocalDateTime.of(diaAgendamento, LocalTime.of(8, 0));
            limiteFechamento = LocalDateTime.of(diaAgendamento, LocalTime.of(18, 0));
        }
        if (dataInicio.isBefore(limiteAbertura) || dataFim.isAfter(limiteFechamento)) {
            throw new IllegalArgumentException("Horário fora do expediente da barbearia!");
        }
    }

    private void validarConflitosHorario(LocalDateTime dataHoraInicio, LocalDateTime dataHoraFim) {
        var conflitos = agendamentoRepository.findConflitos(dataHoraInicio, dataHoraFim);
        if (!conflitos.isEmpty()) {
            throw new IllegalArgumentException("Horário indisponível. Alguém já reservou!");
        }

    }

    private void validarDatasEntrada(LocalDate dataSolicitada) {
        LocalDate hoje = LocalDate.now();
        LocalDate limiteMaximo = LocalDate.now().plusDays(30);
        if (dataSolicitada.isBefore(hoje) || dataSolicitada.isAfter(limiteMaximo)) {
            throw new IllegalArgumentException("Data inválida! Selecione outra.");
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void atualizarStatusAgendamentosPassados() {
        LocalDateTime agora = LocalDateTime.now();
        List<Agendamento> agendamentosAtrasados = agendamentoRepository.findByStatusAgendamentoAndDataHoraFimBefore(StatusAgendamento.AGENDADO, agora);
        agendamentosAtrasados.forEach(agendamento -> agendamento.setStatusAgendamento(StatusAgendamento.AGUARDANDO_PAGAMENTO));
        agendamentoRepository.saveAll(agendamentosAtrasados);
    }
}
