package com.barbearia.barbershop_api.service;

import com.barbearia.barbershop_api.dto.agendamentoDto.DadosEntradaCadastroAgendamento;
import com.barbearia.barbershop_api.dto.agendamentoDto.DadosEntradaReagendamento;
import com.barbearia.barbershop_api.dto.agendamentoDto.DadosSaidaAgendamento;
import com.barbearia.barbershop_api.entity.*;
import com.barbearia.barbershop_api.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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
    @Autowired
    private BarbeiroRepository babeiroRepository;


    @Transactional
    public DadosSaidaAgendamento realizarAgendamento(DadosEntradaCadastroAgendamento dto, Usuario usuarioLogado) {
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

        //buscar Barbeiro
        var barbeiro = babeiroRepository.findById(dto.barbeiroId()).orElseThrow(() -> new IllegalArgumentException("Barbeiro não encontrado!"));
        // Buscar serviço
        var servico = servicoRepository.findById(dto.servicoId()).orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado com o ID informado!"));

        // 5. Calcular horários
        LocalDateTime dataInicio = dto.dataHoraInicio();
        LocalDateTime dataFim = dataInicio.plusMinutes(servico.getDuracaoMinutos());

        // 6. Buscar horários de funcionamento do dia
        validacaoHorarioExpediente(dataInicio, dataFim);
        validarFimExpediente(dataInicio);

        // 7. Validar conflitos de horário
        if(agendamentoRepository.existeConflitoHorario(barbeiro.getId(),dataInicio,dataFim)){
            throw new IllegalArgumentException("O barbeiro selecionado já possui um agendamento neste horário!");
        }
        Agendamento agendamento = new Agendamento();
        agendamento.setDataHoraInicio(dataInicio);
        agendamento.setDataHoraFim(dataFim);
        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setBarbeiro(barbeiro);
        agendamento.setStatusAgendamento(StatusAgendamento.AGENDADO);
        agendamento.setAtivo(true);
        agendamentoRepository.save(agendamento);
        return new DadosSaidaAgendamento(agendamento);
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
    public DadosEntradaReagendamento reagendar(Integer id, DadosEntradaReagendamento dados, Usuario usuarioLogado) {

        LocalDate validacao = dados.dataHoraInicio().toLocalDate();
        validarDatasEntrada(validacao);

        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));

        if (agendamento.getStatusAgendamento() != StatusAgendamento.AGENDADO) {
            throw new IllegalArgumentException("Este agendamento não pode ser reagendado pois está: " + agendamento.getStatusAgendamento());
        }

        validarUsuario(usuarioLogado, agendamento.getCliente());

        Servico servico = servicoRepository.findById(dados.servicoId())
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));

        validarDataPassado(dados.dataHoraInicio());

        LocalDateTime dataInicio = dados.dataHoraInicio();
        LocalDateTime dataFinal = dados.dataHoraInicio().plusMinutes(servico.getDuracaoMinutos());

        validacaoHorarioExpediente(dataInicio, dataFinal);

        var conflitos = agendamentoRepository.contarConflitosParaReagendamento(dataInicio, dataFinal, id);

        if (conflitos > 0) {
            throw new IllegalArgumentException("Horário ocupado! Escolha outro horário.");
        }

        agendamento.setServico(servico);
        agendamento.setDataHoraInicio(dataInicio);
        agendamento.setStatusAgendamento(StatusAgendamento.AGENDADO);

        agendamentoRepository.save(agendamento);

        return new DadosEntradaReagendamento(agendamento);
    }

    public List<LocalTime> listarHorariosDisponiveis(Integer barbeiroId, LocalDate data, Integer servicoId) {
        validarDatasEntrada(data);

        Servico servico = servicoRepository.findById(servicoId).orElseThrow(() -> new IllegalArgumentException("Servico não encontrado!"));
        int duracaoMinutos = servico.getDuracaoMinutos();
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
        List<Agendamento> agendamentosBarbeiro = agendamentoRepository.buscarPorBarbeiroEData(barbeiroId,data);
        List<LocalTime> horariosDisponiveis = new ArrayList<>();

        LocalTime horarioAtual = inicioExpediente.toLocalTime();
        LocalTime fimExpedienteTime = fimExpediente.toLocalTime();

        LocalTime agora = LocalTime.now();
        Boolean ehhoje = data.isEqual(LocalDate.now());

        while ((horarioAtual.plusMinutes(duracaoMinutos).isBefore(fimExpedienteTime))||
                horarioAtual.plusMinutes(duracaoMinutos).equals(fimExpedienteTime)){
            if(ehhoje && horarioAtual.isBefore(agora)){
                horarioAtual = horarioAtual.plusMinutes(duracaoMinutos);
                continue;
            }
            boolean horarioOcupado = false;
            for (Agendamento agendamento : agendamentosBarbeiro) {
                LocalTime inicioAgendado = agendamento.getDataHoraInicio().toLocalTime();
                // Calcula quando o corte agendado termina
                LocalTime fimAgendado = inicioAgendado.plusMinutes(agendamento.getServico().getDuracaoMinutos());

                // Lógica de colisão: Verifica se o novo corte tenta entrar no meio de um corte já existente
                if (horarioAtual.isBefore(fimAgendado) &&
                        horarioAtual.plusMinutes(duracaoMinutos).isAfter(inicioAgendado)) {
                    horarioOcupado = true;
                    break; // Bateu horário! Para de procurar e marca como ocupado.
                }
            }
            if (!horarioOcupado) {
                horariosDisponiveis.add(horarioAtual);
            }

            // Pula para o próximo slot de tempo (Estou usando a duração do serviço, ex: de 40 em 40 min)
            // Dica: Muitos sistemas preferem pular de 30 em 30 min fixo (horarioAtualIteracao.plusMinutes(30))
            horarioAtual = horarioAtual.plusMinutes(duracaoMinutos);
        }
        return horariosDisponiveis;
    }


    public List<DadosSaidaAgendamento> listarAgendamentoPorCliente(Integer id, Usuario usuarioLogado){
        if(usuarioLogado.getPerfil() != Perfil.ADMIN){
            Cliente cliente = clienteRepository.findByUsuarioId(usuarioLogado.getId()).orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado!"));
            if(!cliente.getId().equals(id)){
                throw new IllegalArgumentException("Você não tem permissão para acessar o histórico de outro cliente!");
            }
        }
        List<Agendamento> agendamentos = agendamentoRepository.findByClienteId(id);
        return agendamentos.stream()
                .map(DadosSaidaAgendamento::new)
                .toList();
    }

    public List<DadosSaidaAgendamento> listarAgendamentosPorData(LocalDate data, Usuario usuarioLogado){
        if(data == null){
            data = LocalDate.now();
        }

        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.atTime(23,59,59);

        List<Agendamento> agendamentos;

        if(usuarioLogado.getPerfil() == Perfil.ADMIN){
            agendamentos = agendamentoRepository.findByDataHoraInicioBetween(inicio,fim);
        }else {
            Cliente cliente = clienteRepository.findByUsuarioId(usuarioLogado.getId()).orElseThrow(() -> new IllegalArgumentException("CLiente não encontrado!"));
            agendamentos = agendamentoRepository.findByClienteAndDataHoraInicioBetween(cliente,inicio,fim);
        }
        return agendamentos.stream()
                .map(DadosSaidaAgendamento::new)
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
        Optional<DiaEspecial> diaEspecialOpt = diaEspecialRepository.findByData(diaAgendamento); // pesquisa se tem um dia especial cadastrado para a data do agendamento, caso tenha ele retorna um Optional com o dia especial, caso contrário ele retorna um Optional vazio

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

    private void validarFimExpediente(LocalDateTime dataHoraInicio){
        LocalDateTime agora = LocalDateTime.now();
        LocalTime fechamento = LocalTime.of(18,0);
        LocalTime horarioLimiteAgendamento = fechamento.minusHours(1);
        boolean isAgendamentoParaHoje = dataHoraInicio.toLocalDate().equals(agora.toLocalDate());
        if(isAgendamentoParaHoje && agora.toLocalTime().isAfter(horarioLimiteAgendamento)){
            throw new IllegalArgumentException("A agenda de hoje já está encerrada! Faltam menos de 1 hora para o fechamento.");
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
        agendamentosAtrasados.forEach(agendamento -> agendamento.setStatusAgendamento(StatusAgendamento.CONCLUIDO));
        agendamentoRepository.saveAll(agendamentosAtrasados);
    }
}
