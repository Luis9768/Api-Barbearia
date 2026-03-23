package com.barbearia.barbershop_api.infra.auditoria;

import com.barbearia.barbershop_api.entity.LogAlteracaoServico;
import com.barbearia.barbershop_api.entity.Usuario;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditoriaListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        LogAlteracaoServico revisao = (LogAlteracaoServico) revisionEntity;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")){
            Usuario usuario = (Usuario) authentication.getPrincipal();
            revisao.setAutor(usuario.getLogin());
        }else {
            revisao.setAutor("ROBÔ_DO_SISTEMA");
        }
    }
}
