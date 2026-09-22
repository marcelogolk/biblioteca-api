package br.dev.marcelocarvalho.controller;

import br.dev.marcelocarvalho.dto.LivroAtualizacaoDTO;
import br.dev.marcelocarvalho.dto.LivroDTO;
import br.dev.marcelocarvalho.dto.LivroInclusaoDTO;
import br.dev.marcelocarvalho.service.LivroService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("livros")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LivroController {
    @Inject
    LivroService livroService;

    @GET
    public Response listAllLivros(){
        List<LivroDTO> livros = livroService.listAllLivros();
        return Response
                .status(Response.Status.OK)
                .entity(livros)
                .build();
    }

    @GET
    @Path("/{id}")
    public Response buscarLivroById(@PathParam("id") Long id){
        return Response
                .status(Response.Status.OK)
                .entity(livroService.buscarLivroById(id))
                .build();
    }

    @POST
    public Response incluirLivro(@Valid LivroInclusaoDTO livroInclusaoDTO){
        return Response
                .status(Response.Status.CREATED)
                .entity(livroService.incluirLivro(livroInclusaoDTO))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response atualizarLivro(@PathParam("id")Long id, @Valid LivroAtualizacaoDTO livroAtualizacaoDTO){
        LivroDTO livroDTO = livroService.atualizarLivro(id, livroAtualizacaoDTO);
        return Response
                .status(Response.Status.OK)
                .entity(livroDTO)
                .build();
    }

    @DELETE
    @Path("/{id}")
    public Response excluirLivro(@PathParam("id") Long id) {
        livroService.deletarLivroById(id);
        return Response
                .status(Response.Status.NO_CONTENT)
                .build();
    }
}