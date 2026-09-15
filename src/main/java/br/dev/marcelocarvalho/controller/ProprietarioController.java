package br.dev.marcelocarvalho.controller;

import br.dev.marcelocarvalho.dto.ProprietarioDTO;
import br.dev.marcelocarvalho.dto.ProprietarioInclusaoDTO;
import br.dev.marcelocarvalho.service.ProprietarioService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("proprietarios")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProprietarioController {

    @Inject
    ProprietarioService proprietarioService;

    @GET
    public List<ProprietarioDTO> listAllProprietarios(){
        return proprietarioService.listAllProprietario();
    }

    @GET
    @Path("/{id}")
    public ProprietarioDTO buscarProprietarioById(@PathParam("id") Long id){
        return new ProprietarioDTO(id, "Marcelo", "62920081691");
    }

    @POST
    public Response incluirProprietario(@Valid ProprietarioInclusaoDTO proprietarioInclusaoDTO){
        try {
            ProprietarioDTO proprietarioDTO = proprietarioService.incluirProprietario(proprietarioInclusaoDTO);
            return Response
                    .status(Response.Status.CREATED)
                    .entity(proprietarioDTO)
                    .build();
        } catch (Exception e){
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Erro ao Incluir Proprietario" + e.getMessage())
                    .build();
        }
    }


}