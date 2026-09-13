package br.dev.marcelocarvalho.controller;

import br.dev.marcelocarvalho.dto.ProprietarioDTO;
import br.dev.marcelocarvalho.dto.ProprietarioInclusaoDTO;
import br.dev.marcelocarvalho.service.ProprietarioService;
import io.quarkus.arc.log.LoggerName;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("proprietario")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProprietarioController {

    @Inject
    ProprietarioService proprietarioService;

    @GET
    @Path("/{id}")
    public ProprietarioDTO BuscarProprietarioById(@PathParam("id") Long id){
        return new ProprietarioDTO(id, "Marcelo");
    }

    @POST
    public Response incluirProprietario(ProprietarioInclusaoDTO proprietarioInclusaoDTO){
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
