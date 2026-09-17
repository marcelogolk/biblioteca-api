package br.dev.marcelocarvalho.controller;

import br.dev.marcelocarvalho.dto.ProprietarioAtualizacaoDTO;
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
    public Response listAllProprietarios(){
        List<ProprietarioDTO> proprietarios = proprietarioService.listAllProprietario();
        return Response
                .status(Response.Status.OK)
                .entity(proprietarios)
                .build();
    }

    @GET
    @Path("/{id}")
    public Response buscarProprietarioById(@PathParam("id") Long id){
        return Response
                .status(Response.Status.OK)
                .entity(proprietarioService.buscarProprietarioById(id))
                .build();
    }

    @POST
    public Response incluirProprietario(@Valid ProprietarioInclusaoDTO proprietarioInclusaoDTO){
        return Response
                .status(Response.Status.CREATED)
                .entity(proprietarioService.incluirProprietario(proprietarioInclusaoDTO))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response atualizarProprietario(@PathParam("id")Long id, @Valid ProprietarioAtualizacaoDTO proprietarioAtualizacaoDTO){
        ProprietarioDTO proprietarioDTO = proprietarioService.atualizarProprietario(id, proprietarioAtualizacaoDTO);
        return Response
                .status(Response.Status.OK)
                .entity(proprietarioDTO)
                .build();
    }


    @DELETE
    @Path("/{id}")
    public Response excluirProprietario(@PathParam("id") Long id) {
        proprietarioService.deletarProprietarioById(id);
        return Response
                .status(Response.Status.NO_CONTENT)
                .build();
    }
}