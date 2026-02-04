package com.barbearia.barbershop_api.service;

import com.barbearia.barbershop_api.dto.SaidaAgendamentoDTO;
import com.barbearia.barbershop_api.dto.DadosEntradaCadastroAgendamento;
import com.barbearia.barbershop_api.model.*;
import com.barbearia.barbershop_api.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<SaidaAgendamentoDTO> listarAgendamentos(LocalDate data) {
        if (data == null) {
            return agendamentoRepository.findAll().stream()
                    .map(SaidaAgendamentoDTO::new)
                    .toList();
            //metodo listar agendamentos caso a data esteja vazia
        } else {
            var inicio = data.atStartOfDay();
            var fim = data.atTime(23, 59, 59);
            return agendamentoRepository.findByDataHoraInicioBetween(inicio, fim).stream()
                    .map(SaidaAgendamentoDTO::new)
                    .toList();
        }//se vier com a data preenchida ele retorna com esse filtro de data
    }

    @Transactional
    public void cancelarAgendamento(Integer id, Usuario usuarioLogado) {
        var agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        var autenticacao = SecurityContextHolder.getContext().getAuthentication();
        usuarioLogado = (Usuario) autenticacao.getPrincipal();

        boolean ehDono = agendamento.getCliente().getUsuario().getId().equals(usuarioLogado.getId());
        boolean ehAdmin = usuarioLogado.getPerfil() == Perfil.ADMIN;

        if (!ehAdmin && !ehDono) {
            throw new RuntimeException("Acesso Negado: Você não tem permissão para cancelar este agendamento.");
        }

        LocalDateTime dataAgendamento = agendamento.getDataHoraInicio();
        LocalDateTime agora = LocalDateTime.now();

        if (agora.isAfter(dataAgendamento)) {
            throw new RuntimeException("Não é possível cancelar um agendamento que já ocorreu.");
        }

        long minutosRestantes = Duration.between(agora, dataAgendamento).toMinutes();

        if (minutosRestantes < 120) {
            throw new RuntimeException("Cancelamento negado! O prazo limite é de 2 horas de antecedência.");
        }

        // 6. Deleta
        agendamentoRepository.delete(agendamento);
    }

    public Agendamento reagendar(Integer id, DadosEntradaCadastroAgendamento dados, Usuario usuarioLogado) {

        Agendamento agendamento = agendamentoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado")); //busca o id

        boolean ehAdmin = usuarioLogado.getPerfil() == Perfil.ADMIN; //salva perfil admin numa variavel
        boolean ehDono = agendamento.getCliente().getUsuario().getId().equals(usuarioLogado.getId()); // perfil cliente

        if(!ehAdmin && !ehDono){
            throw  new RuntimeException("Acesso negado! Você não pode alterar o agendamento de outra pessoa!"); // se não for dono nem adm laça erro
        }

        Servico servico = servicoRepository.findById(dados.servicoId()).orElseThrow(() -> new IllegalArgumentException("Servico não encontrado")); //pesquisa serviço
        Cliente cliente;
        if(ehAdmin){
            cliente = clienteRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("CLiente alvo não encontrado."));
        }else {
            cliente = agendamento.getCliente();
        }
        LocalDateTime dataFinal = dados.dataHoraInicio().plusMinutes(servico.getDuracaoMinutos());

        var buscarAgendamento = agendamentoRepository.contarConflitosParaReagendamento(dados.dataHoraInicio(), dataFinal, id);

        if (buscarAgendamento > 0) {
            throw new IllegalArgumentException("Horário ocupado! Escolha outro horário.");
        }

        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setDataHoraInicio(dados.dataHoraInicio());
        agendamento.setDataHoraFim(dataFinal);
        return agendamentoRepository.save(agendamento);
    }

    public Double calcularFaturamento(LocalDate data) {
        var inicio = data.atStartOfDay();
        var fim = data.atTime(23, 59, 59);
        if (data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Não é permitido consultar datas futuras.");
        }
        Double resultado = agendamentoRepository.somarFaturamentoPorData(inicio, fim);
        if (resultado == null) {
            return 0.00;
        } else {
            return resultado;
        }
    }

    public List<LocalTime> listarHorariosDisponiveis(LocalDate data, Integer servicoId) {
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

    public Agendamento realizarAgendamento(DadosEntradaCadastroAgendamento dto, Usuario usuarioLogado) {
        Cliente cliente;
        if(usuarioLogado.getPerfil() == Perfil.ADMIN){  //ve se é um admin fazendo o agendamento
            if(dto.clienteId() == null){
                throw new RuntimeException("Admin deve informar o id do cliente!");
            }
            cliente = clienteRepository.findByUsuarioId(dto.clienteId()).orElseThrow(() -> new RuntimeException("Cliente não encontrado!"));
        }else {
             cliente = clienteRepository.findByUsuarioId(usuarioLogado.getId()).orElseThrow(() -> new IllegalArgumentException("Erro: Perfil de CLiente não encontrado para este usuário!"));
        }

        boolean ehDono = cliente.getUsuario().getId().equals(usuarioLogado.getId());
        boolean ehAdmin = usuarioLogado.getPerfil() == Perfil.ADMIN;
        if(!ehDono && !ehAdmin){
            throw new IllegalArgumentException("Você não pode realizar agendamentos por outra pessoa!");
        }

        var servico = servicoRepository.findById(dto.servicoId()).orElseThrow(() -> new RuntimeException("Servico não encontrado com o ID informado!"));

        LocalDateTime dataInicio = dto.dataHoraInicio();    //Horario do inicio do corte
        LocalDateTime dataFim = dto.dataHoraInicio().plusMinutes(servico.getDuracaoMinutos()); //Horario do fim do corte

        LocalDate diaAgendamento = dataInicio.toLocalDate(); //dia do agendamento
        var diaEspecialOpt = diaEspecialRepository.findByData(diaAgendamento);

        LocalDateTime limiteAbertura; // variaveis controle de tempo horario inicio expediente
        LocalDateTime limiteFechamento; // variavel controle de tempo fim do expediente

        if (diaEspecialOpt.isPresent()) {
            var dia = diaEspecialOpt.get();

            if (Boolean.TRUE.equals(dia.getDiaFolga())) {
                throw new RuntimeException("É dia de folga! Estamos fechados!");
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
            throw new RuntimeException("Horário fora do expediente da Barbearia!");
        }
        var conflitos = agendamentoRepository.findConflitos(dataInicio, dataFim);

        if (!conflitos.isEmpty()) {
            throw new RuntimeException("Horário indisponível. Alguém já reservou!");
        }
        Agendamento agendamento = new Agendamento();
        agendamento.setDataHoraInicio(dataInicio);
        agendamento.setDataHoraFim(dataFim);
        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        return agendamentoRepository.save(agendamento);
    }
    public List<SaidaAgendamentoDTO> listarHistoricoCLiente(Integer id, Usuario usuarioLogado){
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new RuntimeException("Cliente não encontrado."));

        boolean ehAdmin = usuarioLogado.getPerfil() == Perfil.ADMIN;
        boolean ehDono = cliente.getUsuario().getId().equals(usuarioLogado.getId());
        if(!ehAdmin && !ehDono){
            throw new IllegalArgumentException("Erro: Você não pode consultar o histórico de outra pessoa!");
        }
        var agendamento = agendamentoRepository.findByClienteIdOrderByDataHoraInicioDesc(id);

        return agendamento.stream()
                .map(SaidaAgendamentoDTO::new)
                .toList();
    }
    public List<ItemRankingDTO> listarRankingServicos(){
        return agendamentoRepository.findRankingServicos().stream()
                .limit(5)
                .toList();
    }
}
